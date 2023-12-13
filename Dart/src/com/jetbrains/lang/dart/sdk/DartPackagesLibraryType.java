package com.jetbrains.lang.dart.sdk;

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

public final class DartPackagesLibraryType extends LibraryType<DartPackagesLibraryProperties> {

  public static final String DART_PACKAGES_LIBRARY_NAME = "Dart Packages";

  public static final PersistentLibraryKind<DartPackagesLibraryProperties> LIBRARY_KIND =
    new PersistentLibraryKind<>("DartPackagesLibraryType") {
      @Override
      @NotNull
      public DartPackagesLibraryProperties createDefaultProperties() {
        return new DartPackagesLibraryProperties();
      }
    };

  private DartPackagesLibraryType() {
    super(LIBRARY_KIND);
  }

  @Override
  @Nullable
  public String getCreateActionName() {
    return null;
  }

  @Override
  @Nullable
  public NewLibraryConfiguration createNewLibrary(@NotNull final JComponent parentComponent,
                                                  @Nullable final VirtualFile contextDirectory,
                                                  @NotNull final Project project) {
    return null;
  }

  @Override
  @Nullable
  public LibraryPropertiesEditor createPropertiesEditor(@NotNull final LibraryEditorComponent<DartPackagesLibraryProperties> editorComponent) {
    return null;
  }

  @Override
  @Nullable
  public Icon getIcon(DartPackagesLibraryProperties properties) {
    return DartIcons.Dart_16;
  }
}
