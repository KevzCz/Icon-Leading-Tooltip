package net.pixeldreamstudios.iconleadingtooltip.util;

public final class IconLeadingUtil {
    private IconLeadingUtil(){}

    public static boolean isIconGlyph(int cp) {
        return (cp >= 0xE000 && cp <= 0xF8FF)
                || (cp >= 0xF900 && cp <= 0xFAFF)
                || (cp >= 0xF0000 && cp <= 0xFFFFD)
                || (cp >= 0x100000 && cp <= 0x10FFFD);
    }

    public static int[] firstIconSpan(String s) {
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);
            if (isIconGlyph(cp)) return new int[]{ i, i + len };
            i += len;
        }
        return new int[]{ -1, -1 };
    }

    public static String stripSectionCodes(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            char ch = s.charAt(i);
            if (ch == '§') {
                if (i + 1 < s.length() && (s.charAt(i + 1) == 'x' || s.charAt(i + 1) == 'X')) {
                    i += 2;
                    for (int k = 0; k < 6 && i + 1 < s.length(); k++) {
                        if (s.charAt(i) == '§') i += 2; else break;
                    }
                    continue;
                } else {
                    i = Math.min(i + 2, s.length());
                    continue;
                }
            }
            out.append(ch);
            i++;
        }
        return out.toString();
    }
}
