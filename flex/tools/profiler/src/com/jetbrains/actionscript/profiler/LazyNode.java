package com.jetbrains.actionscript.profiler;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: 10.12.10
 * Time: 15:19
 */
public abstract class LazyNode extends DefaultMutableTreeNode {
  private boolean myChildrenLoaded;

  @Override
  public int getChildCount() {
    loadChildren();
    return super.getChildCount();
  }

  @Override
  public TreeNode getChildAt(int index) {
    loadChildren();
    return super.getChildAt(index);
  }

  private void loadChildren() {
    if (!myChildrenLoaded) {
      myChildrenLoaded = true;
      doLoadChildren();
    }
  }

  protected abstract void doLoadChildren();
}
