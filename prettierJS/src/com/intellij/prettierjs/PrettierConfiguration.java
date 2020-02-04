// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.nodejs.util.NodePackageDescriptor;
import com.intellij.javascript.nodejs.util.NodePackageRef;
import com.intellij.lang.javascript.linter.JSNpmLinterState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PrettierConfiguration implements JSNpmLinterState {
  @NotNull
  private final Project myProject;
  private static final String NODE_INTERPRETER_PROPERTY = "prettierjs.PrettierConfiguration.NodeInterpreter";
  private static final String PACKAGE_PROPERTY = "prettierjs.PrettierConfiguration.Package";
  private static final String OLD_INTERPRETER_PROPERTY = "node.js.path.for.package.prettier";
  private static final NodePackageDescriptor PKG_DESC = new NodePackageDescriptor(PrettierUtil.PACKAGE_NAME);

  public PrettierConfiguration(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  public static PrettierConfiguration getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, PrettierConfiguration.class);
  }

  @Override
  @NotNull
  public NodeJsInterpreterRef getInterpreterRef() {
    return NodeJsInterpreterRef.create(ObjectUtils.coalesce(PropertiesComponent.getInstance(myProject).getValue(NODE_INTERPRETER_PROPERTY),
                                                            PropertiesComponent.getInstance(myProject).getValue(OLD_INTERPRETER_PROPERTY)));
  }

  @NotNull
  @Override
  public NodePackageRef getNodePackageRef() {
    return NodePackageRef.create(getPackage());
  }

  @Override
  public JSNpmLinterState withLinterPackage(@NotNull NodePackageRef nodePackage) {
    NodePackage newPackage = nodePackage.getConstantPackage();
    assert newPackage != null : getClass().getSimpleName() + "does not support non-constant package";
    update(this.getInterpreterRef(), newPackage);
    return null;
  }

  @Override
  public JSNpmLinterState withInterpreterRef(NodeJsInterpreterRef ref) {
    update(ref, this.getPackage());
    return this;
  }

  @NotNull
  public NodePackage getPackage() {
    String value = PropertiesComponent.getInstance(myProject).getValue(PACKAGE_PROPERTY);
    if (value != null) {
      return PKG_DESC.createPackage(value);
    }
    NodePackage pkg = PKG_DESC.findUnambiguousDependencyPackage(myProject);
    if (pkg != null) {
      if (pkg.isValid(myProject)) {
        PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, pkg.getSystemDependentPath());
      }
      return pkg;
    }
    return PKG_DESC.createPackage("");
  }

  public void update(@NotNull NodeJsInterpreterRef interpreterRef, @Nullable NodePackage nodePackage) {
    PropertiesComponent.getInstance(myProject).setValue(NODE_INTERPRETER_PROPERTY, interpreterRef.getReferenceName());
    PropertiesComponent.getInstance(myProject).setValue(PACKAGE_PROPERTY, nodePackage != null ? nodePackage.getSystemDependentPath() : null);
  }
}
