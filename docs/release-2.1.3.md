# Entegre Client 2.1.3 yayın kontrolü

Kullanıcı 18 Ekim 2026'da 2.1.3'ün yayımlanmasını ve mevcut arkadaş kurulumlarına
otomatik dağıtılmasını açıkça onayladı. Oyun sunucusu veya oyuncu verisi değiştirilmez.

## Dağıtım

- Mevcut kurulum: main/pack.toml ve index.toml;349 eski kayıt aynen,5 ek dosya.
  İlk açılışta indirme, oyun başlarken yedekli geçiş, sonraki açılışta yeni pencere.
- Yeni kurulum: EntegreClient-v2.1.3.zip;8 dosya,3.796.139byte. Yeni shim ve
  doğrulanmış motorla ilk açılış Türkçe. Hesap, Java yolu, bellek ayarı, harita,
  dünya, seçili mod veya packwiz.json içermez.
- ZIP SHA256: `8ae915fe85cec2e8b05a405ac1c7b90b78ae06df2eff0febc5f40055b9c79c0a`.
- Sürüm sayfasına yalnız Prism ZIP'i ve SHA256 dosyası eklenir. Site latest
  release içindeki ZIP'i seçer; kaynak/güncelleyici ZIP'i ayrıca asset yapılmaz.
- Site latest-release verisini en fazla bir saat önbellekte tutabilir. Mevcut
  Prism kullanıcılarının paket kanalı bu site önbelleğinden bağımsızdır.

## Yayın öncesi tekrar doğrulama

- verify-pack, check-updater, check-pack-compat, resmî0.5.14 ile eski veri
  sahipliği geçiş testleri yeniden geçti.186 kalıcı URI aynen korundu.
- Çeviri6test, BootstrapTest, TransitionTest ve8Java çağrılı eski→yeni→motor
  güncelleme→geri alma zinciri yeniden geçti. Dağıtılan gerçek komut testte var.
- Başlangıç üretim betiğinin self-check'i: aynı girdiden aynı ZIP; ek kurulum
  verisi, yanlış başlatıcı ve eksik checksum reddedildi. GitHub kontrolüne eklendi.
- ZIP yeni geçici dizine çıkarıldı: gerçek shim --help Türkçe çıktı/exit0;
  ardından aynı ZIP ile localhost paketten gerçek GUI ilk kurulum ve tekrar
  açılış1.86sn/1.86sn. instance.cfg, mmc-pack.json ve tüm payloadlar byte olarak
  korundu; test paketi yüklendi. Bu süreler küçük yerel sentetik paket içindir.
- İlk smoke fixture sürüm alanlarını içermediğinden upstream sürüm onayı
  bekledi; fixture gerçek pack.toml sürümleriyle düzeltildi ve iki çağrı geçti.
  Üretim güncelleyicisi bu nedenle değiştirilmedi.
- Bağımsız kaynak/paketleme incelemesinde uygulama bulgusu yok. Temiz ZIP'te
  eski yedek olmadığı için geri alma ve motor onarımı yönergeleri ayrıldı.

## Kabul sınırı

Gerçek Windows/NTFS ve Prism üzerinden Minecraft/Fabric oyun oturumu denenmedi.
Yerel kontroller macOS ARM64/Java25 üzerinde, Java17 hedefli JAR'larla yapıldı.
Bir yayının doğrulanması, arkadaşların oyunu açtığını veya güncellemeyi tamamladığını
kanıtlamaz; oyuncu kurulumlarından telemetri alınmıyor.

## Yeniden üretme

Python3.11+ ile, repo dışında bir çıktı dizini kullanın:

```sh
python3 scripts/verify-pack.py
python3 scripts/check-updater.py
python3 scripts/build-prism-release.py --self-check --output-dir /tmp/entegre-client-release
```

Etiket v2.1.3, yayımlanan main commit'ini göstermeli; yüklenen ZIP ve SHA256
tekrar indirildiğinde yerel doğrulanmış dosyalarla birebir eşleşmeli.
