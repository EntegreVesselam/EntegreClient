# Markers0.2.1 — Minecraft26.2 portu

Bu dal Java25, Fabric0.19.5 ve Minecraft26.2 içindir. Resmi oyun adları kullanılır;
Yarn/remap yoktur. Xaero26.6.0/1.47.0 için bu Minecraft sürümünün JAR'ları sabittir.
Güncel kaynak canlı0.1.3 eşleştirme daveti/sync/16 renk düzeltmelerini içerir.
`./gradlew build compileSmokeJava` sözleşme gruplarını da çalıştırır.
Çıktı `build/libs/entegre-markers-0.2.1.jar` içindeki Minecraft bağımlılığına göre
bu kanala özeldir; diğer oyun profilinin üstüne kopyalamayın.

Aşağıdaki0.1.3 ve önceki kanıtlar tarihçedir; güncel test özeti `docs/dual-profile-release.txt`.

---

# Entegre Markers — yerel hazırlık modülü

Salt okunur Fabric istemci köprüsü. Site, yayımlanmış markerların tek kaynağıdır.
0.1.3 adayı yerelde doğrulandı; yayın koordinatöre aittir. Bu doğrulamada aktif
Minecraft instance'ına kurulum yapılmadı; gerçek oyuncu waypoint'leri
okunmadı/değiştirilmedi. Windows ve gerçek multiplayer dünyasında waypoint
görünümü/kişisel kayıt korunumu bu aday için **NOT RUN**.

## Sabit sürümler ve derleme

- Minecraft **1.21.11**, Java hedefi **21**, Fabric Loader **0.18.4**.
- Xaero Minimap **26.6.0** (`e0EvhWco`), World Map **1.47.0** (`oMGDn8rs`).
- Fabric API **0.141.3+1.21.11**; köprü **0.1.3**. Mevcut Prism loader/API sürümleri korunur.
- Yarn `1.21.11+build.6`, Loom **1.14.10**, Gradle **9.2.1**.
- Wrapper dağıtımı resmi Gradle kaynağından gelir ve SHA256 ile sabitlenmiştir.
- Site projesine veya istemci paketine yeni çalışma zamanı bağımlılığı eklenmez.
  Xaero JAR'ındaki XaeroLib **1.7.3** yalnız geliştirme çalıştırıcısı için
  `.gradle/xaero-library/` içine çıkarılır. Normal Fabric kurulumunda aynı nested
  JAR zaten Xaero tarafından sağlanır; bridge JAR içine kopyalanmaz.

Java21 kurulu bir terminalde, bu dizinde:

```sh
./gradlew clean build
./gradlew contractTest
```

Çıktı: `build/libs/entegre-markers-0.1.3.jar` (remap edilmiş dağıtım JAR'ı).
`-sources.jar` oyun modu değildir. Testler JUnit yerine Java assertion çalıştırıcısı
kullanır: `test` ve `check` görevleri `contractTest`'e bağlıdır. Varsayılan Gradle
JUnit keşfinin sıfır test bulması test başarısı olarak kullanılmaz; gerçek
assertion görevi mutlaka çalışır ve hata durumunda build başarısızdır.

`./gradlew runClient` yalnız bu modülün yeni `run/` dizininde geliştirme oyunu açar;
aktif instance'ı kullanmaz. İnternet yalnız derleme/oyun bağımlılıklarının normal
indirilmesi için gerekir. Testler in-memory HTTP transport kullanır; gerçek site,
Microsoft hesabı veya canlı Minecraft sunucusu çağrılmaz.

## Oyuncu kullanımı

1. Tam desteklenen Xaero sürümleriyle Entegre sunucusuna girin.
2. Sohbetteki **[Haritayı bağla]** bağlantısına tıklayın veya `/entegremarkers`
   yazın. Davet bağlantı başına bir kez gösterilir; kendi başına ağ isteği yapmaz
   veya tarayıcı açmaz. Sabit HTTPS site onay sayfası açık oyuncu eylemiyle açılır.
   Microsoft girişi/onay yalnız sitede yapılır; mod parola veya Microsoft tokenı
   istemez. Oyun öndeyken onay en az **5 saniye** aralıkla sorgulanır; kod **10 dakika**
   sonra biter. Kod ve token oyun sohbetine/loga basılmaz.
3. Onaydan sonra ortak işaretler otomatik eşitlenir. `/entegremarkers sync` veya
   çıplak `/entegremarkers`, eşleşmiş cihazda aynı eşitlemeyi ister; mevcut
   **60 saniyelik** aralık, ön plan ve tek istek sınırını atlamaz. Eşleşme yoksa
   bağlamayı başlatır. `/entegremarkers pair` açık yeniden bağlama komutudur.
4. `/entegremarkers status` yerel durumu gösterir. `/entegremarkers forget` yerel
   yetkiyi kaldırır ve görünür ortak katmanı kapatır. Sunucudaki cihaz tokenını
   iptal etmek için sitedeki cihaz listesini kullanın; forget uzaktaki cihazı
   silmez, istekte bulunmaz.

İstek hedefi yalnız `https://entegrevesselam.com` ve sabit üç API yoludur:
`/api/map/devices/start`, `/api/map/devices/poll`, `/api/client/markers`
(başlat/onay sorgula/oku). Serbest host veya HTTP ayarı yoktur; yönlendirmeler
izlenmez. Tarayıcı onay adresi tam `/map?code=<userCode>` olarak kontrol edilir.
Minecraft hedefi oyuncunun seçtiği `entegrevesselam.com` veya
`entegrevesselam.com:25565` adresidir (büyük/küçük harf duyarsız). DNS/SRV'nin IP'ye
çözmesi bu seçilen adresi değiştirmez. Başka IP, alias, singleplayer ve başka
sunucular otomatik kabul edilmez; yeni alias gerekiyorsa ayrıca doğrulanıp kodun
allowlist'ine eklenmelidir.

Hedef sunucuda ve oyun öndeyken **60 saniyede bir**, aynı anda en fazla bir marker
isteği yapılır. Ağ/JSON işi daemon worker üzerinde, Xaero mutasyonları yalnız
Minecraft ana thread'inde yapılır. HTTP bağlantısı ve toplam istek sınırı **10 saniye**,
streaming cevap sınırı **256 KiB**, snapshot sınırı **1000** kayıttır. Açıklamalar
sadece doğrulanır; oyunda ad, konum, renk ve sabit ikon simgesi gösterilir. Renkler
Xaero'nun 0–15 palet indeksidir; sitede seçilen indeks koyu kırmızı dahil aynen
korunur. İkonlar poi/home/shop/farm/portal'dır.

## Yetki ve kişisel veri sınırı

Cihaz yetkisi `config/entegre-markers/device.json` içinde tutulur; yalnız scoped
cihaz tokenı, en fazla30gün expiry ve rastgele origin UUID içerir. POSIX'te klasör
0700/dosya0600, atomik dosya değiştirme kullanılır; symlink hedefleri reddedilir.
Windows'ta kullanıcı profilinin miras alınan ACL'leri geçerlidir; ACL/oyun kabulü
Windows testinde doğrulanmalıdır. Dosyayı paylaşmayın. `.gitignore` ve
`.packwizignore` bütün `config/entegre-markers/` dizinini dışlar. Eşleştirme kodları,
marker listesi veya hesap parolası dosyaya kaydedilmez.

Xaero26.6.0 gerçek API'si: `BuiltInHudModules.MINIMAP.getCurrentSession()` →
`getWorldManager().getAutoWorld()` → dünya container'ının
`getThirdPartyWaypointManager()` → rastgele origin ID altında
`ThirdPartyWaypoints`. Bu API, tasarımdaki ayrı yönetilen set amacını **runtime
üçüncü taraf katmanıyla** karşılar. Kişisel `WaypointSet`, saveWorld, waypoint
metin dosyaları ve dizin silme API'leri kullanılmaz. Görünen ada veya marker adına
bakarak sahiplik kurulmaz. Daha önce var olan origin ID veya beklenmeyen runtime
nesne içeriği görülürse katman kapatılır; kişisel kaydın üstüne yazılmaz.

Katman yalnız doğru oturum, dimension ve subworld hazırken görünür. Kullanıcının
haritada başka dünyayı seçmesi ortak veriyi yanlış dünyada göstermez. Xaero'nun
üçüncü taraf waypoint görünürlüğü varsayılan set veya “tüm setler” görünümünü
izler; kişisel seçili seti mod değiştirmez. Her yeni bağlam aynı global revision'ı
ayrıca uygular; eski epoch yanıtı ve gerileyen revision atılır. Tam geçerli boş
snapshot ortak katmanı temizler. Bozuk/offline cevap son geçerli katmanı korur;
401/403 katmanı gizler. Yönetilen waypoint'ler diske yazılmadığından başlangıçta
API yeniden yetki vermeden eski koordinatlar görünmez. Xaero eksik/uyumsuzsa
sadece durum mesajı olur; bridge normal oyun yüklenmesini durdurmaz.

## Paketleme ve doğrulama durumu

`marker-sync/` kaynak/build/dev-run dizininin tümü packwiz dışında; `xaero/`
oyuncu verisi zaten dağıtım dışında tutulur. Derleme köprüyü aktif instance'a
kopyalamaz. Yerel 2.1.5 paket adayı yalnız remap edilmiş
`mods/entegre-markers-0.1.3.jar` dosyasını içerir. Kaynaklar, testler ve test
çalıştırma dizinleri dağıtımda yoktur. Yayın koordinatöre aittir. Mevcut Xaero metadata
kesin sürümleriyle zaten eşleştiği için bu görev eski metadata'yı değiştirmedi.

15 Ekim2026 yerel kanıtı:

- Önce durum testi `AssertionError: first request starts` ile beklenen RED.
- Ardından state, parser, HTTP transport/body, dosya, scheduler ve ownership assertion'ları PASS.
- Bozuk JSON kontrolü `AssertionError: unquoted key invalid JSON` RED → strict JSON
  düzeltmesiyle GREEN. Poll kontrolü `no polling below 5 seconds` RED → GREEN.
- Ownership kontrolü `preexisting personal layer must never be claimed` RED →
  exact object identity kontrolüyle GREEN; isim/ID eşitliği kişisel kaydı sahiplenmez.
- Java21 gerçek MC/Fabric/Xaero classpath'ine karşı clean build/remap PASS.
- macOS arm64 + Temurin21.0.12.1, girişsiz dev açılışı: modlar yüklendi, LWJGL,
  OpenAL, kaynak atlasları ve XaeroStage2 tamamlandı. İlk dev açılışı nested
  XaeroLib eksikliğiyle FAIL olmuştu; yukarıdaki dev-only extraction düzeltildi.
  Dev refmap ve macOS shader uyarıları görüldü; bridge runtime exception'ı yoktu.
  Oyun hesabı, dünya veya hedef sunucu bağlantısı kullanılmadı; oluşturulan dev
  süreç kapatıldı. Bu yükleme denemesi gerçek waypoint mutasyonu kanıtı değildir.
- Windows açılışı, çoklu dimension/subworld oyun içi görünüm, kişisel kayıt canonical
  hash karşılaştırması ve temiz import/güncelleme/kaldırma **BLOCKED/NOT RUN**.
  D05–D12/G3 bu kısmi kanıttan tam PASS sayılamaz.

Canlıya geçmeden önce ayrı geçici instance'ta sentetik kişisel waypoint/set
fixture'larıyla önce/sonra hash, boş snapshot, 401, dünyalar arası geçiş ve bridge
kaldırma kontrolleri Windows ve geliştirme işletim sisteminde tamamlanmalıdır.


## 0.1.1 dar güncelleme — 16 Ekim2026

0.1.1 yalnız minimum Fabric Loader/API bağımlılıklarını mevcut Prism kurulumunun
**0.18.4 / 0.141.3** seviyesine indirir. Minecraft1.21.11 ve Xaero26.6.0/1.47.0
API bağlantısı aynıdır. Eski Xaero25.3.10 desteklenmez: gerekli runtime third-party
katman API'si o sürümde yoktur. Diğer modları veya launcher profilini bu modül
kendi başına değiştirmez. Sunucuya ek Paper/Fabric plugini gerekmez; yayımlanmış
markerlar sitenin ayrı cihaz API'sinden okunur. Gerçek site API kullanılabilirliği
ve cihaz yetkisi oyun içi eşleştirmede doğrulanır; bu modülün testleri canlı API'ye
istek yapmaz.

Prism'deki mevcut packwiz-installer **0.5.14**, `pack.toml` ile `mmc-pack.json`
sürüm farkı varsa **Update** onayıyla loader metadata'sını güncelleyebilir; elle
loader seçmek zorunlu değildir. **Continue anyways** güncellemeyi atlar.
[Resmi installer kaynağı](https://github.com/packwiz/packwiz-installer/blob/v0.5.14/src/main/kotlin/link/infra/packwiz/installer/LauncherUtils.kt).
Prism9.4 metadata'yı prelaunch'tan önce yüklediği için loader değişiminde aynı
ilk açılışın yeni loader'ı kullandığı varsayılmaz; bu dar dağıtım mevcut loader'ı
koruyarak o geçişi gerektirmez.

Geliştirme doğrulaması:

```sh
./gradlew clean build
./gradlew runSmoke
```

`src/smoke/` ve `runSmoke`, yalnız geliştirme için ayrı kaynak kümesidir. Gerçek
oyun `CLIENT_STARTED` olayından sonra Fabric'in gerçek komut kayıt eventini yeni
Brigadier dispatcher üzerinde çalıştırır; `entegremarkers` ve `pair/status/forget`
düğümlerinin çalıştırılabilir callback'leri bulunduğunu doğrular. 0.1.1 testinde hiçbir komut
çalıştırılmaz; hesap, dünya, sunucu, cihaz tokenı veya site HTTP çağrısı gerekmez.
0.1.3 native testinin genişletilmiş kapsamı aşağıdadır.
Smoke modunun sınıfları/metadata'sı dağıtım ve sources JAR'larına eklenmez.
Böylece girişsiz açılış ve komut callback kaydı ayrı kanıtlanır; canlı sunucunun
sohbet ekranında komut çalıştırma ve gerçek waypoint kabulü ayrıca doğrulanmalıdır.


## 0.1.2 — isim baş harfleri, renk ve yalnız tam harita

Ortak marker simgesi artık kategoriye göre sabit E/H/F vb. değildir: isimdeki
kelimelerin baş harfleri, tek kelimede ilk harfler kullanılır; en fazla **3 UTF-16 birimi** (surrogate çifti bölünmeden), NFC normalizasyonu ve locale-independent uppercase uygulanır.
Türkçe harfler ve supplementary karakterler kesik surrogate üretmeden korunur.
Örnek: `Çınar Kalesi → ÇK`, `Şehir → ŞEH`. Noktalama/emoji harf sayılmaz; harf/rakam
bulunamazsa `?` kullanılır. Üç birim sınırı bridge'in kısa etiket tercihidir; supplementary harflerde daha az harf çıkabilir;
Xaero'nun gerçek String/getInitials API'sine sentetik native getter testi uygulanır.

**Tarihsel 0.1.2 davranışı; 0.1.3 ile kaldırıldı:**
Site API/UI varsayılan rengi olan **4 (dark red)**, marker ID'sinden deterministik
üretilen açık renk paleti `{6,9,10,11,12,13,14,15}` ile gösterilir. ID değişmedikçe
renk isim düzenlemesinde veya yeniden bağlantıda değişmez. 4 dışındaki bütün açık
API renk değerleri korunur. API varsayılan4 ile özellikle seçilmiş4 arasında bayrak
vermediğinden, özellikle seçilmiş dark red de otomatik palete dönüşür; diğer
renkler aynı kalır. Site kaydı bu yerel gösterim tercihiyle değiştirilmez.

Ortak waypoint oluşturulduktan sonra gerçek Xaero26.6.0 API'sinde
`setVisibility(WaypointVisibilityType.WORLD_MAP_LOCAL)` uygulanır. Bu seçim
**yalnız tam dünya haritasında** gösterim içindir: oyun dünyasındaki HUD işaretleri
ve **minimap işaretleri görünmez**. Xaero'nun hazır enum seçeneği HUD'ı kapatırken
minimap'i açık tutmaz; özel renderer mixin'i eklenmez. `disabled=true` kullanılmaz;
tam haritada gösterilebilmesi için waypoint enabled kalır. Bu ayar yalnız yeni
bridge waypoint nesnesine yapılır; kişisel waypoint, set ve genel Xaero ayarına
yazılmaz. Yetki, origin sahipliği, epoch ve dünya/subworld kontrolleri aynıdır.

Regression kanıtı: bağımsız isim testi önce `use name initials instead of category E`
ile RED, gerçek dev client testi önce `native managed marker must hide from world HUD`
ile RED. 0.1.2 düzeltmesinde native getter/filter smoke; isim, renk, görünürlük,
enabled durumu ve kişisel yeni waypoint'in varsayılan HUD davranışının korunmasını
doğrular. Aynı ad/koordinatlı sentetik kişisel waypoint'e ayar uygulanmaz.
Gerçek multiplayer harita pikseli/oyun içi görünüm ve Windows testi ayrıca
**NOT RUN** kalır; native getter/filter kanıtı bu gözlem yerine geçmez.

Kullanıcının Xaero GUI'sinden kaydedebildiği render override'larının sonraki açılışta
önceden boş origin yaratabilmesi, mevcut exact-object ownership kontrolü için ayrı
inceleme konusudur. Bu görünüm düzeltmesi o sahiplik sınırını gevşetmez ve kişisel
ayar dosyalarını değiştirmez.


## 0.1.3 — site renkleri ve tek adımda bağlama/eşitleme

Renk için yerel ID paleti kaldırıldı; API'nin doğruladığı 0–15 değeri aynen
Xaero waypoint'ine geçirilir. İsim baş harfleri ve yalnız tam harita görünürlüğü
korunur. Eşleşmemiş oyuncuya uygun hedef bağlamında, oyun öndeyken bağlantı
başına bir kez yerel `run_command /entegremarkers pair` daveti gösterilir.
Boyut değişimi veya dünya yüklenmesi daveti tekrarlamaz. Çıplak komut ve `sync`,
eşleşmeye göre mevcut pair/snapshot akışını seçer; ayrıca join isteği oluşturmaz.

18 Ekim 2026 doğrulaması (`verification-0.1.3.json`):

- RED: önce `site-selected palette index 4 must survive client rendering`,
  native istemcide `missing subcommand sync` assertion'ları başarısızdı.
- GREEN: `clean build runSmoke`, dokuz contract grubu ve gerçek MC1.21.11 /
  Fabric0.18.4 / Xaero26.6.0+1.47.0 native çalıştırması geçti.
- Gerçek Xaero nesnelerinde 16 rengin tamamı, Unicode baş harfleri, konumlar,
  tam harita görünürlük filtreleri ve sentetik kişisel waypoint varsayılanları geçti.
- Gerçek Fabric komut event'i/Brigadier callback'leri çalıştırıldı: tıklanabilir
  pair, eşleşmemiş çıplak komut, eşleşmiş sync, status ve forget. Hedef dışı,
  arka plan, bekleyen pair, tek istek ve 60 saniyelik aralık korumaları geçti.
- HTTP sınırı test içi bellek taşıyıcısıyla değiştirildi. Test, sentetik bağlam
  ve geçici cihaz değeri kullandı; tarayıcı açılmadı, gerçek siteye marker/pair
  isteği, hesap, dünya veya sunucu bağlantısı olmadı. Oyun normal kapandı.
- Bu kanıt native API ve komut kabulüdür; gerçek multiplayer sohbetinde fare
  tıklaması, Windows, OAuth onayı ve dünya haritasındaki piksel gözlemi değildir.
  Mevcut shader/dev-refmap ve Gradle deprecation uyarıları sonuçları engellemedi.
- Önceki anı galerisi/native menü kanıtı 2.1.4 adayında alındı; bu son adım
  yalnız menü sürüm yazısını 2.1.5 yaptı. Fotoğraf/menü/updater kabulü tekrarlanmadı.
