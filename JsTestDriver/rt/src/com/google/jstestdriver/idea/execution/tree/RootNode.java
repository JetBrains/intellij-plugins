package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * @author Sergey Simonchik
 */
public class RootNode extends AbstractJstdNode<ConfigNode> {

  public RootNode(@NotNull TreeManager treeManager) {
    super("<internal root node>", treeManager);
  }

}
