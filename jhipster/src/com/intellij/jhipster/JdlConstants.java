// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.openapi.util.NlsSafe;

import java.util.List;
import java.util.Map;

public final class JdlConstants {
  private JdlConstants() {
  }

  public static final @NlsSafe String APPLICATION_BASE_NAME = "baseName";
  public static final String APPLICATION_UNNAMED = "<unnamed>";

  public static final @NlsSafe String CONFIG_BLOCK_NAME = "config";
  public static final @NlsSafe String DEPLOYMENT_BLOCK_NAME = "deployment";

  public static final String TRUE = "true";
  public static final String FALSE = "false";

  public static final List<String> TOP_LEVEL_KEYWORDS = List.of(
    "application",
    "entity",
    "enum",
    "relationship",
    "dto",
    "deployment",
    "entities",
    "service",
    "microservice",
    "paginate",
    "search",
    "use"
  );

  public static final List<String> APPLICATION_NESTED_KEYWORDS = List.of(
    "config",
    "entities",
    "paginate",
    "dto"
  );

  public static final List<String> CONFIGURATION_OPTION_NESTED_KEYWORDS = List.of(
    "with",
    "except"
  );

  public static final List<String> RELATIONSHIP_NESTED_KEYWORDS = List.of(
    "to"
  );

  public static final List<String> RELATIONSHIP_OPTIONS = List.of(
    "Id",
    "OnDelete",
    "OnUpdate"
  );

  public static final List<String> RELATIONSHIP_OPTION_VALUES = List.of(
    "NO ACTION",
    "RESTRICT",
    "CASCADE",
    "SET NULL",
    "SET DEFAULT"
  );

  public static final List<String> RELATIONSHIP_TYPES = List.of(
    "OneToMany",
    "ManyToOne",
    "OneToOne",
    "ManyToMany"
  );

  public static final Map<String, String> FIELD_TYPES = Map.ofEntries(
    Map.entry("String", "java.lang.String"),
    Map.entry("Integer", "java.lang.Integer"),
    Map.entry("Long", "java.lang.Long"),
    Map.entry("Boolean", "java.lang.Boolean"),
    Map.entry("LocalDate", "java.time.LocalDate"),
    Map.entry("ZonedDateTime", "java.time.ZonedDateTime"),
    Map.entry("BigDecimal", "java.math.BigDecimal"),
    Map.entry("Float", "java.lang.Float"),
    Map.entry("Double", "java.lang.Double"),
    Map.entry("Instant", "java.time.Instant"),
    Map.entry("Duration", "java.time.Duration"),
    Map.entry("UUID", "java.util.UUID"),
    Map.entry("Blob", "java.sql.Blob"),
    Map.entry("AnyBlob", "java.sql.Blob"),
    Map.entry("ImageBlob", "java.sql.Blob"),
    Map.entry("TextBlob", "java.sql.Clob"),
    Map.entry("Date", "java.util.Date")
  );

  public static final String REQUIRED_FIELD_CONSTRAINT = "required";

  public static final List<String> FIELD_VALIDATIONS = List.of(
    REQUIRED_FIELD_CONSTRAINT,
    "unique",
    "minlength()",
    "maxlength()",
    "pattern()",
    "min()",
    "max()",
    "minbytes()",
    "maxbytes()"
  );

  public static final String USER_ENTITY_NAME = "User";

  public static final List<String> PREDEFINED_ENTITIES = List.of(
    USER_ENTITY_NAME,
    "Authority"
  );
}
