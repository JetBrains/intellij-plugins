package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.EntityListManager;
import com.intellij.flex.uiDesigner.ProjectInfo;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.Consumer;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LibraryManager extends EntityListManager<VirtualFile, OriginalLibrary> {
  public static LibraryManager getInstance() {
    return ServiceManager.getService(LibraryManager.class);
  }

  public boolean isRegistered(@NotNull OriginalLibrary library) {
    return list.contains(library);
  }

  public int add(@NotNull OriginalLibrary library) {
    return list.add(library);
  }

  @NotNull
  public OriginalLibrary createOriginalLibrary(@NotNull final VirtualFile virtualFile, @NotNull final VirtualFile jarFile,
                                               @NotNull final Consumer<OriginalLibrary> initializer, boolean fromSdk) {
    if (list.contains(jarFile)) {
      return list.getInfo(jarFile);
    }
    else {
      final String path = virtualFile.getPath();
      OriginalLibrary library =
        new OriginalLibrary(virtualFile.getNameWithoutExtension() + "." + Integer.toHexString(path.hashCode()), jarFile, fromSdk);
      initializer.consume(library);
      return library;
    }
  }

  @Nullable
  public PropertiesFile getResourceBundleFile(String locale, String bundleName, ProjectInfo projectInfo) {
    for (Library library : projectInfo.getLibrarySet().getLibraries()) {
      if (library instanceof OriginalLibrary) {
        OriginalLibrary originalLibrary = (OriginalLibrary)library;
        if (originalLibrary.hasResourceBundles()) {
          final THashSet<String> bundles = originalLibrary.resourceBundles.get(locale);
          if (bundles.contains(bundleName)) {
            //noinspection ConstantConditions
            VirtualFile virtualFile = originalLibrary.getFile().findChild("locale").findChild(locale)
              .findChild(bundleName + CatalogXmlBuilder.PROPERTIES_EXTENSION);
            //noinspection ConstantConditions
            return (PropertiesFile)PsiDocumentManager.getInstance(projectInfo.getElement())
              .getPsiFile(FileDocumentManager.getInstance().getDocument(virtualFile));
          }
        }
      }
    }

    return null;
  }
}