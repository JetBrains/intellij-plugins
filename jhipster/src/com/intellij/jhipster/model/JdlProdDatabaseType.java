// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlProdDatabaseType implements JdlModelEnum {
  MYSQL("mysql"),
  MARIADB("mariadb"),
  MSSQL("mssql"),
  POSTGRESQL("postgresql"),
  ORACLE("oracle"),
  MONGODB("mongodb"),
  NO("no"),
  NEO4J("neo4j");

  private final String id;

  JdlProdDatabaseType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
