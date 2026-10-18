# 2.4.1 — iki Prism profili / Minecraft26.3 (2026-10-18)

- Ayrı26.2 ve26.3 profil/Packwiz kanalı; Java25/Fabric0.19.5.
- Kullanıcının Voxy0.2.20-beta / 23b3486 test JAR'ını içerir; Extra ve WorldGen kapalıdır.
- Canlı2.1.5 topluluk menüsü ve marker0.1.3 davranışları korunur; Markers0.2.1 portu.
- Paket/hash/koruma/updater/starter kontrolleri; macOS açılışı. Gerçek GPU LOD/server kabulü henüz yok.

---

## 2.1.5 — Anılar ve harita eşitlemesi

- Ana menüde altı topluluk fotoğrafı, 27 saniyelik aralıklarla rastgele ve yumuşak geçişlerle gösterilir.
- Yükleme ekranı her açılışta tek bir rastgele fotoğraf seçer; fotoğrafların oranları korunur.
- Harita renkleri sitedeki seçimle birebir eşleşir; koyu kırmızı dahil 16 renk korunur.
- Eşleşmemiş oyuncuya sunucu bağlantısı başına bir kez tıklanabilir “Haritayı bağla” daveti gösterilir.
- `/entegremarkers` ve `/entegremarkers sync`, eşleşme yoksa bağlamayı başlatır; eşleşmişse mevcut 60 saniyelik istek aralığıyla eşitler.
- Entegre Markers 0.1.3; kişisel waypoint'ler, diğer mod sürümleri ve güncelleyici korunur.

## 2.1.4 — Topluluk menüsü

- Yükleme ekranı ve ana menü, Eylül topluluk fotoğraflarıyla yenilendi.
- Ana menüye son üç site duyurusu ve tıklanabilir başlıkları eklendi.
- Pano, Galeri, Harita ve Tüm duyurular bağlantıları eklendi.
- Sekiz kısa replik, 12 saniyelik dengeli bir havuzdan seçilir.
- Türkçe telif satırı taşması düzeltildi; sürüm etiketi ayrı satıra alındı.
- Minecraft, Fabric, modlar ve güncelleyici sürümleri değişmedi.

## 2.1.3 — Türkçe güncelleyici

- Entegre tasarımlı Türkçe açılış penceresi; normal açılışta seçenek sayacı yok.
- Mod seçimi ve hata pencerelerinin metinleri Türkçe; kayıtlı seçenekler korunur.
- Bilinen mevcut Prism kurulumuna yedekli otomatik geçiş; komut aynı kalır.
- Güncelleyici motorunun sonraki sürümleri aynı paket kanalıyla gelir.
- Yeni Prism başlangıç ZIP’i Türkçe pencereyi ilk açılışta kullanır; modlar açılışta indirilir.
- Yerel GUI, geçiş, kendi motorunu güncelleme ve geri alma testleri var.
  Gerçek Windows/Prism oturumuyla kabul henüz yapılmadı.

## 2.1.2

- Ortak markerlar E yerine isimlerinden kısa harflerle, yalnız tam ekran dünya haritasında görünür.
- Varsayılan marker renkleri sabit bir paletle çeşitlenir; özel renkler korunur.
- Kişisel waypoint ve genel Xaero ayarları değişmez.

## 2.1.1 — packwiz marker eşitlemesi

- Entegre Markers0.1.1 mevcut Prism/Fabric0.18.4 kurulumuna otomatik dağıtılır.
- Xaero Minimap26.6.0 ve WorldMap1.47.0 eşitleme API uyumu için birlikte güncellenir.
- Eski belge hash hataları indirme yapmayan kalıcı uyumluluk kayıtlarıyla giderilir.
- Eski oyuncu/cache dosyaları aktif dağıtımdan çıkarılır; var olan yerel dosyalar
  silinmesin diye eski sahiplik URI'leri korunur. Temiz kurulum bunları indirmez.
- Diğer mevcut mod sürümleri ve loader korunur; kullanıcı eşleştirmesi siteye özeldir.

# Changelog

## Unreleased

### New Mods
- **Shoulder Surfing Reloaded** - Over-the-shoulder third-person camera perspective for improved aiming

### Updated
- **Voxy Server Side** updated to v0.2.1; now installed client-side as well (side changed from server to both)
- **Forge Config API Port** updated to correct 1.21.11 build (v21.11.1)
- **Shoulder Surfing Reloaded** updated to correct 1.21.11 build

### Removed
- **Freecam** - removed from the pack

### Dependencies Added
- Forge Config API Port (required by Shoulder Surfing Reloaded)

## v2.1.0

### New Mods — Building & Schematics
- **Litematica** - Schematic loading with holographic build guides
- **Tweakeroo** - Accurate block placement, free camera, building tweaks
- **MiniHUD** - Light levels, spawn spheres, structure bounding boxes overlay
- **Syncmatica** - Share schematics with other players on the server
- **Continuity** - Connected textures (glass, bookshelves, etc.)

### New Mods — QoL & Information
- **Enchantment Descriptions** - Shows what enchantments do on books
- **Better Statistics Screen** - Improved statistics UI
- **Fabrishot** - Take high-resolution screenshots
- **BadOptimizations** - Non-rendering game logic optimizations

### Dependencies Added
- MaLiLib (required by Litematica, MiniHUD, Tweakeroo)
- Architectury API (required by Better Statistics Screen)
- TCDCommons API (required by Better Statistics Screen)
- Prickle (required by Enchantment Descriptions)

## v2.0.0

### New Mods
- **Controlify** - Full controller support (Steam Deck, Xbox, PlayStation)
- **Reese's Sodium Options** - Controller-friendly Sodium settings UI
- **Debugify** - Fixes dozens of vanilla Minecraft bugs
- **BetterF3** - Cleaner, more readable debug screen
- **Controlling** - Searchable keybindings menu
- **Bobby** - Extends render distance beyond server limits via client-side caching
- **Better Mount HUD** - Improved HUD when riding
- **Clumps** - Groups XP orbs to reduce lag
- **Better Block Entities** - Faster rendering of block entities
- **Sodium Extra** - Additional Sodium toggle options
- **Dynamic FPS** - Reduces FPS in background (battery saver)
- **Resourcify** - Browse/install resource packs and shaders in-game
- **Smooth Swapping** - Smooth item move animations
- **ThreadTweak** - Optimizes thread priority for limited-core devices
- **Falling Leaves** - Falling leaf particles from trees

### New Resource Pack
- **Crystal Vanilla Tweaks** - Clean vanilla-style texture improvements

### Changes
- All non-dependency mods are now optional (users choose on first launch)
- Updated all mods to latest versions
- Optimized default settings for Steam Deck (60 FPS cap, render distance 10, clouds disabled)
- Lowered Sodium CPU render-ahead limit for better controller input latency
- Improved `.gitignore` (excludes Xaero map data, backups, DS_Store, shader binaries)
- Updated README with new mod descriptions and Steam Deck section
- Removed redundant `readme.me`
- Added LICENSE (MIT)

### Dependencies Added
- Searchables (required by Controlling)

## v1.0.0

### Initial Release
- Base modpack with 30+ mods for Entegre Vesselam server
- Performance: Sodium, Lithium, FerriteCore, EntityCulling, More Culling, ImmediatelyFast, Krypton, ModernFix
- QoL: Inventory Profiles Next, Mouse Tweaks, AppleSkin, Jade, Xaero's Maps, Shulker Box Tooltip
- Visuals: Iris Shaders, Visuality, 3D Skin Layers, NotEnoughAnimations, Chat Heads, Capes
- Audio: Sound Physics Remastered, Presence Footsteps
- Custom FancyMenu title screen and Drippy Loading Screen
- Complementary Reimagined shader pack included
- packwiz-installer-bootstrap for automatic mod syncing
