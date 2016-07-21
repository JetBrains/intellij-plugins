package org.intellij.plugins.postcss.completion.handler;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class PostCssCustomSelectorInsertHandler implements InsertHandler<LookupElement> {

  @Override
  public void handleInsert(InsertionContext context, LookupElement item) {
    Editor editor = context.getEditor();
    Project project = editor.getProject();
    Template template = TemplateManager.getInstance(project).
      createTemplate("post_css_insert_custom_selector_template", "postcss", " :--$END$;");
    TemplateManager.getInstance(project).startTemplate(editor, template);
  }
}