// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.ui.newclass.CreateFlashClassWizard;
import com.intellij.lang.javascript.ui.newclass.CustomVariablesStep;
import com.intellij.lang.javascript.ui.newclass.MainStep;
import com.intellij.lang.javascript.ui.newclass.WizardModel;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CreateFlexComponentFix extends ActionScriptCreateClassOrInterfaceFix {
  public static final Collection<String> FLEX_TEMPLATES_EXTENSIONS =
    List.of(JavaScriptSupportLoader.MXML_FILE_EXTENSION);
  @NonNls static final String FLEX3_COMPONENT_TEMPLATE_NAME = "Flex 3 Component";
  @NonNls static final String FLEX4_COMPONENT_TEMPLATE_NAME = "Flex 4 Component";

  public CreateFlexComponentFix(final PsiDirectory dir) {
    super(dir);
  }

  public CreateFlexComponentFix(final String classFqn, final PsiElement element) {
    super(classFqn, null, element);
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, PsiElement element, final Editor editor, final PsiFile file) {
    if (!super.isAvailable(project, element, editor, file)) {
      return false;
    }

    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    return ModuleType.get(module) == FlexModuleType.getInstance() &&
           !FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().isPureAs();
  }

  public static String[] getAllowedBuiltInTemplates(final Module module) {
    FlexBuildConfiguration c = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    if (c.isPureAs()) {
      return ArrayUtilRt.EMPTY_STRING_ARRAY;
    }

    Sdk sdk = c.getSdk();
    if (sdk != null && StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") < 0) {
      return new String[]{FLEX3_COMPONENT_TEMPLATE_NAME};
    }

    return new String[]{FLEX4_COMPONENT_TEMPLATE_NAME};
  }

  @Override
  protected CreateClassParameters createDialog(final String templateName) {
    return createAndShow(templateName, myContext, myClassNameToCreate, myPackageName);
  }

  public static CreateClassParameters createAndShow(final String templateName,
                                                     final PsiElement context,
                                                     final String classNameToCreate,
                                                     final String packageName) {
    final WizardModel model = new WizardModel(context, true);

    MainStep mainStep = new FlexMainStep(model, context, classNameToCreate, packageName, templateName);
    CustomVariablesStep customVariablesStep = new CustomVariablesStep(model);
    CreateFlashClassWizard w =
      new CreateFlashClassWizard(FlexBundle.message("new.flex.component.dialog.title"), context.getProject(), model,
                                 "New_MXML_Component_dialog", mainStep, customVariablesStep);
    w.show();
    if (w.getExitCode() != DialogWrapper.OK_EXIT_CODE) return null;
    return model;
  }

  @Override
  protected void postProcess(@NotNull final JSClass jsClass, final String superClassFqn) {
    fixParentComponent(jsClass, superClassFqn);
    jsClass.navigate(true);
  }

  public static void fixParentComponent(final JSClass jsClass, final String superClassFqn) {
    final XmlTag tag = (XmlTag)jsClass.getParent();
    if (superClassFqn != null && superClassFqn.equals(tag.getName())) {
      // raw fqn have likely been inserted by template (that equals to what user have entered)
      ApplicationManager.getApplication().runWriteAction(() -> NewFlexComponentAction.setParentComponent((MxmlJSClass)jsClass, superClassFqn));
    }
  }

  @Override
  @NotNull
  public String getName() {
    return FlexBundle.message("create.flex.component.intention.name", myClassNameToCreate);
  }

  @Override
  protected List<FileTemplate> computeApplicableTemplates() {
    return computeApplicableTemplates(myContext);
  }

  public static List<FileTemplate> computeApplicableTemplates(final PsiElement context) {
    Module module = ModuleUtilCore.findModuleForPsiElement(context);
    final String[] allowedBuiltin = getAllowedBuiltInTemplates(module);
    return ContainerUtil
      .filter(ActionScriptCreateClassOrInterfaceFix.getApplicableTemplates(FLEX_TEMPLATES_EXTENSIONS, context.getProject()), fileTemplate -> {
        String name = fileTemplate.getName();
        return ArrayUtil.contains(name, allowedBuiltin) || !NewFlexComponentAction.isClassifierTemplate(name);
      });
  }

  @Override
  protected String getTemplateForTest(final boolean isInterface) {
    return computeApplicableTemplates().get(0).getName();
  }
}
