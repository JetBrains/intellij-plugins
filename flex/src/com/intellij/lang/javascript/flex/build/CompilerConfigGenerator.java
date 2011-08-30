package com.intellij.lang.javascript.flex.build;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeProjectLevelCompilerOptionsHolder;
import com.intellij.lang.javascript.flex.projectStructure.ValueSource;
import com.intellij.lang.javascript.flex.projectStructure.options.CompilerOptions;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformUtils;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jdom.Element;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompilerConfigGenerator {

  private final Module myModule;
  private final String mySdkRootPath;
  private final String mySdkVersion;
  private final FlexIdeBuildConfiguration myConfig;
  private final CompilerOptions myModuleLevelCompilerOptions;
  private final CompilerOptions myProjectLevelCompilerOptions;

  private CompilerConfigGenerator(final Module module,
                                  final String sdkRootPath,
                                  final String sdkVersion,
                                  final FlexIdeBuildConfiguration config) {
    this.myModule = module;
    this.mySdkRootPath = sdkRootPath;
    this.mySdkVersion = sdkVersion;
    this.myConfig = config;
    myModuleLevelCompilerOptions = FlexIdeBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions();
    myProjectLevelCompilerOptions =
      FlexIdeProjectLevelCompilerOptionsHolder.getInstance(module.getProject()).getProjectLevelCompilerOptions();
  }

  /**
   * called from tests via reflection
   */
  private CompilerConfigGenerator(final Module module,
                                  final String sdkRootPath,
                                  final String sdkVersion,
                                  final FlexIdeBuildConfiguration config,
                                  final CompilerOptions moduleLevelCompilerOptions,
                                  final CompilerOptions projectLevelCompilerOptions) {
    assert ApplicationManager.getApplication().isUnitTestMode();
    myModule = module;
    mySdkRootPath = sdkRootPath;
    mySdkVersion = sdkVersion;
    myConfig = config;
    myModuleLevelCompilerOptions = moduleLevelCompilerOptions;
    myProjectLevelCompilerOptions = projectLevelCompilerOptions;
  }

  public static VirtualFile getOrCreateConfigFile(final Module module,
                                                  final String sdkRootPath,
                                                  final String sdkVersion,
                                                  final FlexIdeBuildConfiguration config) throws IOException {
    final String text = new CompilerConfigGenerator(module, sdkRootPath, sdkVersion, config).generateConfigFileText();
    final String name = FlexCompilerHandler.generateConfigFileName(module, config.NAME, PlatformUtils.getPlatformPrefix(), null);
    return FlexCompilationUtils.getOrCreateConfigFile(module.getProject(), name, text);
  }

  private String generateConfigFileText() {
    final Element rootElement =
      new Element(FlexCompilerConfigFileUtil.FLEX_CONFIG, FlexApplicationComponent.HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG);

    addMandatoryOptions(rootElement);
    handleOptionsWithSpecialValues(rootElement);
    addNamespaces(rootElement);
    addRootsFromSdk(rootElement);
    addLibs(rootElement);
    addSourcePaths(rootElement);
    addOtherOptions(rootElement);
    addInputOutputPaths(rootElement);

    return JDOMUtil.writeElement(rootElement, "\n");
  }

  private void addMandatoryOptions(final Element rootElement) {
    if (myConfig.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Web) {
      // todo uncomment in xml and do not add as standard option
      //final String revision = getValueAndSource(CompilerOptionInfo.getOptionInfo("target-player-revision")).first;
      final String revision = "0";
      final String targetPlayer = myConfig.DEPENDENCIES.TARGET_PLAYER + "." + revision;
      addOption(rootElement, CompilerOptionInfo.TARGET_PLAYER_INFO, targetPlayer);
    }
    else if (myConfig.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile) {
      addOption(rootElement, CompilerOptionInfo.MOBILE_INFO, "true");
    }
  }

  /**
   * Adds options that get incorrect default values inside compiler code if not set explicitly.
   */
  private void handleOptionsWithSpecialValues(final Element rootElement) {
    for (final CompilerOptionInfo info : CompilerOptionInfo.getOptionsWithSpecialValues()) {
      final Pair<String, ValueSource> valueAndSource =
        getValueAndSource(info);
      if (valueAndSource.second == ValueSource.GlobalDefault && !valueAndSource.first.isEmpty()) {
        // do not add empty preloader or theme to Web/Desktop, but add not-empty for Mobile projects
        addOption(rootElement, info, valueAndSource.first);
      }
    }
  }

  private void addNamespaces(final Element rootElement) {
    if (myConfig.PURE_ACTION_SCRIPT) return;

    final StringBuilder namespaceBuilder = new StringBuilder();

    if (StringUtil.compareVersionNumbers(mySdkVersion, "4") < 0) {
      namespaceBuilder.
        append("http://www.adobe.com/2006/mxml").
        append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).
        append("${FLEX_SDK}/frameworks/mxml-manifest.xml");
    }
    else {
      namespaceBuilder.
        append("http://ns.adobe.com/mxml/2009").
        append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).
        append("${FLEX_SDK}/frameworks/mxml-2009-manifest.xml");

      if (myConfig.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile ||
          myConfig.DEPENDENCIES.COMPONENT_SET == FlexIdeBuildConfiguration.ComponentSet.SparkAndMx ||
          myConfig.DEPENDENCIES.COMPONENT_SET == FlexIdeBuildConfiguration.ComponentSet.SparkOnly) {
        namespaceBuilder.
          append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR).
          append("library://ns.adobe.com/flex/spark").
          append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).
          append("${FLEX_SDK}/frameworks/spark-manifest.xml");
      }

      if (myConfig.TARGET_PLATFORM != FlexIdeBuildConfiguration.TargetPlatform.Mobile) {
        if (myConfig.DEPENDENCIES.COMPONENT_SET == FlexIdeBuildConfiguration.ComponentSet.SparkAndMx ||
            myConfig.DEPENDENCIES.COMPONENT_SET == FlexIdeBuildConfiguration.ComponentSet.MxOnly) {
          namespaceBuilder.
            append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR).
            append("library://ns.adobe.com/flex/mx").
            append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).
            append("${FLEX_SDK}/frameworks/mx-manifest.xml");
        }

        namespaceBuilder.
          append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR).
          append("http://www.adobe.com/2006/mxml").
          append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).
          append("${FLEX_SDK}/frameworks/mxml-manifest.xml");
      }
    }

    final CompilerOptionInfo info = CompilerOptionInfo.getOptionInfo("compiler.namespaces.namespace");
    addOption(rootElement, info, namespaceBuilder.toString());
  }

  private void addRootsFromSdk(final Element rootElement) {
    final String globalLib = myConfig.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Web
                             ? "${FLEX_SDK}/frameworks/libs/player/{targetPlayerMajorVersion}" +
                               (StringUtil.compareVersionNumbers(mySdkVersion, "4") < 0 ? "" : ".{targetPlayerMinorVersion}") +
                               "/playerglobal.swc"
                             : "${FLEX_SDK}/frameworks/libs/air/airglobal.swc";
    addOption(rootElement, CompilerOptionInfo.EXTERNAL_LIBRARY_INFO, globalLib);

    final StringBuilder libBuilder = new StringBuilder();
    libBuilder
      .append("${FLEX_SDK}/frameworks/libs")
      .append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)
      .append("${FLEX_SDK}/frameworks/locale/{locale}");

    if (myConfig.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Desktop) {
      libBuilder
        .append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)
        .append("${FLEX_SDK}/frameworks/libs/air");
    }

    if (StringUtil.compareVersionNumbers(mySdkVersion, "4.5") >= 0) {
      if (myConfig.TARGET_PLATFORM == FlexIdeBuildConfiguration.TargetPlatform.Mobile) {
        libBuilder
          .append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)
          .append("${FLEX_SDK}/frameworks/libs/mobile")
          .append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)
          .append("${FLEX_SDK}/frameworks/libs/air/servicemonitor.swc");
      }
      else {
        libBuilder
          .append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR)
          .append("${FLEX_SDK}/frameworks/libs/mx");
      }
    }

    addOption(rootElement, CompilerOptionInfo.LIBRARY_PATH_INFO, libBuilder.toString());

    // todo handle RSL/include/external linkage
  }

  private void addLibs(final Element rootElement) {
    // todo implement
  }

  private void addSourcePaths(final Element rootElement) {
    final String localeValue = getValueAndSource(CompilerOptionInfo.getOptionInfo("compiler.locale")).first;
    final List<String> locales = StringUtil.split(localeValue, String.valueOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR));

    final Set<String> sourcePathsWithLocaleToken = new THashSet<String>(); // Set - to avoid duplication of paths like "locale/{locale}"
    final List<String> sourcePathsWithoutLocaleToken = new LinkedList<String>();

    for (final VirtualFile sourceRoot : ModuleRootManager.getInstance(myModule).getSourceRoots(false)) {
      if (locales.contains(sourceRoot.getName())) {
        sourcePathsWithLocaleToken.add(sourceRoot.getParent().getPath() + "/" + FlexCompilerHandler.LOCALE_TOKEN);
      }
      else {
        sourcePathsWithoutLocaleToken.add(sourceRoot.getPath());
      }
    }

    final StringBuilder sourcePathBuilder = new StringBuilder();

    for (final String sourcePath : sourcePathsWithLocaleToken) {
      if (sourcePathBuilder.length() > 0) {
        sourcePathBuilder.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      sourcePathBuilder.append(sourcePath);
    }

    for (final String sourcePath : sourcePathsWithoutLocaleToken) {
      if (sourcePathBuilder.length() > 0) {
        sourcePathBuilder.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      sourcePathBuilder.append(sourcePath);
    }

    addOption(rootElement, CompilerOptionInfo.SOURCE_PATH_INFO, sourcePathBuilder.toString());
  }

  private void addOtherOptions(final Element rootElement) {
    final Map<String, String> options = new THashMap<String, String>(myProjectLevelCompilerOptions.OPTIONS);
    options.putAll(myModuleLevelCompilerOptions.OPTIONS);
    options.putAll(myConfig.COMPILER_OPTIONS.OPTIONS);

    for (final Map.Entry<String, String> entry : options.entrySet()) {
      addOption(rootElement, CompilerOptionInfo.getOptionInfo(entry.getKey()), entry.getValue());
    }
  }

  private void addInputOutputPaths(final Element rootElement) {
    if (myConfig.OUTPUT_TYPE == FlexIdeBuildConfiguration.OutputType.Library) {
      // todo
    }
    else {
      final String pathToMainClassFile = FlexUtils.getPathToMainClassFile(myConfig.MAIN_CLASS, myModule);
      addOption(rootElement, CompilerOptionInfo.MAIN_CLASS_INFO, pathToMainClassFile);
    }

    addOption(rootElement, CompilerOptionInfo.OUTPUT_PATH_INFO, myConfig.getOutputFilePath());
  }

  private void addOption(final Element rootElement,
                         final CompilerOptionInfo info,
                         final String rawValue) {
    if (!info.isApplicable(mySdkVersion, myConfig.TARGET_PLATFORM, myConfig.PURE_ACTION_SCRIPT, myConfig.OUTPUT_TYPE)) {
      return;
    }

    final String value = StringUtil.escapeXml(FlexUtils.replacePathMacros(rawValue, myModule, mySdkRootPath));

    final List<String> elementNames = StringUtil.split(info.ID, ".");
    Element parentElement = rootElement;

    for (int i1 = 0; i1 < elementNames.size() - 1; i1++) {
      parentElement = getOrCreateElement(parentElement, elementNames.get(i1));
    }

    final String elementName = elementNames.get(elementNames.size() - 1);

    switch (info.TYPE) {
      case Group:
        assert false;
        break;
      case Boolean:
      case String:
      case Int:
      case File:
        final Element simpleElement = new Element(elementName, parentElement.getNamespace());
        simpleElement.setText(value);
        parentElement.addContent(simpleElement);
        break;
      case List:
        if (info.LIST_ELEMENTS.length == 1) {
          final Element listHolderElement = new Element(elementName, parentElement.getNamespace());
          for (final String listElementValue : StringUtil.split(value, String.valueOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR))) {
            final Element child = new Element(info.LIST_ELEMENTS[0].NAME, listHolderElement.getNamespace());
            child.setText(listElementValue);
            listHolderElement.addContent(child);
          }
          parentElement.addContent(listHolderElement);
        }
        else {
          for (final String listEntry : StringUtil.split(value, String.valueOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR))) {
            final Element repeatableListHolderElement = new Element(elementName, parentElement.getNamespace());

            final List<String> values =
              StringUtil.split(listEntry, String.valueOf(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR), true, false);
            assert info.LIST_ELEMENTS.length == values.size() : info.ID + "=" + value;

            for (int i = 0; i < info.LIST_ELEMENTS.length; i++) {
              final Element child = new Element(info.LIST_ELEMENTS[i].NAME, repeatableListHolderElement.getNamespace());
              child.setText(values.get(i));
              repeatableListHolderElement.addContent(child);
            }

            parentElement.addContent(repeatableListHolderElement);
          }
        }
        break;
      case IncludeClasses:
        // todo implement
        break;
      case IncludeFiles:
        // todo implement
        break;
    }
  }

  private static Element getOrCreateElement(final Element parentElement, final String elementName) {
    Element child = parentElement.getChild(elementName, parentElement.getNamespace());
    if (child == null) {
      child = new Element(elementName, parentElement.getNamespace());
      parentElement.addContent(child);
    }
    return child;
  }

  private Pair<String, ValueSource> getValueAndSource(final CompilerOptionInfo info) {
    assert !info.isGroup() : info.DISPLAY_NAME;

    final String bcLevelValue = myConfig.COMPILER_OPTIONS.OPTIONS.get(info.ID);
    if (bcLevelValue != null) return Pair.create(bcLevelValue, ValueSource.BC);

    final String moduleLevelValue = myModuleLevelCompilerOptions.OPTIONS.get(info.ID);
    if (moduleLevelValue != null) return Pair.create(moduleLevelValue, ValueSource.ModuleDefault);

    final String projectLevelValue = myProjectLevelCompilerOptions.OPTIONS.get(info.ID);
    if (projectLevelValue != null) return Pair.create(projectLevelValue, ValueSource.ProjectDefault);

    return Pair.create(info.getDefaultValue(mySdkVersion, myConfig.TARGET_PLATFORM), ValueSource.GlobalDefault);
  }
}
