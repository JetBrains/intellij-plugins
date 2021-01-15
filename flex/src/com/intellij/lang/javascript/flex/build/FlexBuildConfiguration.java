package com.intellij.lang.javascript.flex.build;

import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.List;

public class FlexBuildConfiguration {

  public static final String COMPONENT_NAME = "Flex.Build.Configuration";

  public enum Type {
    Default("idea-flex"), FlexUnit("idea-flexunit"), OverriddenMainClass("idea-flex-main-class");

    @NonNls private final String myConfigFilePrefix;

    Type(final @NonNls String configFilePrefix) {
      myConfigFilePrefix = configFilePrefix;
    }

    public String getConfigFilePrefix() {
      return myConfigFilePrefix;
    }
  }

  public static final @NonNls String APPLICATION = "Application";
  public static final @NonNls String LIBRARY = "Library";

  private static final Type myType = Type.Default;
  public boolean DO_BUILD;
  public String OUTPUT_TYPE = APPLICATION;
  public boolean USE_DEFAULT_SDK_CONFIG_FILE = true;
  public boolean USE_CUSTOM_CONFIG_FILE = false;
  public String CUSTOM_CONFIG_FILE = "";
  public boolean USE_CUSTOM_CONFIG_FILE_FOR_TESTS = false;
  public String CUSTOM_CONFIG_FILE_FOR_TESTS = "";
  public String MAIN_CLASS = "";
  public String OUTPUT_FILE_NAME = "";
  public boolean USE_FACET_COMPILE_OUTPUT_PATH = false;
  public String FACET_COMPILE_OUTPUT_PATH = "";
  public String FACET_COMPILE_OUTPUT_PATH_FOR_TESTS = "";
  public boolean INCLUDE_RESOURCE_FILES_IN_SWC = false;
  public String TARGET_PLAYER_VERSION = "";
  public boolean STATIC_LINK_RUNTIME_SHARED_LIBRARIES = true;
  public boolean USE_LOCALE_SETTINGS = false;
  public String LOCALE = "en_US"; // comma separated if more than one
  public List<NamespaceAndManifestFileInfo> NAMESPACE_AND_MANIFEST_FILE_INFO_LIST = new ArrayList<>();
  public List<ConditionalCompilationDefinition> CONDITIONAL_COMPILATION_DEFINITION_LIST = new ArrayList<>();
  public List<String> CSS_FILES_LIST = new ArrayList<>();
  public String ADDITIONAL_COMPILER_OPTIONS = "";
  private static final int OUR_CURRENT_VERSION = 3;
  public String PATH_TO_SERVICES_CONFIG_XML = "";
  public String CONTEXT_ROOT = "";

  public int VERSION;

  @Transient
  public Type getType() {
    return myType;
  }

  public static class NamespaceAndManifestFileInfo implements Cloneable {
    public String NAMESPACE = "http://";
    public String MANIFEST_FILE_PATH = "";
    public boolean INCLUDE_IN_SWC = true;

    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      NamespaceAndManifestFileInfo that = (NamespaceAndManifestFileInfo)o;

      if (INCLUDE_IN_SWC != that.INCLUDE_IN_SWC) return false;
      if (!MANIFEST_FILE_PATH.equals(that.MANIFEST_FILE_PATH)) return false;
      if (!NAMESPACE.equals(that.NAMESPACE)) return false;

      return true;
    }

    public int hashCode() {
      int result = NAMESPACE.hashCode();
      result = 31 * result + MANIFEST_FILE_PATH.hashCode();
      result = 31 * result + (INCLUDE_IN_SWC ? 1 : 0);
      return result;
    }

    @Override
    protected NamespaceAndManifestFileInfo clone() {
      try {
        return (NamespaceAndManifestFileInfo)super.clone();
      }
      catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static class ConditionalCompilationDefinition implements Cloneable {
    public String NAME = "";
    public String VALUE = "";

    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final ConditionalCompilationDefinition that = (ConditionalCompilationDefinition)o;

      if (!NAME.equals(that.NAME)) return false;
      if (!VALUE.equals(that.VALUE)) return false;

      return true;
    }

    public int hashCode() {
      int result = NAME.hashCode();
      result = 31 * result + VALUE.hashCode();
      return result;
    }

    @Override
    protected ConditionalCompilationDefinition clone() {
      try {
        return (ConditionalCompilationDefinition)super.clone();
      }
      catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
