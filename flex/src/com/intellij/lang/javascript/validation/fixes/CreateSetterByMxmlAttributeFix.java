package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public class CreateSetterByMxmlAttributeFix extends CreateJSPropertyAccessorIntentionAction {
  private String myReferencedName;
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
                               final PsiFile file,
                               final PsiElement anchorParent) {
    template.addTextSegment("public ");
    writeFunctionAndName(template, myReferencedName, file, null, referenceExpression);

    template.addTextSegment("(");
    template.addTextSegment(myReferencedName + ":");
    CreateFieldByMxmlAttributeFix.addTypeVariableByMxmlAttributeValue(template, myAttributeValue);
    template.addTextSegment(")");

    template.addTextSegment(":");
    addReturnType(template, referenceExpression, file);

    final PsiElement clazz = findClass(anchorParent);
    if (clazz == null || clazz instanceof JSClass && !((JSClass)clazz).isInterface()) {
      template.addTextSegment(" {");
      addBody(template, file);
      template.addTextSegment("}");
    }
    else {
      addSemicolonSegment(template, file);
      template.addEndVariable();
    }
  }

  private void addBody(final Template template, final PsiFile file) {
    String varName = myReferencedName;
    String paramName = varName;
    varName = JSRefactoringUtil.transformAccessorNameToPropertyName(varName, file.getProject());

    if (varName.equals(paramName)) {
      varName = StringUtil.fixVariableNameDerivedFromPropertyName(varName);
    }

    addVarName(template, varName);
    template.addEndVariable();
    template.addTextSegment(" = " + paramName);
    addSemicolonSegment(template, file);
  }
}
