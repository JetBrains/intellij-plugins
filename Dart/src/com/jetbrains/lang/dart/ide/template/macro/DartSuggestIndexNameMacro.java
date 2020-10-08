package com.jetbrains.lang.dart.ide.template.macro;

import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.util.DartRefactoringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class DartSuggestIndexNameMacro extends Macro {
  @Override
  public String getName() {
    return "dartSuggestIndexName";
  }

  @NotNull
  @Override
  public String getDefaultValue() {
    return "i";
  }

  @Override
  public Result calculateResult(Expression @NotNull [] params, ExpressionContext context) {
    final PsiElement at = context.getPsiElementAtStartOffset();
    final Set<String> names = DartRefactoringUtil.collectUsedNames(at);
    for (char i = 'i'; i < 'z'; ++i) {
      if (!names.contains(Character.toString(i))) {
        return new TextResult(Character.toString(i));
      }
    }
    return null;
  }
}
