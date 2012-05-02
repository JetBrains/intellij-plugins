package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Maps;
import com.google.jstestdriver.idea.execution.tc.TC;
import com.google.jstestdriver.idea.execution.tc.TCMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractJstdNode<T extends AbstractJstdNode> {

  private final String myName;
  private final PrintStream myOutStream;
  private final PrintStream myErrStream;
  private Map<String, T> myChildByName = null;

  public AbstractJstdNode(@NotNull String name, @NotNull AbstractJstdNode parent) {
    this(name, parent.getOutStream(), parent.getErrStream());
  }

  public AbstractJstdNode(@NotNull String name, @NotNull PrintStream outStream, @NotNull PrintStream errStream) {
    myName = name;
    myOutStream = outStream;
    myErrStream = errStream;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  public void addChild(@NotNull T child) {
    Map<String, T> map = getChildByNameMap();
    map.put(child.getName(), child);
    if (child instanceof TestNode) {
      TCMessage message = TC.testStarted(child.getName());
      message.print(myOutStream);
    } else {
      TCMessage message = TC.testSuiteStarted(child.getName());
      message.print(myOutStream);
    }
  }

  @NotNull
  public Map<String, T> getChildByNameMap() {
    if (myChildByName == null) {
      myChildByName = Maps.newHashMap();
    }
    return myChildByName;
  }

  @Nullable
  public T findChildByName(@NotNull String childName) {
    return getReadOnlyChildren().get(childName);
  }

  @NotNull
  public PrintStream getOutStream() {
    return myOutStream;
  }

  @NotNull
  public PrintStream getErrStream() {
    return myErrStream;
  }

  @NotNull
  private Map<String, T> getReadOnlyChildren() {
    if (myChildByName == null) {
      return Collections.emptyMap();
    }
    return myChildByName;
  }

  @NotNull
  public Collection<T> getChildren() {
    return getReadOnlyChildren().values();
  }

}
