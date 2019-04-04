package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractNodeWithParent<T extends AbstractNodeWithParent> extends AbstractNode<T> {

  private final AbstractNode<AbstractNodeWithParent<T>> myParent;
  private final String myName;

  public AbstractNodeWithParent(@NotNull String name, @NotNull AbstractNode parent) {
    super(parent.getTreeManager());
    myName = name;
    //noinspection unchecked
    myParent = (AbstractNode<AbstractNodeWithParent<T>>) parent;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public AbstractNode getParent() {
    return myParent;
  }

  @Nullable
  public abstract String getProtocolId();

  @Nullable
  public abstract String getLocationPath();

  @NotNull
  public abstract TCMessage createStartedMessage();

  public void detachFromParent() {
    myParent.removeChild(this);
  }
}
