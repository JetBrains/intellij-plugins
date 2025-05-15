// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.intentions;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.generation.ActionScriptGenerateEventHandler;
import com.intellij.lang.javascript.generation.ActionScriptGenerateEventHandler.EventConstantInfo;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CreateEventHandlerIntention extends BaseIntentionAction {

  public CreateEventHandlerIntention() {
    setText(FlexBundle.message("intention.create.event.handler"));
  }

  @Override
  public @NotNull String getFamilyName() {
    return getText();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
    // keep consistency with JavaScriptGenerateEventHandler.GenerateEventHandlerFix.beforeInvoke()

    final XmlAttribute xmlAttribute = ActionScriptGenerateEventHandler.getXmlAttribute(psiFile, editor);
    final String eventType = xmlAttribute == null ? null : ActionScriptGenerateEventHandler.getEventType(xmlAttribute);
    if (eventType != null) {
      return true;
    }

    final JSCallExpression callExpression = ActionScriptGenerateEventHandler.getEventListenerCallExpression(psiFile, editor);
    if (callExpression != null) {
      return true;
    }

    final @Nullable EventConstantInfo eventConstantInfo = ActionScriptGenerateEventHandler.getEventConstantInfo(psiFile, editor);
    return eventConstantInfo != null;
  }

  @Override
  public void invoke(final @NotNull Project project, final Editor editor, final PsiFile psiFile) throws IncorrectOperationException {
    new ActionScriptGenerateEventHandler().invoke(project, editor, psiFile);
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
