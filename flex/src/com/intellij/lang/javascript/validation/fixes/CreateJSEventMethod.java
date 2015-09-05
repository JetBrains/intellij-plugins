package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiFile;

/**
* Created by IntelliJ IDEA.
* User: Maxim.Mossienko
* Date: 04.06.2009
* Time: 18:25:54
* To change this template use File | Settings | File Templates.
*/
public class CreateJSEventMethod extends CreateJSFunctionIntentionActionBase {
  private final Computable<String> myEventTypeGenerator;

  public CreateJSEventMethod(String invokedName, Computable<String> eventTypeGenerator) {
    super(invokedName, "javascript.create.event.handler.intention.name");
    myEventTypeGenerator = eventTypeGenerator;
  }

  @Override
  protected void addParameters(Template template, JSReferenceExpression refExpr, PsiFile file) {
    Expression expression = new MyExpression("event");

    template.addVariable("$event$", expression, expression, true);
    template.addTextSegment(":");
    String text = myEventTypeGenerator.compute();
    text = ImportUtils.importAndShortenReference(text, refExpr, true, true).first;
    template.addTextSegment(text);
  }

  @Override
  protected void addReturnType(Template template, JSReferenceExpression referenceExpression, PsiFile psifile) {
    template.addTextSegment("void");
  }

  @Override
  protected void addBody(Template template, JSReferenceExpression refExpr, PsiFile file) {
    template.addTextSegment("\n");
    template.addEndVariable();
    template.addTextSegment("\n");
  }
}
