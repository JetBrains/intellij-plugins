package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.EntityListManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

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
}