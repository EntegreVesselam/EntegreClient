# Yayın öncesi yerel doğrulama — 18 Ekim 2026

Sürüm: güncelleyici0.2.0-local / geçişmodu0.1.0 / Entegre Client2.1.3.
Kullanıcı bu kontrollerin ardından 2.1.3 yayınını açıkça onayladı. Güncel
yayın durumu: [GitHub v2.1.3](https://github.com/EntegreVesselam/EntegreClient/releases/tag/v2.1.3).
Ortam: macOS ARM64, APFS, Azul Java25. Kaynaklar Java17 hedefiyle derlendi.
Gerçek oyuncu instance'ı, Minecraft, hesap girişi veya sunucu başlatılmadı.
Bütün çalışma dizinleri sentetik; indirmeler yalnız localhost üzerinden.

## Koordinatör tarafından çalıştırılan kontroller

- `python3 build.py`: üç JAR derlendi. Pinned upstream SHA256 doğru;
  7 upstream girdi değişti (manifest, ana pencere ve beş sınıfın arayüz
  sabitleri), kalan 3704 girdi byte olarak aynı. Tekrarlı arşiv girdisi yok;
  mevcut upstream lisansları korunuyor.
- Çeviri: 6 Python testi. 52 sabit Türkçe; eksik/beklenmeyen sabit, yanlış sınıf,
  bozuk/truncated sınıf, long/double ve modified UTF-8 davranışı sınandı.
  Yöntem bytecode'u ve metadata aynı; arayüz dışı girdiler değişmedi.
- `LauncherTest`: varsayılan GUI/timeout0, ayarlar, önizleme ve12 geçersiz argüman.
- `WindowTest --text`: 21 durum metni; `WindowTest`: gerçek Swing düğmeleri,
  ilerleme, mod ayarları, iptal ve arayüz iş parçacığı davranışı.
- `tests/integration.py`: 11 gerçek installer çalıştırması. İlk kurulum ve
  varsayılan kapalı mod; tekrar; yönetilen dosya güncellemesi; preserve/unmanaged
  byte korunumu; mod seçimini aç/kapat ve tekrar koruma; seçenek iptali; ana
  iptal/kapat; bozuk index/indirme hash'i. Türkçe seçim/hata başlıkları ve düğme
  eylemleri doğrulandı. Güncel sentetik küçük paket1,30sn; Devam tıklaması yok.
- `BootstrapTest`: gerçek childexit7 ve shim mainexit1/7; argv/cwd korunumu;
  çalışan özel kopyanın yönetilen motor değiştirilirken aynı kalması ve temizlenmesi;
  eksik/bozuk/truncated/büyük motor, checksum biçimi ve symlink reddi. Yalnız
  bilinen eski SHA'lı backup doğrulama hatasında kullanılır; child hatası fallback
  başlatmaz. Fallback testi resmî bootstrap `--help` ile offline yapıldı.
- `TransitionTest`: iki komutun altı doğrudan/QSettings biçimi; old→shim, tam yedek,
  tekrar/no-op; kişisel dosya/instance.cfg korunumu; bilinmeyen/eksik bootstrap,
  özel/global komut ve disablemarker; payload hash/eksik/truncation; bütün
  ilgili symlinkler; yedek çakışması; gerçek yazma/atomik hedef hatası; engel
  kaldırıldıktan sonra başarılı tekrar. Yedek son adına kısmen yazılmaz:
  temp+force+SHA ardından ezmeyen atomik hardlink ile tamamı görünür olur.
- `tests/auto_update.py`: 8 Java çağrısında gerçek eski bootstrap ve pinli yerel
  installercache → payloadindirme → geçiş çekirdeği → aynı bootstrapadıyla
  gerçek yeni GUI → kendi yönetilen motorunu güncelleme → sonraki açılış →
  tekrar/no-op → devre dışı bırakıp doğrulanmış yedekten gerçek geri alma.
  Kişisel ayarlar, Prism komutu ve tombstone sahipliği korundu. İlk yeni shim
  testinde güncel sentetik paket1,23sn. Fabric adapter gerçek oyun içinde çalıştırılmadı.
- Son uyumluluk kontrolünde public v2.0 ZIP'in komutunun ilk dar kontrolle
  eşleşmediği bulundu. ZIP'teki gerçek kaçışlı komut ve minecraft dizini teste
  eklendi; eski kodda başarısız, düzeltmede başarılı oldu. Yerel profil alanı
  aynı komutla eşleşti (salt okunur, diğer içerik alınmadı). GitHub URL alias'ı
  ve canonical raw URL'nin mevcut pack.toml içerikleri birebir aynı doğrulandı.
  Son eski→yeni→motor güncelleme→geri alma testi gerçek dağıtımın cfg değeriyle
  yeniden geçti; test indirme URL'si yalnız localhost'tur.
- `RenderPreview`: gerçek Swing bileşenleri `dist/preview.png` dosyasına çizildi
  ve görsel incelendi; kullanıcı ekranı yakalanmadı.

Bu süreler küçük yerel sahte paket içindir; gerçek internet/mod paketi açılış
performansı ölçülmedi. Ağ ve Minecraft yüklemesi devam eder.

## Client dağıtım adayı

İzole kaynak: `../EntegreClient-updater-release`, taban156c94b7ba55c03ec18a6df7f578e4f8912bb2d8,
yerel dal `codex/entegre-updater-auto-update`. Aşağıdaki testler yayın öncesi
yerel kanıttır; gerçek arkadaş kurulumlarından telemetri alınmadı.

- Koruyucu refresh ve `verify-pack.py`:354 kayıt;186 kalıcı emekli URI doğru.
- Önce/sonra karşılaştırması:349 mevcut kayıt birebir aynı, yalnız beş yeni
  managedasset; Minecraft1.21.11/Fabric0.18.4, marker0.1.2 ve diğer modlar aynı.
- `check-updater.py`: çift SHA256, JAR giriş noktaları, zorunlu clientmodu,
  kaynak/yedek dışlaması, kökteki orijinalbootstrap korunumu geçti.
- Beş yeni payload Gitignore dışında; `.gitignore` ve `.packwizignore` kaynak/
  runtime yedekleri dışlıyor. CI'a offline payload kontrolü eklendi; uzaktaki
  CI çalıştırılmadı.
- `check-pack-compat.py`: kayıt silme/değiştirme reddi, kişisel dosyayı okumama,
  çakışma, gerçek guardedrefresh/tekrar, güvensizrefresh tespiti ve kurtarma geçti.
- `check-packwiz-migration.py` resmî0.5.14 ile: eski→yeni, tekrar, sürüm atlama,
  temiz kurulum, duplicate sahiplik ve sentetik negatif silme kontrolleri geçti.
- Bağımsız salt okunur inceleme yedek kesintisi riskini buldu; atomik yayınla
  giderildi, yeni regresyonlar geçti. Kaynak ve dağıtım ayrı incelendi; beş
  asset ve kaynak eşitliği ile dışlamalar doğrulandı.

## Açık kabul sınırları

- Gerçek Windows/NTFS, Linux, Prism ve Fabric/Minecraft başlangıcı denenmedi.
  Java17 bytecode derlendi; test çalışma zamanıJava25 idi. Hardlink/atomikmove
  desteklemeyen dosya sisteminde geçiş eski başlatıcıyı korur. Fiziksel güç
  kesintisi testi yapılmadı; yazma ve yayın başarısızlığı ayrı sınandı.
- Otomatik geçiş yalnız bilinen orijinalbootstrap ve belgelenmiş instancekomutu
  içindir. Farklı/global/ekargümanlı komutlar değiştirilmez. İlk eski ekran
  bir kez kalır; oyun başlayınca ikinci açılış için geçiş yapılır.
- Ana/modseçimi/hata UI Türkçe; dış mod açıklamaları ve teknik stacktrace özgün
  dildedir. İngilizce seçimi sonraki iş. Pinli motorun upstream davranışı
  sürer; hata penceresinde açıkça güncellemesiz devam seçeneği mevcuttur.
- Gelecek motor sürümleri aynı kanal üzerinden gelir; sabit bootstrap'ın
  kendisinin sonraki sürümüne geçiş bu uygulamanın kapsamı değildir.
- Bu belge yerel test kapsamını kaydeder. Yayın yetkisi alındı; gerçek
  Windows/Prism oyun kabulü henüz yapılmadı. Yayın, arkadaşların oyunu açtığı
  veya güncellemeyi tamamladığı anlamına gelmez.

## Teslim dosyaları SHA256

- `entegre-updater.jar`: `03be01fa3dc055156dee55b955cce4a6455744de89800e3bd31a8ec8a2d3e154`
- `entegre-bootstrap.jar`: `20c0c2a94e351f64f0978235b85ebb8bd5ed8938662a8b021015bba9d24785ca`
- `entegre-updater-transition-0.1.0.jar`: `8ce65189148fb6a93b3a0736b324c72d65cfb8a93bf9f0578a492dc9b712333a`
