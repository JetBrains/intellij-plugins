package com.intellij.tapestry.core.model.presentation.valueresolvers;

import org.apache.commons.chain.Command;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all value resolver commands.
 */
public abstract class AbstractValueResolver implements Command {

    /**
     * Trims and removes the prefix of the value.
     *
     * @param value the value.
     * @return the cleaned up value.
     */
    @Nullable
    public static String getCleanValue(@Nullable String value) {
        if (value == null) return null;

        String cleanValue = value.trim();

        if (cleanValue.startsWith("${")) {
            final int endIndex = cleanValue.lastIndexOf('}');
            cleanValue = endIndex != -1 ? cleanValue.substring(2, endIndex) : cleanValue.substring(2);
            cleanValue = cleanValue.trim();
        }

        String prefix = getPrefix(value, "");

        if (prefix == null) return null;

        return prefix.length() == 0 ? cleanValue : cleanValue.substring(prefix.length() + 1).trim();

    }

    /**
     * Finds the prefix in the value.
     *
     * @param value         the value.
     * @param defaultPrefix the default prefix.
     * @return the defined prefix in the value, the default prefix if no prefix was defined or {@code null} if the given value is invalid.
     */
    @Nullable
    protected static String getPrefix(@Nullable String value, @Nullable String defaultPrefix) {
        if (value == null)
            return null;

        if (value.indexOf(':') != -1) {
            String prefix = value.substring(0, value.indexOf(':'));

            if (prefix.length() > 0) {
                if (prefix.startsWith("${"))
                    return prefix.substring(2);
                else
                    return prefix;
            } else
                return null;
        } else
            return defaultPrefix;
    }
}
