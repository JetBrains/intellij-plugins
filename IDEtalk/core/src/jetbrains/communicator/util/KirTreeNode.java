// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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

  @Override
  public int getChildCount() {
    return getChildNodes().size();
  }

  @Override
  public boolean getAllowsChildren() {
    return getChildCount() > 0;
  }

  @Override
  public boolean isLeaf() {
    return getChildCount() == 0;
  }

  @Override
  public Enumeration children() {
    return Collections.enumeration(getChildNodes());
  }

  @Override
  public TreeNode getParent() {
    return myParent;
  }

  @Override
  public TreeNode getChildAt(int childIndex) {
    return (TreeNode) getChildNodes().get(childIndex);
  }

  @Override
  public int getIndex(TreeNode node) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

}
