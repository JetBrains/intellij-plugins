package util;

public class StringUtil {

    private static final String ESCAPE_START = "IDEA-ESCAPE-START";
    private static final String ESCAPE_END = "IDEA-ESCAPE-END";

    public static String escape(final String str) {
        final String escaped = doEscape(str);
        return str.equals(escaped) ? str : ESCAPE_START + escaped + ESCAPE_END;
    }

    private static String doEscape(final String str) {
        final StringBuilder buffer = new StringBuilder();

        for (int idx = 0; idx < str.length(); idx++) {
            char ch = str.charAt(idx);
            switch (ch) {
                case '\b':
                    buffer.append("\\b");
                    break;

                case '\t':
                    buffer.append("\\t");
                    break;

                case '\n':
                    buffer.append("\\n");
                    break;

                case '\f':
                    buffer.append("\\f");
                    break;

                case '\r':
                    buffer.append("\\r");
                    break;

                case '\\':
                    buffer.append("\\\\");
                    break;

                default:
                    if (Character.isISOControl(ch)) {
                        String hexCode = Integer.toHexString(ch).toUpperCase();
                        buffer.append("\\u");
                        int paddingCount = 4 - hexCode.length();
                        while (paddingCount-- > 0) {
                            buffer.append(0);
                        }
                        buffer.append(hexCode);
                    } else {
                        buffer.append(ch);
                    }
            }
        }
        return buffer.toString();
    }
}
