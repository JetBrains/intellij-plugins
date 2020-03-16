package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.actions.newfile.CreateFlexComponentFix;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.IncorrectOperationException;
import java.util.HashMap;
import icons.JavaScriptPsiIcons;
import org.jetbrains.annotations.Nullable;

public class NewFlexComponentUmlAction extends NewJSClassUmlActionBase {

  public NewFlexComponentUmlAction() {
    super(FlexBundle.messagePointer("new.flex.component.uml.action.text"), FlexBundle.messagePointer("new.flex.component.action.description"),
          JavaScriptPsiIcons.Classes.XmlBackedClass);
  }

  @Override
  public String getActionName() {
    return FlexBundle.message("new.flex.component.command.name");
  }

  @Override
  protected CreateClassParameters showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage) {
    return CreateFlexComponentFix.createAndShow(null, dirAndPackage.first, null, dirAndPackage.second);
  }

  @Nullable
  @Override
  public Object createElement(final DiagramDataModel<Object> model,
                              final CreateClassParameters params,
                              final AnActionEvent event) {
    final Ref<JSClass> clazz = new Ref<>();
    CommandProcessor.getInstance().executeCommand(params.getTargetDirectory().getProject(), () -> {
      try {
        ActionScriptCreateClassOrInterfaceFix
          .createClass(params.getTemplateName(), params.getClassName(), params.getPackageName(), getSuperClass(params),
                       params.getInterfacesFqns(), params.getTargetDirectory(), getActionName(), true,
                       new HashMap<>(params.getTemplateAttributes()),
                       jsClass -> {
                         CreateFlexComponentFix.fixParentComponent(jsClass, params.getSuperclassFqn());
                         clazz.set(jsClass);
                       });
      }
      catch (Exception e) {
        throw new IncorrectOperationException(e);
      }
    }, JavaScriptBundle.message(FlexBundle.message("new.flex.component.command.name")), null);
    return clazz.get();
  }
}
