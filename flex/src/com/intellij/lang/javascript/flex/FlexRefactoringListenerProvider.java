package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackage;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.impl.DirectoryIndex;
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

public class FlexRefactoringListenerProvider implements RefactoringElementListenerProvider {
  @Nullable
  public RefactoringElementListener getListener(final PsiElement element) {
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (element instanceof PsiDirectoryContainer || element instanceof JSPackage || element instanceof JSPackageStatement) {
      return new PackageRefactoringListener(module, getPackageName(element));
    }

    if (module != null && ModuleType.get(module) == FlexModuleType.getInstance()) {
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
    }

    return null;
  }

  @Nullable
  public static JSClass getJSClass(final PsiElement element) {
    if (element instanceof JSClass) {
      return (JSClass)element;
    }
    if (element instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((XmlFile)element)) {
      return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
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
      return DirectoryIndex.getInstance(element.getProject()).getPackageName(directories[0].getVirtualFile());
    }
    return ((JSQualifiedNamedElement)element).getQualifiedName();
  }

  private static class PackageRefactoringListener extends RefactoringElementAdapter {
    private @Nullable final Module myModule;
    private final String myOldPackageName;

    public PackageRefactoringListener(final @Nullable Module module, final String oldPackageName) {
      myModule = module;
      myOldPackageName = oldPackageName;
    }

    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      if (StringUtil.isEmpty(myOldPackageName)) return;

      final String newPackageName = getPackageName(newElement);
      updatePackageName(newElement, newPackageName);
    }

    private void updatePackageName(PsiElement newElement, String newPackageName) {
      if (myModule == null) {
        for (final Module module : ModuleManager.getInstance(newElement.getProject()).getModules()) {
          updateForModule(module, myOldPackageName, newPackageName);
        }
      }
      else {
        updateForModule(myModule, myOldPackageName, newPackageName);
      }
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      updatePackageName(newElement, oldQualifiedName);
    }

    private static void updateForModule(final @NotNull Module module, final String oldPackageName, final String newPackageName) {
      final String oldPackageWithDot = oldPackageName + ".";
      /*
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module)) {
        if (config.MAIN_CLASS.startsWith(oldPackageWithDot)) {
          config.MAIN_CLASS = (newPackageName.isEmpty() ? "" : (newPackageName + "."))
                              + config.MAIN_CLASS.substring(oldPackageWithDot.length());
        }
      }
      */
    }
  }

  private static class JSClassRefactoringListener extends RefactoringElementAdapter {
    private @Nullable final Module myModule;
    private final String myOldClassName;

    public JSClassRefactoringListener(final @NotNull Module module, final String oldClassName) {
      myModule = module;
      myOldClassName = oldClassName;
    }

    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      final JSClass newClass = getJSClass(newElement);
      if (newClass == null) return;

      /*
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(myModule)) {
        if (config.MAIN_CLASS.equals(myOldClassName)) {
          config.MAIN_CLASS = newClass.getQualifiedName();
        }
      }
      */
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      /*
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(myModule)) {
        if (config.MAIN_CLASS.equals(myOldClassName)) {
          config.MAIN_CLASS = oldQualifiedName;
        }
      }
      */
    }
  }

  private static abstract class FileRefactoringListener extends RefactoringElementAdapter {
    protected final Module myModule;
    protected final String myOldFilePath;

    public FileRefactoringListener(final Module module, final String oldFilePath) {
      myModule = module;
      myOldFilePath = oldFilePath;
    }

    public void elementRenamedOrMoved(@NotNull final PsiElement newElement) {
      final VirtualFile file = newElement instanceof PsiFile ? ((PsiFile)newElement).getVirtualFile() : null;
      if (file != null) {
        filePathChanged(file.getPath());
      }
    }

    @Override
    public void undoElementMovedOrRenamed(@NotNull PsiElement newElement, @NotNull String oldQualifiedName) {
      filePathChanged(oldQualifiedName);
    }

    protected abstract void filePathChanged(final String newFilePath);
  }

  private static class CssFileRefactoringListener extends FileRefactoringListener {
    public CssFileRefactoringListener(final Module module, final String oldFilePath) {
      super(module, oldFilePath);
    }

    protected void filePathChanged(final String newFilePath) {
      /*
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(myModule)) {
        for (int i = 0; i < config.CSS_FILES_LIST.size(); i++) {
          final String cssFilePath = config.CSS_FILES_LIST.get(i);
          if (Comparing.strEqual(myOldFilePath, cssFilePath)) {
            config.CSS_FILES_LIST.set(i, newFilePath);
          }
        }
      }
      */
    }
  }

  private static class XmlFileRefactoringListener extends FileRefactoringListener {
    public XmlFileRefactoringListener(final Module module, final String oldFilePath) {
      super(module, oldFilePath);
    }

    protected void filePathChanged(final String newFilePath) {
      /*
      for (final FlexBuildConfiguration config : FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(myModule)) {
        if (Comparing.strEqual(myOldFilePath, config.CUSTOM_CONFIG_FILE)) {
          config.CUSTOM_CONFIG_FILE = newFilePath;
        }

        if (Comparing.strEqual(myOldFilePath, config.CUSTOM_CONFIG_FILE_FOR_TESTS)) {
          config.CUSTOM_CONFIG_FILE_FOR_TESTS = newFilePath;
        }

        if (Comparing.strEqual(myOldFilePath, config.PATH_TO_SERVICES_CONFIG_XML)) {
          config.PATH_TO_SERVICES_CONFIG_XML = newFilePath;
        }

        for (int i = 0; i < config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.size(); i++) {
          final FlexBuildConfiguration.NamespaceAndManifestFileInfo info = config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.get(i);
          if (Comparing.strEqual(myOldFilePath, info.MANIFEST_FILE_PATH)) {
            info.MANIFEST_FILE_PATH = newFilePath;
          }
        }
      }
      */
    }
  }
}
