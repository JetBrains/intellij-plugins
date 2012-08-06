package com.google.jstestdriver.idea.assertFramework.qunit.codeInsight;

import com.google.jstestdriver.idea.assertFramework.codeInsight.AbstractJsGenerateAction;
import com.google.jstestdriver.idea.assertFramework.codeInsight.GenerateActionContext;
import com.google.jstestdriver.idea.assertFramework.codeInsight.JsGeneratorUtils;
import com.google.jstestdriver.idea.assertFramework.qunit.*;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractQUnitGenerateMissingLifecycleMethodAction extends AbstractJsGenerateAction {
  @NotNull
  @Override
  public abstract String getHumanReadableDescription();

  @NotNull
  public abstract String getMethodName();

  @Override
  public boolean isEnabled(@NotNull GenerateActionContext context) {
    Runnable generator = createGenerator(context);
    return generator != null;
  }

  @Override
  public void actionPerformed(@NotNull GenerateActionContext context) {
    Runnable generator = createGenerator(context);
    if (generator != null) {
      generator.run();
    }
  }

  @Nullable
  private static QUnitModuleStructure findModuleStructure(@NotNull GenerateActionContext context) {
    QUnitFileStructureBuilder builder = QUnitFileStructureBuilder.getInstance();
    QUnitFileStructure fileStructure = builder.fetchCachedTestFileStructure(context.getJsFile());
    if (fileStructure.hasQUnitSymbols()) {
      QUnitTestMethodStructure testMethodStructure = fileStructure.findTestMethodStructureContainingOffset(context.getDocumentCaretOffset());
      if (testMethodStructure != null) {
        AbstractQUnitModuleStructure ms = testMethodStructure.getModuleStructure();
        if (ms instanceof QUnitModuleStructure) {
          return (QUnitModuleStructure) ms;
        }
      }
      QUnitModuleStructure moduleStructure = fileStructure.findModuleStructureContainingOffset(context.getDocumentCaretOffset());
      if (moduleStructure != null) {
        return moduleStructure;
      }
    }
    return null;
  }

  @Nullable
  private Runnable createGenerator(@NotNull GenerateActionContext context) {
    QUnitModuleStructure moduleStructure = findModuleStructure(context);
    if (moduleStructure != null) {
      boolean noNeededMethod = moduleStructure.findLifecycleMethodByName(getMethodName()) == null;
      if (noNeededMethod) {
        return createGenerator(context, moduleStructure);
      }
    }
    return null;
  }

  @Nullable
  private Runnable createGenerator(final @NotNull GenerateActionContext context, final @NotNull QUnitModuleStructure moduleStructure) {
    return new Runnable() {
      @Override
      public void run() {
        JSObjectLiteralExpression lifecycleObjectLiteral = moduleStructure.getLifecycleObjectLiteral();
        String str = getMethodName() + ": function() {|}";
        if (lifecycleObjectLiteral != null) {
          JsGeneratorUtils.generateProperty(lifecycleObjectLiteral, context, str);
        } else {
          JSArgumentList argumentList = moduleStructure.getEnclosingCallExpression().getArgumentList();
          if (argumentList != null) {
            JsGeneratorUtils.generateObjectLiteralWithPropertyAsArgument(context, "{\n" + str + "\n}", argumentList, 1);
          }
        }
      }
    };
  }

}
