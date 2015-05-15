package org.intellij.plugins.markdown.braces;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.markdown.lang.MarkdownLanguage;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;

public class MarkdownBraceMatcher extends PairedBraceMatcherAdapter {

    public MarkdownBraceMatcher() {
        super(new MyPairedBraceMatcher(), MarkdownLanguage.INSTANCE);
    }

    private static class MyPairedBraceMatcher implements PairedBraceMatcher {

        @Override public BracePair[] getPairs() {
            return new BracePair[]{
                    new BracePair(MarkdownTokenTypes.LPAREN, MarkdownTokenTypes.RPAREN, false),
                    new BracePair(MarkdownTokenTypes.LBRACKET, MarkdownTokenTypes.RBRACKET, false),
                    new BracePair(MarkdownTokenTypes.LT, MarkdownTokenTypes.GT, false),
                    new BracePair(MarkdownTokenTypes.CODE_FENCE_START, MarkdownTokenTypes.CODE_FENCE_END, true)
            };
        }

        @Override public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, IElementType contextType) {
            return true;
        }

        @Override public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
            return openingBraceOffset;
        }
    }
}
