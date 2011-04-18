package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSParameterList;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 17, 2008
 *         Time: 9:39:02 PM
 */
public class OverrideMethodsFix extends BaseCreateMethodsFix<JSFunction> {
  public OverrideMethodsFix(final JSClass jsClass) {
    super(jsClass);
  }

  protected String buildFunctionBodyText(final String retType, final JSParameterList parameterList, final JSFunction func) {
    return buildDelegatingText(retType, parameterList, func, "super", anchor != null ? anchor:myJsClass);
  }

  public static String buildDelegatingText(String retType, JSParameterList parameterList, JSFunction func, String qualifier, @NotNull PsiElement anchor) {
    @NonNls StringBuilder functionText = new StringBuilder();
    functionText.append("{\n");

    if (!"void".equals(retType)) {
      functionText.append("return ");
    }

    functionText.append(qualifier).append(".");
    final JSAttributeList attributeList = func.getAttributeList();
    if (attributeList != null) {
      if (attributeList.getNamespace() != null) {
        functionText.append(calcNamespaceId(attributeList, JSResolveUtil.getNamespaceValue(attributeList), anchor)).append("::");
      }
    }
    functionText.append(func.getName());

    if (func.isGetProperty()) {

    } else if (func.isSetProperty()) {
      functionText.append("=").append(parameterList.getParameters()[0].getName());
    } else {
      functionText.append("(");
      boolean first = true;
      for (JSParameter param : parameterList.getParameters()) {
        if (!first) functionText.append(",");
        first = false;
        functionText.append(param.getName());
      }
      functionText.append(")");
    }

    functionText.append(JSChangeUtil.getSemicolon(func.getProject())).append("}");
    return functionText.toString();
  }

  protected String buildFunctionAttrText(String attrText, final JSAttributeList attributeList, final JSFunction function) {
    attrText = super.buildFunctionAttrText(attrText, attributeList, function);
    final PsiElement element = JSResolveUtil.findParent(function);
    attrText = StringUtil.replace(attrText, "override", "").trim();

    if (element instanceof JSClass && !"Object".equals(((JSClass)element).getQualifiedName())) {
      final PsiElement typeElement = attributeList != null ? attributeList.findAccessTypeElement():null;
      if (typeElement == null) {
        attrText += " override";
      } else {
        final int index = attrText.indexOf(typeElement.getText());
        attrText = attrText.substring(0, index) + ((index > 0)?" ":"") + "override " + attrText.substring(index);
      }
    }

    return attrText;
  }
}
