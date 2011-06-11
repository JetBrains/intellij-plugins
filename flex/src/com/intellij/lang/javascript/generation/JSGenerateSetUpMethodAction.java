package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JSGenerateSetUpMethodAction extends GenerateFlexUnitMethodActionBase {

  protected void buildTemplate(final Template template, final JSClass jsClass) {
    if (JSInheritanceUtil.isParentClass(jsClass, FlexUnitSupport.FLEX_UNIT_1_TESTCASE_CLASS)) {
      template.addTextSegment("[Before]\npublic override function setUp():void{\n");
      template.addTextSegment("super.setUp();");
      template.addEndVariable();
      template.addTextSegment("\n}");
    }
    else {
      template.addTextSegment("[Before]\npublic function setUp():void{\n");
      template.addEndVariable();
      template.addTextSegment("\n}");
    }
  }

  protected boolean isApplicableForJsClass(final @NotNull JSClass jsClass, final PsiFile psiFile, final @NotNull Editor editor) {
    return super.isApplicableForJsClass(jsClass, psiFile, editor) && jsClass.findFunctionByName("setUp") == null;
  }
}
