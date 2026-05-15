package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.util.NlsContexts.HintText;
import com.intellij.openapi.util.NlsContexts.Label;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;

/**
 * @author Sergey Simonchik
 */
public class JSHintTreeNode extends CheckedTreeNode {

  private final Tree myTree;
  private final @HintText String myDescription;
  private final @Label String myTitle;
  private Object myValue;
  private boolean myMouseInside = false;

  public JSHintTreeNode(@NotNull Tree tree, @NotNull JSHintOptionGroup optionGroup) {
    super(optionGroup);
    myTree = tree;
    myDescription = optionGroup.getDescription();
    myTitle = optionGroup.getTitle();
  }

  public JSHintTreeNode(@NotNull Tree tree, @NotNull JSHintOption option, @NotNull @HintText String description) {
    super(option);
    myTree = tree;
    myDescription = description;
    myTitle = option.getShortDescription();
    if (OptionTypes.isEnumOption(option)) {
      myValue = option.getDefaultValue();
    }
  }

  public @Label String getTitle() {
    return myTitle;
  }

  public @Nullable Object getValue() {
    return myValue;
  }

  public void setValue(@Nullable Object value) {
    JSHintOption option = getUserDataAsOption();
    if (value == null && option != null) {
      value = option.getDefaultValue();
    }
    myValue = value;
    myTree.getModel().valueForPathChanged(new TreePath(getPath()), getUserObject());
  }

  public void setMouseInside(boolean mouseInside) {
    boolean changed = myMouseInside != mouseInside;
    myMouseInside = mouseInside;
    if (changed) {
      myTree.repaint();
    }
  }

  public boolean isMouseInside() {
    return myMouseInside;
  }

  public @NotNull @HintText String getDescription() {
    return myDescription;
  }

  public @Nullable JSHintOption getUserDataAsOption() {
    return ObjectUtils.tryCast(getUserObject(), JSHintOption.class);
  }

  public @Nullable JSHintOptionGroup getUserDataAsOptionGroup() {
    return ObjectUtils.tryCast(getUserObject(), JSHintOptionGroup.class);
  }

  public boolean isEditLinkNeeded() {
    JSHintOption option = getUserDataAsOption();
    if (option == null) {
      return false;
    }
    return !OptionTypes.isBooleanOption(option);
  }

  @Override
  public String toString() {
    JSHintOption option = getUserDataAsOption();
    if (option != null) {
      return "option " + option.getKey();
    }
    JSHintOptionGroup optionGroup = getUserDataAsOptionGroup();
    if (optionGroup != null) {
      return "optionGroup " + optionGroup.getTitle();
    }
    return "internal error";
  }

}
