package com.jetbrains.lang.dart.ide.surroundWith.expression;

import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.psi.DartNamedArgument;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartWithBracketsExpressionSurrounder extends DartWithExpressionSurrounder {

  public boolean isApplicable(@NotNull PsiElement[] elements) {
    // Limit this to named arguments; the intent is to convert a Flutter child: param to children:, which may involve creating red code.
    return super.isApplicable(elements) && elements[0].getParent() instanceof DartNamedArgument;
  }

  @Override
  public String getTemplateDescription() {
    return "[expr]";
  }

  @Override
  protected String getTemplateText(PsiElement expr) {
    String text = expr.getText();
    int nlIndex = text.lastIndexOf('\n');
    if (nlIndex < 0) {
      return "[" + expr.getText() + "]";
    }
    Matcher matcher = Pattern.compile("\\n", Pattern.MULTILINE).matcher(text);
    String newText = matcher.replaceAll("\n  ");
    return "[\n" + newText + ",]\n";
  }
}
