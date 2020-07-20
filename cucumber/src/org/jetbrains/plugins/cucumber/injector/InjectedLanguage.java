package org.jetbrains.plugins.cucumber.injector;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

import java.util.Arrays;

public enum InjectedLanguage {
    JSON {
        @Override
        Language getLanguage() {
            return JsonLanguage.INSTANCE;
        }
    },
    XML {
        @Override
        Language getLanguage() {
            return XMLLanguage.INSTANCE;
        }

        /**
         * Override of the default behaviour for XML. The PSI for XML follows the structure below:
         *
         * - host.getFirstChild() => The opening docstring separator
         * - host.getFirstChild().getNextSibling() => The first PSI element of the XML structure
         * - host.getFirstChild().getNextSibling().getNextSibling() => The next PSI element of the XML structure
         * - ...
         * - host.getLastChild() => The closing docstring separator
         * @param host the element where the language will be injected into
         * @return the computed text range
         */
        @Override
        TextRange getRangeInsideHost(@NotNull final PsiLanguageInjectionHost host) {
            final int startOfXml = host.getFirstChild().getNextSibling().getTextRange().getEndOffset();
            final PsiElement beforeClosingDocString = host.getLastChild().getPrevSibling();
            final int endOfXml = beforeClosingDocString.getTextRange().getEndOffset();
            final int trailingSpacesLength = endOfXml <= startOfXml ? 0 :
                    InjectedLanguage.trailingSpacesLength(beforeClosingDocString.getText());
            final int startOffset = startOfXml - host.getTextRange().getStartOffset();
            final int length = endOfXml - startOfXml - trailingSpacesLength;

            return TextRange.from(startOffset, length);
        }
    },
    YAML {
        @Override
        Language getLanguage() {
            return YAMLLanguage.INSTANCE;
        }
    };

    abstract Language getLanguage();

    /**
     * Compute the text range inside the element into which the language will be injected. By default it is assumed
     * that the PSI structure for docstrings is:
     *
     * - host.getFirstChild() => The opening docstring separator
     * - host.getFirstChild().getNextSibling() => The text within the docstring
     * - host.getLastChild() => The closing docstring separator
     *
     * This method will compute the range spanning from the first character after the language type string and any
     * subsequent whitespace to the last character of the docstring text excluding any trailing whitespace.
     *
     * @param host the element where the language will be injected into
     * @return the computed text range
     */
    TextRange getRangeInsideHost(@NotNull final PsiLanguageInjectionHost host) {
        final PsiElement docStringSep = host.getFirstChild();
        final PsiElement docStringText = docStringSep.getNextSibling();
        final String afterLangType = StringUtils.substringAfter(docStringText.getText(), name().toLowerCase());
        final int leadingSpacesLength = leadingSpacesLength(afterLangType);
        final int langTypeAndSpacesLength = name().length() + leadingSpacesLength;
        final int startOffset = docStringSep.getTextLength() + langTypeAndSpacesLength;
        final int trailingSpacesLength = startOffset + docStringSep.getTextLength() >= host.getTextLength() ? 0 :
                trailingSpacesLength(docStringText.getText());
        // Skip the docstring separators, the content type string and any leading/trailing whitespace
        final int length = host.getTextLength() - startOffset - trailingSpacesLength - docStringSep.getTextLength();

        return TextRange.from(startOffset, length);
    }

    static InjectedLanguage getInjectedLanguage(final String text) {
        return Arrays.stream(InjectedLanguage.values()).
                filter(value -> StringUtils.startsWithIgnoreCase(text, value.name())).findFirst().orElse(null);
    }

    private static int leadingSpacesLength(final String text) {
        return StringUtils.length(text) - StringUtils.length(StringUtils.stripStart(text, null));
    }

    private static int trailingSpacesLength(final String text) {
        return StringUtils.length(text) - StringUtils.length(StringUtils.stripEnd(text, null));
    }
}
