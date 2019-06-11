package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.ConstantNode;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;

public class JSGenerateTestMethodAction extends GenerateFlexUnitMethodActionBase {


  @Override
  protected void buildTemplate(final Template template, final JSClass jsClass) {
    template.addTextSegment("[Test]\npublic function test");
    template.addVariable(new ConstantNode("Name"), true);
    template.addTextSegment("():void{\n");
    template.addEndVariable();
    template.addTextSegment("\n}");
  }
}
