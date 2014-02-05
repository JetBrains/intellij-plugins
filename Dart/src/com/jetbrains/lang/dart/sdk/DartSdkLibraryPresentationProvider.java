package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.roots.libraries.DummyLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryKind;
import com.intellij.openapi.roots.libraries.LibraryPresentationProvider;
import com.intellij.openapi.vfs.VirtualFile;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DartSdkLibraryPresentationProvider extends LibraryPresentationProvider<DummyLibraryProperties> {

  private static final LibraryKind KIND = LibraryKind.create("dart");

  protected DartSdkLibraryPresentationProvider() {
    super(KIND);
  }

  @Nullable
  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @Nullable
  public DummyLibraryProperties detect(@NotNull final List<VirtualFile> classesRoots) {
    for (VirtualFile root : classesRoots) {
      if (isDartSdkLibRoot(root)) {
        return DummyLibraryProperties.INSTANCE;
      }
    }
    return null;
  }

  static boolean isDartSdkLibRoot(final VirtualFile root) {
    return root.isInLocalFileSystem() &&
           root.isDirectory() &&
           root.getName().equals("lib") &&
           root.findFileByRelativePath("core/core.dart") != null;
  }
}
