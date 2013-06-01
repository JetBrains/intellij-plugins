package com.jetbrains.lang.dart.ide.template.macro;

import com.intellij.codeInsight.template.*;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartRefactoringUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public abstract class DartFilterByClassMacro extends Macro {
  @Override
  public Result calculateResult(@NotNull Expression[] params, ExpressionContext context) {
    final PsiElement at = context.getPsiElementAtStartOffset();
    final Set<DartComponentName> variables = DartRefactoringUtil.collectUsedComponents(at);
    final List<DartComponentName> filtered = ContainerUtil.filter(variables, new Condition<DartComponentName>() {
      @Override
      public boolean value(DartComponentName name) {
        final PsiElement nameParent = name.getParent();
        if (nameParent instanceof DartClass) {
          return false;
        }
        final DartClassResolveResult result = DartResolveUtil.getDartClassResolveResult(nameParent);
        final DartClass dartClass = result.getDartClass();
        return dartClass != null && filter(dartClass);
      }
    });
    return filtered.isEmpty() ? null : new PsiElementResult(filtered.iterator().next());
  }

  protected abstract boolean filter(@NotNull DartClass dartClass);
}
