package net.pixeldreamstudios.iconleadingtooltip.util;

public final class IconLeadingUtil {
    private IconLeadingUtil(){}

    public static boolean isIconGlyph(int cp) {
        return (cp >= 0xE000 && cp <= 0xF8FF)
                || (cp >= 0xF900 && cp <= 0xFAFF)
                || (cp >= 0x1CD00 && cp <= 0x1CDFF)
                || (cp >= 0x1FB00 && cp <= 0x1FBFF)
                || (cp >= 0xF0000 && cp <= 0xFFFFD)
                || (cp >= 0x100000 && cp <= 0x10FFFD);
    }

    public static int[] firstIconSpan(String s) {
        int formattingStart = -1;

        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);

            if (cp == '§') {
                if (formattingStart == -1) {
                    formattingStart = i;
                }

                if (i + 1 < s.length()) {
                    int nextCp = s.codePointAt(i + 1);
                    if (nextCp == 'x' || nextCp == 'X') {
                        i += 2;
                        for (int k = 0; k < 6 && i < s.length(); k++) {
                            if (s.charAt(i) == '§' && i + 1 < s.length()) {
                                i += 2;
                            } else {
                                break;
                            }
                        }
                        continue;
                    } else {
                        i += 2;
                        continue;
                    }
                } else {
                    i++;
                    continue;
                }
            }

            if (isIconGlyph(cp)) {
                int actualStart = (formattingStart >= 0) ? formattingStart : i;
                return new int[]{ actualStart, i + len };
            }

            formattingStart = -1;
            i += len;
        }

        return new int[]{ -1, -1 };
    }

    public static String stripSectionCodes(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int len = Character.charCount(cp);

            if (cp == '§') {
                if (i + 1 < s.length()) {
                    int nextCp = s.codePointAt(i + 1);
                    if (nextCp == 'x' || nextCp == 'X') {
                        i += 2;
                        for (int k = 0; k < 6 && i < s.length(); k++) {
                            int checkCp = s.codePointAt(i);
                            if (checkCp == '§' && i + 1 < s.length()) {
                                i += 2;
                            } else {
                                break;
                            }
                        }
                        continue;
                    } else {
                        i += 2;
                        continue;
                    }
                } else {
                    i++;
                    continue;
                }
            }
            out.appendCodePoint(cp);
            i += len;
        }
        return out.toString();
    }
}
