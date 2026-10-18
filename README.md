# 2.4.2 düzeltmesi

Tekrarlanan sürüm geçişi uyarısı giderildi. Uyarıda kalan 2.4.1 oyuncuları,
Prism kapalıyken [onarım ZIP’ini](https://github.com/EntegreVesselam/EntegreClient/releases/download/v2.4.2/EntegreClient-v2.4.2-onarim.zip)
profil klasörünün içine çıkarmalı. Yalnız iki güncelleyici dosyası yenilenir.
Yeni kurulum için [2.4.2 yayını](https://github.com/EntegreVesselam/EntegreClient/releases/tag/v2.4.2).
Ayrıntı: docs/release-2.4.2.txt. Aşağıdaki 2.4.1 notları tarihçedir.

# Entegre Client 2.4.1 — 26.2 varsayılan / 26.3 ayrı profil

Mevcut oyuncular normal güncellemeyle **26.2** kanalına yönlendirilir.
İlk açılış yeni güncelleyiciyi teslim eder; sonraki açılış profili yedekler ve
Prism'i tamamen kapatıp açmanızı ister. **Java 25** seçili olmalıdır.
Bu main paketinin 1.21.11 metadata'sı yalnız eski motorlara güvenli teslim için
korunur; hedef oyun sürümü 26.2'dir. Teknik akış: `docs/26x-followup-rollout.txt`.

Yeni kurulumda [26.2 ZIP](https://github.com/EntegreVesselam/EntegreClient/releases/download/v2.4.1/EntegreClient-v2.4.1-mc26.2.zip)
veya [ayrı 26.3 ZIP](https://github.com/EntegreVesselam/EntegreClient/releases/download/v2.4.1/EntegreClient-v2.4.1-mc26.3.zip)
dosyasını Prism'e içe aktarın. İki sürüm için iki ayrı profil kullanın.

26.2'de Voxy/Extra/WorldGen; 26.3'te Voxy beta vardır, Extra/WorldGen kapalıdır.
Haber/etkinlik menüsü ve ortada kalan Friends/yardımcı düğmeler düzeltildi.
İki site metin kaynağı HTTP 200 ile kontrol edildi. Gerçek GPU LOD çizimi,
canlı oyun sunucusuyla LOD akışı, Windows ve gerçek Prism yeniden başlatma
kabulü henüz tamamlanmadı.

---

Aşağıdaki 2.1.5 notları tarihçedir.

# Entegre Client — 2.1.5

Ana menü altı topluluk fotoğrafını 27 saniyelik aralıklarla rastgele gösterir.
Yükleme ekranı her yeni açılışta tek fotoğraf seçer; görüntü oranları korunur.
Kısa replikler, son üç site duyurusu ve Pano / Galeri / Harita kısayolları korunur.
Harita işaretleri sitedeki renk seçimini aynen kullanır; oyundan tek tıklamayla
cihaz bağlama ve otomatik eşitleme desteklenir.

**Mevcut oyuncular:** Oyunu kapatıp Prism'deki aynı kurulumu yeniden açın;
normal paket güncellemesi yeni fotoğrafları ve harita köprüsünü getirir. Yeni bir ZIP kurmanız gerekmez.
FancyMenu/Drippy isteğe bağlı seçimleri korunur; bu menü bu modlar seçiliyken görünür.
İnternet olmadığında yerel görseller ve oyun seçenekleri açılmaya devam eder.
Haber kaynağı oturumda önbelleklenebilir; anlık bildirim değildir.

**Yeni kurulum:** [EntegreClient-v2.1.5.zip](https://github.com/EntegreVesselam/EntegreClient/releases/tag/v2.1.5)
dosyasını Prism'e içe aktarın. Küçük başlangıç paketi modları ilk açılışta indirir.
Görseller ve kontrol kapsamı: [anılar](docs/community-memories.md),
[topluluk menüsü](docs/community-menu.md), [harita köprüsü](marker-sync/README.md).

---

# Entegre Client — 2.1.3

Türkçe Entegre güncelleme penceresi eklendi; normal açılıştaki 10 saniyelik
seçenek sayacı kaldırıldı. Mod seçimleri, kişisel ayarlar ve mevcut 186 kalıcı
uyumluluk kaydı korunur. Minecraft/Fabric ve diğer mod sürümleri değişmez.

**Mevcut oyuncular:** Prism'de aynı kurulumu açın; tekrar indirme veya komut
ayarı gerekmez. İlk açılış geçiş dosyalarını alır; Minecraft başlayınca eski
başlatıcı yedeklenir. Sonraki açılışta aynı komut Türkçe Entegre penceresini açar.
Eski ekranın bir kez görünmesi normaldir. Sonraki güncelleyici motoru sürümleri
de aynı paket kanalıyla otomatik gelir.

**Yeni kurulum:** [v2.1.3 sürümündeki EntegreClient-v2.1.3.zip dosyasını](https://github.com/EntegreVesselam/EntegreClient/releases/tag/v2.1.3)
Prism'e içe aktarın. Bu küçük başlangıç paketi ilk açılışta Türkçe pencereyi
açar; modlar oradan indirilir ve isteğe bağlı modlar sorulur. Mevcut oyuncular
bu ZIP'i eski kurulumun üzerine açmamalı; normal açılış yeterlidir.

İngilizce seçimi sonraki işe bırakıldı. Dışarıdan gelen mod açıklamaları ve
teknik hata ayrıntıları çevrilmez. Desteklenen komutlar, geri alma ve derleme:
[güncelleyici yönergesi](launcher/README.md). Test kapsamı ve gerçek
Windows/Prism kabul sınırı: [doğrulama](launcher/docs/verification.md).

---

# Entegre Client — otomatik marker güncellemesi

## 2.1.5 — mevcut Prism kurulumu için

Minecraft1.21.11 / Fabric0.18.4 / FabricAPI0.141.3 korunur. Bu sürüm,
Entegre Markers0.1.3 köprüsünü mevcut Xaero Minimap26.6.0 / WorldMap1.47.0 ile
packwiz üzerinden dağıtır; diğer mevcut mod sürümlerini değiştirmez. Complementary
shader metadata ile indirilir; mevcut Iris seçimi `preserve` ile korunur.

Prism açılış komutu aynı kalır: mevcut `packwiz-installer-bootstrap.jar` ve
`https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/main/pack.toml`.
Oyunu kapatıp aynı instance'ı yeniden açın. Kurucu hatası varsa oyunu devam
ettirmek yerine hatayı bildirin; mod JAR'ını elle kopyalamak gerekmez.

Sunucuya `entegrevesselam.com` adresiyle girdikten sonra sohbetteki
**[Haritayı bağla]** bağlantısına tıklayın veya `/entegremarkers` yazın. Site
hesabıyla cihazı bir kez onaylayın; sonraki eşitlemeler otomatik yapılır.
`/entegremarkers sync` bağlı cihazda eşitlemeyi ister; eşleşme yoksa bağlamayı
başlatır. `/entegremarkers status` durumu gösterir; `/entegremarkers forget`
yerel bağlantıyı kaldırır. Oyun öndeyken liste 60 saniye aralıkla okunur;
elle komut da bu aralığı ve aynı anda tek istek sınırını korur. Sunucuya ek plugin gerekmez; veri korunan site API'sinden
gelir. Kişisel waypoint dosyaları yerine ayrı Xaero runtime katmanı kullanılır.

Ortak markerlar yalnız tam ekran dünya haritasında görünür; oyun içi HUD ve
minimap üzerinde görünmez. İsimlerden en fazla üç karakterlik kısa harfler
üretilir. Sitede seçilen 16 rengin tamamı, koyu kırmızı dahil aynen korunur. Kişisel waypoint ayarlarına veya dosyalarına dokunulmaz.

## Kişisel veriyi koruyan eski paket geçişi

Eski paket bazı oyuncu/cache dosyalarını yanlışlıkla yönetiyordu. Bunların eski
URI'leri kalıcı, indirme yapmayan uyumluluk kayıtları olarak indekste tutulur.
Mevcut dosyalar silinmez veya okunup yeniden dağıtılmaz; temiz kurulumda bu
eski dosyalar oluşturulmaz. Eski URI kayıtlarını silmek eski kurulumlarda veri
silebilir; görünürde gereksiz diye kaldırmayın. `compatibility/` kaynağı ve
üçüncü taraf installer davranışı `docs/packwiz-migration.md` içinde açıklanır.

Bakımda doğrudan `packwiz refresh` yerine Python3.11+ ile:

```sh
python3 scripts/refresh-pack.py
python3 scripts/verify-pack.py
python3 scripts/check-pack-compat.py
PACKWIZ_INSTALLER_JAR=/absolute/packwiz-installer.jar python3 scripts/check-packwiz-migration.py
```

Minecraft oyun içi/Windows kabulü gerçek kullanıcı oturumuyla ayrıca doğrulanır.
Eski oyuncu instance'ını test uğruna silmeyin veya sıfırlamayın.

---

# 🏰 Entegre Vesselam - Client

**Entegre Vesselam** (2013-∞) sunucusu için özel olarak hazırlanmış resmi mod paketidir.
*This is the official modpack tailored for the Entegre Vesselam server.*

---

<details>
<summary><strong>🇹🇷 Türkçe - Mod Listesi ve Açıklamalar (Tıkla ve Genişlet)</strong></summary>

### 🏰 Entegre Vesselam - Resmi Client Mod Paketi

Bu paket, sunucu deneyimini iyileştirmek; performansı maksimize etmek, ticareti ve oynanışı kolaylaştırmak (QoL) ve oyunun atmosferini zenginleştirmek için hazırlanmıştır.

#### 🚀 Performans ve Optimizasyon
*Oyunun yağ gibi akması için kaputun altındaki geliştirmeler.*

* **Sodium:** Minecraft'ın grafik motorunu baştan yazar, muazzam FPS artışı sağlar.
* **Lithium:** Oyun içi fizik ve döngülerin (tick) hesaplamasını optimize eder, sunucu/client gecikmesini azaltır.
* **FerriteCore:** RAM kullanımını ciddi oranda düşürür.
* **EntityCulling:** Duvar arkasında kalan (görünmeyen) yaratık ve blokları işlemeyerek performansı artırır.
* **More Culling:** EntityCulling'in kapsamadığı blok yüzeylerini ve eşyaları optimize eder.
* **ImmediatelyFast:** Arayüz ve metinlerin çizim hızını artırır (VBO optimizasyonu).
* **Krypton:** Ağ (Network) trafiğini optimize eder, ping dalgalanmalarını azaltır.
* **ModernFix:** Oyunun açılış hızını artırır ve birçok eski Minecraft bug'ını düzeltir, RAM tüketimini iyileştirir.
* **Fast IP Ping:** Sunucu listesindeki ping değerinin daha hızlı yüklenmesini sağlar.
* **Better Block Entities:** Sandık, yatak, tabela gibi blok varlıklarının render performansını artırır.
* **Sodium Extra:** Sodium'a ek ayarlar ekler (animasyonlar, parçacıklar, sis vb. kontrolü).
* **Dynamic FPS:** Oyun arka plandayken FPS'i düşürür, pil ömrünü uzatır (Steam Deck için ideal).
* **Clumps:** XP kürelerini gruplandırarak lag'ı azaltır.
* **Debugify:** Mojang'ın düzeltmediği onlarca vanilla bug'ını düzeltir.
* **BadOptimizations:** Render dışı oyun mantığını optimize eder.
* **ThreadTweak:** Thread önceliklerini optimize eder (Steam Deck gibi az çekirdekli cihazlar için).
* **Voxy:** Distant Horizons benzeri LOD (Level of Detail) sistemiyle sunucu render mesafesinin çok ötesinde arazi görünümü sağlar.
* **Voxy WorldGen:** Voxy'nin arazi oluşturma kalitesini ve hızını artırır.
* **Voxy Extra:** Voxy'ye ek görsel ve işlevsel iyileştirmeler ekler.
* **Voxy Server Side (v0.2.1):** Voxy'nin sunucu tarafında çalışmasını sağlar; tam uyum için istemciye de yüklenir.

#### 🛠️ Yaşam Kalitesi (QoL) & Ticaret
*Hayatta kalmayı ve ticareti keyifli hale getiren araçlar.*

* **Inventory Profiles Next:** Tek tuşla (R) sandıkları ve envanteri otomatik düzenler. Kırılan zırhı/aleti otamatik yeniler.
* **Mouse Tweaks:** Eşyaları sürükleyerek hızlıca sandığa koyma/alma gibi gelişmiş fare kontrolleri ekler.
* **AppleSkin:** Yemeklerin ne kadar doyuracağını ve gizli "doygunluk" (saturation) barını gösterir.
* **Jade:** Baktığınız bloğun ne olduğunu, sandıksa içini (açmadan), fırınsa pişme durumunu ekranın tepesinde gösterir.
* **Xaero's Minimap:** Ekranın köşesine, çevreyi ve yaratıkları gösteren bir mini harita ekler.
* **Xaero's World Map:** "M" tuşu ile açılan, gittiğiniz yerleri kaydeden tam ekran dünya haritası.
* **Shulker Box Tooltip:** Shulker kutularını yere koymadan, envanterde üzerine gelerek içini görmenizi sağlar.
* **Durability Tooltip:** Eşyaların ne kadar canı kaldığını renkli ve sayıyla gösterir.
* **Logical Zoom:** Optifine'daki gibi "Zoom" yapmanızı sağlar.
* **No Chat Reports:** Microsoft'un sohbet raporlama/banlama sistemini devre dışı bırakarak özgür iletişim sağlar.
* **Mod Menu:** Yüklü modların listesini ve ayarlarını görmenizi sağlayan menü.
* **BetterF3:** Debug ekranını (F3) daha temiz ve okunabilir hale getirir.
* **Controlling:** Mod tuş atamalarını arayabilir ve çakışmaları kolayca görebilirsiniz.
* **Bobby:** Sunucu render mesafesinin ötesinde chunk'ları yerel olarak önbelleğe alarak görüş mesafesini artırır.
* **Better Mount HUD:** Ata/tekneye binerken HUD'u iyileştirir.
* **Enchantment Descriptions:** Büyülü kitaplardaki büyülerin ne yaptığını açıklar.
* **Better Statistics Screen:** İstatistik ekranını daha kullanışlı ve okunabilir hale getirir.
* **Fabrishot:** Çok yüksek çözünürlüklü ekran görüntüleri almanızı sağlar.
* **Resourcify:** Oyun içinden kaynak paketleri ve shader'ları indirip yönetmenizi sağlar.
* **Smooth Swapping:** Envanterde eşya taşırken yumuşak animasyonlar ekler.
* **Shoulder Surfing Reloaded:** Üçüncü şahıs kamerasını omuz üstü perspektifine alarak nişan almayı ve genel görüşü iyileştirir.

#### 🏗️ İnşa ve Şematik Araçları
* **Litematica:** Şematik dosyalarını yükleyip holografik rehber olarak kullanarak blok blok inşa yapmanızı sağlar.
* **Tweakeroo:** Hassas blok yerleştirme, serbest kamera ve inşa kolaylıkları.
* **MiniHUD:** Işık seviyesi, spawn alanları, yapı sınırları gibi bilgileri ekranda gösterir.
* **Syncmatica:** Sunucudaki diğer oyuncularla Litematica şematiklerini paylaşmanızı sağlar.
* **Continuity:** Bağlantılı dokular (cam, kitaplık vb.) ekler.

#### 🎮 Steam Deck ve Kontrolcü Desteği
* **Controlify:** Tam kontrolcü desteği (Steam Deck, Xbox, PlayStation). Gyro aiming, ekran üstü tuş ikonları ve menü navigasyonu.
* **Reese's Sodium Options:** Sodium ayar ekranını kontrolcü ile daha kolay kullanılabilir hale getirir.

#### 🎨 Görsellik ve Atmosfer
*Vesselam dünyasını daha canlı kılan estetik dokunuşlar.*

* **Iris:** Shader (gölgelendirme) paketlerini kullanabilmenizi sağlar.
* **Visuality:** Kırılan bloklardan parçacıklar dökülmesi, slime vıcıklığı gibi tatlı görsel efektler ekler.
* **Falling Leaves:** Ağaçlardan yaprakların süzülerek yere düşmesini sağlar, ormanlara canlılık katar.
* **3D-Skin-Layers:** Oyuncu skinlerindeki 2. katmanları (ceket, saç vb.) 3 boyutlu ve derinlikli gösterir.
* **NotEnoughAnimations:** Yemek yeme, harita tutma, kürek çekme gibi eksik animasyonları ekler.
* **Dynamic Crosshair:** Nişangahın (artı işareti) baktığınız şeye veya kullandığınız silaha göre şekil değiştirmesini sağlar.
* **Chat Heads:** Sohbet kısmında mesaj yazan kişinin kafasını gösterir.
* **Capes:** Optifine veya diğer pelerin modlarıyla uyumlu pelerinleri görmenizi sağlar.
* **Drippy Loading Screen:** Sunucuya özel yükleme ekranı tasarımı (Vesselam Loading).
* **FancyMenu:** Ana menü özelleştirmesi (Vesselam Menu).

#### 🔊 Ses ve Duygu
* **Sound Physics Remastered:** Mağaralarda yankı, blokların arkasında ses boğulması gibi gerçekçi ses fiziği ekler.
* **Presence Footsteps:** Yürüdüğünüz zemine göre (çim, taş, tahta) çok daha gerçekçi ve tok adım sesleri çıkarır.
* **Melody:** (Gerekli kütüphane/Müzik entegrasyonu).

#### ⚙️ Teknik Altyapı ve Kütüphaneler
*Diğer modların çalışması için gerekli olan çekirdek dosyalar.*
*Fabric API, Cloth Config v20, Fabric Language Kotlin, Konkrete (FancyMenu için), Packet Fixer (Bağlantı kararlılığı), Placeholder API, SuperMartijn642's Config Library, libIPN (Inventory Profiles için), Searchables (Controlling için), MaLiLib (Litematica/MiniHUD/Tweakeroo için), Architectury API, TCDCommons API, Prickle, Forge Config API Port (Shoulder Surfing için).*

#### 🖼️ Kaynak Paketleri
* **EAA_Golem:** Entegre Vesselam sunucusuna özel Golem (EAA) tekstür paketi.
* **ESK_Golem:** Entegre Vesselam sunucusuna özel Golem (ESK) tekstür paketi.

> **Not:** Bağımlılık modları (kütüphaneler) dışındaki tüm modlar isteğe bağlıdır. İlk çalıştırmada packwiz-installer hangi modları kurmak istediğinizi soracaktır.

</details>

<details>
<summary><strong>🇬🇧 English - Mod List & Descriptions (Click to Expand)</strong></summary>

### 🏰 Entegre Vesselam - Official Client Modpack

This pack is specially crafted for the **Entegre Vesselam** server (2013-Present). The goal is to maximize performance, facilitate trade and gameplay (QoL), and enrich the game atmosphere.

#### 🚀 Performance & Optimization
*Under-the-hood improvements for a buttery smooth experience.*

* **Sodium:** Replaces the rendering engine, providing massive FPS boosts.
* **Lithium:** Optimizes game physics and tick calculations, reducing server/client lag.
* **FerriteCore:** Significantly reduces RAM usage.
* **EntityCulling:** Skips rendering non-visible entities and blocks behind walls to boost performance.
* **More Culling:** Optimizes block faces and items not covered by EntityCulling.
* **ImmediatelyFast:** Speeds up immediate mode rendering (UI, text).
* **Krypton:** Optimizes network traffic, reducing ping jitter.
* **ModernFix:** Speeds up game startup and fixes various Minecraft bugs, improves RAM usage.
* **Fast IP Ping:** Speeds up server list pinging.
* **Better Block Entities:** Faster rendering of block entities (chests, beds, signs, etc.).
* **Sodium Extra:** Additional toggle options for Sodium (animations, particles, fog, etc.).
* **Dynamic FPS:** Reduces FPS when the game is in the background, saving battery life (ideal for Steam Deck).
* **Clumps:** Groups XP orbs together to reduce lag.
* **Debugify:** Fixes dozens of vanilla Minecraft bugs that Mojang hasn't addressed.
* **BadOptimizations:** Optimizes non-rendering game logic.
* **ThreadTweak:** Optimizes thread priority for limited-core devices (ideal for Steam Deck).
* **Voxy:** LOD (Level of Detail) rendering far beyond the server's render distance, similar to Distant Horizons.
* **Voxy WorldGen:** Improves the quality and speed of terrain generation for Voxy.
* **Voxy Extra:** Additional visual and functional enhancements on top of Voxy.
* **Voxy Server Side (v0.2.1):** Enables Voxy support on the server side; also installed client-side for full compatibility.

#### 🛠️ Quality of Life (QoL) & Trade
*Tools to make survival and trading more enjoyable.*

* **Inventory Profiles Next:** Auto-sorts chests and inventory with one key (R). Auto-replaces broken tools/armor.
* **Mouse Tweaks:** Advanced mouse controls like drag-to-move items for fast looting/sorting.
* **AppleSkin:** Shows food saturation and exhaustion levels on the HUD.
* **Jade:** Shows what block or entity you are looking at (WAILA), including chest contents and furnace progress.
* **Xaero's Minimap:** Adds a minimap to the corner of the screen showing surroundings and entities.
* **Xaero's World Map:** Full-screen world map (M key) that saves your exploration progress.
* **Shulker Box Tooltip:** View shulker box contents in your inventory without placing them.
* **Durability Tooltip:** Shows item durability numerically and with color indicators.
* **Logical Zoom:** Adds a zoom key (similar to Optifine).
* **No Chat Reports:** Disables Microsoft's chat reporting telemetry for free communication.
* **Mod Menu:** Menu to view and configure installed mods.
* **BetterF3:** Cleaner, more readable debug screen (F3).
* **Controlling:** Searchable keybindings menu, easily spot key conflicts.
* **Bobby:** Caches chunks client-side to extend render distance beyond server limits.
* **Better Mount HUD:** Improved HUD when riding horses, boats, etc.
* **Enchantment Descriptions:** Shows what enchantments do on enchanted books.
* **Better Statistics Screen:** Improved and more readable statistics UI.
* **Fabrishot:** Take extremely high-resolution screenshots.
* **Resourcify:** Browse and install resource packs and shaders in-game.
* **Smooth Swapping:** Smooth item move animations in inventories.
* **Shoulder Surfing Reloaded:** Shifts the third-person camera to an over-the-shoulder perspective for improved aiming and visibility.

#### 🏗️ Building & Schematic Tools
* **Litematica:** Load schematics and use holographic overlays to build block-by-block in survival.
* **Tweakeroo:** Accurate block placement, free camera, and building QoL tweaks.
* **MiniHUD:** Configurable overlay showing light levels, spawn spheres, structure bounding boxes.
* **Syncmatica:** Share Litematica schematics with other players on the server.
* **Continuity:** Connected textures for glass, bookshelves, and more.

#### 🎮 Steam Deck & Controller Support
* **Controlify:** Full controller support (Steam Deck, Xbox, PlayStation). Gyro aiming, on-screen button prompts, and menu navigation.
* **Reese's Sodium Options:** Controller-friendly Sodium settings UI.

#### 🎨 Visuals & Atmosphere
*Aesthetic touches to make the Vesselam world more alive.*

* **Iris:** Adds support for Shaders.
* **Visuality:** Adds nice particle effects (block breaking particles, slime blobs, etc.).
* **Falling Leaves:** Adds falling leaf particles from trees.
* **3D-Skin-Layers:** Renders the 2nd layer of player skins (jackets, hair) in 3D.
* **NotEnoughAnimations:** Adds missing third-person animations (eating, holding maps, rowing).
* **Dynamic Crosshair:** Adapts the crosshair based on what you are looking at or holding.
* **Chat Heads:** Displays player heads next to their chat messages.
* **Capes:** Support for Optifine or other cape mods.
* **Drippy Loading Screen:** Custom server loading screen design.
* **FancyMenu:** Custom main menu design.

#### 🔊 Audio & Immersion
* **Sound Physics Remastered:** Adds realistic sound attenuation, reverberation, and absorption through blocks.
* **Presence Footsteps:** Realistic and satisfying footstep sounds based on the material you walk on.
* **Melody:** (Library for music integration).

#### ⚙️ Technical & Libraries
*Core files required for other mods to function.*
*Fabric API, Cloth Config v20, Fabric Language Kotlin, Konkrete, Packet Fixer, Placeholder API, SuperMartijn642's Config Library, libIPN, Searchables, MaLiLib (for Litematica/MiniHUD/Tweakeroo), Architectury API, TCDCommons API, Prickle, Forge Config API Port (required by Shoulder Surfing).*

#### 🖼️ Resource Packs
* **EAA_Golem:** Custom Golem (EAA) texture pack made specifically for Entegre Vesselam.
* **ESK_Golem:** Custom Golem (ESK) texture pack made specifically for Entegre Vesselam.

> **Note:** All mods except dependency libraries are optional. On first launch, packwiz-installer will ask which mods you'd like to install.

</details>
