// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSIconProvider;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class NewActionScriptClassUmlAction extends NewJSClassUmlActionBase {

  public NewActionScriptClassUmlAction() {
    super(FlexBundle.messagePointer("new.actionscript.class.uml.action.text"), FlexBundle
            .messagePointer("new.actionscript.class.action.description"),
          JSIconProvider.AS_INSTANCE.getClassIcon());
  }

  @Override
  public @NotNull String getActionName() {
    return FlexBundle.message("new.actionscript.class.command.name");
  }

  @Override
  protected @Nullable CreateClassParameters showDialog(final Project project, Pair<PsiDirectory, String> dirAndPackage) {
    return ActionScriptCreateClassOrInterfaceFix
      .createAndShow(null, dirAndPackage.first, null, true, dirAndPackage.second, null, JavaScriptBundle
                       .message("new.actionscript.class.dialog.title"),
                     () -> ActionScriptCreateClassOrInterfaceFix
                       .getApplicableTemplates(ActionScriptCreateClassOrInterfaceFix.ACTIONSCRIPT_TEMPLATES_EXTENSIONS, project));
  }

  @Override
  public @Nullable Object createElement(final DiagramDataModel<Object> model,
                                        final CreateClassParameters params,
                                        final AnActionEvent event) {
    final Ref<JSClass> clazz = new Ref<>();
    CommandProcessor.getInstance().executeCommand(params.getTargetDirectory().getProject(), () -> {
      try {
        ActionScriptCreateClassOrInterfaceFix
          .createClass(params.getTemplateName(), params.getClassName(), params.getPackageName(), getSuperClass(params),
                       params.getInterfacesFqns(), params.getTargetDirectory(), getActionName(), true,
                       new HashMap<>(params.getTemplateAttributes()),
                       jsClass -> clazz.set(jsClass));
      }
      catch (Exception e) {
        throw new IncorrectOperationException(e);
      }
    }, JavaScriptBundle.message(FlexBundle.message("new.actionscript.class.command.name")), null);
    return clazz.get();
  }
}
