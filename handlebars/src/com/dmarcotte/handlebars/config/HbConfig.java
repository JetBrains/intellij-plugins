// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dmarcotte.handlebars.config.Property.*;

public final class HbConfig {

  public static boolean isAutoGenerateCloseTagEnabled() {
    return getBooleanPropertyValue(AUTO_GENERATE_CLOSE_TAG);
  }

  public static void setAutoGenerateCloseTagEnabled(boolean enabled) {
    setBooleanPropertyValue(AUTO_GENERATE_CLOSE_TAG, enabled);
  }

  public static boolean isAutocompleteMustachesEnabled() {
    return getBooleanPropertyValue(AUTOCOMPLETE_MUSTACHES);
  }

  public static void setAutocompleteMustachesEnabled(boolean enabled) {
    setBooleanPropertyValue(AUTOCOMPLETE_MUSTACHES, enabled);
  }

  public static boolean isFormattingEnabled() {
    return getBooleanPropertyValue(FORMATTER);
  }

  public static void setFormattingEnabled(boolean enabled) {
    setBooleanPropertyValue(FORMATTER, enabled);
  }

  public static boolean isAutoCollapseBlocksEnabled() {
    return getBooleanPropertyValue(AUTO_COLLAPSE_BLOCKS);
  }

  public static void setAutoCollapseBlocks(boolean enabled) {
    setBooleanPropertyValue(AUTO_COLLAPSE_BLOCKS, enabled);
  }

  @NotNull
  public static Language getCommenterLanguage() {
    final Language id = Language.findLanguageByID(getStringPropertyValue(COMMENTER_LANGUAGE_ID));
    return id == null ? HTMLLanguage.INSTANCE : id;
  }

  public static void setCommenterLanguage(Language language) {
    if (language == null) {
      setStringPropertyValue(COMMENTER_LANGUAGE_ID, null);
    }
    else {
      setStringPropertyValue(COMMENTER_LANGUAGE_ID, language.getID());
    }
  }

  public static String getRawOpenHtmlAsHandlebarsValue(Project project) {
    return getStringPropertyValue(SHOULD_OPEN_HTML, project);
  }

  public static boolean shouldOpenHtmlAsHandlebars(Project project) {
    String value = getRawOpenHtmlAsHandlebarsValue(project);
    return ENABLED.equals(value);
  }

  public static boolean setShouldOpenHtmlAsHandlebars(boolean value, Project project) {
    setBooleanPropertyValue(SHOULD_OPEN_HTML, value, project);
    return true;
  }

  private static String getStringPropertyValue(Property property, Project project) {
    return getProperties(project).getValue(property.getStringName(), property.getDefault());
  }

  @NotNull
  private static PropertiesComponent getProperties(@Nullable Project project) {
    return project == null ? PropertiesComponent.getInstance() : PropertiesComponent.getInstance(project);
  }

  private static void setStringPropertyValue(@NotNull Property property, @Nullable String value, @Nullable Project project) {
    getProperties(project).setValue(property.getStringName(), value, property.getDefault());
  }

  private static String getStringPropertyValue(Property property) {
    return getStringPropertyValue(property, null);
  }

  private static void setStringPropertyValue(Property property, String value) {
    setStringPropertyValue(property, value, null);
  }

  private static boolean getBooleanPropertyValue(Property property) {
    return ENABLED.equals(getStringPropertyValue(property));
  }

  private static void setBooleanPropertyValue(@NotNull Property property, boolean enabled, @Nullable Project project) {
    setStringPropertyValue(property, enabled ? ENABLED : DISABLED, project);
  }

  private static void setBooleanPropertyValue(Property property, boolean enabled) {
    setBooleanPropertyValue(property, enabled, null);
  }
}
