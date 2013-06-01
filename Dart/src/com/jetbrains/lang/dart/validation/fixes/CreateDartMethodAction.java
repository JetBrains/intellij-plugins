package com.jetbrains.lang.dart.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartCallExpression;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartExpression;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateDartMethodAction extends CreateDartFunctionActionBase {

  private final boolean myStatic;

  public CreateDartMethodAction(@NotNull String name, boolean isStatic) {
    super(name);
    myStatic = isStatic;
  }

  @NotNull
  @Override
  public String getName() {
    return myStatic ? DartBundle.message("dart.create.static.method.fix.name", myFunctionName)
                    : DartBundle.message("dart.create.method.fix.name", myFunctionName);
  }

  @Override
  protected void buildFunctionText(Template template, @NotNull DartCallExpression callExpression) {
    if (myStatic) {
      template.addTextSegment("static ");
    }
    super.buildFunctionText(template, callExpression);
  }

  @Override
  protected PsiElement getScopeBody(PsiElement element) {
    return null;
  }

  @Nullable
  @Override
  protected PsiElement findAnchor(PsiElement element) {
    DartCallExpression callExpression = PsiTreeUtil.getParentOfType(element, DartCallExpression.class);
    assert callExpression != null;
    DartExpression expression = callExpression.getExpression();
    DartReference[] dartReferences = PsiTreeUtil.getChildrenOfType(expression, DartReference.class);
    DartClass dartClass = dartReferences == null ? PsiTreeUtil.getParentOfType(element, DartClass.class)
                                                 : dartReferences[0].resolveDartClass().getDartClass();
    return DartResolveUtil.getBody(dartClass);
  }
}
