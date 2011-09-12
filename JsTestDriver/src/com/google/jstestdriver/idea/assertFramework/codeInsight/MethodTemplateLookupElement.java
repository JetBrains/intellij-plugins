package com.google.jstestdriver.idea.assertFramework.codeInsight;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import org.jetbrains.annotations.NotNull;

public abstract class MethodTemplateLookupElement extends LookupItem<String> {

  private final Template myTemplate;

  protected MethodTemplateLookupElement(
    @NotNull String lookupString,
    @NotNull Template template
  ) {
    super(lookupString, lookupString);
    myTemplate = template;
    setPriority(4.0);  // to outweigh LookupElements from JSCompletionContributor
  }

  public abstract void renderElement(LookupElementPresentation presentation);

  @Override
  public void handleInsert(final InsertionContext context) {
    context.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());
    context.setAddCompletionChar(false);
    TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
    templateManager.startTemplate(context.getEditor(), "", myTemplate);
  }

}
