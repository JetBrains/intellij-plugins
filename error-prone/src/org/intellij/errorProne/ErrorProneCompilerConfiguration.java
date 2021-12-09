// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.errorProne;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerOptions;

@State(name = "ErrorProneCompilerSettings", storages = @Storage("compiler.xml"))
public class ErrorProneCompilerConfiguration implements PersistentStateComponent<JpsJavaCompilerOptions> {
  private final JpsJavaCompilerOptions mySettings = new JpsJavaCompilerOptions();

  @Override
  @NotNull
  public JpsJavaCompilerOptions getState() {
    return mySettings;
  }

  @Override
  public void loadState(@NotNull JpsJavaCompilerOptions state) {
    XmlSerializerUtil.copyBean(state, mySettings);
  }

  public static JpsJavaCompilerOptions getOptions(Project project) {
    return project.getService(ErrorProneCompilerConfiguration.class).getState();
  }}
