package com.intellij.lang.javascript.flex.run;

import com.intellij.lang.javascript.flex.FlexRefactoringListenerProvider;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;

public abstract class FlexRunConfigRefactoringListener extends RefactoringElementAdapter {
  protected final FlexRunConfiguration myRunConfiguration;

  public FlexRunConfigRefactoringListener(final FlexRunConfiguration runConfiguration) {
    myRunConfiguration = runConfiguration;
  }

  protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
    final boolean generatedName = myRunConfiguration.isGeneratedName();
    updateParams(newElement);
    if (generatedName) {
      myRunConfiguration.setName(myRunConfiguration.suggestedName());
    }
  }

  protected abstract void updateParams(PsiElement newElement);

  private static void updatePackage(final FlexRunConfiguration runConfiguration, final String newPackage) {
    final FlexRunnerParameters params = runConfiguration.getRunnerParameters();
    final boolean isFlexUnit = params instanceof FlexUnitRunnerParameters;

    if (isFlexUnit) {
      if (((FlexUnitRunnerParameters)params).getScope() == FlexUnitRunnerParameters.Scope.Package) {
        ((FlexUnitRunnerParameters)params).setPackageName(newPackage);
      }
      else {
        final String oldFqn = ((FlexUnitRunnerParameters)params).getClassName();
        ((FlexUnitRunnerParameters)params).setClassName(
          newPackage + (StringUtil.isEmpty(newPackage) ? "" : ".") + StringUtil.getShortName(oldFqn));
      }
    }
    else {
      final String oldFqn = params.getMainClassName();
      params.setMainClassName(newPackage + (StringUtil.isEmpty(newPackage) ? "" : ".") + StringUtil.getShortName(oldFqn));
    }
  }

  public static class JSClassRefactoringListener extends FlexRunConfigRefactoringListener {
    public JSClassRefactoringListener(final FlexRunConfiguration runConfiguration) {
      super(runConfiguration);
    }

    protected void updateParams(final PsiElement newElement) {
      final JSClass newClass = FlexRefactoringListenerProvider.getJSClass(newElement);
      if (newClass == null) return;

      final FlexRunnerParameters params = myRunConfiguration.getRunnerParameters();
      final boolean isFlexUnit = params instanceof FlexUnitRunnerParameters;

      final String qName = newClass.getQualifiedName();
      if (StringUtil.isNotEmpty(qName)) {
        if (isFlexUnit) {
          ((FlexUnitRunnerParameters)params).setClassName(qName);
        }
        else {
          params.setMainClassName(qName);
        }
      }
    }
  }

  public static class PsiDirectoryRefactoringListener extends FlexRunConfigRefactoringListener {

    public PsiDirectoryRefactoringListener(final FlexRunConfiguration runConfiguration) {
      super(runConfiguration);
    }

    protected void updateParams(final PsiElement newElement) {
      if (!(newElement instanceof PsiDirectory)) return;
      updatePackage(myRunConfiguration,
                    DirectoryIndex.getInstance(newElement.getProject()).getPackageName(((PsiDirectory)newElement).getVirtualFile()));
    }
  }

  public static class JSFunctionRefactoringListener extends FlexRunConfigRefactoringListener {

    public JSFunctionRefactoringListener(final FlexUnitRunConfiguration runConfiguration) {
      super(runConfiguration);
    }

    protected void updateParams(final PsiElement newElement) {
      if (!(newElement instanceof JSFunction)) return;
      final String newName = ((JSFunction)newElement).getName();
      if (newName != null && StringUtil.isNotEmpty(newName)) {
        ((FlexUnitRunConfiguration)myRunConfiguration).getRunnerParameters().setMethodName(newName);
      }
    }
  }

  public static class PackageRefactoringListener extends FlexRunConfigRefactoringListener {

    public PackageRefactoringListener(final FlexRunConfiguration runConfiguration) {
      super(runConfiguration);
    }

    protected void updateParams(final PsiElement newElement) {
      if (newElement instanceof PsiDirectoryContainer) {
        updatePackage(myRunConfiguration, FlexRefactoringListenerProvider.getPackageName(newElement));
      }
      else if (newElement instanceof JSPackage || newElement instanceof JSPackageStatement) {
        updatePackage(myRunConfiguration, ((JSQualifiedNamedElement)newElement).getQualifiedName());
      }
    }
  }
}
