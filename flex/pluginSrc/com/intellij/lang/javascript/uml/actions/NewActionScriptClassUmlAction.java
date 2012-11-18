package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramDataModel;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.actions.newfile.CreateFlexComponentFix;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSIconProvider;
import com.intellij.lang.javascript.ui.newclass.CreateFlashClassWizard;
import com.intellij.lang.javascript.ui.newclass.CustomVariablesStep;
import com.intellij.lang.javascript.ui.newclass.MainStep;
import com.intellij.lang.javascript.ui.newclass.WizardModel;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.Consumer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NewActionScriptClassUmlAction extends NewJSClassUmlActionBase {

  public NewActionScriptClassUmlAction() {
    super(JSBundle.message("new.actionscript.class.uml.action.text"), JSBundle.message("new.actionscript.class.action.description"),
          JSIconProvider.AS_INSTANCE.getClassIcon());
  }

  @Override
  public String getActionName() {
    return JSBundle.message("new.actionscript.class.command.name");
  }

  @Nullable
  @Override
  protected CreateClassParameters showDialog(Project project, Pair<PsiDirectory, String> dirAndPackage) {
    return CreateClassOrInterfaceFix
      .createAndShow(null, dirAndPackage.first, null, true, dirAndPackage.second, null, JSBundle.message("new.actionscript.class.dialog.title"),
                     new Computable<List<FileTemplate>>() {
                       @Override
                       public List<FileTemplate> compute() {
                         return CreateClassOrInterfaceFix
                           .getApplicableTemplates(CreateClassOrInterfaceFix.ACTIONSCRIPT_TEMPLATES_EXTENSIONS);
                       }
                     });
  }

  @Nullable
  @Override
  public Object createElement(final DiagramDataModel<Object> model,
                              final CreateClassParameters params,
                              final AnActionEvent event) {
    final Ref<JSClass> clazz = new Ref<JSClass>();
    CommandProcessor.getInstance().executeCommand(params.getTargetDirectory().getProject(), new Runnable() {
      @Override
      public void run() {
        try {
          CreateClassOrInterfaceFix
            .createClass(params.getTemplateName(), params.getClassName(), params.getPackageName(), getSuperClass(params),
                         params.getInterfacesFqns(), params.getTargetDirectory(), getActionName(), true,
                         new HashMap<String, Object>(params.getTemplateAttributes()),
                         new Consumer<JSClass>() {
                           @Override
                           public void consume(final JSClass jsClass) {
                             clazz.set(jsClass);
                           }
                         });
        }
        catch (Exception e) {
          throw new IncorrectOperationException(e.getMessage(), e);
        }
      }
    }, JSBundle.message(JSBundle.message("new.actionscript.class.command.name")), null);
    return clazz.get();
  }
}
