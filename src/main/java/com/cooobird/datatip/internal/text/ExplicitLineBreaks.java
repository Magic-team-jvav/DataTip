package com.cooobird.datatip.internal.text;

/**
 * 解码文本中的显式换行标记。
 */
final class ExplicitLineBreaks {
    private ExplicitLineBreaks() {
    }

    static String decode(String text) {
        int firstEscape = text.indexOf('\\');
        int firstCarriageReturn = text.indexOf('\r');
        if (firstEscape < 0 && firstCarriageReturn < 0) return text;

        StringBuilder decoded = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '\r') {
                decoded.append('\n');
                if (index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                continue;
            }
            if (current != '\\' || index + 1 >= text.length()) {
                decoded.append(current);
                continue;
            }

            char escaped = text.charAt(index + 1);
            if (escaped == 'n') {
                decoded.append('\n');
                index++;
            } else if (escaped == 'r') {
                decoded.append('\n');
                index++;
                if (index + 2 < text.length()
                    && text.charAt(index + 1) == '\\'
                    && text.charAt(index + 2) == 'n') {
                    index += 2;
                }
            } else if (escaped == '\\') {
                decoded.append('\\');
                index++;
            } else {
                decoded.append(current);
            }
        }
        return decoded.toString();
    }
}

