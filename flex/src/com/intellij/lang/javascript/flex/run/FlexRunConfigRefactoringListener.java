package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.lang.javascript.flex.FlexRefactoringListenerProvider;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.roots.PackageIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;

public abstract class FlexRunConfigRefactoringListener extends RefactoringElementAdapter {
  protected final RunConfigurationBase myRunConfiguration;

  public FlexRunConfigRefactoringListener(final RunConfigurationBase runConfiguration) {
    assert runConfiguration instanceof FlashRunConfiguration || runConfiguration instanceof FlexUnitRunConfiguration
      : runConfiguration.getType().getDisplayName();
    myRunConfiguration = runConfiguration;
  }

  @Override
  protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
    updateParams(newElement);
  }

  @Override
  public void undoElementMovedOrRenamed(@NotNull final PsiElement newElement, @NotNull final String oldQualifiedName) {
    undo(newElement, oldQualifiedName);
  }

  protected abstract void updateParams(PsiElement newElement);

  protected abstract void undo(final PsiElement element, final String name);

  private static void updatePackage(final RunConfigurationBase runConfiguration, final String newPackage) {
    if (runConfiguration instanceof FlexUnitRunConfiguration) {
      final FlexUnitRunnerParameters params = ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
      if (params.getScope() == FlexUnitRunnerParameters.Scope.Package) {
        params.setPackageName(newPackage);
      }
      else {
        final String oldFqn = params.getClassName();
        params.setClassName(StringUtil.getQualifiedName(newPackage, StringUtil.getShortName(oldFqn)));
      }
    }
    else {
      final FlashRunnerParameters params = ((FlashRunConfiguration)runConfiguration).getRunnerParameters();
      final String oldFqn = params.getOverriddenMainClass();
      params.setOverriddenMainClass(StringUtil.getQualifiedName(newPackage, StringUtil.getShortName(oldFqn)));
    }
  }

  public static class JSClassRefactoringListener extends FlexRunConfigRefactoringListener {
    public JSClassRefactoringListener(final RunConfigurationBase runConfiguration) {
      super(runConfiguration);
    }

    @Override
    protected void updateParams(final PsiElement newElement) {
      final JSClass newClass = FlexRefactoringListenerProvider.getJSClass(newElement);
      if (newClass == null) return;

      final String qName = newClass.getQualifiedName();
      if (StringUtil.isNotEmpty(qName)) {
        if (myRunConfiguration instanceof FlexUnitRunConfiguration) {
          ((FlexUnitRunConfiguration)myRunConfiguration).getRunnerParameters().setClassName(qName);
        }
        else {
          ((FlashRunConfiguration)myRunConfiguration).getRunnerParameters().setOverriddenMainClass(qName);
        }
      }
    }

    @Override
    public void undo(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      if (myRunConfiguration instanceof FlexUnitRunConfiguration) {
        ((FlexUnitRunConfiguration)myRunConfiguration).getRunnerParameters().setClassName(oldQualifiedName);
      }
      else {
        ((FlashRunConfiguration)myRunConfiguration).getRunnerParameters().setOverriddenMainClass(oldQualifiedName);
      }
    }
  }

  public static class PsiDirectoryRefactoringListener extends FlexRunConfigRefactoringListener {
    public PsiDirectoryRefactoringListener(final RunConfigurationBase runConfiguration) {
      super(runConfiguration);
    }

    @Override
    protected void updateParams(final PsiElement newElement) {
      if (!(newElement instanceof PsiDirectory)) return;
      updatePackage(myRunConfiguration,
                    PackageIndex.getInstance(newElement.getProject()).getPackageNameByDirectory(((PsiDirectory)newElement).getVirtualFile()));
    }

    @Override
    public void undo(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      updatePackage(myRunConfiguration, oldQualifiedName);
    }
  }

  public static class JSFunctionRefactoringListener extends FlexRunConfigRefactoringListener {
    public JSFunctionRefactoringListener(final FlexUnitRunConfiguration runConfiguration) {
      super(runConfiguration);
    }

    @Override
    protected void updateParams(final PsiElement newElement) {
      if (!(newElement instanceof JSFunction)) return;
      final String newName = ((JSFunction)newElement).getName();
      if (newName != null && StringUtil.isNotEmpty(newName)) {
        ((FlexUnitRunConfiguration)myRunConfiguration).getRunnerParameters().setMethodName(newName);
      }
    }

    @Override
    public void undo(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      final int methodIdx = oldQualifiedName.lastIndexOf(".") + 1;
      if (methodIdx > 0 && methodIdx < oldQualifiedName.length()) {
        ((FlexUnitRunConfiguration)myRunConfiguration).getRunnerParameters().setMethodName(oldQualifiedName.substring(methodIdx));
      }
    }
  }

  public static class PackageRefactoringListener extends FlexRunConfigRefactoringListener {
    public PackageRefactoringListener(final RunConfigurationBase runConfiguration) {
      super(runConfiguration);
    }

    @Override
    protected void updateParams(final PsiElement newElement) {
      if (newElement instanceof PsiDirectoryContainer) {
        updatePackage(myRunConfiguration, FlexRefactoringListenerProvider.getPackageName(newElement));
      }
      else if (newElement instanceof JSPackage || newElement instanceof JSPackageStatement) {
        updatePackage(myRunConfiguration, ((JSQualifiedNamedElement)newElement).getQualifiedName());
      }
    }

    @Override
    public void undo(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      updatePackage(myRunConfiguration, oldQualifiedName);
    }
  }
}
