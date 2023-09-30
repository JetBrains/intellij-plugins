// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlCacheProvider implements JdlModelEnum {
  NO("no"),
  CAFFEINE("caffeine"),
  EHCACHE("ehcache"),
  HAZELCAST("hazelcast"),
  INFINISPAN("infinispan"),
  MEMCACHED("memcached"),
  REDIS("redis");

  private final String id;

  JdlCacheProvider(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
