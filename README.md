# Entegre Client 2.4.7 — Minecraft 26.3

Java **25**, Fabric **0.19.5**, LWJGL **3.4.3**, Intermediary **26.3**.

[EntegreClient-v2.4.7.zip indir](https://github.com/EntegreVesselam/EntegreClient/releases/download/v2.4.7/EntegreClient-v2.4.7.zip)
ve Prism’de **Kurulum Ekle → İçe Aktar** ile ayrı profil olarak açın.
Ardından Java 25 seçip başlatın. İlk açılışta modlar indirilir; sonraki
güncellemeler aynı minecraft-26.3 kanalından gelir. Eski profilinizin üzerine
çıkarmayın; dünyalarınızı, haritalarınızı ve ayarlarınızı koruyun.
Açık minecraft-26.2 kanalını kullanan profiller 26.2’de kalır.

## Bu sürüm

- [Voxy 0.2.20-beta](https://modrinth.com/mod/voxy/version/iIrM8ZKQ), resmi
  Minecraft 26.3 derlemesine (`5bf33350`) taşındı. Özel beta yerine resmi CDN
  kullanılır; sürüm adı aynı olsa da dosya hash'i değişti.
- Fabric API **0.164.0**, Freecam **1.5.1**, Xaero Minimap **26.6.1** ve
  Xaero World Map **1.47.1** güncellendi. Xaero modları birlikte güncellenir.
- Sodium **0.9.2**, Iris **1.11.7** ve Reese's Sodium Options **2.2.5** korunur.
  JVM/RAM/thread ayarları zorlanmaz. Bu sürüm için ölçülmüş FPS kazancı yoktur.

Önceki 2.4.6 paketinden korunanlar:

- Duyuru ve etkinlik kartları daha geniş, satır aralıkları daha rahattır.
  Ana menüde tek güncel duyuru ve tek yaklaşan etkinlik gösterilir; tüm içerikler
  için mevcut site bağlantıları kullanılır. Dar GUI boyutlarında kaydırma korunur.
- macOS’ta başlıkların, alt bağlantıların ve sürüm yazısının kırpılması giderildi.
- Entegre Markers 0.2.2: mesajlar varsayılan Türkçe; `bagla` / `bağla`, `durum`,
  `esitle` / `eşitle`, `unut` komutları eklendi. Eski komutlar, mevcut eşleştirme ve
  kişisel işaretler korunur. Minecraft genel dil tercihi değiştirilmez.
- İstatistik hazırlığı bu sürüme dahil değildir.

Önceki 2.4.5 paketinden korunanlar:

- Bobby, Falling Leaves, Visuality, Clumps, Architectury API, üst düzey
  Text Placeholder API ve Packet Fixer çıkarıldı.
- [Particle Rain 4.0.2](https://modrinth.com/mod/particle-rain),
  [Icons 1.14](https://modrinth.com/resourcepack/icons) ve
  [Even Better Enchants v3](https://modrinth.com/resourcepack/even-better-enchants) eklendi.
  Modrinth içerikleri özgün CDN adreslerinden indirilir.
- NoTotemOfUndyingOnOffHand, QuietThunderPack ve
  TurtlePumpkinPackByEminGT etkin. Bu kullanıcı paketlerinin yalnız metadata’sı
  kaynak biçimi 97.1’e uyarlandı; model, ses ve dokuları korundu.
- Ana menüde Tek Oyunculu en üstte; oyun, haber ve etkinlikler ayrı kartlarda.
  Galeri/Wallpaper bağlantıları ve toplam 16 topluluk slaytı vardır.

## Mod seçenekleri ve kişisel ayarlar

Yeni profilde 60 üst düzey mod kurulur; sekiz seçenek varsayılan kapalıdır:
Controlify, Sound Physics Remastered, Presence Footsteps, 3D Skin Layers,
Not Enough Animations, Smooth Swapping, Capes ve Shoulder Surfing.
Tüm seçenekler açıkken 68 mod vardır; gömülü kütüphaneler bu sayıya dahil değildir.
Güncelleyici seçenek ekranı veya `--settings` ile tercihler değiştirilebilir.
Mevcut isteğe bağlı mod seçimleri korunur; eski zorunlu Controlify yeni
isteğe bağlı duruma geçerken varsayılan kapalı olur.

Yeni profil Voxy SSAO Basic ve daha seyrek Particle Rain efektleri kullanır.
Thread sayısı otomatik kalır; Sodium 0.9.2 ve Iris 1.11.7 korunur.
`options.txt`, Voxy/Particle Rain ayarları ve kayıtlı sunucu listesi
`preserve=true` ile mevcut kişisel dosyaların üzerine yazılmaz.
Harita/LOD önbelleği veya oyuncu verileri paketlenmez ve silinmez.
186 kalıcı uyumluluk kaydı korunur. Varsayılan sunucu `entegrevesselam.com`;
marker eşitlemesi `entegrevesselam.com` ve `ugurdindar.com` adreslerini destekler.

## Doğrulama sınırı

Paket/hash, bağımlılık çözümü, güncelleyici seçimleri, veri koruması ve menü
geometrisi kontrol edildi. Tarayıcı tasarım önizlemesi gerçek Minecraft
çizimi değildir. Bu sürüm için ölçülmüş FPS artışı, canlı Voxy LOD akışı veya
Windows Prism uçtan uca kabulü iddia edilmiyor. Even Better Enchants v3
resmî sürüm listesinde 26.3 etiketi taşımıyor.
