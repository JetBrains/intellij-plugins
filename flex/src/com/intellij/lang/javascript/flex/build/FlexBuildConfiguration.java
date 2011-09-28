package com.intellij.lang.javascript.flex.build;

import com.intellij.ProjectTopics;
import com.intellij.facet.FacetManager;
import com.intellij.lang.javascript.flex.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ui.configuration.ModuleEditor;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ModuleStructureConfigurable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Dec 28, 2007
 * Time: 12:39:06 AM
 * To change this template use File | Settings | File Templates.
 */
@State(
  name = FlexBuildConfiguration.COMPONENT_NAME,
  storages = {
    @Storage(
      file = "$MODULE_FILE$"
    )
  }
)
public class FlexBuildConfiguration implements ModuleComponent, PersistentStateComponent<FlexBuildConfiguration>, Cloneable {

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

  private Type myType = Type.Default;
  public boolean DO_BUILD;
  public String OUTPUT_TYPE = APPLICATION;
  public boolean USE_DEFAULT_SDK_CONFIG_FILE = true;
  public boolean USE_CUSTOM_CONFIG_FILE = false;
  public String CUSTOM_CONFIG_FILE = "";
  public boolean USE_CUSTOM_CONFIG_FILE_FOR_TESTS = false;
  public String CUSTOM_CONFIG_FILE_FOR_TESTS = "";
  /** @deprecated */
  public String APPLICATION_ENTRY_POINT = "";
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
  public List<NamespaceAndManifestFileInfo> NAMESPACE_AND_MANIFEST_FILE_INFO_LIST = new ArrayList<NamespaceAndManifestFileInfo>();
  public List<ConditionalCompilationDefinition> CONDITIONAL_COMPILATION_DEFINITION_LIST = new ArrayList<ConditionalCompilationDefinition>();
  public List<String> CSS_FILES_LIST = new ArrayList<String>();
  public String ADDITIONAL_COMPILER_OPTIONS = "";
  private static final int OUR_CURRENT_VERSION = 3;
  public String PATH_TO_SERVICES_CONFIG_XML = "";
  public String CONTEXT_ROOT = "";

  public int VERSION;

  private Module myModule;
  private boolean isThisFlexFacetConfiguration;

  public FlexBuildConfiguration(final Module module) {
    myModule = module;
    isThisFlexFacetConfiguration = false;
  }

  /**
   * Default constructor doesn't initialize <code>myModule</code> field.
   * If this <code>FlexBuildConfiguration</code> is used for Flex facet make sure
   * to call {@link #facetInitialized(com.intellij.lang.javascript.flex.FlexFacet)} when facet gets initialized.
   */
  public FlexBuildConfiguration() {
  }

  /**
   * A hacky way to initialize <code>myModule</code> field for <code>FlexBuildConfiguration</code> of Flex facet.
   */
  public void facetInitialized(final FlexFacet flexFacet) {
    myModule = flexFacet.getModule();
    isThisFlexFacetConfiguration = true;
  }

  public static FlexBuildConfiguration getInstance(final Module module) {
    return module.getComponent(FlexBuildConfiguration.class);
  }

  public static FlexBuildConfiguration getInstance(final FlexFacet flexFacet) {
    return ((FlexFacetConfigurationImpl)flexFacet.getConfiguration()).getFlexBuildConfiguration();
  }

  public void projectOpened() {}

  public void projectClosed() {}

  public void moduleAdded() {}

  @NotNull
  public String getComponentName() {
    return "Flex.Build.Configuration";
  }

  public void initComponent() {
    if (ModuleType.get(myModule) instanceof FlexModuleType) {
      myModule.getMessageBus().connect(myModule).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
        public void beforeRootsChange(final ModuleRootEvent event) {
        }

        public void rootsChanged(final ModuleRootEvent event) {
          final Sdk sdk = FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(myModule);
          TargetPlayerUtils.updateTargetPlayerIfMajorOrMinorVersionDiffers(FlexBuildConfiguration.this, sdk);

          final ModuleEditor moduleEditor =
            FlexUtils.getModuleEditor(myModule, ModuleStructureConfigurable.getInstance(myModule.getProject()));
          if (moduleEditor != null) {
            // update UI
            moduleEditor.moduleCountChanged();
          }
        }
      });
    }
  }

  public void disposeComponent() {
  }

  public FlexBuildConfiguration getState() {
    // avoid storing component for non-Flex modules
    if (myModule != null && (ModuleType.get(myModule) instanceof FlexModuleType || isThisFlexFacetConfiguration)) {
      VERSION = OUR_CURRENT_VERSION;
    }
    return this;
  }

  public void loadState(final FlexBuildConfiguration state) {
    DO_BUILD = state.DO_BUILD;
    OUTPUT_FILE_NAME = state.OUTPUT_FILE_NAME;
    USE_FACET_COMPILE_OUTPUT_PATH = state.USE_FACET_COMPILE_OUTPUT_PATH;
    FACET_COMPILE_OUTPUT_PATH = state.FACET_COMPILE_OUTPUT_PATH;
    FACET_COMPILE_OUTPUT_PATH_FOR_TESTS = state.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS;
    OUTPUT_TYPE = state.OUTPUT_TYPE;
    USE_CUSTOM_CONFIG_FILE = state.USE_CUSTOM_CONFIG_FILE;
    CUSTOM_CONFIG_FILE = state.CUSTOM_CONFIG_FILE;
    USE_CUSTOM_CONFIG_FILE_FOR_TESTS = state.USE_CUSTOM_CONFIG_FILE_FOR_TESTS;
    CUSTOM_CONFIG_FILE_FOR_TESTS = state.CUSTOM_CONFIG_FILE_FOR_TESTS;
    MAIN_CLASS = state.MAIN_CLASS;
    ADDITIONAL_COMPILER_OPTIONS = state.ADDITIONAL_COMPILER_OPTIONS;
    STATIC_LINK_RUNTIME_SHARED_LIBRARIES = state.STATIC_LINK_RUNTIME_SHARED_LIBRARIES;
    INCLUDE_RESOURCE_FILES_IN_SWC = state.INCLUDE_RESOURCE_FILES_IN_SWC;
    TARGET_PLAYER_VERSION = state.TARGET_PLAYER_VERSION;
    if ("".equals(TARGET_PLAYER_VERSION) && myModule != null) {
      TARGET_PLAYER_VERSION = TargetPlayerUtils.getTargetPlayerVersion(FlexUtils.getFlexSdkForFlexModuleOrItsFlexFacets(myModule));
    }
    USE_LOCALE_SETTINGS = state.USE_LOCALE_SETTINGS;
    LOCALE = state.LOCALE;
    NAMESPACE_AND_MANIFEST_FILE_INFO_LIST = state.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST;
    CONDITIONAL_COMPILATION_DEFINITION_LIST = state.CONDITIONAL_COMPILATION_DEFINITION_LIST;
    CSS_FILES_LIST = state.CSS_FILES_LIST;
    PATH_TO_SERVICES_CONFIG_XML = state.PATH_TO_SERVICES_CONFIG_XML;
    CONTEXT_ROOT = state.CONTEXT_ROOT;
    USE_DEFAULT_SDK_CONFIG_FILE = state.USE_DEFAULT_SDK_CONFIG_FILE;


    VERSION = OUR_CURRENT_VERSION;

    if (state.VERSION == 0) {
      USE_CUSTOM_CONFIG_FILE = false;
      CUSTOM_CONFIG_FILE = "";
    }

    if (state.VERSION < 2) {
      OUTPUT_FILE_NAME = OUTPUT_FILE_NAME.substring(OUTPUT_FILE_NAME.lastIndexOf('/') + 1);
      OUTPUT_FILE_NAME = OUTPUT_FILE_NAME.substring(OUTPUT_FILE_NAME.lastIndexOf('\\') + 1);
    }

    if (state.VERSION < 3) {
      final int lastDot = state.APPLICATION_ENTRY_POINT.lastIndexOf(".");
      final int lastSlash = state.APPLICATION_ENTRY_POINT.lastIndexOf("/");
      if (lastDot > lastSlash) {
        MAIN_CLASS = state.APPLICATION_ENTRY_POINT.substring(lastSlash + 1, lastDot);
      }
    }
  }

  public String getCompileOutputPath() {
    return isThisFlexFacetConfiguration && USE_FACET_COMPILE_OUTPUT_PATH
           ? FACET_COMPILE_OUTPUT_PATH
           : VfsUtil.urlToPath(CompilerModuleExtension.getInstance(myModule).getCompilerOutputUrl());
  }

  public String getCompileOutputPathForTests() {
    return isThisFlexFacetConfiguration && USE_FACET_COMPILE_OUTPUT_PATH
           ? FACET_COMPILE_OUTPUT_PATH_FOR_TESTS
           : VfsUtil.urlToPath(CompilerModuleExtension.getInstance(myModule).getCompilerOutputUrlForTests());
  }

  public String getOutputFileFullPath() {
    return getCompileOutputPath() + "/" + OUTPUT_FILE_NAME;
  }

  public static Collection<FlexBuildConfiguration> getConfigForFlexModuleOrItsFlexFacets(final Module module) {
    final Collection<FlexBuildConfiguration> configurations = new ArrayList<FlexBuildConfiguration>();
    if (ModuleType.get(module) instanceof FlexModuleType) {
      configurations.add(getInstance(module));
    }
    else {
      final Collection<FlexFacet> flexFacets = FacetManager.getInstance(module).getFacetsByType(FlexFacet.ID);
      for (FlexFacet flexFacet : flexFacets) {
        configurations.add(getInstance(flexFacet));
      }
    }
    return configurations;
  }

  @Transient
  public Type getType() {
    return myType;
  }

  public void setType(Type type) {
    myType = type;
  }

  public Module getModule() {
    return myModule;
  }

  @Override
  public FlexBuildConfiguration clone() {
    final FlexBuildConfiguration clone;
    try {
      clone = (FlexBuildConfiguration)super.clone();

      clone.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST =
        new ArrayList<NamespaceAndManifestFileInfo>(NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.size());
      for (NamespaceAndManifestFileInfo namespaceAndManifestFileInfo : NAMESPACE_AND_MANIFEST_FILE_INFO_LIST) {
        clone.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.add(namespaceAndManifestFileInfo.clone());
      }

      clone.CONDITIONAL_COMPILATION_DEFINITION_LIST =
        new ArrayList<ConditionalCompilationDefinition>(CONDITIONAL_COMPILATION_DEFINITION_LIST.size());
      for (ConditionalCompilationDefinition conditionalCompilationDefinition : CONDITIONAL_COMPILATION_DEFINITION_LIST) {
        clone.CONDITIONAL_COMPILATION_DEFINITION_LIST.add(conditionalCompilationDefinition.clone());
      }

      clone.CSS_FILES_LIST = new ArrayList<String>(CSS_FILES_LIST);
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    return clone;
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
