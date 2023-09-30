// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.model;

@SuppressWarnings("unused")
public enum JdlLanguage implements JdlModelEnum {
  ALBANIAN("al"),
  ARABIC("ar-ly"),
  ARMENIAN("hy"),
  BELARUSIAN("by"),
  BENGALI("bn"),
  BULGARIAN("bg"),
  CATALAN("ca"),
  CHINESE("zh-tw"),
  CHINESE_SIMPLIFIED("zh-cn"),
  CROATIAN("hr"),
  CZECH("cs"),
  DANISH("da"),
  DUTCH("nl"),
  ENGLISH("en"),
  ESTONIAN("et"),
  FARSI("fa"),
  FINNISH("fi"),
  FRENCH("fr"),
  GALICIAN("gl"),
  GERMAN("de"),
  GREEK("el"),
  HINDI("hi"),
  HUNGARIAN("hu"),
  INDONESIAN("id"),
  ITALIAN("it"),
  JAPANESE("ja"),
  KOREAN("ko"),
  MARATHI("mr"),
  MYANMAR("my"),
  POLISH("po"),
  PORTUGUESE_BRAZILIAN("pt-br"),
  PORTUGUESE("pt-br"),
  PUNJABI("pa"),
  ROMANIAN("ro"),
  RUSSIAN("ru"),
  SLOVAK("sk"),
  SERBIAN("sr"),
  SINHALA("si"),
  SPANISH("es"),
  SWEDISH("sv"),
  TURKISH("tr"),
  TAMIL("ta"),
  TELUGU("te"),
  THAI("th"),
  UKRAINIAN("uk"),
  UZBEK_CYR("uz-Cyrl-uz"),
  UZBEK_LAT("uz-Latn-uz"),
  VIETNAMESE("vi");

  private final String id;

  JdlLanguage(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return id;
  }
}
