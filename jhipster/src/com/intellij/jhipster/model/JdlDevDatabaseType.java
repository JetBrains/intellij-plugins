// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlDevDatabaseType implements JdlModelEnum {
  H2DISK("h2Disk"),
  H2MEMORY("h2Memory"),
  MYSQL("mysql"),
  MARIADB("mariadb"),
  MSSQL("mssql"),
  POSTGRESQL("postgresql"),
  ORACLE("oracle"),
  MONGODB("mongodb"),
  NO("no"),
  NEO4J("neo4j");

  private final String id;

  JdlDevDatabaseType(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
