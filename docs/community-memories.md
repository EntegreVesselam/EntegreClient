# Anı fotoğrafları — yerel aday

18 Ekim 2026. Entegre Client 2.1.4 içeriği üzerinde; yayın yapılmadı.

Ana menü altı yerel anı fotoğrafını FancyMenu'nün slayt gösterisiyle değiştirir.
Süre 27 saniye, geçiş yumuşaktır. Yerleşik rastgele seçim ardışık aynı kareyi
engeller; tüm havuz bitmeden tekrar etmeyen bir sıra vaat etmez. FancyMenu 3.8.4
ilk geçişi sürenin yarısında başlatır; sonraki geçişler 27 saniyedir. Geçişin
opaklık hızı kare hızından etkilenir.

Yükleme ekranında aynı altı kareden biri seçilir ve yükleme boyunca sabit kalır.
Yazı, logo ve gerçek ilerleme çubuğu ortak düzen içinde korunur. Fotoğraf düzenleri
tek rastgele gruptadır; seçim ekran yeniden boyutlandırıldığında değişmez.
FancyMenu yeni ekran algıladığında yeniden kura çeker; aynı sınıftaki yükleme
ekranı arada başka ekran başlatılmadan yeniden açılırsa önbellekteki kare
korunabilir. Yeni kura aynı fotoğrafı tekrar seçebilir.

Görseller oranı korunarak 1920×1080 çerçeveye sığdırıldı; boşluklar koyu renkle
dolduruldu, özgün sahne kırpılmadı veya esnetilmedi. Ekrandaki cover yerleşimi
pencere oranına göre çerçevenin kenarlarını kırpabilir; görüntüyü esnetmez.
Ortak oran, slayt gösterisinin ilk karenin ölçülerini bütün karelerde kullanması
nedeniyle gereklidir. Kaynaklar ve özgün dosya hashleri
[community-memories-assets.json](community-memories-assets.json) içindedir.

## Dar değişiklik

- Ana menüde yalnız `menu_background` bloğu değişti.
- Yüklemenin eski fotoğraf elementi altı küçük rastgele düzene taşındı; sabit
  öğeler değişmedi.
- Duyuru URL'si, oyun düğmeleri, klavye davranışı, çok oyunculu ekranı ve
  güncelleyici aynı kaldı. Yeni mod veya bağımlılık eklenmedi.
- Paket indeksi yeni fotoğrafları ve yapılandırmayı izler. Belgeler pakete girmez.

## Doğrulama

Ayrı boş oyun klasöründe Minecraft 1.21.11, Fabric 0.18.4, FancyMenu 3.8.4 ve
Drippy 3.1.1 ile gerçek renderer çalıştırıldı. Test hesabı sentetik, duyuru içeriği
yerel örnektir; dünya, hesap veya sunucu listesi kopyalanmadı. Test kopyasında
yalnız geliştirme sınıf eşlemesi ve intro atlama ayarı uyarlandı.

- Dört seçim `05 → 04 → 02 → 04`; aralıklar 13,513 / 27,013 / 27,005 saniye.
  Yumuşak geçişte iki karenin aynı anda çizildiği framebuffer ile doğrulandı.
- Soğuk açılışta `03`, dört kaynak yenilemesinde `01 → 05 → 06 → 02` seçildi.
  Her yenilemede tam bir fotoğraf vardı; 58–62 tick boyunca seçim sabit kaldı.
  Ortak başlık, logo ve gerçek ilerleme çubuğu göründü.
- 1280×720 ve 640×480 pencereler incelendi; küçük GUI 320×240. Düğme ve
  duyuru alanları görünür kaldı; fotoğraflar esnemedi.
- İlk native deneme eksik `type = slideshow` başlığını yakaladı. Başlık
  eklendikten sonra tüm native kabul tekrarlandı ve geçti; son logda hata yok.
- Paket doğrulayıcı, guarded refresh, uyumluluk ve updater kontrolleri geçti:
  370 kayıt, 186 kalıcı uyumluluk URI'si; yalnız iki mevcut düzen kaydı değişti,
  13 yeni içerik kaydı eklendi. Sürüm/mod/updater metadata değişmedi.
- Bağımsız salt okunur inceleme kapsam ve hash korunumunu doğruladı.

Önizlemeler: [menü](previews/community-memories-menu.png),
[geçiş](previews/community-memories-fade.png),
[küçük ekran](previews/community-memories-small.png),
[yükleme — şehir](previews/community-memories-loading-city.png),
[yükleme — TNT anısı](previews/community-memories-loading-tnt.png).
Ölçümler ve dosya hashleri: [doğrulama kaydı](community-memories-verification.json).

Test oyunu normal kapatıldı. Windows, tam Prism intro akışı, gerçek hesap ve
canlı oyun bağlantısı bu değişiklikte denenmedi. Düğme eylemleri tekrar
tıklanmadı; mevcut düğme tanımları byte karşılaştırmasıyla korunmuş durumda.
