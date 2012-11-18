package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.ui.newclass.MainStep;
import com.intellij.lang.javascript.ui.newclass.WizardModel;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.openapi.util.ClassLoaderUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import org.apache.velocity.runtime.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
* User: ksafonov
*/
public abstract class FlexMainStep extends MainStep {
  private final WizardModel myModel;

  public FlexMainStep(final WizardModel model,
                      final PsiElement context,
                      final String className,
                      final String packageName,
                      final String templateName) {
    super(model, context.getProject(), className, true, packageName, null, true, templateName, context,
          JSBundle.message("choose.base.component.title"), new Computable<List<FileTemplate>>() {
      @Override
      public List<FileTemplate> compute() {
        return CreateFlexComponentFix.computeApplicableTemplates(context);
      }
    });
    myModel = model;
    setSuperclassLabelText(JSBundle.message("parent.component.label.text"));
  }

  @Override
  protected boolean canFinish() {
    if (!super.canFinish()) {
      return false;
    }

    if (myDataPanel.isSuperclassFieldEnabled()) {
      if (!JSUtils.isValidClassName(myDataPanel.getSuperclassFqn(), true)) {
        return false;
      }
      if (!(JSResolveUtil.findClassByQName(myDataPanel.getSuperclassFqn(), getSuperclassScope()) instanceof JSClass)) {
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
                                return FileTemplateManager.getInstance().getInternalTemplate(myModel.getTemplateName());
                              }
                            });
      String[] attributes = FileTemplateUtil.calculateAttributes(template.getText(), new Properties(), true);
      if (ArrayUtil.contains(CreateClassOrInterfaceFix.SUPERCLASS, attributes)) {
        myModel.setSuperclassFqn(myDataPanel.getSuperclassFqn());
      }
    }
    catch (IOException e) {
      // ignore as the action will not succeed
    }
    catch (ParseException e) {
      // ignore as the action will not succeed
    }
  }
}
