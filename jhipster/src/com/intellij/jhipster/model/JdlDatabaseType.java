// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlDatabaseType implements JdlModelEnum {
  NO("no"),
  SQL("sql"),
  CASSANDRA("cassandra"),
  COUCHBASE("couchbase"),
  MONGODB("mongodb"),
  NEO4J("neo4j");

  private final String id;

  JdlDatabaseType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
