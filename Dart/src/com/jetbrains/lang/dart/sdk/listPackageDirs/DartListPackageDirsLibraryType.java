package com.jetbrains.lang.dart.sdk.listPackageDirs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.NewLibraryConfiguration;
import com.intellij.openapi.roots.libraries.PersistentLibraryKind;
import com.intellij.openapi.roots.libraries.ui.LibraryEditorComponent;
import com.intellij.openapi.roots.libraries.ui.LibraryPropertiesEditor;
import com.intellij.openapi.vfs.VirtualFile;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DartListPackageDirsLibraryType extends LibraryType<DartListPackageDirsLibraryProperties> {

  public static final PersistentLibraryKind<DartListPackageDirsLibraryProperties> LIBRARY_KIND =
    new PersistentLibraryKind<DartListPackageDirsLibraryProperties>("DartListPackageDirsLibraryType") {
      @NotNull
      public DartListPackageDirsLibraryProperties createDefaultProperties() {
        return new DartListPackageDirsLibraryProperties();
      }
    };

  protected DartListPackageDirsLibraryType() {
    super(LIBRARY_KIND);
  }

  @Nullable
  public String getCreateActionName() {
    return null;
  }

  @Nullable
  public NewLibraryConfiguration createNewLibrary(@NotNull final JComponent parentComponent,
                                                  @Nullable final VirtualFile contextDirectory,
                                                  @NotNull final Project project) {
    return null;
  }

  @Nullable
  public LibraryPropertiesEditor createPropertiesEditor(@NotNull final LibraryEditorComponent<DartListPackageDirsLibraryProperties> editorComponent) {
    return null;
  }

  @Nullable
  public Icon getIcon() {
    return DartIcons.Dart_16;
  }
}
