package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class CreateSetterByMxmlAttributeFix extends CreateJSPropertyAccessorIntentionAction {
  private final String myReferencedName;
  private final String myAttributeValue;

  public CreateSetterByMxmlAttributeFix(final String referencedName, final String attributeValue) {
    super(referencedName, false);
    myReferencedName = referencedName;
    myAttributeValue = attributeValue;
  }

  @Override
  protected void buildTemplate(final Template template,
                               final JSReferenceExpression referenceExpression,
                               final boolean staticContext,
                               @NotNull final PsiElement anchorParent) {
    template.addTextSegment("public ");
    writeFunctionAndName(template, myReferencedName, anchorParent, null, referenceExpression);

    template.addTextSegment("(");
    template.addTextSegment(myReferencedName + ":");
    CreateFieldByMxmlAttributeFix.addTypeVariableByMxmlAttributeValue(template, myAttributeValue);
    template.addTextSegment(")");

    template.addTextSegment(":");
    addReturnType(template, referenceExpression, anchorParent);

    final PsiElement clazz = findClass(anchorParent);
    if (clazz == null || clazz instanceof JSClass && !((JSClass)clazz).isInterface()) {
      template.addTextSegment(" {");
      addBody(template, anchorParent);
      template.addTextSegment("}");
    }
    else {
      addSemicolonSegment(template, anchorParent);
      template.addEndVariable();
    }
  }

  private void addBody(final Template template, final PsiElement context) {
    String varName = myReferencedName;
    String paramName = varName;
    varName = JSRefactoringUtil.transformAccessorNameToPropertyName(varName, context);

    if (varName.equals(paramName)) {
      varName = StringUtil.fixVariableNameDerivedFromPropertyName(varName);
    }

    addVarName(template, varName);
    template.addEndVariable();
    template.addTextSegment(" = " + paramName);
    addSemicolonSegment(template, context);
  }
}
