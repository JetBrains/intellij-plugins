// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlAuthenticationType implements JdlModelEnum {
  NO("no"),
  JWT("jwt"),
  SESSION("session"),
  OAUTH2("oauth2");

  private final String id;

  JdlAuthenticationType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
