package com.intellij.javascript.flex.refactoring;

import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveDirectoryWithClassesProcessor;
import com.intellij.refactoring.rename.DirectoryAsPackageRenameHandlerBase;
import com.intellij.util.Query;

/**
 * @author ksafonov
 */
public class FlexDirectoryAsPackageRenameHandler extends DirectoryAsPackageRenameHandlerBase<FlexPackageImpl> {

  @Override
  protected VirtualFile[] occursInPackagePrefixes(FlexPackageImpl aPackage) {
    return VirtualFile.EMPTY_ARRAY;
  }

  @Override
  protected boolean isIdentifier(String name, Project project) {
    return LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.ECMA_SCRIPT_L4).isIdentifier(name, project);
  }

  @Override
  protected String getQualifiedName(FlexPackageImpl aPackage) {
    return aPackage.getQualifiedName();
  }

  @Override
  protected FlexPackageImpl getPackage(PsiDirectory psiDirectory) {
    Project project = psiDirectory.getProject();
    DirectoryIndex index = DirectoryIndex.getInstance(project);
    String packageName = index.getPackageName(psiDirectory.getVirtualFile());
    if (packageName == null) return null;
    Query<VirtualFile> dirs = index.getDirectoriesByPackageName(packageName, false);
    if (dirs.findFirst() == null) return null;
    return new FlexPackageImpl((PsiManagerEx)PsiManager.getInstance(project), packageName);
  }

  @Override
  protected BaseRefactoringProcessor createProcessor(final String newQName,
                                                     Project project,
                                                     final PsiDirectory[] dirsToRename,
                                                     boolean searchInComments,
                                                     boolean searchInNonJavaFiles) {
    return new MoveDirectoryWithClassesProcessor(project, dirsToRename, null, searchInComments, searchInNonJavaFiles, false, null) {
      @Override
      public TargetDirectoryWrapper getTargetDirectory(final PsiDirectory dir) {
        return new TargetDirectoryWrapper(dir.getParentDirectory(), StringUtil.getShortName(newQName));
      }

      @Override
      protected String getTargetName() {
        return newQName;
      }

      @Override
      protected String getCommandName() {
        return RefactoringBundle.message(dirsToRename.length == 1 ? "rename.directory.command.name" : "rename.directories.command.name");
      }
    };

  }
}
