package com.intellij.coldFusion.UI.editorActions;

import com.intellij.codeInsight.hint.DefaultImplementationTextSelectioner;
import com.intellij.coldFusion.model.psi.CfmlAttribute;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 4/23/12
 */
public class CfmlImplementationTextSelectioner extends DefaultImplementationTextSelectioner {

  @Override
  public int getTextEndOffset(@NotNull PsiElement element) {

    PsiElement parent = element.getParent();
    if (parent instanceof CfmlAttribute) {
      parent = parent.getParent();
    }
    if (parent instanceof CfmlFunction || parent instanceof CfmlTag) {
      return parent.getTextRange().getEndOffset();
    }
    return super.getTextEndOffset(element);
  }
}
