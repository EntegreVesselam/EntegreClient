# Eski packwiz sahipliğiyle güvenli uyumluluk

Kaynak: eski public index commitfb13701f1242c635023f295d487d98f723347e3a.
Eski listedeki186URI yeni aktif dağıtımda yoktur. Bunları doğrudan kaldırmak
packwiz-installer0.5.14 UpdateManager'ın eski manifestteki cachedLocation dosyasını
koşulsuz silmesine yol açar. Kişisel haritaları ve aynı mod JAR'ına bağlı eski
metadata sahipliklerini korumak için URI'ler kalıcı tutulur.

Her emekli URI tam olarak `metafile=false`, `preserve=true`, `alias="."`,
`hash-format="sha256"` ve boş içeriğin SHA256'sını taşır. `.` her zaman mevcut
oyun dizinidir. Installer metadata indirmez; dizin dosya gibi okunamaz ve
`preserve` mevcut hedef için indirmeden döner. Eski cachedLocation sahipliği
manifestte kalabilir: bu bir sahiplikten çıkarma değildir. URI'ler kalıcıdır.

Aktif dağıtım oyuncu/cache dosyası içermez. Şu anki Git ağacından eski dosyalar
kaldırılır; geçmiş Git commitleri yeniden yazılmaz. Registry yol adlarını içerir,
harita içeriğini içermez; oyuna kurulmaz. Temiz kurulumda emekli URI'lerden dosya,
oyuncu dizini veya ağ isteği oluşmaması test edilir.

`refresh-pack.py` önce standart refresh yapar, sonra registry kayıtlarını ekler,
pack/index hashlerini düzeltir ve strict verifier çalıştırır. Hata durumunda
pack/index önceki byte'lara geri döner. Kaynak commit, 186 URI sayısı ve sıralı URI özeti doğrulayıcıda sabittir;
registry ile index birlikte daraltılsa da kontrol reddeder. Baseline değişimi
ayrıca inceleme ve geçiş kanıtı gerektirir. Verifier eksik veya değiştirilmiş URI,
özellik/alias/hash, yinelenen hedef, traversal ve kayıt dışı oyuncu dosyasını
reddeder. Bu yüzden yalnız başarılı verifier/CI çıktısı yayımlanır.

Gerçek installer ile sentetik test: eski→yeni, tekrar, sürüm atlama, temiz→tekrar,
hiç özel URI download olmaması ve mevcut byte korunumu. Negatif kontrol yalnız
sentetik dosyada bir URI kaldırıldığında dosyanın gerçekten silindiğini kanıtlar.
Aynı JAR'ı paylaşan eski metadata sahiplikleri de test edilir. Gerçek oyuncu
verileri testte kullanılmaz. Runtime köprüsü kişisel waypoint dosyası yazmaz.

Bu davranış packwiz-installer0.5.14 ile doğrulanır. Installer güncellenirken aynı
korunma matrisi tekrar çalıştırılmalı; emekli URI'ler temizlenmemelidir.

Resmî kaynaklar:
- https://github.com/packwiz/packwiz-installer/blob/v0.5.14/src/main/kotlin/link/infra/packwiz/installer/UpdateManager.kt
- https://github.com/packwiz/packwiz-installer/blob/v0.5.14/src/main/kotlin/link/infra/packwiz/installer/DownloadTask.kt

Prism notu: packwiz-installer sürüm farkında mmc-pack.json güncelleyebilir; bu
sürüm loader değişimi istemez. Daha geniş mod güncellemesi ve Fabric0.19.5 geçişi
ayrı yerel2.3.0-preview çalışmasıdır; bu dar otomatik güncellemeye dahil değildir.
