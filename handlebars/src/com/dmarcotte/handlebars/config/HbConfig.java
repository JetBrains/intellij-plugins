package com.dmarcotte.handlebars.config;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.lang.Language;

import static com.dmarcotte.handlebars.config.Property.*;

public class HbConfig {

  public static boolean isAutoGenerateCloseTagEnabled() {
    return getBooleanPropertyValue(AUTO_GENERATE_CLOSE_TAG);
  }

  public static void setAutoGenerateCloseTagEnabled(boolean enabled) {
    setBooleanPropertyValue(AUTO_GENERATE_CLOSE_TAG, enabled);
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

  public static Language getCommenterLanguage() {
    return Language.findLanguageByID(getStringPropertyValue(COMMENTER_LANGUAGE_ID));
  }

  public static void setCommenterLanguage(Language language) {
    if (language == null) {
      setStringPropertyValue(COMMENTER_LANGUAGE_ID, null);
    }
    else {
      setStringPropertyValue(COMMENTER_LANGUAGE_ID, language.getID());
    }
  }

  private static String getStringPropertyValue(Property property) {
    return new PropertyAccessor(PropertiesComponent.getInstance())
      .getPropertyValue(property);
  }

  private static void setStringPropertyValue(Property property, String value) {
    new PropertyAccessor(PropertiesComponent.getInstance())
      .setPropertyValue(property, value);
  }

  private static boolean getBooleanPropertyValue(Property property) {
    return ENABLED.equals(getStringPropertyValue(property));
  }

  private static void setBooleanPropertyValue(Property property, boolean enabled) {
    setStringPropertyValue(property, enabled ? ENABLED : DISABLED);
  }
}
