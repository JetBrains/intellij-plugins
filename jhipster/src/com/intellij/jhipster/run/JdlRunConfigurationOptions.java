// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.run;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

final class JdlRunConfigurationOptions extends RunConfigurationOptions {
  private final StoredProperty<String> jhipsterLocation = string("").provideDelegate(this, "jhipsterLocation");
  private final StoredProperty<String> jdlFileLocation = string("").provideDelegate(this, "jdlLocation");
  private final StoredProperty<String> outputLocation = string("").provideDelegate(this, "outputLocation");

  // todo --force parameter

  public String getJHipsterLocation() {
    return jhipsterLocation.getValue(this);
  }

  public void setJhipsterLocation(String path) {
    jhipsterLocation.setValue(this, path);
  }

  public String getJdlLocation() {
    return jdlFileLocation.getValue(this);
  }

  public void setJdlLocation(String path) {
    jdlFileLocation.setValue(this, path);
  }

  public String getOutputLocation() {
    return outputLocation.getValue(this);
  }

  public void setOutputLocation(String path) {
    outputLocation.setValue(this, path);
  }
}
