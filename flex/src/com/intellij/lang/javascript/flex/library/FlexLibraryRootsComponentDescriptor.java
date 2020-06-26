// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.library;

import com.intellij.icons.AllIcons;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.ui.Util;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.DefaultLibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.UIBundle;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class FlexLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {

  @Override
  public OrderRootTypePresentation getRootTypePresentation(@NotNull final OrderRootType type) {
    if (type instanceof JavadocOrderRootType) {
      return new OrderRootTypePresentation(FlexBundle.message("documentation.order.root.type.name"), AllIcons.FileTypes.Text);
    }
    return DefaultLibraryRootsComponentDescriptor.getDefaultPresentation(type);
  }

  @NotNull
  @Override
  public LibraryRootsDetector getRootsDetector() {
    return new FlexLibraryRootsDetector();
  }

  @NotNull
  @Override
  public List<? extends RootDetector> getRootDetectors() {
    return FlexLibraryRootsDetector.getRootDetectors();
  }

  @NotNull
  @Override
  public FileChooserDescriptor createAttachFilesChooserDescriptor(String libraryName) {
    FileChooserDescriptor d = super.createAttachFilesChooserDescriptor(libraryName);
    d.setTitle(UIBundle.message("file.chooser.default.title"));
    d.setDescription(FlexBundle.message("choose.library.files.description", ApplicationNamesInfo.getInstance().getFullProductName()));
    return d;
  }

  @Override
  public String getAttachFilesActionName() {
    return FlexBundle.message("add.library.components.action.name");
  }

  @Override
  @NotNull
  public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
    return Collections.singletonList(new AddDocUrlDescriptor());
  }

  private static final class AddDocUrlDescriptor extends AttachRootButtonDescriptor {
    private AddDocUrlDescriptor() {
      super(JavadocOrderRootType.getInstance(), IconUtil.getAddLinkIcon(), FlexBundle.message("add.doc.url.button"));
    }

    @Override
    public VirtualFile[] selectFiles(@NotNull JComponent parent,
                                     @Nullable VirtualFile initialSelection,
                                     @Nullable Module contextModule,
                                     @Nullable LibraryEditor libraryEditor) {
      final VirtualFile vFile = Util.showSpecifyJavadocUrlDialog(parent);
      if (vFile != null) {
        return new VirtualFile[]{vFile};
      }
      return VirtualFile.EMPTY_ARRAY;
    }
  }
}


