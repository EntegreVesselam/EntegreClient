# Entegre Client 2.1.4 yayın kaydı

Kullanıcı 18 Ekim 2026'da önce siteyi, sonra client'ı yayımlamayı açıkça istedi.
Duyuru endpoint'i canlıda doğrulanmadan client main kanalı güncellenmez.

Bu sürüm üç menü düzeni, iki yerel fotoğraf ve replik havuzunu dağıtır. Yeni mod,
Minecraft/Fabric sürüm değişimi veya güncelleyici payload değişimi yoktur. Önceki
354 kayıt korunur,357 kayıt vardır;186 kalıcı uyumluluk URI'si aynıdır.

Mevcut Prism kurulumları aynı pre-launch komutuyla main/pack.toml dosyasını okur;
oyun kapatılıp yeniden açıldığında güncellemeyi alır. Başlangıç ZIP'i yeni
kurulumlar içindir; hesap/Java yolu/bellek/oyuncu/mod seçimi içermez. Menü modlarını
kapatmış kullanıcıların isteğe bağlı seçimi zorla değiştirilmez.

Yerel gerçek Minecraft kabulü macOS'ta, sahte kullanıcı ve localhost haberleriyle
`docs/community-menu.md` içinde kayıtlıdır. Yayın hazırlığında yalnız 2.1.3 sürüm
etiketi 2.1.4 yapıldı; aynı uzunlukta metin, aynı konum ve eylemler korundu. Önizleme
ve native kaynak hash kaydı, bu etiket değişiminden önceki yerel adaya aittir.
Windows/NTFS ve gerçek Prism intro geçişi kabulü çalıştırılmadı.

## Yayın kontrolü

- Güncel origin/main tabanı ve dar dosya farkı karşılaştırılır.
- Guarded refresh, verify-pack, updater, compat ve gerçek installer sahiplik
  kontrolleri çalıştırılır; yeni Prism ZIP'i reproducible self-check ile üretilir.
- PR CI başarılı olduktan sonra main'e alınır, main CI izlenir.
- v2.1.4 etiketi main commit'ine verilir. Yalnız EntegreClient-v2.1.4.zip ve
  SHA256 dosyası release asset'i yapılır; latest-release API bu ZIP'i seçer.
- Anonim public pack/index ve yeni/etkilenen altı payload tekrar indirilip hash
  doğrulanır. ZIP+SHA tekrar indirilip yerel çıktıyla birebir karşılaştırılır.
- Site beslemesi ve /client sürüm bilgisinin canlı durumu kontrol edilir.

Kontroller ve yayın SHA/URL sonucu doğrulanınca bu kayda eklenir.

## Yerel yayın ön kontrolü sonucu

Guarded refresh, verify-pack357 kayıt, check-updater, check-pack-compat ve checksum
pinli resmî installer0.5.14 ile eski/atlanan/temiz kurulum veri sahipliği testleri
geçti. Yeni ZIP self-check reproducibility ve yanlış/eksik/fazla dosya reddi geçti.
ZIP8dosya/3.796.139byte; SHA256
`52920aa1835a52ea0b9155dd4fac9b38b7f416808390f5cd2353276ad9354613`.
Yerel çıktı `/tmp/entegre-client-release-2.1.4/EntegreClient-v2.1.4.zip`.
