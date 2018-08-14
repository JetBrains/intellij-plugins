package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JSGenerateSetUpMethodAction extends GenerateFlexUnitMethodActionBase {

  @Override
  protected void buildTemplate(final Template template, final JSClass jsClass) {
    if (JSInheritanceUtil
          .findMember("setUp", jsClass, JSInheritanceUtil.SearchedMemberType.Methods, JSFunction.FunctionKind.SIMPLE, true) != null) {
      template.addTextSegment("[Before]\npublic override function setUp():void{\nsuper.setUp();");
      template.addEndVariable();
      template.addTextSegment("\n}");
    }
    else {
      template.addTextSegment("[Before]\npublic function setUp():void{\n");
      template.addEndVariable();
      template.addTextSegment("\n}");
    }
  }

  @Override
  protected boolean isApplicableForMemberContainer(final @NotNull PsiElement jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    return jsClass instanceof JSClass && super.isApplicableForMemberContainer(jsClass, psiFile, editor) && ((JSClass)jsClass).findFunctionByName("setUp") == null;
  }
}
