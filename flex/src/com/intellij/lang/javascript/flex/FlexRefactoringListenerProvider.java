package com.intellij.lang.javascript.flex;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDirectoryContainer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.listeners.RefactoringElementAdapter;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class FlexRefactoringListenerProvider implements RefactoringElementListenerProvider {

  @Override
  @Nullable
  public RefactoringElementListener getListener(final PsiElement element) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (element instanceof PsiDirectoryContainer || element instanceof JSPackage || element instanceof JSPackageStatement) {
      final String packageName = getPackageName(element);
      return StringUtil.isEmpty(packageName) ? null : new PackageRefactoringListener(element.getProject(), module, packageName);
    }

    if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return null;

    final JSClass jsClass = getJSClass(element);
    if (jsClass != null) {
      return new JSClassRefactoringListener(module, jsClass.getQualifiedName());
    }

    final VirtualFile file = element instanceof PsiFile ? element.getContainingFile().getVirtualFile() : null;
    if (file != null) {
      if ("css".equalsIgnoreCase(file.getExtension())) {
        return new CssFileRefactoringListener(module, file.getPath());
      }
      else if ("xml".equalsIgnoreCase(file.getExtension())) {
        return new XmlFileRefactoringListener(module, file.getPath());
      }
    }

    return null;
  }

  @Nullable
  public static JSClass getJSClass(final PsiElement element) {
    if (element instanceof JSClass) {
      return (JSClass)element;
    }
    if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((XmlFile)element)) {
      return XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
    }
    if (element instanceof JSFile) {
      return JSPsiImplUtils.findClass((JSFile)element);
    }
    return null;
  }

  @Nullable
  public static String getPackageName(final PsiElement element) {
    assert element instanceof PsiDirectoryContainer || element instanceof JSPackage || element instanceof JSPackageStatement;

    if (element instanceof PsiDirectoryContainer) {
      final PsiDirectory[] directories = ((PsiDirectoryContainer)element).getDirectories();
      if (directories.length == 0) return null;
      return ProjectFileIndex.getInstance(element.getProject()).getPackageNameByDirectory(directories[0].getVirtualFile());
    }
    return ((JSQualifiedNamedElement)element).getQualifiedName();
  }

  private static class PackageRefactoringListener extends RefactoringElementAdapter {
    private @NotNull final Project myProject;
    private @Nullable final Module myModule;
    private final String myOldPackageName;
    private String myNewPackageName;

    PackageRefactoringListener(final @NotNull Project project, final @Nullable Module module, final String oldPackageName) {
      myProject = project;
      myModule = module;
      myOldPackageName = oldPackageName;
    }

    @Override
    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      final String newPackageName = getPackageName(newElement);
      if (newPackageName != null) {
        myNewPackageName = newPackageName;
        updatePackageName(myOldPackageName, myNewPackageName);
      }
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      if (myNewPackageName != null) {
        updatePackageName(myNewPackageName, myOldPackageName);
      }
    }

    private void updatePackageName(final String oldPackageName, final String newPackageName) {
      if (myModule == null) {
        for (final Module module : ModuleManager.getInstance(myProject).getModules()) {
          if (ModuleType.get(module) == FlexModuleType.getInstance()) {
            packageNameChanged(module, oldPackageName, newPackageName);
          }
        }
      }
      else {
        packageNameChanged(myModule, oldPackageName, newPackageName);
      }
    }

    private static void packageNameChanged(final @NotNull Module module, final String oldPackageName, final String newPackageName) {
      final String oldPackageWithDot = oldPackageName + ".";

      for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
        if (bc.getOutputType() == OutputType.Application && bc.getMainClass().startsWith(oldPackageWithDot)) {
          final String mainClass = (newPackageName.isEmpty() ? "" : (newPackageName + ".")) +
                                   bc.getMainClass().substring(oldPackageWithDot.length());
          FlexProjectConfigurationEditor.makeNonStructuralModification(bc, configuration -> configuration.setMainClass(mainClass));
        }

        if (BCUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
          final Collection<FlexBuildConfiguration.RLMInfo> oldRLMs = bc.getRLMs();
          final Collection<FlexBuildConfiguration.RLMInfo> newRLMs = new ArrayList<>();
          boolean changed = false;

          for (FlexBuildConfiguration.RLMInfo rlm : oldRLMs) {
            if (rlm.MAIN_CLASS.startsWith(oldPackageWithDot)) {
              changed = true;
              final String mainClass = (newPackageName.isEmpty() ? "" : (newPackageName + ".")) +
                                       rlm.MAIN_CLASS.substring(oldPackageWithDot.length());
              final String outputFileName = rlm.OUTPUT_FILE.equals(BCUtils.suggestRLMOutputPath(rlm.MAIN_CLASS))
                                            ? BCUtils.suggestRLMOutputPath(mainClass)
                                            : rlm.OUTPUT_FILE;
              newRLMs.add(new FlexBuildConfiguration.RLMInfo(mainClass, outputFileName, rlm.OPTIMIZE));
            }
            else {
              newRLMs.add(rlm);
            }
          }

          if (changed) {
            FlexProjectConfigurationEditor.makeNonStructuralModification(bc, configuration -> configuration.setRLMs(newRLMs));
          }
        }
      }
    }
  }

  private static class JSClassRefactoringListener extends RefactoringElementAdapter {
    private final Module myModule;
    private final String myOldClassName;
    private String myNewClassName;

    JSClassRefactoringListener(final Module module, final String oldClassName) {
      myModule = module;
      myOldClassName = oldClassName;
    }

    @Override
    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      final JSClass newClass = getJSClass(newElement);
      if (newClass != null) {
        myNewClassName = newClass.getQualifiedName();
        classNameChanged(myOldClassName, myNewClassName);
      }
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      if (myNewClassName != null) {
        classNameChanged(myNewClassName, myOldClassName);
      }
    }

    private void classNameChanged(final String oldClassName, final String newClassName) {
      for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations()) {
        if (bc.getOutputType() == OutputType.Application && bc.getMainClass().equals(oldClassName)) {
          FlexProjectConfigurationEditor.makeNonStructuralModification(bc, configuration -> configuration.setMainClass(newClassName));
        }

        if (BCUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
          final Collection<FlexBuildConfiguration.RLMInfo> oldRLMs = bc.getRLMs();
          final Collection<FlexBuildConfiguration.RLMInfo> newRLMs = new ArrayList<>();
          boolean changed = false;

          for (FlexBuildConfiguration.RLMInfo rlm : oldRLMs) {
            if (rlm.MAIN_CLASS.equals(oldClassName)) {
              changed = true;
              final String outputFileName = rlm.OUTPUT_FILE.equals(BCUtils.suggestRLMOutputPath(rlm.MAIN_CLASS))
                                            ? BCUtils.suggestRLMOutputPath(newClassName)
                                            : rlm.OUTPUT_FILE;
              newRLMs.add(new FlexBuildConfiguration.RLMInfo(newClassName, outputFileName, rlm.OPTIMIZE));
            }
            else {
              newRLMs.add(rlm);
            }
          }

          if (changed) {
            FlexProjectConfigurationEditor.makeNonStructuralModification(bc, configuration -> configuration.setRLMs(newRLMs));
          }
        }
      }
    }
  }

  private static abstract class FileRefactoringListener extends RefactoringElementAdapter {
    protected final @NotNull Module myModule;
    protected final String myOldFilePath;
    protected String myNewFilePath;

    FileRefactoringListener(@NotNull final Module module, final String oldFilePath) {
      myModule = module;
      myOldFilePath = oldFilePath;
    }

    @Override
    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      final VirtualFile file = newElement instanceof PsiFile ? ((PsiFile)newElement).getVirtualFile() : null;
      if (file != null) {
        myNewFilePath = file.getPath();
        filePathChanged(myOldFilePath, myNewFilePath);
      }
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldFilePath) {
      if (myNewFilePath != null) {
        filePathChanged(myNewFilePath, myOldFilePath);
      }
    }

    protected abstract void filePathChanged(final String oldFilePath, final String newFilePath);
  }

  private static class CssFileRefactoringListener extends FileRefactoringListener {
    CssFileRefactoringListener(final Module module, final String oldFilePath) {
      super(module, oldFilePath);
    }

    @Override
    protected void filePathChanged(final String oldFilePath, final String newFilePath) {
      for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations()) {
        if (BCUtils.canHaveRLMsAndRuntimeStylesheets(bc)) {
          final Collection<String> cssFiles = bc.getCssFilesToCompile();
          if (cssFiles.isEmpty()) continue;

          final Collection<String> newCssFiles = new ArrayList<>(cssFiles.size());
          boolean changed = false;
          for (String cssFile : cssFiles) {
            if (cssFile.equals(oldFilePath)) {
              newCssFiles.add(newFilePath);
              changed = true;
            }
            else {
              newCssFiles.add(cssFile);
            }
          }

          if (changed) {
            FlexProjectConfigurationEditor.makeNonStructuralModification(bc,
                                                                         configuration -> configuration.setCssFilesToCompile(newCssFiles));
          }
        }
      }
    }
  }

  private static class XmlFileRefactoringListener extends FileRefactoringListener {
    XmlFileRefactoringListener(final Module module, final String oldFilePath) {
      super(module, oldFilePath);
    }

    @Override
    protected void filePathChanged(final String oldFilePath, final String newFilePath) {
      for (FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations()) {
        if (bc.getCompilerOptions().getAdditionalConfigFilePath().equals(oldFilePath)) {
          FlexProjectConfigurationEditor.makeNonStructuralModification(bc,
                                                                       configuration -> configuration.getCompilerOptions().setAdditionalConfigFilePath(newFilePath));
        }

        // TODO update services-config, manifest files
      }
    }
  }
}
