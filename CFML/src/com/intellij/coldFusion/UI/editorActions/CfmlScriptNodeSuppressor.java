/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.editorActions;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author vnikolaenko
 */
// TODO: to generalize with com.jetbrains.php.config.sdk.PhpDiagnosticScriptNodeSuppressor
public class CfmlScriptNodeSuppressor implements TreeStructureProvider, DumbAware {

  private static final Key<String> MARKER = Key.create(CfmlScriptNodeSuppressor.class.getName() + ".MARKER");

  public Collection<AbstractTreeNode> modify(AbstractTreeNode parent, Collection<AbstractTreeNode> children, ViewSettings settings) {
    ArrayList<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
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

  public Object getData(Collection<AbstractTreeNode> selected, String dataName) {
    return null;
  }

  public static void suppress(VirtualFile file) {
    ApplicationManager.getApplication().assertWriteAccessAllowed();
    file.putUserDataIfAbsent(MARKER, "");
  }

  private static boolean hasMarker(VirtualFile file) {
    return file.getUserData(MARKER) != null;
  }
}
