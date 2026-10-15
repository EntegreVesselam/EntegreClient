# Yerel doğrulama — 2026-10-16

Durum: 2026-10-16 tarihinde kullanıcı yayına alımı açıkça onayladı. Bu belge yayın öncesi yerel kanıtları kaydeder; uzak GitHub kontrolü ve canlı hash doğrulaması yayın sırasında ayrıca tamamlanmalıdır.

Aday: Entegre Client 2.1.1, Minecraft 1.21.11, Fabric 0.18.4, Fabric API 0.141.3. Entegre Markers 0.1.1, Xaero Minimap 26.6.0 ve WorldMap 1.47.0. Diğer mevcut mod sürümleri korundu; eski tekil ModernFix metadata kaydı aktif klasöre aynen taşındı. Complementary metadata ile indirilir, mevcut Iris tercihi korunur. Yerel 2.3.0-preview değişiklikleri bu dar sürüme taşınmadı veya geri alınmadı.

Geçen kontroller:

- Son pack/index bütünlüğü: 349 kayıt, 186 kalıcı uyumluluk URI'si; korumalı refresh ve tekrar, rollback, koordineli registry/index silme ve değiştirme negatif kontrolleri.
- Resmî packwiz-installer 0.5.14 ile sentetik geçiş, sürüm atlama, temiz kurulum ve tekrar. Kişisel byte korunumu ve çift metadata sahipliği; hatalı URI kaldırmanın gerçekten sildiği negatif kontroller.
- Tam eski 346 URI sahipliğiyle kurulan sentetik instance üzerinden gerçek aday pakete güncelleme ve tekrar; ayrıca tam temiz kurulum ve tekrar. Her iki adayda 77 JAR, exact bridge hash, yeni Xaero ve korunmuş ModernFix doğrulandı. 118 sentetik kişisel/ayar dosyası byte düzeyinde korundu; eski kişisel URI'lere sıfır indirme isteği. Gerçek oyuncu dosyaları kullanılmadı.
- Java 21 clean build ve yedi çekirdek test grubu; artifact ve sources JAR içinde smoke/test/veri yok.
- İzole macOS oyun açılışı: gerçek Fabric 0.18.4/API 0.141.3 ve Xaero sürümleri, gerçek Fabric kayıt olayıyla pair/status/forget callback'leri doğrulandı. Hesap, dünya veya sunucu bağlantısı yapılmadı.
- Bağımsız inceleme: açık P1/P2 yok; diff boşluk kontrolü temiz.

Bridge SHA256: c02d7b9b7836d0d2ea98ed6aba44c999f20f68bff3e8bdf8f9f02321ac13bad3.

Açık kabul: GitHub Actions gerçek uzaktan koşusu, canlı packwiz dağıtımı, gerçek oyuncu ile eşleştirme/marker görüntüsü/kişisel waypoint korunumu ve Windows. Tam 77 modlu üretim oyununun açılışı bu aşamada çalıştırılmadı; oyun açılış kanıtı bridge/Xaero/Fabric bileşenleri içindir.

Mevcut Prism bağlantısı main pack.toml adresini kullanır; komut değişikliği veya elle JAR kurulumu gerektirmez. Yayın sonrası aynı instance yeniden açılır, bir kez /entegremarkers pair ile site hesabından onaylanır. Sunucu plugini gerekmez.
