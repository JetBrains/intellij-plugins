// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

// TODO: to generalize with com.jetbrains.php.config.sdk.PhpDiagnosticScriptNodeSuppressor
public final class CfmlScriptNodeSuppressor implements TreeStructureProvider, DumbAware {

  private static final Key<String> MARKER = Key.create(CfmlScriptNodeSuppressor.class.getName() + ".MARKER");

  @Override
  public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
    ArrayList<AbstractTreeNode<?>> result = new ArrayList<>();
    for (AbstractTreeNode child : children) {
      if (child.getValue() instanceof PsiFile) {
        VirtualFile file = ((PsiFile)child.getValue()).getVirtualFile();
        if (file != null && hasMarker(file)) {
          continue;
        }
      }
      result.add(child);
    }
    return result;
  }

  public static void suppress(VirtualFile file) {
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    file.putUserData(MARKER, "");
  }

  private static boolean hasMarker(VirtualFile file) {
    return file.getUserData(MARKER) != null;
  }
}
