package com.entegrevesselam.markers;
import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
record Snapshot(long revision, List<Marker> markers) {
    static final int MAX_BYTES = 256 * 1024;
    record Marker(String id, String name, String description, String mapId, String dimension,
                  int x, int y, int z, int color, String icon, int version) {}
    private static final Map<String,String> WORLDS = Map.of("world", "minecraft:overworld", "world_nether", "minecraft:the_nether", "world_the_end", "minecraft:the_end");
    static Snapshot parse(byte[] bytes) {
        if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("snapshot_size");
        // REPORT rejects damaged UTF-8 rather than displaying replacement characters.
        final String json;
        try { json = StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString(); }
        catch (java.nio.charset.CharacterCodingException e) { throw new IllegalArgumentException("snapshot_utf8"); }
        JsonObject root = strictJson(json);
        if (number(root,"schemaVersion",1,1) != 1) throw new IllegalArgumentException("schema");
        long revision = number(root,"revision",0,9_007_199_254_740_991L);
        Instant.parse(string(root,"updatedAt",1,64));
        JsonArray list = root.getAsJsonArray("markers");
        if (list == null || list.size() > 1000) throw new IllegalArgumentException("marker_count");
        List<Marker> result = new ArrayList<>(); Set<String> ids = new HashSet<>();
        for (JsonElement value : list) {
            JsonObject m = value.getAsJsonObject();
            String id = string(m,"id",1,128), name = string(m,"name",1,80), description = string(m,"description",0,500);
            if (!id.matches("[A-Za-z0-9_-]+") || !ids.add(id)) throw new IllegalArgumentException("marker_id");
            String map = string(m,"mapId",1,64), dimension = string(m,"dimension",1,64);
            if (!dimension.equals(WORLDS.get(map))) throw new IllegalArgumentException("marker_world");
            int x = (int)number(m,"x",-29999984,29999984), z = (int)number(m,"z",-29999984,29999984);
            boolean overworld = dimension.equals("minecraft:overworld");
            int y = (int)number(m,"y",overworld ? -64 : 0,overworld ? 319 : 255);
            int color = (int)number(m,"color",0,15), version = (int)number(m,"version",1,Integer.MAX_VALUE);
            String icon = string(m,"icon",1,16);
            if (!Set.of("poi","home","shop","farm","portal").contains(icon)) throw new IllegalArgumentException("marker_icon");
            result.add(new Marker(id,name,description,map,dimension,x,y,z,color,icon,version));
        }
        return new Snapshot(revision,List.copyOf(result));
    }
    static JsonObject strictJson(String json) {
        com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new java.io.StringReader(json));
        reader.setStrictness(Strictness.STRICT);
        JsonObject result = JsonParser.parseReader(reader).getAsJsonObject();
        try { if (reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) throw new IllegalArgumentException("trailing_json"); }
        catch (java.io.IOException e) { throw new IllegalArgumentException("invalid_json"); }
        return result;
    }
    static String string(JsonObject object, String key, int min, int max) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new IllegalArgumentException(key);
        String s = value.getAsString();
        if (s.length() < min || s.length() > max || s.codePoints().anyMatch(c -> Character.isISOControl(c))) throw new IllegalArgumentException(key);
        return s;
    }
    static long number(JsonObject object, String key, long min, long max) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException(key);
        try { long n = value.getAsBigDecimal().longValueExact(); if (n < min || n > max) throw new IllegalArgumentException(key); return n; }
        catch (ArithmeticException e) { throw new IllegalArgumentException(key); }
    }
}
