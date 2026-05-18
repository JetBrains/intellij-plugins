package com.intellij.lang.javascript.linter.jshint.rhino;

import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.RhinoException;

/**
 * @author Sergey Simonchik
 */
public class WrappedRhinoException extends Exception {

  private final RhinoException myRhinoException;

  public WrappedRhinoException(@NotNull RhinoException cause) {
    super(cause);
    myRhinoException = cause;
  }

  public @NotNull RhinoException getRhinoException() {
    return myRhinoException;
  }

}
