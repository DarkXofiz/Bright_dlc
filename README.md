# Bright

Bright, Minecraft 1.20.1 Fabric icin hazirlanmis hafif bir istemci modudur.
Sadece 3 modul icerir, hepsinin kendi ayar paneli vardir:

- **Hitbox** — hedef varliklarin carpisma boyutunu XZ/Y eksenlerinde buyutur.
- **Trigger** — nisangah bir hedefin uzerindeyken otomatik vurur (gecikme,
  kalkan/yemek engelleme ayarlari ile).
- **ESP** — canli varliklarin etrafina renkli tel kafes kutu cizer; "Duvar
  Icinden Gor" acikken engellerin arkasindan da gorunur.

## Ayar ekrani

Menu herhangi bir tus ile acilmaz. ModMenu modu kuruluysa, mod listesinde
Bright'in yanindaki dislisine tiklayarak ayni ekrani acarsiniz. Ekrandaki
her modulun solundaki satira tiklamak acar/kapatir, sagdaki dislisi o
modulun ayar panelini gosterir. Ayarlar `config/bright.json` dosyasinda
otomatik olarak kaydedilir.

## Derleme

```
./gradlew build
```

Cikti `build/libs/bright-1.0.0.jar` altinda olusur.

> Not: Bu proje 1.21.x hedefli onceki bir surumden 1.20.1'e tasindi
> (Yarn/Fabric API surumleri ve bazi render cagrilari degisti — ozellikle
> `RenderSystem.setShader` ve `Tessellator` kullanimlari). Bu ortamda
> internet erisimi olmadigi icin Minecraft/Yarn kutuphaneleri indirilip
> gercek bir Gradle derlemesi yapilamadi; ilk derlemede kucuk bir mapping
> uyusmazligi cikarsa (ornegin `tessellator.draw()` yerine baska bir isim
> beklenmesi gibi) hatanin gosterdigi satiri duzeltmeniz yeterli olur.
