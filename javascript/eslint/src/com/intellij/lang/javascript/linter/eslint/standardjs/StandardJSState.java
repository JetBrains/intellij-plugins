package com.intellij.lang.javascript.linter.eslint.standardjs;

import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import org.jetbrains.annotations.NotNull;

public class StandardJSState implements JSNpmLinterState<StandardJSState> {

  public static final StandardJSState DEFAULT = new StandardJSState(new NodePackage(""));

  private final @NotNull NodePackage myNodePackage;
  private final @NotNull NodePackageRef myNodePackageRef;

  public StandardJSState(@NotNull NodePackage nodePackage) {
    myNodePackageRef = NodePackageRef.create(nodePackage);
    myNodePackage = nodePackage;
  }

  public boolean isDefault() {
    return myNodePackage.isEmptyPath();
  }

  public @NotNull NodePackage getNodePackage() {
    return myNodePackage;
  }

  @Override
  public @NotNull NodePackageRef getNodePackageRef() {
    return myNodePackageRef;
  }

  @Override
  public StandardJSState withLinterPackage(@NotNull NodePackageRef nodePackage) {
    NodePackage constantPackage = nodePackage.getConstantPackage();
    assert constantPackage != null : this.getClass().getSimpleName() + " does not support non-constant package refs";
    return new StandardJSState(constantPackage);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!myNodePackage.equals(((StandardJSState)o).myNodePackage)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myNodePackage.hashCode();
  }

  @Override
  public String toString() {
    return "StandardJSState{" +
           "myPackagePath='" + myNodePackage + '\'' +
           '}';
  }
}