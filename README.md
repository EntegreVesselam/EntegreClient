# Entegre Client 2.4.4 — Minecraft 26.3

Varsayılan tek kurulum paketi artık Minecraft **26.3** kullanır.
Java **25**, Fabric **0.19.5**, LWJGL **3.4.3** ve Intermediary **26.3** hazırdır.

[EntegreClient-v2.4.4.zip indir](https://github.com/EntegreVesselam/EntegreClient/releases/download/v2.4.4/EntegreClient-v2.4.4.zip)
ve Prism’de **Kurulum Ekle / Add Instance → İçe Aktar / Import** ile açın.
Profil adı **Entegre Client**. ZIP’i eski profilin üstüne çıkarmayın;
eski profili harita, ayar ve kayıtlı dünyalarıyla saklayın. Yeni profilin ilk
açılışında modlar indirilir; sonraki güncellemeler minecraft-26.3 kanalından gelir.
Mevcut açıkça minecraft-26.2 kanalını kullanan profiller 26.2’de kalır;
26.3’e geçmek için güncel tek ZIP’i ayrı profil olarak içe aktarın.

**Voxy:** Kullanıcının ilettiği 0.2.20-beta-23b3486 JAR’ı içerilir.
Voxy Extra ve Voxy WorldGen için 26.3 uyumlu yayın bulunmadığından kapalıdır.
26.2 modlarını 26.3’e zorla yükleyen sürüm geçersiz kılmaları kullanılmaz.
Gerçek GPU LOD çizimi ve canlı sunucu LOD akışı doğrulanmadı.

Varsayılan sunucu hem ad hem gerçek IP alanında **entegrevesselam.com**.
**entegrevesselam.com** ve **ugurdindar.com**, büyük harf ve açık **:25565**
biçimleriyle marker bağlama/eşitleme için kabul edilir. API asıl alan adında kalır.
Yeni ZIP doğru fabrika sunucu kaydını içerir; mevcut kişisel sunucu listeleri
preserve=true ile korunur. Harita/LOD/ayar dosyaları paketlenmez veya taşınmaz.

Eski main adresini kullanan güncel motorun varsayılan geçiş hedefi artık26.3’tür.
main, eski motorlara uyumlu teslim metadata’sını korur; güncel oyun/mod paketi
minecraft-26.3 kanalındadır. Önceki Prism Intermediary ve sürüm cache düzeltmeleri,
haber/etkinlik menüsü, Friends düzeltmesi ve186 kalıcı koruma kaydı korunur.
Windows’ta gerçek Prism kullanıcı kabulü henüz yapılmadı.
