package com.intellij.lang.javascript.flex.build;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.*;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.util.PathUtil;
import com.intellij.util.PlatformUtils;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompilerConfigGenerator {

  private static final String[] LIB_ORDER =
    {"framework", "textLayout", "osmf", "spark", "sparkskins", "rpc", "charts", "spark_dmv", "osmf", "mx", "advancedgrids"};

  private final Module myModule;
  private final FlexIdeBuildConfiguration myConfig;
  private final String mySdkHome;
  private final String mySdkVersion;
  private final String[] mySdkRootUrls;
  private final CompilerOptions myModuleLevelCompilerOptions;
  private final CompilerOptions myProjectLevelCompilerOptions;

  private CompilerConfigGenerator(final @NotNull Module module,
                                  final @NotNull FlexIdeBuildConfiguration config,
                                  final @NotNull String sdkHome,
                                  final @NotNull String sdkVersion,
                                  final @NotNull String[] sdkRootUrls,
                                  final @NotNull CompilerOptions moduleLevelCompilerOptions,
                                  final @NotNull CompilerOptions projectLevelCompilerOptions) {
    myModule = module;
    myConfig = config;
    mySdkHome = sdkHome;
    mySdkVersion = sdkVersion;
    mySdkRootUrls = sdkRootUrls;
    myModuleLevelCompilerOptions = moduleLevelCompilerOptions;
    myProjectLevelCompilerOptions = projectLevelCompilerOptions;
  }

  public static VirtualFile getOrCreateConfigFile(final Module module, final FlexIdeBuildConfiguration config) throws IOException {
    final SdkEntry sdkEntry = config.getDependencies().getSdkEntry();
    final Library sdkLib = sdkEntry == null ? null : sdkEntry.findLibrary();
    if (sdkLib == null) {
      throw new IOException(FlexBundle.message("sdk.not.set.for.bc.0.of.module.1", config.getName(), module.getName()));
    }

    final CompilerConfigGenerator generator =
      new CompilerConfigGenerator(module, config, FlexSdk.getHomePath(sdkLib), FlexSdk.getFlexVersion(sdkLib),
                                  sdkLib.getUrls(OrderRootType.CLASSES),
                                  FlexBuildConfigurationManager.getInstance(module).getModuleLevelCompilerOptions(),
                                  FlexProjectLevelCompilerOptionsHolder.getInstance(module.getProject())
                                    .getProjectLevelCompilerOptions());
    final String text = generator.generateConfigFileText();
    final String name =
      FlexCompilerHandler.generateConfigFileName(module, config.getName(), PlatformUtils.getPlatformPrefix().toLowerCase(), null);
    return FlexCompilationUtils.getOrCreateConfigFile(module.getProject(), name, text);
  }

  private String generateConfigFileText() {
    final Element rootElement =
      new Element(FlexCompilerConfigFileUtil.FLEX_CONFIG, FlexApplicationComponent.HTTP_WWW_ADOBE_COM_2006_FLEX_CONFIG);

    addDebugOption(rootElement);
    addMandatoryOptions(rootElement);
    handleOptionsWithSpecialValues(rootElement);
    addSourcePaths(rootElement);
    addNamespaces(rootElement);
    addRootsFromSdk(rootElement);
    addLibs(rootElement);
    addOtherOptions(rootElement);
    addInputOutputPaths(rootElement);

    return JDOMUtil.writeElement(rootElement, "\n");
  }

  private void addDebugOption(final Element rootElement) {
    final FlexCompilerProjectConfiguration instance = FlexCompilerProjectConfiguration.getInstance(myModule.getProject());
    final boolean debug =
      myConfig.getOutputType() == OutputType.Library ? instance.SWC_DEBUG_ENABLED : instance.SWF_DEBUG_ENABLED;
    addOption(rootElement, CompilerOptionInfo.DEBUG_INFO, String.valueOf(debug));
  }

  private void addMandatoryOptions(final Element rootElement) {
    if (myConfig.getTargetPlatform() == TargetPlatform.Web) {
      // todo uncomment in xml and do not add as standard option
      //final String revision = getValueAndSource(CompilerOptionInfo.getOptionInfo("target-player-revision")).first;
      final String revision = "0";
      final String targetPlayer = myConfig.getDependencies().getTargetPlayer() + "." + revision;
      addOption(rootElement, CompilerOptionInfo.TARGET_PLAYER_INFO, targetPlayer);
    }
    else if (myConfig.getTargetPlatform() == TargetPlatform.Mobile) {
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
    if (myConfig.isPureAs()) return;

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

      if (myConfig.getTargetPlatform() == TargetPlatform.Mobile ||
          myConfig.getDependencies().getComponentSet() == ComponentSet.SparkAndMx ||
          myConfig.getDependencies().getComponentSet() == ComponentSet.SparkOnly) {
        namespaceBuilder.
          append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR).
          append("library://ns.adobe.com/flex/spark").
          append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).
          append("${FLEX_SDK}/frameworks/spark-manifest.xml");
      }

      if (myConfig.getTargetPlatform() != TargetPlatform.Mobile) {
        if (myConfig.getDependencies().getComponentSet() == ComponentSet.SparkAndMx ||
            myConfig.getDependencies().getComponentSet() == ComponentSet.MxOnly) {
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
    final CompilerOptionInfo localeInfo = CompilerOptionInfo.getOptionInfo("compiler.locale");
    if (!getValueAndSource(localeInfo).first.isEmpty()) {
      addOption(rootElement, CompilerOptionInfo.LIBRARY_PATH_INFO, mySdkHome + "/frameworks/locale/{locale}");
    }

    final Map<String, String> libNameToRslInfo = new THashMap<String, String>();

    for (final String swcUrl : mySdkRootUrls) {
      LinkageType linkageType = BCUtils.getSdkEntryLinkageType(swcUrl, myConfig.getNature(), myConfig.getDependencies().getTargetPlayer(),
                                                               myConfig.getDependencies().getComponentSet());

      // check applicability
      if (linkageType == null) continue;
      // resolve default
      if (linkageType == LinkageType.Default) linkageType = myConfig.getDependencies().getFrameworkLinkage();
      if (linkageType == LinkageType.Default) linkageType = BCUtils.getDefaultFrameworkLinkage(myConfig.getNature());

      final CompilerOptionInfo info = linkageType == LinkageType.Merged ? CompilerOptionInfo.LIBRARY_PATH_INFO :
                                      linkageType == LinkageType.RSL ? CompilerOptionInfo.LIBRARY_PATH_INFO :
                                      linkageType == LinkageType.External ? CompilerOptionInfo.EXTERNAL_LIBRARY_INFO :
                                      linkageType == LinkageType.Include ? CompilerOptionInfo.INCLUDE_LIBRARY_INFO :
                                      null;

      final String swcPath = VirtualFileManager.extractPath(StringUtil.trimEnd(swcUrl, JarFileSystem.JAR_SEPARATOR));
      assert info != null : swcPath + ": " + linkageType.getShortText();

      addOption(rootElement, info, swcPath);

      if (linkageType == LinkageType.RSL) {
        final String swcName = PathUtil.getFileName(swcPath);
        assert swcName.endsWith(".swc") : swcUrl;
        final String libName = swcName.substring(0, swcName.length() - ".swc".length());

        final String swzVersion = libName.equals("textLayout")
                                  ? getTextLayoutSwzVersion(mySdkVersion)
                                  : libName.equals("osmf")
                                    ? getOsmfSwzVersion(mySdkVersion)
                                    : mySdkVersion;
        final String swzUrl;
        swzUrl = libName.equals("textLayout")
                 ? "http://fpdownload.adobe.com/pub/swz/tlf/" + swzVersion + "/textLayout_" + swzVersion + ".swz"
                 : "http://fpdownload.adobe.com/pub/swz/flex/" + mySdkVersion + "/" + libName + "_" + swzVersion + ".swz";

        final StringBuilder rslBuilder = new StringBuilder();
        rslBuilder
          .append(swcPath)
          .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append(swzUrl)
          .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append("http://fpdownload.adobe.com/pub/swz/crossdomain.xml")
          .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append(libName).append('_').append(swzVersion).append(".swz")
          .append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append(""); // no failover policy file url

        libNameToRslInfo.put(libName, rslBuilder.toString());
      }
    }

    addRslInfo(rootElement, libNameToRslInfo);
  }

  private void addRslInfo(final Element rootElement, final Map<String, String> libNameToRslInfo) {
    if (libNameToRslInfo.isEmpty()) return;

    // RSL order is important!
    for (final String libName : LIB_ORDER) {
      final String rslInfo = libNameToRslInfo.remove(libName);
      if (rslInfo != null) {
        addOption(rootElement, CompilerOptionInfo.RSL_TWO_URLS_PATH_INFO, rslInfo);
      }
    }

    // now add other in random order, though up to Flex SDK 4.5.1 the map should be empty at this stage
    for (final String rslInfo : libNameToRslInfo.values()) {
      addOption(rootElement, CompilerOptionInfo.RSL_TWO_URLS_PATH_INFO, rslInfo);
    }
  }

  private static String getTextLayoutSwzVersion(final String sdkVersion) {
    return sdkVersion.startsWith("4.0")
           ? "textLayout_1.0.0.595"
           : sdkVersion.startsWith("4.1")
             ? "1.1.0.604"
             : "2.0.0.232";
  }

  private static String getOsmfSwzVersion(final String sdkVersion) {
    return StringUtil.compareVersionNumbers(sdkVersion, "4.5") < 0 ? "4.0.0.13495" : "1.0.0.16316";
  }

  private void addLibs(final Element rootElement) {
    for (final DependencyEntry entry : myConfig.getDependencies().getEntries()) {
      final LinkageType linkageType = entry.getDependencyType().getLinkageType();

      if (entry instanceof BuildConfigurationEntry) {
        if (linkageType == LinkageType.LoadInRuntime) continue;

        final FlexIdeBuildConfiguration config = ((BuildConfigurationEntry)entry).findBuildConfiguration();

        if (config != null) {
          addLib(rootElement, config.getOutputFilePath(), linkageType);
        }
      }
      else if (entry instanceof ModuleLibraryEntry) {
        final LibraryOrderEntry orderEntry = FlexProjectRootsUtil.findOrderEntry((ModuleLibraryEntry)entry, ModuleRootManager.getInstance(myModule));

        if (orderEntry == null) continue;

        for (VirtualFile libFile : orderEntry.getRootFiles(OrderRootType.CLASSES)) {
          libFile = FlexCompilerHandler.getRealFile(libFile);

          if (libFile != null && libFile.isDirectory()) {
            addOption(rootElement, CompilerOptionInfo.SOURCE_PATH_INFO, libFile.getPath());
          }
          else if (libFile != null && !libFile.isDirectory() && "swc".equalsIgnoreCase(libFile.getExtension())) {
            addLib(rootElement, libFile.getPath(), linkageType);
          }
        }
      }
    }
  }

  private void addLib(final Element rootElement, final String swcPath, final LinkageType linkageType) {
    final CompilerOptionInfo info = linkageType == LinkageType.Merged || linkageType == LinkageType.RSL
                                    ? CompilerOptionInfo.LIBRARY_PATH_INFO
                                    : linkageType == LinkageType.External
                                      ? CompilerOptionInfo.EXTERNAL_LIBRARY_INFO
                                      : linkageType == LinkageType.Include
                                        ? CompilerOptionInfo.INCLUDE_LIBRARY_INFO
                                        : null;
    assert info != null : swcPath + ": " + linkageType;

    addOption(rootElement, info, swcPath);

    if (linkageType == LinkageType.RSL) {
      // todo add RSL URLs
    }
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
    final Map<String, String> options = new THashMap<String, String>(myProjectLevelCompilerOptions.getAllOptions());
    options.putAll(myModuleLevelCompilerOptions.getAllOptions());
    options.putAll(myConfig.getCompilerOptions().getAllOptions());

    for (final Map.Entry<String, String> entry : options.entrySet()) {
      addOption(rootElement, CompilerOptionInfo.getOptionInfo(entry.getKey()), entry.getValue());
    }
  }

  private void addInputOutputPaths(final Element rootElement) {
    if (myConfig.getOutputType() == OutputType.Library) {
      final ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(myModule.getProject()).getFileIndex();

      ContentIterator ci = new ContentIterator() {
        public boolean processFile(final VirtualFile fileOrDir) {
          if (FlexCompilerHandler.includeInCompilation(myModule.getProject(), fileOrDir)) {
            //if (!isTest && projectFileIndex.isInTestSourceContent(fileOrDir)) {
            //  return true;
            //}

            final VirtualFile rootForFile = projectFileIndex.getSourceRootForFile(fileOrDir);
            if (rootForFile != null) {
              final String packageText = VfsUtilCore.getRelativePath(fileOrDir.getParent(), rootForFile, '.');
              assert packageText != null;
              final String qName = (packageText.length() > 0 ? packageText + "." : "") + fileOrDir.getNameWithoutExtension();

              if (FlexCompilerHandler.isMxmlOrFxgOrASWithPublicDeclaration(myModule, fileOrDir, qName)) {
                addOption(rootElement, CompilerOptionInfo.INCLUDE_CLASSES_INFO, qName);
              }
            }
          }
          return true;
        }
      };

      ModuleRootManager.getInstance(myModule).getFileIndex().iterateContent(ci);
    }
    else {
      final String pathToMainClassFile = FlexUtils.getPathToMainClassFile(myConfig.getMainClass(), myModule);
      addOption(rootElement, CompilerOptionInfo.MAIN_CLASS_INFO, pathToMainClassFile);
    }

    addOption(rootElement, CompilerOptionInfo.OUTPUT_PATH_INFO, myConfig.getOutputFilePath());
  }

  private void addOption(final Element rootElement,
                         final CompilerOptionInfo info,
                         final String rawValue) {
    if (!info.isApplicable(mySdkVersion, myConfig.getTargetPlatform(), myConfig.isPureAs(), myConfig.getOutputType())) {
      return;
    }

    final String value = StringUtil.escapeXml(FlexUtils.replacePathMacros(rawValue, myModule, mySdkHome));

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
          final Element listHolderElement = getOrCreateElement(parentElement, elementName);
          for (final String listElementValue : StringUtil.split(value, String.valueOf(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR))) {
            final Element child = new Element(info.LIST_ELEMENTS[0].NAME, listHolderElement.getNamespace());
            child.setText(listElementValue);
            listHolderElement.addContent(child);
          }
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

    final String bcLevelValue = myConfig.getCompilerOptions().getOption(info.ID);
    if (bcLevelValue != null) return Pair.create(bcLevelValue, ValueSource.BC);

    final String moduleLevelValue = myModuleLevelCompilerOptions.getOption(info.ID);
    if (moduleLevelValue != null) return Pair.create(moduleLevelValue, ValueSource.ModuleDefault);

    final String projectLevelValue = myProjectLevelCompilerOptions.getOption(info.ID);
    if (projectLevelValue != null) return Pair.create(projectLevelValue, ValueSource.ProjectDefault);

    return Pair.create(info.getDefaultValue(mySdkVersion, myConfig.getTargetPlatform()), ValueSource.GlobalDefault);
  }
}
