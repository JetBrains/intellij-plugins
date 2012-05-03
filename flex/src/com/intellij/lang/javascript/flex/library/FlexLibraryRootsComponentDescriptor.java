package com.intellij.lang.javascript.flex.library;

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
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class FlexLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {

  private static final Icon DOC_ICON = IconLoader.findIcon("documentation.png");

  public OrderRootTypePresentation getRootTypePresentation(@NotNull final OrderRootType type) {
    if (type instanceof JavadocOrderRootType) {
      return new OrderRootTypePresentation(FlexBundle.message("documentation.order.root.type.name"), DOC_ICON);
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
    throw new UnsupportedOperationException("should not be called");
  }

  @NotNull
  @Override
  public FileChooserDescriptor createAttachFilesChooserDescriptor() {
    FileChooserDescriptor d = super.createAttachFilesChooserDescriptor();
    d.setDescription(FlexBundle.message("choose.library.files.description", ApplicationNamesInfo.getInstance().getFullProductName()));
    return d;
  }

  @NotNull
  public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
    return Arrays.asList(new AddDocUrlDescriptor());
  }

  private static class AddDocUrlDescriptor extends AttachRootButtonDescriptor {
    private AddDocUrlDescriptor() {
      super(JavadocOrderRootType.getInstance(), FlexBundle.message("add.doc.url.button"));
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


