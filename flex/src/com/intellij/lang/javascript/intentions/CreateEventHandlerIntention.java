package com.intellij.lang.javascript.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.generation.JavaScriptGenerateEventHandler;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class CreateEventHandlerIntention extends PsiElementBaseIntentionAction {

  public CreateEventHandlerIntention() {
    setText(FlexBundle.message("intention.create.event.handler"));
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(final @NotNull Project project, final Editor editor, final @NotNull PsiElement element) {
    // keep consistency with JavaScriptGenerateEventHandler.GenerateEventHandlerFix.beforeInvoke()

    final PsiFile psiFile = element.getContainingFile();
    final XmlAttribute xmlAttribute = JavaScriptGenerateEventHandler.getXmlAttribute(psiFile, editor);
    final String eventType = xmlAttribute == null ? null : JavaScriptGenerateEventHandler.getEventType(xmlAttribute);
    if (eventType != null) {
      return true;
    }

    final JSCallExpression callExpression = JavaScriptGenerateEventHandler.getEventListenerCallExpression(psiFile, editor);
    if (callExpression != null) {
      return true;
    }

    final Trinity<JSExpressionStatement, String, String> eventConstantInfo =
      JavaScriptGenerateEventHandler.getEventConstantInfo(psiFile, editor);
    if (eventConstantInfo != null) {
      return true;
    }

    return false;
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    new JavaScriptGenerateEventHandler().invoke(project, editor, file);
  }

  public boolean startInWriteAction() {
    return false;
  }
}
