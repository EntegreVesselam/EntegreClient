package com.entegrevesselam.markers;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
/** Pure display policy; the server's marker ID remains the ownership/update key. */
final class MarkerAppearance {
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N}]*");
    private static final int[] PALETTE = {6,9,10,11,12,13,14,15};
    static String initials(String name) {
        var matcher = WORD.matcher(Normalizer.normalize(name,Normalizer.Form.NFC));
        List<String> words = new ArrayList<>();
        while(matcher.find()) words.add(matcher.group());
        if(words.isEmpty()) return "?";
        StringBuilder result = new StringBuilder();
        if(words.size()==1) result.append(words.getFirst());
        else for(String word : words) result.appendCodePoint(word.codePointAt(0));
        String upper = result.toString().toUpperCase(Locale.ROOT);
        int end = 0;
        while(end < upper.length()) {
            int next = end + Character.charCount(upper.codePointAt(end));
            if(next > 3) break;
            end = next;
        }
        return upper.substring(0,end);
    }
    static int color(String id, int color) {
        // The API/UI default is palette index4 (dark red); every other supplied value is explicit.
        return color == 4 ? PALETTE[Math.floorMod(id.hashCode(),PALETTE.length)] : color;
    }
}
