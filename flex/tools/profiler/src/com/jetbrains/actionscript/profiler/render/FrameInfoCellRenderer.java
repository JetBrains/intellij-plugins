package com.jetbrains.actionscript.profiler.render;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.actionscript.profiler.base.FrameInfoProducer;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.util.ResolveUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author: Fedor.Korotkov
 */
public class FrameInfoCellRenderer extends AbstractInfoCellRenderer {
  private final GlobalSearchScope scope;
  private Icon scopeIcon;
  private Icon nonScopeIcon;

  public FrameInfoCellRenderer(GlobalSearchScope scope) {
    this.scope = scope;
  }

  public void setScopeIcon(Icon scopeIcon) {
    this.scopeIcon = scopeIcon;
  }

  public void setNonScopeIcon(Icon nonScopeIcon) {
    this.nonScopeIcon = nonScopeIcon;
  }

  @Override
  protected void customizeCellRenderer(Object value) {
    if (value instanceof DefaultMutableTreeNode) {
      value = ((DefaultMutableTreeNode)value).getUserObject();
    }
    if (!(value instanceof FrameInfoProducer) && !(value instanceof FrameInfo)) {
      if (value != null && value.toString() != null) {
        append(value.toString());
      }
      return;
    }

    FrameInfo frameInfo;
    if (value instanceof FrameInfoProducer) {
      frameInfo = ((FrameInfoProducer)value).getFrameInfo();
    }
    else {
      frameInfo = (FrameInfo)value;
    }
    appendFrameInfo(frameInfo);

    boolean inScope;
    if (frameInfo.getFilePath() != null) {
      final VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(VfsUtil.pathToUrl(frameInfo.getFilePath()));
      inScope = vf != null && scope.accept(vf);
    }
    else {
      inScope = ResolveUtil.containsInScope(frameInfo.getQName(), scope);
    }
    setIcon(inScope ? scopeIcon : nonScopeIcon);
  }
}
