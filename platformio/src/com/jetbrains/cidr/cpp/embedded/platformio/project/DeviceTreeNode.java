package com.jetbrains.cidr.cpp.embedded.platformio.project;

import com.intellij.icons.AllIcons;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class DeviceTreeNode implements TreeNode {

  private final DeviceTreeNode myParent;
  private final String myName;
  private final TYPE myType;
  private final BoardInfo myBoardInfo;
  private List<DeviceTreeNode> myChildren = Collections.emptyList();

  public DeviceTreeNode(@Nullable DeviceTreeNode parent, @NotNull TYPE type, @NotNull String name, @NotNull BoardInfo boardInfo) {
    this.myParent = parent;
    this.myName = name;
    this.myType = type;
    this.myBoardInfo = boardInfo;
  }

  @Override
  @NotNull
  public DeviceTreeNode getChildAt(int childIndex) {
    return myChildren.get(childIndex);
  }

  @Override
  public int getChildCount() {
    return myChildren.size();
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public BoardInfo getBoardInfo() {
    return myBoardInfo;
  }

  @Override
  @Nullable
  public DeviceTreeNode getParent() {
    return myParent;
  }

  public void setChildren(@NotNull List<DeviceTreeNode> children) {
    this.myChildren = children;
  }

  @Override
  public boolean getAllowsChildren() {
    return !myChildren.isEmpty();
  }

  @Override
  public int getIndex(@NotNull TreeNode node) {
    return myChildren.indexOf(node);
  }

  @Override
  public boolean isLeaf() {
    return myChildren.isEmpty();
  }

  @TestOnly
  public boolean hasSameValues(@NotNull String name, @NotNull TYPE type, @NotNull BoardInfo boardInfo) {
    if (!name.equals(this.myName)) return false;
    if (!type.equals(this.myType)) return false;
    return Objects.equals(boardInfo, this.myBoardInfo);
  }

  @Override
  @NotNull
  public Enumeration<DeviceTreeNode> children() {
    return Collections.enumeration(myChildren);
  }

  @NotNull
  public DeviceTreeNode add(DeviceTreeNode child) {
    if (myChildren.isEmpty()) {
      myChildren = new SmartList<>(child);
    }
    else {
      myChildren.add(child);
    }
    return child;
  }

  @NotNull
  public TYPE getType() {
    return myType;
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
