package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
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
import com.intellij.openapi.util.ClassLoaderUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.apache.velocity.runtime.parser.ParseException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * User: ksafonov
 */
public class CreateFlexComponentFix extends CreateClassOrInterfaceFix {
  public static final Collection<String> FLEX_TEMPLATES_EXTENSIONS =
    Arrays.asList(JavaScriptSupportLoader.MXML_FILE_EXTENSION);
  @NonNls static final String FLEX3_COMPONENT_TEMPLATE_NAME = "Flex 3 Component";
  @NonNls static final String FLEX4_COMPONENT_TEMPLATE_NAME = "Flex 4 Component";
  private String myParentComponentToSet;

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

    MainStep mainStep = new MainStep(model, myContext.getProject(),
                                     myClassNameToCreate,
                                     true,
                                     myPackageName,
                                     null,
                                     true,
                                     templateName,
                                     myContext,
                                     JSBundle.message("choose.base.component.title"), new Computable<List<FileTemplate>>() {
      @Override
      public List<FileTemplate> compute() {
        return computeApplicableTemplates();
      }
    }) {
      @Override
      protected boolean canFinish() {
        if (!super.canFinish()) {
          return false;
        }

        if (isSuperclassFieldEnabled()) {
          if (!JSUtils.isValidClassName(getSuperclassFqn(), true)) {
            return false;
          }
          if (!(JSResolveUtil.findClassByQName(getSuperclassFqn(), getSuperclassScope()) instanceof JSClass)) {
            return false;
          }
        }
        return true;
      }

      @Override
      protected boolean canBeSuperClass(final JSClass jsClass) {
        // hiding classes with no default constructor can be confusing: "where is my class?"
        return super.canBeSuperClass(jsClass)/* &&
               (jsClass.getConstructor() == null || jsClass.getConstructor().getParameterList().getParameters().length == 0)*/;
      }

      @Override
      public void commit(final CommitType commitType) throws CommitStepException {
        super.commit(commitType);
        // let's replace parent component only if template contains 'Superclass' macro
        final FileTemplate template;
        try {
          template = ClassLoaderUtil
            .runWithClassLoader(CreateClassOrInterfaceFix.class.getClassLoader(),
                                new ThrowableComputable<FileTemplate, IOException>() {
                                  @Override
                                  public FileTemplate compute() throws IOException {
                                    return FileTemplateManager.getInstance().getInternalTemplate(model.getTemplateName());
                                  }
                                });
          String[] attributes = FileTemplateUtil.calculateAttributes(template.getText(), new Properties(), true);
          if (ArrayUtil.contains(CreateClassOrInterfaceFix.SUPERCLASS, attributes)) {
            myParentComponentToSet = getSuperclassFqn();
          }
        }
        catch (IOException e) {
          // ignore as the action will not succeed
        }
        catch (ParseException e) {
          // ignore as the action will not succeed
        }
      }
    };
    mainStep.setSuperclassLabelText(JSBundle.message("parent.component.label.text"));
    CustomVariablesStep customVariablesStep = new CustomVariablesStep(model);
    CreateFlashClassWizard w = new CreateFlashClassWizard(
      JSBundle.message("new.flex.component.dialog.title"), myContext.getProject(), model, mainStep, customVariablesStep);
    w.show();
    if (w.getExitCode() != DialogWrapper.OK_EXIT_CODE) return null;
    return model;
  }

  @Override
  protected void postProcess(@NotNull final JSClass jsClass) {
    final XmlTag tag = (XmlTag)jsClass.getParent();
    if (myParentComponentToSet != null && myParentComponentToSet.equals(tag.getName())) {
      // raw fqn have likely been inserted by template (that equals to what user have entered)
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          NewFlexComponentAction.setParentComponent((XmlBackedJSClassImpl)jsClass, myParentComponentToSet);
        }
      });
    }
    jsClass.navigate(true);
  }

  @NotNull
  public String getName() {
    return FlexBundle.message("create.flex.component.intention.name", myClassNameToCreate);
  }

  @Override
  protected List<FileTemplate> computeApplicableTemplates() {
    Module module = ModuleUtilCore.findModuleForPsiElement(myContext);
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
