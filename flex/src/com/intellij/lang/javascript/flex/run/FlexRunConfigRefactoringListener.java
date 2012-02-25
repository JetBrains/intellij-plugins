package com.intellij.lang.javascript.flex.run;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;

public abstract class FlexRunConfigRefactoringListener extends RefactoringElementAdapter {
  //protected final FlexRunConfiguration myRunConfiguration;

  /*
  public FlexRunConfigRefactoringListener(final FlexRunConfiguration runConfiguration) {
    myRunConfiguration = runConfiguration;
  }
  */

  /*
  protected void elementRenamedOrMoved(@NotNull PsiElement newElement) {
    final boolean generatedName = myRunConfiguration.isGeneratedName();
    updateParams(newElement);
    if (generatedName) {
      myRunConfiguration.setName(myRunConfiguration.suggestedName());
    }
  }
  */

  protected abstract void updateParams(PsiElement newElement);

  /*
  private static void updatePackage(final FlexRunConfiguration runConfiguration, final String newPackage) {
    final FlexRunnerParameters params = runConfiguration.getRunnerParameters();
    final boolean isFlexUnit = params instanceof FlexUnitRunnerParameters;

    if (isFlexUnit) {
      if (((FlexUnitRunnerParameters)params).getScope() == NewFlexUnitRunnerParameters.Scope.Package) {
        ((FlexUnitRunnerParameters)params).setPackageName(newPackage);
      }
      else {
        final String oldFqn = ((FlexUnitRunnerParameters)params).getClassName();
        ((FlexUnitRunnerParameters)params).setClassName(StringUtil.getQualifiedName(newPackage, StringUtil.getShortName(oldFqn)));
      }
    }
    else {
      final String oldFqn = params.getMainClassName();
      params.setMainClassName(StringUtil.getQualifiedName(newPackage, StringUtil.getShortName(oldFqn)));
    }
  }
  */

  /*
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

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      final FlexRunnerParameters params = myRunConfiguration.getRunnerParameters();
      final boolean isFlexUnit = params instanceof FlexUnitRunnerParameters;
      if (isFlexUnit) {
        ((FlexUnitRunnerParameters)params).setClassName(oldQualifiedName);
      }
      else {
        params.setMainClassName(oldQualifiedName);
      }
    }
  }
  */

  /*
  public static class PsiDirectoryRefactoringListener extends FlexRunConfigRefactoringListener {

    public PsiDirectoryRefactoringListener(final FlexRunConfiguration runConfiguration) {
      super(runConfiguration);
    }

    protected void updateParams(final PsiElement newElement) {
      if (!(newElement instanceof PsiDirectory)) return;
      updatePackage(myRunConfiguration,
                    DirectoryIndex.getInstance(newElement.getProject()).getPackageName(((PsiDirectory)newElement).getVirtualFile()));
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      updatePackage(myRunConfiguration, oldQualifiedName);
    }
  }
  */

  /*
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

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      final int methodIdx = oldQualifiedName.indexOf("#") + 1;
      if (methodIdx > 0 && methodIdx < oldQualifiedName.length()) {
        ((FlexUnitRunConfiguration)myRunConfiguration).getRunnerParameters().setMethodName(oldQualifiedName.substring(methodIdx));
      }
    }
  }
  */

  /*
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

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      updatePackage(myRunConfiguration, oldQualifiedName);
    }
  }
  */
}
