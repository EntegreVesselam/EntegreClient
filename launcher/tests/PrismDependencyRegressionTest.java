package com.entegre.launcher;

import com.google.gson.*;
import java.io.StringReader;
import java.nio.file.*;
import java.util.Comparator;

/** Prism's published Fabric metadata requires intermediary for both unobfuscated versions.
 * https://meta.prismlauncher.org/v1/net.fabricmc.fabric-loader/0.19.5.json
 * https://meta.prismlauncher.org/v1/net.fabricmc.intermediary/26.2.json (and 26.3)
 */
public final class PrismDependencyRegressionTest {
    public static void main(String[] args) throws Exception {
        Path instance=Files.createTempDirectory("entegre-prism-dependency-");
        try {
            Path game=Files.createDirectory(instance.resolve("minecraft"));
            Files.writeString(instance.resolve("instance.cfg"),"[General]\nname=Player choice\n");
            Path profile=instance.resolve("mmc-pack.json");
            for(String target:new String[]{"26.2","26.3"}) {
                String lwjgl=target.equals("26.2")?"3.4.1":"3.4.3";
                String resolved="{\"formatVersion\":1,\"components\":[{\"uid\":\"net.minecraft\",\"version\":\""+target+"\",\"cachedVersion\":\""+target+"\"},{\"uid\":\"org.lwjgl3\",\"version\":\""+lwjgl+"\"},{\"uid\":\"net.fabricmc.fabric-loader\",\"version\":\"0.19.5\",\"cachedRequires\":[{\"uid\":\"net.fabricmc.intermediary\"}]},{\"uid\":\"net.fabricmc.intermediary\",\"version\":\""+target+"\",\"cachedRequires\":[{\"uid\":\"net.minecraft\",\"equals\":\""+target+"\"}]}]}";
                Files.writeString(profile,resolved);
                if(ProfileMigration.prepare(game,target)) throw new AssertionError("LOOP: Prism-resolved "+target+" profile was changed again");
                if(!Files.readString(profile).equals(resolved)) throw new AssertionError("Stable profile rewritten");
                // A broken 2.4.1 starter/profile omitted intermediary; repair must add it once.
                JsonObject data=JsonParser.parseReader(new StringReader(resolved)).getAsJsonObject();
                JsonArray filtered=new JsonArray();for(Object raw:data.get("components").getAsJsonArray()) {
                    JsonObject c=((JsonElement)raw).getAsJsonObject();if(!c.get("uid").getAsString().equals("net.fabricmc.intermediary")) filtered.add(c);
                }
                data.add("components",filtered);Files.writeString(profile,new Gson().toJson(data));
                if(!ProfileMigration.prepare(game,target)) throw new AssertionError("Missing required component not repaired");
                if(ProfileMigration.prepare(game,target)) throw new AssertionError("Repaired profile not idempotent");
                String text=Files.readString(profile);
                if(!text.contains("net.fabricmc.intermediary")) throw new AssertionError("Fabric dependency missing");
                System.out.println("PASS Prism metadata boundary: "+target+" resolved profile unchanged; missing intermediary repaired once");
            }
        } finally {try(var paths=Files.walk(instance)) {for(Path p:paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);}}
    }
}
