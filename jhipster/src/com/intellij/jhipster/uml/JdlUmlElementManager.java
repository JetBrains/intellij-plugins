// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.uml;

import com.intellij.diagram.AbstractDiagramElementManager;
import com.intellij.diagram.DiagramBuilder;
import com.intellij.jhipster.JdlIconsMapping;
import com.intellij.jhipster.psi.JdlFile;
import com.intellij.jhipster.uml.model.*;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointerManager;
import com.intellij.ui.SimpleColoredText;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

final class JdlUmlElementManager extends AbstractDiagramElementManager<JdlNodeData> {
  @Override
  public @Nullable JdlNodeData findInDataContext(@NotNull DataContext dataContext) {
    var file = CommonDataKeys.PSI_FILE.getData(dataContext);
    if (!(file instanceof JdlFile)) return null;

    var virtualFile = file.getVirtualFile();
    if (virtualFile == null) return null;

    return getRootData(file.getProject(), virtualFile);
  }

  static @NotNull JdlDiagramRootData getRootData(Project project, VirtualFile virtualFile) {
    var disposable = project.getService(JdlDiagramService.class);
    var filePointer = VirtualFilePointerManager.getInstance().create(virtualFile, disposable, null);

    return new JdlDiagramRootData(filePointer);
  }

  @Override
  public Object @NotNull [] getNodeItems(JdlNodeData nodeElement) {
    if (nodeElement instanceof JdlEntityNodeData) {
      return ((JdlEntityNodeData)nodeElement).getProperties().toArray();
    }

    if (nodeElement instanceof JdlEnumNodeData) {
      return ((JdlEnumNodeData)nodeElement).getOptions().toArray();
    }

    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public boolean canBeBuiltFrom(@Nullable Object element) {
    return element instanceof JdlDiagramRootData || super.canBeBuiltFrom(element);
  }

  @Override
  public boolean isAcceptableAsNode(@Nullable Object o) {
    return o instanceof JdlEntityNodeData || o instanceof JdlEnumNodeData;
  }

  @Override
  public @Nullable SimpleColoredText getItemName(@Nullable JdlNodeData nodeElement,
                                                 @Nullable Object nodeItem,
                                                 @NotNull DiagramBuilder builder) {
    if (nodeItem instanceof JdlEntityNodeField) {
      return new SimpleColoredText(((JdlEntityNodeField)nodeItem).getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    if (nodeItem instanceof JdlEnumNodeItem) {
      return new SimpleColoredText(((JdlEnumNodeItem)nodeItem).getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

    return null;
  }

  @Override
  public @Nullable SimpleColoredText getItemType(@Nullable JdlNodeData nodeElement,
                                                 @Nullable Object nodeItem,
                                                 @Nullable DiagramBuilder builder) {
    if (nodeItem instanceof JdlEntityNodeField) {
      String type = ((JdlEntityNodeField)nodeItem).getType();
      if (type == null) return null;

      return new SimpleColoredText(type, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }
    return null;
  }

  @Override
  public @Nullable Icon getItemIcon(@Nullable JdlNodeData nodeElement, @Nullable Object nodeItem, @Nullable DiagramBuilder builder) {
    if (nodeItem instanceof JdlEntityNodeField) {
      if (((JdlEntityNodeField)nodeItem).isRequired()) {
        return JdlIconsMapping.getRequiredFieldIcon();
      }
      return JdlIconsMapping.getFieldIcon();
    }
    if (nodeItem instanceof JdlEnumNodeItem) {
      return JdlIconsMapping.getFieldIcon();
    }
    return null;
  }

  @Override
  public @Nullable @Nls String getElementTitle(JdlNodeData node) {
    return node.getName();
  }

  @Override
  public @Nullable @Nls String getNodeTooltip(JdlNodeData node) {
    return null;
  }
}
