package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.javascript.flex.mxml.schema.CodeContextHolder;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
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
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.lang.javascript.validation.fixes.CreateClassParameters;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.apache.velocity.runtime.parser.ParseException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class NewFlexComponentAction extends NewActionScriptClassAction {

  @NonNls private static final String FLEX3_COMPONENT_TEMPLATE_NAME = "Flex 3 Component";
  @NonNls private static final String FLEX4_COMPONENT_TEMPLATE_NAME = "Flex 4 Component";

  private static final String[] FLEX_CLASSIFIER_TEMPLATES =
    new String[]{FLEX3_COMPONENT_TEMPLATE_NAME, FLEX4_COMPONENT_TEMPLATE_NAME};

  public static boolean isClassifierTemplate(String templateName) {
    return ArrayUtil.contains(templateName, FLEX_CLASSIFIER_TEMPLATES);
  }

  @Override
  protected boolean isAvailableIn(final Module module) {
    return super.isAvailableIn(module) && !FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().isPureAs();
  }

  @Override
  protected CreateClassOrInterfaceAction createAction(final PsiDirectory dir) {
    final Ref<String> parentComponentToSet = new Ref<String>();
    return new CreateClassOrInterfaceAction(dir) {
      @Override
      protected CreateClassParameters createDialog(final String templateName) {
        final WizardModel model = new WizardModel(dir, true);
        MainStep mainStep = new MainStep(model, dir.getProject(),
                                         null,
                                         true,
                                         DirectoryIndex.getInstance(dir.getProject()).getPackageName(dir.getVirtualFile()),
                                         null,
                                         true,
                                         templateName,
                                         dir,
                                         JSBundle.message("choose.base.component.title")) {

          @Override
          protected List<FileTemplate> getApplicableTemplates() {
            Module module = ModuleUtilCore.findModuleForPsiElement(dir);
            final String[] allowedBuiltin = getAllowedBuiltInTemplates(module);
            return ContainerUtil
              .filter(CreateClassOrInterfaceAction.getApplicableTemplates(FLEX_TEMPLATES_EXTENSIONS), new Condition<FileTemplate>() {
                @Override
                public boolean value(final FileTemplate fileTemplate) {
                  String name = fileTemplate.getName();
                  return ArrayUtil.contains(name, allowedBuiltin) || !isClassifierTemplate(name);
                }
              });
          }

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
                .runWithClassLoader(CreateClassOrInterfaceAction.class.getClassLoader(),
                                    new ThrowableComputable<FileTemplate, IOException>() {
                                      @Override
                                      public FileTemplate compute() throws IOException {
                                        return FileTemplateManager.getInstance().getInternalTemplate(model.getTemplateName());
                                      }
                                    });
              String[] attributes = FileTemplateUtil.calculateAttributes(template.getText(), new Properties(), true);
              if (ArrayUtil.contains(CreateClassOrInterfaceAction.SUPERCLASS, attributes)) {
                parentComponentToSet.set(getSuperclassFqn());
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
          JSBundle.message("new.flex.component.dialog.title"), dir.getProject(), model, mainStep, customVariablesStep);
        w.show();
        if (w.getExitCode() != DialogWrapper.OK_EXIT_CODE) return null;
        return model;
      }

      @Override
      protected void postProcess(@NotNull final JSClass jsClass) {
        final XmlTag tag = (XmlTag)jsClass.getParent();
        if (!parentComponentToSet.isNull() && parentComponentToSet.get().equals(tag.getName())) {
          // raw fqn have likely been inserted by template (that equals to what user have entered)
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              setParentComponent((XmlBackedJSClassImpl)jsClass, parentComponentToSet.get());
            }
          });
        }
        jsClass.navigate(true);
      }
    };
  }

  private static String[] getAllowedBuiltInTemplates(final Module module) {
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
  protected String getCommandName() {
    return FlexBundle.message("new.flex.component.command.name");
  }

  public static void setParentComponent(final XmlBackedJSClassImpl clazz, final String newParentQname) {
    Pair<String, String> prefixAndNamespace = getPrefixAndNamespace(clazz.getParent(), newParentQname);
    clazz.setBaseComponent(newParentQname, prefixAndNamespace.first, prefixAndNamespace.second);
  }

  public static Pair<String, String> getPrefixAndNamespace(XmlTag tag, String qName) {
    Module module = ModuleUtilCore.findModuleForPsiElement(tag);
    boolean isFlex4Template = ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, tag.knownNamespaces());

    CodeContextHolder holder = CodeContextHolder.getInstance(module.getProject());
    // ensure namespace is loaded into code context (including all the namespaces from all the libraries)
    CodeContext.getContext(JavaScriptSupportLoader.MXML_URI4, module);
    Collection<String> namespaces = holder.getNamespaces(module);
    String[] illegalNamespaces =
      isFlex4Template ? new String[]{JavaScriptSupportLoader.MXML_URI} : JavaScriptSupportLoader.FLEX_4_NAMESPACES;
    for (String namespace : namespaces) {
      if (ArrayUtil.contains(namespace, illegalNamespaces) || CodeContext.isPackageBackedNamespace(namespace)) {
        continue;
      }
      CodeContext codeContext =
        CodeContext.isStdNamespace(namespace) ? holder.getStandardContext(namespace, module) : holder.getCodeContext(namespace, module);
      if (codeContext == null) {
        continue;
      }
      if (codeContext.getElementDescriptor(StringUtil.getShortName(qName), qName) != null) {
        return Pair.create(FlexSchemaHandler.getDefaultPrefix(namespace), namespace);
      }
    }
    String packageName = StringUtil.getPackageName(qName);
    return Pair.create("local", StringUtil.isEmpty(packageName) ? "*" : packageName + ".*");
  }
}
