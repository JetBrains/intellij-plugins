package com.jetbrains.lang.dart.ide.editor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.impl.PsiBasedStripTrailingSpacesFilter;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartRecursiveVisitor;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.lang.dart.DartTokenTypes.RAW_TRIPLE_QUOTED_STRING;
import static com.jetbrains.lang.dart.DartTokenTypes.REGULAR_STRING_PART;

public final class DartStripTrailingSpacesFilterFactory extends PsiBasedStripTrailingSpacesFilter.Factory {

  @NotNull
  @Override
  protected PsiBasedStripTrailingSpacesFilter createFilter(@NotNull final Document document) {
    return new DartStripTrailingSpacesFilter(document);
  }

  @Override
  protected boolean isApplicableTo(@NotNull final Language language) {
    return language == DartLanguage.INSTANCE;
  }

  private static class DartStripTrailingSpacesFilter extends PsiBasedStripTrailingSpacesFilter {
    protected DartStripTrailingSpacesFilter(@NotNull final Document document) {
      super(document);
    }

    @Override
    protected void process(@NotNull final PsiFile psiFile) {
      PsiElementVisitor visitor = new DartRecursiveVisitor() {
        @Override
        public void visitStringLiteralExpression(@NotNull DartStringLiteralExpression literalExpression) {
          super.visitStringLiteralExpression(literalExpression);
          for (ASTNode child = literalExpression.getNode().getFirstChildNode(); child != null; child = child.getTreeNext()) {
            final IElementType type = child.getElementType();
            if (type == RAW_TRIPLE_QUOTED_STRING || type == REGULAR_STRING_PART) {
              // keep trailing spaces in multiline string literals
              disableRange(child.getTextRange(), false);
            }
          }
        }
      };
      psiFile.accept(visitor);
    }
  }
}
