# Duyurular ve etkinlikler — 2.1.6 yerel adayı

18 Ekim 2026. Taban: yayımlanmış 2.1.5 (`2c5112d`). Bu belge yerel adayı ve
kabul sınırlarını kaydeder; yayın koordinatöre aittir. Önce iki site metin kaynağı,
sonra client paketi yayımlanmalıdır.

## Oyuncunun gördüğü değişiklik

Duyurular ile etkinlikler ayrı, koyu paneller üzerinde gösterilir. Gövde yazısı
1.0 ölçeklidir; 0.85 küçültmenin ince ve dengesiz pikselleri kaldırıldı. İki sütun
küçük pencerede daralır, geniş pencerede 240 GUI birimine kadar büyür. Oyun
düğmeleri kendi Minecraft işlevlerini kullanır. İlk duyuru ve ilk etkinlik
640×480 / GUI320×240 sınırında tamamen okunur; sonraki kayıtlar ilgili alanın
kaydırma çubuğundan okunur. Sol repliğin iki satırı için de yeterli yükseklik ayrıldı.

Duyurular: son iki başlık ve kısa özet. Etkinlikler: siteden gelen en yakın iki
uygun etkinliğin başlığı, tarihi ve varsa TSİ saati. Başlıklar ilgili duyuru veya
pano yazısını açar. Bağımsız **Tüm duyurular** ve **Tüm etkinlikler** düğmeleri
`/announcements` ve `/events` sayfalarını açar. Pano, Galeri, Harita bağlantıları
sol panelde korunur. Başlıkların, yerel fotoğrafların ve oyun düğmelerinin görünmesi
uzak metnin tamamlanmasına bağlı değildir. İnternet yokken metin boş kalabilir;
oyun düğmeleri ve site bağlantıları yerinde kalır. Yeni yükleme/önbellek sistemi yoktur.

## İlk açılışın somut nedeni

2.1.5 kaynağı `/api/client-announcements/text` uzantısızdı. FancyMenu3.8.4,
`ResourceHandler.get` içinde dosya türünü URL'den tanıyamayınca
`FileType.isFileTypeWebAdvanced → WebUtils.getMimeType → URLConnection.getContentType`
yoluna giriyor. Native thread dump bu HTTP beklemesini **Render thread** üzerinde
gösterdi. Kontrollü 12 saniye gecikmeli uzantısız kaynakta tekrar sorgular oluştu
ve ilk çizim 80 saniyeyi aştı; test alıcısı kapatılınca watchdog oturumu sonlandırdı.
Bu başarısız koşul düzeltmenin gerekçesidir, kabul başarısı sayılmaz.

Aynı JAR, aynı 12 saniye gecikme ve `.txt` sonlu URL ile menüyü hemen çizdi:
ilk title karesi başlangıçtan 6640ms, metin 18632ms; tek GET yapıldı. `FileType`
uzantıyı yerelde tanıdı, `PlainText.web` yüklemesi arka planda kaldı. Bu nedenle
iki üretim kaynağı **tam olarak `.txt` ile biter**, query eki kullanılmaz:

- `https://entegrevesselam.com/api/client-announcements/menu.txt`
- `https://entegrevesselam.com/api/client-events/menu.txt`

Normal public 2.1.5 feed testinde ilk boş alan yaklaşık 185ms sonra doldu;
JSON/placeholder kaçağı veya bozuk newline görülmedi. Yüklü durumda eski 200×79
alan ve 0.85 ölçeği okuma güçlüğünü artırıyordu. Oturum içi resource cache davranışı
korunur; anlık etkinlik bildirimi veya periyodik yenileme vaat edilmez.

## Kabul kanıtı ve sınırlar

`community-events-verification.json` son dosya/hash ve test kaydını içerir.
Native Minecraft1.21.11, Fabric0.18.4, FancyMenu3.8.4, Java21/macOS kullanıldı.
Yalnız ayrı `/tmp` oyun klasörleri, sahte kullanıcı, uzun Türkçe sentetik metinler
ve localhost alıcı kullanıldı. Geliştirme class adları eşlendi, intro testte atlandı.
Üretim site URL'leri testte yalnız localhost `.txt` kaynaklarıyla değiştirildi.

İki boyutta ilk kayıtların görünümü, replik satırları, bağımsız bağlantı eylemleri
ve uzak kaynak yokken menünün kullanımı kontrol edilir. Dış linkler native
OpenLink/OS açma sınırında yakalanır; gerçek tarayıcı oturumu açılmaz. Dünyaya,
oyun sunucusuna veya gerçek hesaba girilmez. Windows, gerçek Prism intro geçişi
ve canlı yeni endpoint kabulü bu yerel kanıta dahil değildir.

Fotoğraf/slideshow dosyaları, yükleme ekranı, updater, marker bridge0.1.3,
mod metadata'sı ve oyuncu/cihaz verisi bu değişiklikte aynı kalır. Packwiz'in
186 kalıcı uyumluluk URI'si korunur. Kaynak/belge/önizleme dosyaları oyun paketine girmez.
