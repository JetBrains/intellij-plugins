package com.jetbrains.actionscript.profiler.base;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * @author: Fedor.Korotkov
 */
public class NavigatableTree extends JTree implements DataProvider {
  @Override
  @Nullable
  public Object getData(@NonNls String dataId) {
    if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
      return navigatableSelectedItem();
    }
    return null;
  }

  @Nullable
  private Navigatable navigatableSelectedItem() {
    final TreePath path = getSelectionPath();
    if (path == null) {
      return null;
    }
    final Object component = path.getLastPathComponent();
    if (component instanceof NavigatableDataProducer) {
      return ((NavigatableDataProducer)component).getNavigatable();
    }
    return null;
  }
}
