package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JSGenerateTearDownMethodAction extends GenerateFlexUnitMethodActionBase {

  protected void buildTemplate(final Template template, final JSClass jsClass) {
    if (JSInheritanceUtil
          .findMember("tearDown", jsClass, JSInheritanceUtil.SearchedMemberType.Methods, JSFunction.FunctionKind.SIMPLE, true) != null) {
      template.addTextSegment("[After]\npublic override function tearDown():void{\nsuper.tearDown();");
      template.addEndVariable();
      template.addTextSegment("\n}");
    }
    else {
      template.addTextSegment("[After]\npublic function tearDown():void{\n");
      template.addEndVariable();
      template.addTextSegment("\n}");
    }
  }

  protected boolean isApplicableForMemberContainer(final @NotNull PsiElement jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    return jsClass instanceof JSClass && super.isApplicableForMemberContainer(jsClass, psiFile, editor) && ((JSClass)jsClass).findFunctionByName("tearDown") == null;
  }
}
