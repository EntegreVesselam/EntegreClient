# Entegre Vesselam — Türkçe Prism güncelleyicisi

Prism'in oyun öncesinde açtığı Entegre tasarımlı güncelleme penceresi.
Normal açılıştaki 10 saniyelik seçenek sayacı kaldırıldı. Paket kontrolü,
indirme, dosya doğrulaması ve kayıtlı mod seçimleri Packwiz installer 0.5.14
ile sürer. Hesap girişi ve Minecraft'ı başlatma Prism'de kalır.

Varsayılan dil Türkçe: ana pencere, seçenek ekranı, hata pencereleri ve düğmeler.
Modların dışarıdan gelen adları/açıklamaları ve teknik hata ayrıntıları çevrilmez.
İngilizce dil seçimi bu sürümde yoktur.

## Mevcut arkadaş grubuna otomatik geçiş

Bu kaynakların ve üretilen beş dağıtım dosyasının Entegre Client paket kanalında
yayımlanması gerekir. Yerel hazırlık tek başına arkadaşların bilgisayarını değiştirmez.

1. İlk açılışta mevcut Packwiz beş küçük güncelleyici dosyasını indirir.
2. Minecraft başlayınca `entegre_updater_transition` istemci modu, desteklenen
   eski bootstrap'ı doğrular, yedekler ve tek atomik işlemle değiştirir.
3. Sonraki açılışta aynı Prism komutu Türkçe Entegre penceresini açar.
4. Sonraki motor sürümleri aynı Packwiz kanalıyla iner; indirildikten sonraki
   açılışta kullanılır. İngilizce seçimi ileride bu yolla dağıtılabilir.

İlk açılışta eski ekranın bir kez görülmesi normaldir. Minecraft'a kadar
ulaşmayan açılışta geçiş çalışmaz. `instance.cfg` değiştirilmez.

Desteklenen kurulum: instance'a özel `OverrideCommands=true` ve aşağıdaki
komutun doğrudan ya da QSettings biçimi ve bilinen özgün bootstrap SHA256'sı:

```text
"$INST_JAVA" -jar packwiz-installer-bootstrap.jar https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/main/pack.toml
```

Sitede dağıtılan `EntegreClient-v2.0.zip` içindeki gerçek komut da desteklenir:

```ini
OverrideCommands=true
PreLaunchCommand=\"$INST_JAVA\" -jar \"$INST_DIR/minecraft/packwiz-installer-bootstrap.jar\" https://github.com/EntegreVesselam/EntegreClient/raw/main/pack.toml
```

Bu biçim public ZIP'ten doğrulandı; yerel Entegre profilinin ilgili alanıyla da
eşleşti. `minecraft` oyun diziniyle doğrudan/kaçışlı biçimleri test edildi.
İki komutun paket adresi aynı içeriğe ulaşıyor. Komutu elle değiştirmek gerekmez.

Bilinmeyen bootstrap, farklı/ek seçenekli veya global komut değiştirilmez.
Dosya sistemi atomik değiştirmeyi ve hardlink ile yedek oluşturmayı desteklemiyorsa
eski başlatıcı korunur. Yerel APFS testi geçti; Windows/NTFS ve gerçek
Minecraft/Fabric oturumuyla kabul henüz yapılmadı.

## Dağıtım düzeni

| Derleme çıktısı | Packwiz tarafından yönetilen hedef |
| --- | --- |
| `dist/entegre-updater.jar` | `entegre-updater/engine.jar` |
| `dist/entegre-updater.sha256` | `entegre-updater/engine.sha256` |
| `dist/entegre-bootstrap.jar` | `entegre-updater/bootstrap.jar` |
| `dist/entegre-bootstrap.sha256` | `entegre-updater/bootstrap.sha256` |
| `dist/entegre-updater-transition-0.1.0.jar` | `mods/entegre-updater-transition-0.1.0.jar` |

Kökteki `packwiz-installer-bootstrap.jar` doğrudan Packwiz dağıtımına EKLENMEZ.
Mevcut kalıcı `alias="."` / `preserve=true` uyumluluk kaydı aynen korunur.
`launcher/` kaynakları, yedekler ve devre dışı bırakma dosyası paketten dışlanır.
Oluşturulan `entegre-updater/bootstrap-backup.jar` yalnız oyuncunun yerel yedeğidir.

Sabit küçük bootstrap, motoru doğrulayıp özel geçici kopyadan çalıştırır; böylece
çalışan JAR'ın Windows'ta yönetilen dosyayı kilitlemesi önlenir. Çıkış kodu
Prism'e aynen aktarılır. Eski kurulumdan geçmiş ve yedeği bulunan kullanıcılarda
eksik/bozuk motor için yalnız doğrulanmış eski yedek çalıştırılabilir; başlamış yeni motorun hatası sessizce başarıya çevrilmez.
Bootstrap'ın kendi sonraki sürümüne otomatik geçiş bu sürümün kapsamı değildir.

## Önizleme, seçenekler ve geri alma

Java 17+ ve masaüstü oturumu gerekir. Yalnız görünüm (ağ/dosya etkisi yok):

```sh
java -jar dist/entegre-updater.jar --preview
```

Geçişten sonra mevcut Minecraft klasöründe isteğe bağlı modları değiştirmek için:

```sh
java -jar packwiz-installer-bootstrap.jar --settings
```

Normal açılışta yeni isteğe bağlı modların seçim ekranı korunur; bütün modlar
kendiliğinden etkinleştirilmez. Güncelleme ağı ve Minecraft yüklemesi gerçek
işlerdir; sayacın kaldırılması bu süreleri sıfırlamaz.

Aşağıdaki geri alma, eski kurulumdan otomatik geçmiş ve yerel yedeği bulunan
kullanıcılar içindir. Yeni başlangıç ZIP’i doğrudan yeni başlatıcıyı içerir;
eski başlatıcı yedeği oluşturmaz. Geri alma oyun ve güncelleyici kapalıyken yapılır:

1. Minecraft klasöründe boş `.entegre-updater-disabled` dosyasını oluşturun.
2. Yerel `entegre-updater/bootstrap-backup.jar` dosyasının SHA256'sını doğrulayın:
   `a8fbb24dc604278e97f4688e82d3d91a318b98efc08d5dbfcbcbcab6443d116c`.
3. Doğrulanmış yedeği kökteki `packwiz-installer-bootstrap.jar` üzerine kopyalayın.
   Prism komutu aynı kalır. Yeniden geçiş istenirse devre dışı bırakma dosyası kaldırılır.

Yeni 2.1.3 başlangıç paketinde eski yedek bulunmaz. Motor bozulmuşsa oyun ve
güncelleyici kapalıyken yayımlanan 2.1.3 ZIP'inden yalnız
`minecraft/entegre-updater/engine.jar` ve `engine.sha256` dosyalarını mevcut
Minecraft klasörünüzdeki `entegre-updater/` içine birlikte geri koyun. ZIP'i
kurulumun tamamının üzerine açmayın. Bu, yeni motoru onarır; eski Packwiz'e
geri dönüş işlemi değildir.

Oyuncu ayarları, haritaları veya `packwiz.json` silinmez. Yedeği doğrulanmayan
kurulumda rastgele bir dosya kullanılmaz. Paket kanalını eski sürüme döndürmek
kişisel veriye sahip eski URI'leri kaldırabileceğinden bu geri almanın yerine geçmez.

## Kaynaktan derleme ve kontroller

Python 3.9+, JDK 17+, derleme için Fabric Loader 0.18.4 JAR'ı gerekir.
`FABRIC_LOADER_JAR` değişkeniyle yol verilebilir; yoksa mevcut Gradle önbelleği
aranır. Derleme aracı bağımlılığıdır; oyuna yeni bir üçüncü taraf bağımlılık eklenmez.
Eksik resmî Packwiz JAR'ı sabit SHA256 doğrulamasıyla indirilir.

```sh
python3 build.py
python3 -m unittest discover -s tests -p 'test_*.py'
java -ea -cp build/test-classes:dist/entegre-updater.jar com.entegre.launcher.LauncherTest
java -ea -cp build/test-classes:dist/entegre-updater.jar WindowTest --text
java -ea -cp build/test-classes:dist/entegre-updater.jar WindowTest
java -ea -cp build/test-classes:dist/entegre-bootstrap.jar BootstrapTest
java -ea -cp build/test-classes:dist/entegre-bootstrap.jar:dist/entegre-updater-transition-0.1.0.jar TransitionTest
python3 tests/integration.py
python3 tests/auto_update.py
```

Windows'ta classpath ayıracı `:` yerine `;` olur. GUI testleri sırayla
çalıştırılır. Testler yeni geçici dizin ve localhost verisi kullanır.

Derleme upstream ana penceresini ve manifestini değiştirir; beş sınıftaki
52 arayüz metnini Türkçeleştirir. Çeviride bytecode ve sınıf yapısı korunur.
Diğer upstream girdiler byte düzeyinde aynı kalır; lisans bildirimleri korunur.
Kanıt ve açık kabul sınırları `docs/verification.md` dosyasındadır.
