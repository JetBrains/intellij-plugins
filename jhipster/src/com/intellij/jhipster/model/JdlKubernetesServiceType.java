// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlKubernetesServiceType implements JdlModelEnum {
  LOAD_BALANCER("LoadBalancer"),
  NODE_PORT("NodePort"),
  INGRESS("Ingress");

  private final String id;

  JdlKubernetesServiceType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
