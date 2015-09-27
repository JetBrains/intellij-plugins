package com.google.jstestdriver.idea.rt.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.rt.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractNode<T extends AbstractNodeWithParent> {

  private final int myId;
  private final TreeManager myTreeManager;
  private Map<String, T> myChildByName = null;

  public AbstractNode(@NotNull TreeManager treeManager) {
    if (this instanceof RootNode) {
      myId = 0;
    } else {
      myId = treeManager.getNextNodeId();
    }
    myTreeManager = treeManager;
  }

  public int getId() {
    return myId;
  }

  @NotNull
  public TreeManager getTreeManager() {
    return myTreeManager;
  }

  public void addChild(@NotNull T child) {
    Map<String, T> map = myChildByName;
    if (map == null) {
      map = Maps.newHashMap();
      myChildByName = map;
    }
    map.put(child.getName(), child);
    TCMessage startedMessage = child.createStartedMessage();
    myTreeManager.printTCMessage(startedMessage);
  }

  @Nullable
  public T findChildByName(@NotNull String childName) {
    if (myChildByName == null) {
      return null;
    }
    return myChildByName.get(childName);
  }

  @NotNull
  public Collection<T> getChildren() {
    if (myChildByName == null) {
      return Collections.emptyList();
    }
    return myChildByName.values();
  }

  void removeChild(@NotNull T child) {
    myChildByName.remove(child.getName());
  }
}
