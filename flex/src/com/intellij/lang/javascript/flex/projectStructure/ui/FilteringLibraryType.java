package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryType;
import com.intellij.openapi.roots.libraries.ui.*;
import com.intellij.openapi.roots.ui.configuration.FacetsProvider;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * User: ksafonov
 */
class FilteringLibraryType<P extends LibraryProperties> extends LibraryType<P> {
  private final LibraryType<P> myLibraryType;
  private final OrderRootType[] myEditableRootTypes;

  public FilteringLibraryType(LibraryType<P> libraryType, OrderRootType[] editableRootTypes) {
    super(libraryType.getKind());
    myLibraryType = libraryType;
    myEditableRootTypes = editableRootTypes;
  }

  @Override
  public String getCreateActionName() {
    return myLibraryType.getCreateActionName();
  }

  @NotNull
  @Override
  public P createDefaultProperties() {
    return myLibraryType.createDefaultProperties();
  }

  @Override
  public LibraryPropertiesEditor createPropertiesEditor(@NotNull LibraryEditorComponent<P> libraryEditorComponent) {
    return myLibraryType.createPropertiesEditor(libraryEditorComponent);
  }

  @Override
  public Icon getIcon() {
    return myLibraryType.getIcon();
  }

  @Override
  public boolean isSuitableModule(@NotNull Module module, @NotNull FacetsProvider facetsProvider) {
    return myLibraryType.isSuitableModule(module, facetsProvider);
  }

  @Override
  public LibraryRootsComponentDescriptor createLibraryRootsComponentDescriptor() {
    return new FilteringLibraryRootsComponentDescriptor(myLibraryType.createLibraryRootsComponentDescriptor());
  }

  @Override
  public P detect(@NotNull List<VirtualFile> classesRoots) {
    return myLibraryType.detect(classesRoots);
  }

  @Override
  public OrderRootType[] getExternalRootTypes() {
    return myLibraryType.getExternalRootTypes();
  }

  @Override
  public OrderRootType[] getAdditionalRootTypes() {
    return myLibraryType.getAdditionalRootTypes();
  }

  private class FilteringLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {

    private final LibraryRootsComponentDescriptor myDescriptor;

    public FilteringLibraryRootsComponentDescriptor(LibraryRootsComponentDescriptor descriptor) {
      myDescriptor = descriptor;
    }

    @Override
    public OrderRootTypePresentation getRootTypePresentation(@NotNull OrderRootType type) {
      return myDescriptor.getRootTypePresentation(type);
    }

    @NotNull
    @Override
    public List<? extends RootDetector> getRootDetectors() {
      return ContainerUtil.filter(myDescriptor.getRootDetectors(), new Condition<RootDetector>() {
        @Override
        public boolean value(RootDetector rootDetector) {
          return ArrayUtil.contains(rootDetector.getRootType(), myEditableRootTypes);
        }
      });
    }

    @NotNull
    @Override
    public List<? extends AttachRootButtonDescriptor> createAttachButtons() {
      return ContainerUtil.filter(myDescriptor.createAttachButtons(), new Condition<AttachRootButtonDescriptor>() {
        @Override
        public boolean value(AttachRootButtonDescriptor attachRootButtonDescriptor) {
          return ArrayUtil.contains(attachRootButtonDescriptor.getRootType(), myEditableRootTypes);
        }
      });
    }
  }
}
