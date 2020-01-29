package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.icons.AllIcons;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class DeviceTreeNode implements TreeNode {

  private final DeviceTreeNode parent;
  private final String name;
  private final TYPE type;
  private final String[] cliKeys;
  private List<DeviceTreeNode> children = Collections.emptyList();

  public DeviceTreeNode(@Nullable DeviceTreeNode parent, @NotNull TYPE type, @NotNull String name, @NotNull String... cliKeys) {
    this.parent = parent;
    this.name = name;
    this.type = type;
    this.cliKeys = cliKeys;
  }

  @Override
  @NotNull
  public DeviceTreeNode getChildAt(int childIndex) {
    return children.get(childIndex);
  }

  @Override
  public int getChildCount() {
    return children.size();
  }

  @NotNull
  public String getName() {
    return name;
  }

  @NotNull
  public String[] getCliKeys() {
    return cliKeys;
  }

  @Override
  @Nullable
  public DeviceTreeNode getParent() {
    return parent;
  }

  @NotNull
  public List<DeviceTreeNode> getChildren() {
    return children;
  }

  public void setChildren(@NotNull List<DeviceTreeNode> children) {
    this.children = children;
  }

  @Override
  public boolean getAllowsChildren() {
    return !children.isEmpty();
  }

  @Override
  public int getIndex(@NotNull TreeNode node) {
    //noinspection SuspiciousMethodCalls
    return children.indexOf(node);
  }

  @Override
  public boolean isLeaf() {
    return children.isEmpty();
  }

  @TestOnly
  public boolean hasSameValues(@NotNull String name, @NotNull TYPE type, @NotNull String... cliKeys) {
    if (!name.equals(this.name)) return false;
    if (!type.equals(this.type)) return false;
    return Arrays.equals(cliKeys, this.cliKeys);
  }

  @Override
  @NotNull
  public Enumeration<DeviceTreeNode> children() {
    return Collections.enumeration(children);
  }

  @NotNull
  public DeviceTreeNode add(DeviceTreeNode child) {
    if (children.isEmpty()) {
      children = new SmartList<>(child);
    }
    else {
      children.add(child);
    }
    return child;
  }

  @NotNull
  public TYPE getType() {
    return type;
  }

  public enum TYPE {
    ROOT,
    VENDOR(AllIcons.Gutter.Colors),
    BOARD(AllIcons.Actions.GroupBy),
    FRAMEWORK(AllIcons.General.GearPlain);
    private final Icon icon;

    TYPE(Icon icon) {
      this.icon = icon;
    }

    TYPE() {
      icon = null;
    }

    @Nullable
    public Icon getIcon() {
      return icon;
    }
  }

  @NotNull
  public static String searchText(@NotNull TreePath path) {
    Object[] pathArray = path.getPath();
    if (pathArray.length == 0) return "";
    StringBuilder builder = new StringBuilder(((DeviceTreeNode)pathArray[0]).getName());
    for (int i = 1; i < pathArray.length; i++) {
      DeviceTreeNode node = (DeviceTreeNode)pathArray[i];
      builder.append(' ').append(node.getName());
    }
    return builder.toString();
  }

  @Override
  @NotNull
  public String toString() {
    return getName();
  }
}
