// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlTestFramework implements JdlModelEnum {
  CYPRESS("cypress"),
  PROTRACTOR("protractor"),
  CUCUMBER("cucumber"),
  GATLING("gatling");

  private final String id;

  JdlTestFramework(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}