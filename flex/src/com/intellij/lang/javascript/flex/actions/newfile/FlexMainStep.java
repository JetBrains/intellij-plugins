package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.javascript.flex.resolve.FlexResolveHelper;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.ui.newclass.MainStep;
import com.intellij.lang.javascript.ui.newclass.WizardModel;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.util.ClassLoaderUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import org.apache.velocity.runtime.parser.ParseException;

import java.io.IOException;
import java.util.Properties;

public class FlexMainStep extends MainStep {
  private final WizardModel myModel;

  public FlexMainStep(final WizardModel model,
                      final PsiElement context,
                      final String className,
                      final String packageName,
                      final String templateName) {
    super(model, context.getProject(), className, true, packageName, null, true, templateName, context,
          JavaScriptBundle.message("choose.base.component.title"), () -> CreateFlexComponentFix.computeApplicableTemplates(context));
    myModel = model;
    setSuperclassLabelText(FlexBundle.message("parent.component.label.text"));
  }

  @Override
  protected boolean canFinish() {
    if (!super.canFinish()) {
      return false;
    }

    if (isSuperclassFieldEnabled()) {
      if (!FlexResolveHelper.isValidClassName(getSuperclassFqn(), true)) {
        return false;
      }
      if (!(ActionScriptClassResolver.findClassByQNameStatic(getSuperclassFqn(), getSuperclassScope()) instanceof JSClass)) {
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
      template = ClassLoaderUtil.<FileTemplate, IOException>computeWithClassLoader(
        ActionScriptCreateClassOrInterfaceFix.class.getClassLoader(),
        () -> FileTemplateManager.getDefaultInstance().getInternalTemplate(myModel.getTemplateName()));
      String[] attributes = FileTemplateUtil.calculateAttributes(template.getText(), new Properties(), true, myProject);
      if (ArrayUtil.contains(ActionScriptCreateClassOrInterfaceFix.SUPERCLASS, attributes)) {
        myModel.setSuperclassFqn(getSuperclassFqn());
      }
    }
    catch (IOException | ParseException e) {
      // ignore as the action will not succeed
    }
  }
}
