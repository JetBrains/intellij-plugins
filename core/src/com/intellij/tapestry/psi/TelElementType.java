package com.intellij.tapestry.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.tapestry.lang.TelFileType;
import com.intellij.lang.ASTNode;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 *         Date: Jun 22, 2009
 *         Time: 10:14:34 PM
 */
public class TelElementType extends IElementType {
  public TelElementType(@NotNull @NonNls String debugName) {
    super(debugName, TelFileType.INSTANCE.getLanguage());
  }

  public PsiElement createPsiElement(ASTNode node) {
    return new ASTWrapperPsiElement(node);
  }
}
