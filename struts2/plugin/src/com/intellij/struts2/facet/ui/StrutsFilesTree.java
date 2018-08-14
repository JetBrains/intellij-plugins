/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.facet.ui;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.pointers.VirtualFilePointer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.CheckboxTreeBase;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.xml.config.ConfigFilesTreeBuilder;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsFilesTree extends CheckboxTreeBase {

  public StrutsFilesTree() {
    super(new CheckboxTreeCellRendererBase() {
      @Override
      public void customizeRenderer(final JTree tree,
                                    final Object value,
                                    final boolean selected,
                                    final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
        ConfigFilesTreeBuilder.renderNode(value, expanded, getTextRenderer());
      }
    }, null);

    ConfigFilesTreeBuilder.installSearch(this);
  }

  public void updateFileSet(final StrutsFileSet fileSet) {
    final Set<VirtualFile> configured = new HashSet<>();
    TreeUtil.traverse((TreeNode)getModel().getRoot(), node -> {
      final CheckedTreeNode checkedTreeNode = (CheckedTreeNode)node;
      if (!checkedTreeNode.isChecked()) {
        return true;
      }
      final Object object = checkedTreeNode.getUserObject();
      VirtualFile virtualFile = null;
      if (object instanceof XmlFile) {
        virtualFile = ((XmlFile)object).getVirtualFile();
      }
      else if (object instanceof VirtualFile) {
        virtualFile = (VirtualFile)object;
      }
      if (virtualFile != null) {
        if (!fileSet.hasFile(virtualFile)) {
          fileSet.addFile(virtualFile);
        }
        configured.add(virtualFile);
      }
      return true;
    });

    for (Iterator<VirtualFilePointer> i = fileSet.getFiles().iterator(); i.hasNext(); ) {
      final VirtualFilePointer pointer = i.next();
      final VirtualFile file = pointer.getFile();
      if (file == null || !configured.contains(file)) {
        i.remove();
      }
    }
  }
}
