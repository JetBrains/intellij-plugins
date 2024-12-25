// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
      public @NotNull DartPackagesLibraryProperties createDefaultProperties() {
        return new DartPackagesLibraryProperties();
      }
    };

  private DartPackagesLibraryType() {
    super(LIBRARY_KIND);
  }

  @Override
  public @Nullable String getCreateActionName() {
    return null;
  }

  @Override
  public @Nullable NewLibraryConfiguration createNewLibrary(final @NotNull JComponent parentComponent,
                                                            final @Nullable VirtualFile contextDirectory,
                                                            final @NotNull Project project) {
    return null;
  }

  @Override
  public @Nullable LibraryPropertiesEditor createPropertiesEditor(final @NotNull LibraryEditorComponent<DartPackagesLibraryProperties> editorComponent) {
    return null;
  }

  @Override
  public @Nullable Icon getIcon(DartPackagesLibraryProperties properties) {
    return DartIcons.Dart_16;
  }
}
