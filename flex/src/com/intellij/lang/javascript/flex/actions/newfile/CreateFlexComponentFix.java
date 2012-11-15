package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.ui.newclass.CreateFlashClassWizard;
import com.intellij.lang.javascript.ui.newclass.CustomVariablesStep;
import com.intellij.lang.javascript.ui.newclass.MainStep;
import com.intellij.lang.javascript.ui.newclass.WizardModel;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * User: ksafonov
 */
public class CreateFlexComponentFix extends CreateClassOrInterfaceFix {
  public static final Collection<String> FLEX_TEMPLATES_EXTENSIONS =
    Arrays.asList(JavaScriptSupportLoader.MXML_FILE_EXTENSION);
  @NonNls static final String FLEX3_COMPONENT_TEMPLATE_NAME = "Flex 3 Component";
  @NonNls static final String FLEX4_COMPONENT_TEMPLATE_NAME = "Flex 4 Component";

  public CreateFlexComponentFix(final PsiDirectory dir) {
    super(dir);
  }

  public CreateFlexComponentFix(final String classFqn, final PsiElement element) {
    super(classFqn, null, element);
  }

  @Override
  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    if (!super.isAvailable(project, editor, file)) {
      return false;
    }

    final Module module = ModuleUtilCore.findModuleForPsiElement(file);
    return ModuleType.get(module) == FlexModuleType.getInstance() &&
           !FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().isPureAs();
  }

  public static String[] getAllowedBuiltInTemplates(final Module module) {
    FlexBuildConfiguration c = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
    if (c.isPureAs()) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    Sdk sdk = c.getSdk();
    if (sdk != null && StringUtil.compareVersionNumbers(sdk.getVersionString(), "4") < 0) {
      return new String[]{FLEX3_COMPONENT_TEMPLATE_NAME};
    }

    return new String[]{FLEX4_COMPONENT_TEMPLATE_NAME};
  }

  @Override
  protected CreateClassParameters createDialog(final String templateName) {
    final WizardModel model = new WizardModel(myContext, true);

    MainStep mainStep = new FlexMainStep(model, myContext, myClassNameToCreate, myPackageName, templateName);
    CustomVariablesStep customVariablesStep = new CustomVariablesStep(model);
    CreateFlashClassWizard w = new CreateFlashClassWizard(
      FlexBundle.message("new.flex.component.dialog.title"), myContext.getProject(), model, mainStep, customVariablesStep);
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
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          NewFlexComponentAction.setParentComponent((XmlBackedJSClassImpl)jsClass, superClassFqn);
        }
      });
    }
  }

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
      .filter(CreateClassOrInterfaceFix.getApplicableTemplates(FLEX_TEMPLATES_EXTENSIONS), new Condition<FileTemplate>() {
        @Override
        public boolean value(final FileTemplate fileTemplate) {
          String name = fileTemplate.getName();
          return ArrayUtil.contains(name, allowedBuiltin) || !NewFlexComponentAction.isClassifierTemplate(name);
        }
      });
  }

  @Override
  protected String getTemplateForTest(final boolean isInterface) {
    return computeApplicableTemplates().get(0).getName();
  }
}
