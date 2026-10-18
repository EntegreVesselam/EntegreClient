"""Translate exact UI literals in the pinned Packwiz 0.5.14 class files.

No instructions, constant indexes, dynamic mod data or unknown UTF8 bytes change.
The caller remains responsible for verifying the complete vendor JAR's SHA256.
"""

import struct

_PREFIX = "link/infra/packwiz/installer/"
_TRANSLATIONS = {
    _PREFIX + "ui/gui/GUIHandler.class": {
        "packwiz-installer": "Entegre Vesselam",
        "This modpack has no optional mods!": "Bu pakette isteğe bağlı mod bulunmuyor.",
        "Optional mods": "Mod ayarları",
        "Quit": "Çık",
        "Ignore": "Güncellemeden başlat",
        "The installation was cancelled. Would you like to quit the game, or ignore the update and start the game?":
            "Kurulum iptal edildi. Çıkmak mı, yoksa güncellemeyi atlayıp oyunu başlatmak mı istiyorsun?",
        "Cancelled installation": "Kurulum iptal edildi",
        "<html>This modpack uses newer versions of the following:<br><ul>":
            "<html>Bu paket, aşağıdakilerin daha yeni sürümlerini kullanıyor:<br><ul>",
        "Not found": "Bulunamadı",
        "New versions:<ul>": "Yeni sürümler:<ul>",
        "</ul><br>Would you like to update the versions, launch without updating, or cancel the launch?":
            "</ul><br>Sürümleri güncellemek, güncellemeden başlatmak veya başlatmayı iptal etmek ister misin?",
        "Cancel": "İptal",
        "Continue anyways": "Güncellemeden başlat",
        "Update": "Güncelle",
        "Updating MultiMC versions": "Oyun sürümlerini güncelle",
        "Continue without installing": "Kurulum yapmadan devam et",
        "Continue without updating": "Güncellemeden devam et",
    },
    _PREFIX + "ui/gui/OptionsSelectWindow.class": {
        "Select optional mods...": "İsteğe bağlı modları seç",
        "Select an option...": "Açıklamasını görmek için bir mod seç",
        "OK": "Tamam",
        "Cancel": "İptal",
    },
    _PREFIX + "ui/gui/OptionsSelectWindow$OptionTableModel.class": {
        "Enabled": "Etkin",
        "Mod name": "Mod adı",
    },
    _PREFIX + "ui/gui/ExceptionListWindow.class": {
        "Failed file downloads": "İndirilemeyen dosyalar",
        "One or more errors were encountered while installing the modpack!":
            "Paket kurulurken bir veya daha fazla hata oluştu.",
        "Select a file": "Ayrıntılar için bir dosya seç",
        "Continue": "Devam",
        "Attempt to continue installing, excluding the failed downloads":
            "İndirilemeyen dosyaları atlayarak kuruluma devam etmeyi dene",
        "Cancel launch": "Başlatmayı iptal et",
        "Stop launching the game": "Oyunun başlatılmasını durdur",
        "Ignore update": "Güncellemeyi atla",
        "Start the game without attempting to update": "Güncelleme yapmadan oyunu başlat",
        "Open missing mods": "Eksik modları aç",
        "Open missing mods in your browser": "Eksik modların sayfalarını tarayıcıda aç",
        " errored": " dosyada hata",
        "Report issue": "Sorun bildir",
    },
    _PREFIX + "Main.class": {
        "packwiz-installer": "Entegre Vesselam",
        "Invalid HTTP/HTTPS URL for pack file: ": "Paket dosyası için geçersiz HTTP/HTTPS adresi: ",
        "Failed to parse file path for pack file: ": "Paket dosyasının yolu okunamadı: ",
        "Invalid pack file path: ": "Geçersiz paket dosyası yolu: ",
        "Unknown side name: ": "Bilinmeyen kurulum tarafı: ",
        "Invalid pack folder path": "Geçersiz paket klasörü yolu",
        "Invalid MultiMC folder path": "Geçersiz oyun profili klasörü yolu",
        "Invalid manifest file path": "Geçersiz kurulum kayıt dosyası yolu",
        "Invalid timeout value": "Geçersiz bekleme süresi",
        "Update process failed": "Güncelleme işlemi başarısız oldu",
        "Unsupported scheme for pack file: ": "Paket dosyası için desteklenmeyen adres türü: ",
        "pack.toml URI to install from must be specified!": "Kurulum için pack.toml adresi belirtilmeli.",
        "Too many arguments specified!": "Çok fazla komut seçeneği belirtildi.",
        "Failed to parse command line arguments": "Başlatma seçenekleri okunamadı",
        "Failed to parse command line arguments: ": "Başlatma seçenekleri okunamadı: ",
        "A fatal error occurred: \n": "Kritik bir hata oluştu: \n",
    },
}
LOCALIZED_ENTRIES = frozenset(_TRANSLATIONS)


def localize_entry(name: str, data: bytes) -> bytes:
    if name not in LOCALIZED_ENTRIES:
        return data
    offset = 0

    def take(size):
        nonlocal offset
        if offset + size > len(data):
            raise ValueError(f"Truncated UI class: {name}")
        value = data[offset:offset + size]
        offset += size
        return value

    def u2():
        return int.from_bytes(take(2), "big")

    if take(4) != b"\xca\xfe\xba\xbe":
        raise ValueError(f"Invalid UI class magic: {name}")
    take(4)  # Minor and major version are preserved.
    pool_count = u2()
    if pool_count < 1:
        raise ValueError(f"Empty constant pool: {name}")
    entries, utf8, class_names, string_indexes = {}, {}, {}, set()
    index = 1
    widths = {3: 4, 4: 4, 5: 8, 6: 8, 7: 2, 8: 2, 9: 4, 10: 4,
              11: 4, 12: 4, 15: 3, 16: 2, 17: 4, 18: 4, 19: 2, 20: 2}
    while index < pool_count:
        start = offset
        tag = take(1)[0]
        if tag == 1:
            utf8[index] = take(u2())
        elif tag in widths:
            payload = take(widths[tag])
            if tag == 7:
                class_names[index] = int.from_bytes(payload, "big")
            elif tag == 8:
                string_indexes.add(int.from_bytes(payload, "big"))
        else:
            raise ValueError(f"Unsupported constant tag {tag}: {name}")
        entries[index] = data[start:offset]
        index += 2 if tag in (5, 6) else 1
    if index != pool_count or not string_indexes.issubset(utf8):
        raise ValueError(f"Invalid constant pool indexes: {name}")
    pool_end = offset
    take(2)  # Access flags.
    this_class = u2()
    if utf8.get(class_names.get(this_class)) != name[:-6].encode("ascii"):
        raise ValueError(f"Unexpected UI class identity: {name}")
    take(2)  # Superclass.
    take(2 * u2())  # Interfaces.

    def attributes():
        for _ in range(u2()):
            if u2() not in utf8:
                raise ValueError(f"Invalid attribute name: {name}")
            take(int.from_bytes(take(4), "big"))

    for _ in range(2):  # Fields and methods.
        for _ in range(u2()):
            take(2)  # Access flags.
            if u2() not in utf8 or u2() not in utf8:
                raise ValueError(f"Invalid member name or descriptor: {name}")
            attributes()
    attributes()
    if offset != len(data):
        raise ValueError(f"Trailing data in UI class: {name}")

    translations = {key.encode("ascii"): value for key, value in _TRANSLATIONS[name].items()}
    found = set()
    for index in string_indexes:
        original = utf8[index]
        if original not in translations:
            continue
        value = translations[original]
        # These translations use BMP text without NUL, where UTF8 and JVM modified UTF8 agree.
        if "\0" in value or any(ord(character) > 0xffff for character in value):
            raise ValueError(f"Unsupported translation encoding: {name}")
        encoded = value.encode("utf8")
        if len(encoded) > 65535:
            raise ValueError(f"Translation exceeds JVM string limit: {name}")
        entries[index] = b"\x01" + struct.pack(">H", len(encoded)) + encoded
        found.add(original)
    if found != translations.keys():
        raise ValueError(f"Required UI literals missing in pinned class: {name}")
    return data[:10] + b"".join(entries.values()) + data[pool_end:]
