package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.execution.tc.TC;
import com.google.jstestdriver.idea.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractJstdNode<T extends AbstractJstdNode> {

  private final int myId;
  private final String myName;
  private final TreeManager myTreeManager;
  private Map<String, T> myChildByName = null;

  public AbstractJstdNode(@NotNull String name,
                          @NotNull TreeManager treeManager) {
    if (this instanceof RootNode) {
      myId = 0;
    } else {
      myId = treeManager.getNextNodeId();
    }
    myName = name;
    myTreeManager = treeManager;
  }

  public int getId() {
    return myId;
  }

  @NotNull
  public String getName() {
    return myName;
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
    if (child instanceof TestNode) {
      TCMessage message = TC.testStarted((TestNode) child);
      myTreeManager.printTCMessage(message);
    } else {
      TCMessage message = TC.testSuiteStarted((AbstractSuiteNode) child);
      myTreeManager.printTCMessage(message);
    }
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

}
