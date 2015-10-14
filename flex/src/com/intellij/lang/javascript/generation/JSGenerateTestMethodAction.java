package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.fixes.BaseCreateFix;

public class JSGenerateTestMethodAction extends GenerateFlexUnitMethodActionBase {


  protected void buildTemplate(final Template template, final JSClass jsClass) {
    template.addTextSegment("[Test]\npublic function test");
    template.addVariable(new BaseCreateFix.MyExpression("Name"), true);
    template.addTextSegment("():void{\n");
    template.addEndVariable();
    template.addTextSegment("\n}");
  }
}
