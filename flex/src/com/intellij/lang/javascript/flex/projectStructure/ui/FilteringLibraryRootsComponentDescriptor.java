package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.AttachRootButtonDescriptor;
import com.intellij.openapi.roots.libraries.ui.LibraryRootsComponentDescriptor;
import com.intellij.openapi.roots.libraries.ui.OrderRootTypePresentation;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.util.Condition;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
* User: ksafonov
*/
public class FilteringLibraryRootsComponentDescriptor extends LibraryRootsComponentDescriptor {

  private final LibraryRootsComponentDescriptor myDescriptor;
  private final OrderRootType[] myEditableRootTypes;

  public FilteringLibraryRootsComponentDescriptor(LibraryRootsComponentDescriptor descriptor, OrderRootType[] editableRootTypes) {
    myDescriptor = descriptor;
    myEditableRootTypes = editableRootTypes;
  }

  @Override
  public OrderRootType[] getRootTypes() {
    return myEditableRootTypes;
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
