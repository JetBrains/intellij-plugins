package com.jetbrains.actionscript.profiler.base;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * @author: Fedor.Korotkov
 */
public class NavigatableTree extends JTree implements DataProvider {
  @Override
  public Object getData(@NonNls String dataId) {
    if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
      return navigatableSelectedItem();
    }
    return null;
  }

  private Navigatable navigatableSelectedItem() {
    final TreePath path = getSelectionPath();
    final Object component = path.getLastPathComponent();
    if (component instanceof Navigatable) {
      return (Navigatable)component;
    }
    if (component instanceof NavigatableDataProducer) {
      return ((NavigatableDataProducer)component).getNavigatableData();
    }
    return null;
  }
}
