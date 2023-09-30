// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlMessageBroker implements JdlModelEnum {
  FALSE("false"),
  KAFKA("kafka"),
  PULSAR("pulsar");

  private final String id;

  JdlMessageBroker(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
