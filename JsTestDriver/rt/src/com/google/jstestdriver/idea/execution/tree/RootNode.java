package com.google.jstestdriver.idea.execution.tree;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

/**
 * @author Sergey Simonchik
 */
public class RootNode extends AbstractJstdNode<BrowserNode> {

  public RootNode(@NotNull String name, @NotNull PrintStream outStream, @NotNull PrintStream errStream) {
    super(name, outStream, errStream);
  }

}
