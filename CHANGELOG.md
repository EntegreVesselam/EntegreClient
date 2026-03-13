# Changelog

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
