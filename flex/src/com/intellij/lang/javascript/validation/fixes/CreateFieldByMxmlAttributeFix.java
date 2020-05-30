package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class CreateFieldByMxmlAttributeFix extends CreateJSVariableIntentionAction {
  private final String myAttributeValue;

  public CreateFieldByMxmlAttributeFix(final String referencedName, final String attributeValue) {
    super(referencedName, true, false, false);
    myAttributeValue = attributeValue;
  }

  @Override
  protected void buildTemplate(final Template template,
                               final JSReferenceExpression referenceExpression,
                               final boolean staticContext,
                               @NotNull final PsiElement anchorParent) {
    template.addTextSegment("public ");
    template.addTextSegment("var ");
    template.addTextSegment(myReferencedName);
    template.addEndVariable();
    template.addTextSegment(":");
    addTypeVariableByMxmlAttributeValue(template, myAttributeValue);
    addSemicolonSegment(template, anchorParent);
  }

  protected static void addTypeVariableByMxmlAttributeValue(final Template template, final String attributeValue) {
    template.addVariable(new ConstantNode(guessMxmlAttributeType(attributeValue)), true);
  }

  @NotNull
  private static String guessMxmlAttributeType(final String attributeValue) {
    if (StringUtil.isEmpty(attributeValue)) {
      return "String";
    }

    if ("true".equals(attributeValue) || "false".equals(attributeValue)) {
      return "Boolean";
    }

    if (attributeValue.length() > 2 && attributeValue.startsWith("0x")) {
      Long.parseLong(attributeValue.substring(2), 16);
      return "uint";
    }

    try {
      Integer.parseInt(attributeValue);
      return "int";
    }
    catch (NumberFormatException e) {/* ignore */}

    try {
      Double.parseDouble(attributeValue);
      return "Number";
    }
    catch (NumberFormatException e) {/* ignore */}

    return "String";
  }
}
