/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package jetbrains.communicator.util;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Kir
 */
public abstract class KirTreeNode implements TreeNode {
  private final TreeNode myParent;

  protected KirTreeNode(TreeNode parent) {
    myParent = parent;
  }

  protected abstract List getChildNodes();
  protected abstract Component renderIn(JLabel label, boolean selected, boolean hasFocus);

  public int getChildCount() {
    return getChildNodes().size();
  }

  public boolean getAllowsChildren() {
    return getChildCount() > 0;
  }

  public boolean isLeaf() {
    return getChildCount() == 0;
  }

  public Enumeration children() {
    return Collections.enumeration(getChildNodes());
  }

  public TreeNode getParent() {
    return myParent;
  }

  public TreeNode getChildAt(int childIndex) {
    return (TreeNode) getChildNodes().get(childIndex);
  }

  public int getIndex(TreeNode node) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

}
