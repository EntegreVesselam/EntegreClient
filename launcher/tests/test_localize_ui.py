import pathlib
import struct
import sys
import unittest
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))
from localize_ui import LOCALIZED_ENTRIES, localize_entry

PREFIX = "link/infra/packwiz/installer/"
SAMPLES = {
    PREFIX + "ui/gui/GUIHandler.class": ("Optional mods", "Mod ayarları"),
    PREFIX + "ui/gui/OptionsSelectWindow.class": ("Select optional mods...", "İsteğe bağlı modları seç"),
    PREFIX + "ui/gui/OptionsSelectWindow$OptionTableModel.class": ("Mod name", "Mod adı"),
    PREFIX + "ui/gui/ExceptionListWindow.class": ("Failed file downloads", "İndirilemeyen dosyalar"),
    PREFIX + "Main.class": ("A fatal error occurred: \n", "Kritik bir hata oluştu: \n"),
}


def constants(data):
    """Read raw constants independently so unchanged bytes can be compared."""
    count = struct.unpack_from(">H", data, 8)[0]
    result = {}
    offset, index = 10, 1
    widths = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
              11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
    while index < count:
        start = offset
        tag = data[offset]
        offset += 1
        if tag == 1:
            size = struct.unpack_from(">H", data, offset)[0]
            offset += 2 + size
        else:
            offset += widths[tag]
        result[index] = data[start:offset]
        index += 2 if tag in (5, 6) else 1
    return result, data[offset:]


class LocalizationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.jar = zipfile.ZipFile(ROOT / "vendor/packwiz-installer-0.5.14.jar")

    @classmethod
    def tearDownClass(cls):
        cls.jar.close()

    def test_target_classes_have_turkish_literals_and_unchanged_instructions(self):
        self.assertEqual(LOCALIZED_ENTRIES, set(SAMPLES))
        for name, (english, turkish) in SAMPLES.items():
            with self.subTest(name=name):
                original = self.jar.read(name)
                translated = localize_entry(name, original)
                before, original_body = constants(original)
                after, translated_body = constants(translated)
                self.assertEqual(original[:10], translated[:10])
                self.assertEqual(original_body, translated_body, "Fields, code and attributes must not change")
                self.assertEqual(before.keys(), after.keys())
                self.assertIn(b"\x01" + struct.pack(">H", len(turkish.encode())) + turkish.encode(), after.values())
                self.assertNotIn(b"\x01" + struct.pack(">H", len(english)) + english.encode(), after.values())
                for index, value in before.items():
                    if value[0] != 1:
                        self.assertEqual(value, after[index], "Only UTF8 constants may change")

    def test_non_target_entries_round_trip_byte_for_byte(self):
        for name in self.jar.namelist():
            if name not in LOCALIZED_ENTRIES:
                original = self.jar.read(name)
                self.assertEqual(localize_entry(name, original), original, name)

    def test_missing_required_literal_is_rejected(self):
        for name, (english, _) in SAMPLES.items():
            data = self.jar.read(name).replace(english.encode(), b"x" * len(english), 1)
            with self.subTest(name=name), self.assertRaises(ValueError):
                localize_entry(name, data)

    def test_wrong_class_and_truncated_input_are_rejected(self):
        name = PREFIX + "ui/gui/GUIHandler.class"
        original = self.jar.read(name)
        malformed = [b"", b"not a Java class", original[:9], original[:20], original[:-1],
                     b"\0\0\0\0" + original[4:], original[:10] + b"\x02" + original[11:],
                     self.jar.read(PREFIX + "ui/gui/OptionsSelectWindow.class")]
        for data in malformed:
            with self.subTest(length=len(data)), self.assertRaises(ValueError):
                localize_entry(name, data)

    def test_non_literal_utf8_remain_unchanged(self):
        name = PREFIX + "ui/gui/OptionsSelectWindow$OptionTableModel.class"
        data = self.jar.read(name)
        before, _ = constants(data)
        after, _ = constants(localize_entry(name, data))
        string_indexes = {struct.unpack(">H", raw[1:])[0] for raw in before.values() if raw[0] == 8}
        for index, raw in before.items():
            if index not in string_indexes:
                self.assertEqual(raw, after[index])

    def test_double_slot_constants_modified_utf8_and_partial_matches_are_preserved(self):
        name = PREFIX + "ui/gui/GUIHandler.class"
        original = self.jar.read(name)
        _, body = constants(original)
        end = len(original) - len(body)
        count = struct.unpack_from(">H", original, 8)[0]
        modified_utf8 = b"\xc0\x80\xed\xa0\x80\xed\xb0\x80"
        dynamic_name = b"My Optional mods"
        added = (b"\x05" + bytes(8) + b"\x06" + bytes(8)
                 + b"\x01" + struct.pack(">H", len(modified_utf8)) + modified_utf8
                 + b"\x01" + struct.pack(">H", len(dynamic_name)) + dynamic_name
                 + b"\x08" + struct.pack(">H", count + 5))
        data = original[:8] + struct.pack(">H", count + 7) + original[10:end] + added + body
        before, _ = constants(data)
        after, _ = constants(localize_entry(name, data))
        for index in (count, count + 2, count + 4, count + 5, count + 6):
            self.assertEqual(before[index], after[index])


if __name__ == "__main__":
    unittest.main()
