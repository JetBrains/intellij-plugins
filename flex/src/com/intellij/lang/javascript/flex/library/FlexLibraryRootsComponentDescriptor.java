package com.intellij.lang.javascript.flex.library;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileElement;
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
import java.util.Collections;
import java.util.List;

public class FlexLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {

  private static final Icon DOC_ICON = IconLoader.findIcon("documentation.png");

  public OrderRootTypePresentation getRootTypePresentation(@NotNull final OrderRootType type) {
    if (type instanceof JavadocOrderRootType) {
      return new OrderRootTypePresentation("Documentation", DOC_ICON);
    }
    return DefaultLibraryRootsComponentDescriptor.getDefaultPresentation(type);
  }

  @NotNull
  @Override
  public List<? extends RootDetector> getRootDetectors() {
    return Collections.emptyList();
  }

  @NotNull
  public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
    return Arrays.asList(new AddSwcDescriptor(), new AddSwcDirectoriesDescriptor(), new AddRawASLibraryDescriptor(),
                         new AddSwcSourcesDescriptor(), new AddDocDescriptor(), new AddDocUrlDescriptor());
  }

  private static class AddSwcDescriptor extends ChooserBasedAttachRootButtonDescriptor {
    private AddSwcDescriptor() {
      super(OrderRootType.CLASSES, FlexBundle.message("add.swc.files.button"));
    }

    public FileChooserDescriptor createChooserDescriptor() {
      return new FileChooserDescriptor(false, false, true, false, false, true) {
        public boolean isFileSelectable(final VirtualFile file) {
          return super.isFileSelectable(file) && "swc".equalsIgnoreCase(file.getExtension());
        }

        public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
          return super.isFileVisible(file, showHiddenFiles) && (file.isDirectory() || isFileSelectable(file));
        }
      };
    }

    public String getChooserTitle(final String libraryName) {
      return FlexBundle.message("select.swc.files");
    }

    public String getChooserDescription() {
      return null;
    }
  }

  private static class AddSwcDirectoriesDescriptor extends ChooserBasedAttachRootButtonDescriptor {
    private AddSwcDirectoriesDescriptor() {
      super(OrderRootType.CLASSES, FlexBundle.message("add.folder.with.swc.files.button"));
    }

    public FileChooserDescriptor createChooserDescriptor() {
      return new FileChooserDescriptor(false, true, false, false, false, true) {
        public boolean isFileVisible(final VirtualFile file, final boolean showHiddenFiles) {
          // SWC files visible for convenience, but not selectable
          return super.isFileVisible(file, showHiddenFiles) || "swc".equalsIgnoreCase(file.getExtension());
        }
      };
    }

    public boolean addAsJarDirectories() {
      return true;
    }

    public String getChooserTitle(final String libraryName) {
      return FlexBundle.message("select.folder.with.swc.files");
    }

    public String getChooserDescription() {
      return null;
    }
  }

  private static class AddRawASLibraryDescriptor extends ChooserBasedAttachRootButtonDescriptor {
    private AddRawASLibraryDescriptor() {
      super(OrderRootType.CLASSES, FlexBundle.message("add.raw.actionscript.libraries.button"));
    }

    public FileChooserDescriptor createChooserDescriptor() {
      return FileChooserDescriptorFactory.createMultipleFoldersDescriptor();
    }

    public String getChooserTitle(final String libraryName) {
      return FlexBundle.message("select.folder.with.raw.actionscript.sources");
    }

    public String getChooserDescription() {
      return null;
    }
  }

  private static class AddSwcSourcesDescriptor extends ChooserBasedAttachRootButtonDescriptor {
    private AddSwcSourcesDescriptor() {
      super(OrderRootType.SOURCES, FlexBundle.message("add.swc.sources.button"));
    }

    public String getChooserTitle(final String libraryName) {
      return FlexBundle.message("select.swc.sources");
    }

    public String getChooserDescription() {
      return null;
    }

    @Override
    public FileChooserDescriptor createChooserDescriptor() {
      return JARS_ZIPS_FOLDERS;
    }

    //@NotNull
    //public VirtualFile[] scanForActualRoots(@NotNull final VirtualFile[] rootCandidates, JComponent parent) {
    //  return PathUIUtils.scanAndSelectDetectedJavaSourceRoots(parent, rootCandidates);   TODO: implement for ActionScript (IDEA-47751)
    //}
  }

  private static class AddDocDescriptor extends ChooserBasedAttachRootButtonDescriptor {
    private AddDocDescriptor() {
      super(JavadocOrderRootType.getInstance(), FlexBundle.message("add.doc.button"));
    }

    public String getChooserTitle(final String libraryName) {
      return FlexBundle.message("select.doc");
    }

    public String getChooserDescription() {
      return null;
    }

    @Override
    public FileChooserDescriptor createChooserDescriptor() {
      return JARS_ZIPS_FOLDERS;
    }
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

  private static final FileChooserDescriptor JARS_ZIPS_FOLDERS = new FileChooserDescriptor(true, true, true, true, false, true) {

    @Override
    public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
      if(FileElement.isFileHidden(file)) {
        return false;
      }

      if (file.isDirectory()) {
        return true;
      }
      else {
        String name = file.getName();
        return name.endsWith(".zip") || name.endsWith(".jar");
      }
    }
  };
}


