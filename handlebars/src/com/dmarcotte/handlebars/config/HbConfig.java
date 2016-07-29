package com.dmarcotte.handlebars.config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dmarcotte.handlebars.config.Property.*;

public class HbConfig {

  public static final String STRING_ARRAY_SEPARATOR = "\n";

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

  public static boolean isResolvePartialsPathsFromNameEnabled() {
    return getBooleanPropertyValue(RESOLVE_PARTIALS_PATHS);
  }

  public static void setResolvePartialsPathsFromNameEnabled(boolean enabled) {
    setBooleanPropertyValue(RESOLVE_PARTIALS_PATHS, enabled);
  }

  public static String[] getTemplatesLocations() {
    String templatesLocations = getStringPropertyValue(TEMPLATES_LOCATIONS);
    return getArrayFromString(templatesLocations);
  }

  public static void setTemplatesLocations(String templatesLocations) {
    setStringPropertyValue(TEMPLATES_LOCATIONS, templatesLocations);
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
    return new PropertyAccessor(getProperties(project)).getPropertyValue(property);
  }

  @NotNull
  private static PropertiesComponent getProperties(@Nullable Project project) {
    return project == null ? PropertiesComponent.getInstance() : PropertiesComponent.getInstance(project);
  }


  private static void setStringPropertyValue(@NotNull Property property, @Nullable String value, @Nullable Project project) {
    new PropertyAccessor(getProperties(project)).setPropertyValue(property, value);
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

  public static String[] getArrayFromString(String str) {
    return str.replaceAll(" ", "").split(STRING_ARRAY_SEPARATOR);
  }

  public static String[] getNormalizedTemplatesLocations() {
    String[] templatesLocations = getTemplatesLocations();
    String[] normalizedTemplatesLocations = new String[templatesLocations.length];

    for(int i = 0; i < templatesLocations.length; i++) {
      normalizedTemplatesLocations[i] = normalizeTemplateLocation(templatesLocations[i]);
    }

    return normalizedTemplatesLocations;
  }

  private static String normalizeTemplateLocation(String templateLocation) {
    templateLocation = templateLocation.startsWith("/") ? templateLocation : "/" + templateLocation;
    templateLocation = templateLocation.endsWith("/") ? templateLocation : templateLocation + "/";
    return templateLocation;
  }
}
