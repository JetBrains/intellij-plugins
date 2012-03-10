package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.ui.Util;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.AttachRootButtonDescriptor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.OrderRootTypePresentation;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
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
  public List<? extends RootDetector> getRootDetectors() {
    return Arrays.asList(new FlexSwcLibrariesRootDetector(),
                         new FlexDocsRootDetector(),
                         new FlexRawAsLibrariesRootDetector(),
                         new FlexSourcesRootDetector(),
                         new FlexSwcFoldersRootDetector());
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


