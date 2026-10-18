# Topluluk menüsü — yerel aday

Sonraki fotoğraf havuzu değişikliği: [Anı fotoğrafları](community-memories.md).

18 Ekim 2026. Yayımlanmış 2.1.3 tabanı (`c591f283`) üzerinde hazırlanmıştır.
İlk yerel adaydan sonra kullanıcının açık yayın talebiyle sürüm 2.1.4 hazırlandı;
güncelleme kanalı aynı kaldı. Yayın sonucu `release-2.1.4.md` belgesinde tutulur.
Gerçek Prism instance'ına, hesaplara veya sunucu kayıtlarına kurulmadı.

## Oyuncunun göreceği değişiklikler

- Açılışta sıcak ışıklı yapı ve oyuncu fotoğrafı, Entegre logosu ve gerçek yükleme
  çubuğu bulunur. Görsel yerel paketten gelir; yükleme ekranı haber indirmez.
- Ana menü ve çok oyunculu ekranı güncel şehir fotoğrafını kullanır. Fotoğraflar
  esnetilmez; ekran oranına göre kenarlardan kırpılır.
- Ana menüde sol tarafta oyun seçenekleri, sağ tarafta topluluktan duyurular vardır.
  Çok Oyunculu, Tek Oyunculu, Ayarlar, Modlar ve Çıkış mevcut Minecraft düğmeleridir.
- Sekiz kısa replik 12 saniyelik aralıklarla rastgele seçilir. Mevcut Entegre/Vesselam
  başlık oyunları ve çaylı kolaj dosyası korunur.
- Duyuru başlıkları ilgili site yazısını açar. Pano, Galeri ve Harita kısayolları ile
  ayrı “Tüm duyurular” düğmesi oyuncuyu mevcut topluluk alanlarına götürür.
- Ekrandaki sürüm 2.1.4 ile eşleştirildi; teknik donanım bilgi bloğu kaldırıldı.

## Duyuruların çalışması

Mevcut FancyMenu 3.8.4 Text v2 öğesi şu kaynağı asenkron okur:
`https://entegrevesselam.com/api/client-announcements/text`.
Ek mod veya hesap bağlama gerekmez. Site tarafındaki eşlik eden değişiklik en yeni
üç duyuruyu kısa, güvenli metin ve sabit site bağlantılarıyla üretir.

**Önce site endpoint'i yayımlanmalıdır.** Bu yerel çalışma canlı siteyi güncellemez.
FancyMenu kaynağı oturum içinde önbellekleyebilir; bu sürüm anlık bildirim veya
periyodik haber yenileme vaat etmez. Yeni oyun oturumu / menü kaynağını yeniden
yükleme yeni yanıt alır. Sunucu tarafındaki 60 saniyelik cache ayrı bir katmandır.

İnternet veya endpoint yoksa haber alanı boş kalabilir. Oyun seçenekleri, yerel
görseller, replikler ve bağımsız site bağlantıları kullanılabilir kalır. Dış linkler FancyMenu'nün
mevcut bağlantı açma yolunu kullanır; tarayıcı oturumu veya giriş bilgisi taşınmaz.
Pano/galeri/harita yetkisi gerekiyorsa site kendi normal giriş akışını uygular.

## Görseller ve replik kaynakları

Orijinal görseller Browser üzerinden indirildi; piksel içeriği değiştirilmedi.
Paylaşan kişi, paylaşım tarihi, ölçü ve SHA-256 bilgileri
[`community-assets.json`](community-assets.json) içindedir. Paylaşım tarihi fotoğrafın
çekildiği tarih olarak yorumlanmaz. Süresi dolabilen Discord CDN adresleri pakette yoktur.

- Şehir: [Flowingday16, 15 Eylül 2026](https://discord.com/channels/565637723180761108/580140119487414316/1549165424573157608).
- Yükleme: [Kaylorex, 29 Eylül 2026](https://discord.com/channels/565637723180761108/580140119487414316/1554500526987542680).
- [Küçük Entegre çağrısı](https://discord.com/channels/565637723180761108/580140119487414316/915313124058488922),
  [Ice Tea reklamı](https://discord.com/channels/565637723180761108/580140119487414316/1316049034234036326),
  [Lag Bakanlığı](https://discord.com/channels/565637723180761108/580140119487414316/1399502272727093439),
  [demiryolları afişi](https://discord.com/channels/565637723180761108/580140119487414316/1448002605921079439),
  [shulker çağrısı](https://discord.com/channels/565637723180761108/580140119487414316/1046193774851801178),
  [redstone mühendisi](https://discord.com/channels/565637723180761108/580140119487414316/1502405306062475284),
  [Hıyar kelime oyunu](https://discord.com/channels/565637723180761108/580140119487414316/587608720666787860).

Replikler kısa menü metni olarak kullanılır; oyun kuralı veya performans vaadi değildir.

## Kapsam ve kontrol

Paket farkı üç FancyMenu düzeni, iki fotoğraf ve bir replik dosyasıdır. `index.toml`
ve `pack.toml` yalnız bu içeriklerin kayıt/hash güncellemesini taşır. Belgeler oyun
paketine girmez. Eski dosyalar silinmez; 186 kalıcı uyumluluk URI'si korunur.

- `scripts/verify-pack.py`: 357 kayıt doğrulandı (taban 354).
- `scripts/check-pack-compat.py`: dosya sahipliği, çakışma ve geri alma kontrolleri geçti.
- `scripts/check-updater.py`: mevcut güncelleyici kontrolleri geçti.
- Yalnız üç mevcut düzen kaydı değişti; güncelleyici ve mod kayıtları aynı kaldı.
- Gerçek FancyMenu görünüm, küçük ekran, klavye odağı ve kaynak hatası kontrolleri aşağıdadır.

Kurulum için bu klasörü gerçek instance üzerine bütünüyle kopyalamayın. Olağan paket
yayın yolu kullanılmadan oyuncuların istemcisi değişmez. İlk yerel hazırlık commit/push/yayın içermiyordu; ardından kullanıcı 2.1.4 yayınına
açıkça yetki verdi. Bu yayının sonucu `release-2.1.4.md` içindedir.

## Gerçek Minecraft önizlemesi

Ayrı `/tmp/entegre-community-native` oturumunda Minecraft 1.21.11, Fabric 0.18.4,
FancyMenu 3.8.4 ve mevcut menü bağımlılıkları kullanıldı. Sahte EntegreTest kullanıcısı,
boş oyun klasörü ve localhost üzerindeki **ÖRNEK** duyurularla çalışıldı. Hesap,
dünya, sunucu listesi veya updater kopyalanmadı; dünyaya/sunucuya girilmedi.

- 1280×720 (GUI640×360), 800×500 (GUI400×250) ve 640×480 (normal minimum
  GUI320×240) görünümünde panel, Türkçe replik ve düğmeler incelendi.
- 1920×1080 ve 21:9 geniş ekran görünümü incelendi. macOS Retina'da
  2560×1080 pencere isteği 5120×2160 framebuffer üretti; bu fiziksel Windows
  2560×1080 testinin yerine geçmez. Fotoğraf oranı korundu.
- Türkçe telif metninin taşması bulundu ve düzeltildi: bu widget'ın sabit
  genişlik özelleştirmesi kaldırıldı, Minecraft'ın kendi dil uyumlu yerleşimi
  korundu. Sürüm etiketi ayrı satıra alındı. Son görüntülerde taşma yok.
- Ayarlar gerçek OptionsScreen'i, Modlar gerçek ModsScreen'i açtı.
- Tab odağı Tüm duyurular düğmesine ulaştı; görünür odak çerçevesi incelendi.
  Gerçek düğme eylemi işletim sistemi tarayıcı açma sınırında yakalanarak doğru
  canonical hedef doğrulandı; dış tarayıcı açılmadı. FancyMenu'nün bu yolu ayrıca
  onay penceresi göstermeden bağlantıyı işletim sistemine iletir.
- Duyuru kutusuna gerçek scroll olayı iletildi; offset 0’dan -50’ye değişti ve
  üçüncü başlık/özet framebuffer’da görüldü. Tab ardından Enter ile canonical
  duyurular hedefi yeniden yakalandı.
- Erişilemeyen localhost kaynağıyla haber gövdesi boş kaldı; ana menü, yerel
  arka plan ve sabit Tüm duyurular düğmesi kullanılabilir kaldı. Bu hata denemesi
  footer düzeltmesinden önceki aynı davranışa sahip düzende yapıldı.
- Gerçek loading ve resource reload sırasında fotoğraf, başlık, logo ve gerçek
  progress görüldü. Çok erken yüklemede fontlar hazır olmadığından metinler
  henüz görünmeyebilir.

Geliştirme ortamının sınıf adları nedeniyle test kopyasında yalnız
`customizablemenus.txt` eşlemesi uyarlandı. Testte eski uzun intro atlandı;
üretim intro/options dosyası değiştirilmedi. Tam Prism intro→menü geçişi ve
Windows kabulü çalıştırılmadı. 320×180 zorlanmış GUI deneyi Minecraft'ın normal
minimum yüksekliğinin altındadır ve kabul ölçüsü olarak kullanılmadı.

Önizlemeler gerçek oyun framebuffer'larıdır; duyurular sentetik test içeriğidir:

- [Ana menü](previews/community-menu.png)
- [En küçük normal GUI](previews/community-menu-small.png)
- [Geniş görünüm](previews/community-menu-wide.png)
- [Klavye odağı](previews/community-menu-keyboard.png)
- [Yükleme](previews/community-loading.png)

Makine tarafından okunabilen son ölçü/hash kaydı: `community-menu-verification.json`.
[Kaydırılmış duyuru kutusu](previews/community-menu-scrolled.png).

Bize ait test Minecraft ve localhost fixture süreçleri deneme sonunda kapatıldı.
