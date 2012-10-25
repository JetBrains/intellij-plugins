package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.ModuleSettings;
import com.intellij.facet.FacetManagerImpl;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexCompositeSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.*;
import com.intellij.openapi.roots.impl.libraries.JarDirectories;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import gnu.trove.THashMap;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.serialization.JDomSerializationUtil;
import org.jetbrains.jps.model.serialization.facet.JpsFacetSerializer;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

/**
 * User: ksafonov
 */
class FlexModuleConverter extends ConversionProcessor<ModuleSettings> {

  private static final Logger LOG = Logger.getInstance(FlexModuleConverter.class.getName());

  private static final String FLEX_FACET_TYPE_ID = "flex";
  private static final @NonNls String FLEX_SDK_ATTR_NAME = "flex_sdk";

  private static final String NAMESPACE_AND_MANIFEST_FILE_INFO_LIST_ELEMENT_NAME = "NAMESPACE_AND_MANIFEST_FILE_INFO_LIST";
  private static final String CONDITIONAL_COMPILER_DEFINITION_LIST_ELEMENT_NAME = "CONDITIONAL_COMPILATION_DEFINITION_LIST";
  private static final String CSS_FILES_LIST_ELEMENT_NAME = "CSS_FILES_LIST";
  private static final String FILE_PATH_ELEMENT_NAME = "FILE_PATH";

  private final ConversionParams myParams;

  public FlexModuleConverter(ConversionParams params) {
    myParams = params;
  }

  @Override
  public boolean isConversionNeeded(ModuleSettings moduleSettings) {
    return isConversionNeededStatic(moduleSettings);
  }

  static boolean isConversionNeededStatic(ModuleSettings moduleSettings) {
    if (!hasFlex(moduleSettings)) return false;

    return moduleSettings.getComponentElement(FlexBuildConfigurationManagerImpl.COMPONENT_NAME) == null;
  }

  static boolean hasFlex(ModuleSettings moduleSettings) {
    return isFlexModule(moduleSettings) || !getFlexFacets(moduleSettings).isEmpty();
  }

  public static boolean isFlexModule(ModuleSettings moduleSettings) {
    return FlexModuleType.MODULE_TYPE_ID.equals(moduleSettings.getModuleType());
  }

  private static boolean isJavaModule(ModuleSettings module) {
    return StdModuleTypes.JAVA.getId().equals(module.getModuleType());
  }

  public static List<Element> getFlexFacets(ModuleSettings module) {
    if (!isJavaModule(module)) return Collections.emptyList();
    return new ArrayList<Element>(module.getFacetElements(FLEX_FACET_TYPE_ID));
  }

  @Override
  public void process(ModuleSettings moduleSettings) throws CannotConvertException {
    FlexBuildConfigurationManagerImpl configurationManager = ConversionHelper.createBuildConfigurationManager();

    Collection<Element> orderEntriesToAdd = new ArrayList<Element>();
    Set<String> usedSdksNames = new HashSet<String>();
    final Set<Element> usedModuleLibrariesEntries = new HashSet<Element>();
    if (isFlexModule(moduleSettings)) {
      ModifiableFlexBuildConfiguration newConfiguration =
        (ModifiableFlexBuildConfiguration)configurationManager.getBuildConfigurations()[0];
      newConfiguration.setName(generateModuleBcName(moduleSettings));

      Element oldConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
      FlexBuildConfiguration oldConfiguration =
        oldConfigurationElement != null ? XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class) : null;
      processConfiguration(oldConfiguration, newConfiguration, moduleSettings, false, null, usedSdksNames, orderEntriesToAdd,
                           usedModuleLibrariesEntries);
      if (oldConfigurationElement != null) {
        oldConfigurationElement.detach();
      }
    }
    else {
      List<Element> flexFacets = getFlexFacets(moduleSettings);
      for (int i = 0; i < flexFacets.size(); i++) {
        Element facet = flexFacets.get(i);
        ModifiableFlexBuildConfiguration newConfiguration;
        if (i == 0) {
          newConfiguration = (ModifiableFlexBuildConfiguration)configurationManager.getBuildConfigurations()[0];
        }
        else {
          newConfiguration = ConversionHelper.createBuildConfiguration(configurationManager);
        }
        newConfiguration.setName(generateFacetBcName(flexFacets, facet));
        Element oldConfigurationElement = facet.getChild(JpsFacetSerializer.CONFIGURATION_TAG);
        if (oldConfigurationElement != null) {
          FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class);

          try {
            readNamespaceAndManifestInfoList(oldConfigurationElement, oldConfiguration);
            readConditionalCompilerDefinitionList(oldConfigurationElement, oldConfiguration);
            readCssFilesList(oldConfigurationElement, oldConfiguration);
          }
          catch (InvalidDataException ignore) {/* unlucky */}

          final String facetSdkName = oldConfigurationElement.getAttributeValue(FLEX_SDK_ATTR_NAME);
          processConfiguration(oldConfiguration, newConfiguration, moduleSettings, true, facetSdkName, usedSdksNames, orderEntriesToAdd,
                               usedModuleLibrariesEntries);
        }
        else {
          processConfiguration(null, newConfiguration, moduleSettings, true, null, usedSdksNames, orderEntriesToAdd,
                               usedModuleLibrariesEntries);
        }
      }
      moduleSettings.setModuleType(FlexModuleType.MODULE_TYPE_ID);
      moduleSettings.getComponentElement(FacetManagerImpl.COMPONENT_NAME).getChildren(JpsFacetSerializer.FACET_TAG).removeAll(flexFacets);
    }

    Collection<Element> allEntries = new ArrayList<Element>();
    allEntries.addAll(usedModuleLibrariesEntries);
    allEntries.addAll(orderEntriesToAdd);
    for (Element orderEntry : allEntries) {
      if (DependencyScope.readExternal(orderEntry) == DependencyScope.TEST) {
        orderEntry.removeAttribute(DependencyScope.SCOPE_ATTR);
      }
    }

    if (!usedSdksNames.isEmpty()) {
      Element sdkEntryElement = new Element(OrderEntryFactory.ORDER_ENTRY_ELEMENT_NAME);
      sdkEntryElement.setAttribute(OrderEntryFactory.ORDER_ENTRY_TYPE_ATTR, "jdk");
      final String compositeSdkName = FlexCompositeSdk.getCompositeName(ArrayUtil.toStringArray(usedSdksNames));
      sdkEntryElement.setAttribute(ModuleJdkOrderEntryImpl.JDK_NAME_ATTR, compositeSdkName);
      sdkEntryElement.setAttribute(ModuleJdkOrderEntryImpl.JDK_TYPE_ATTR, FlexCompositeSdk.TYPE_ID);
      moduleSettings.getOrderEntries().add(sdkEntryElement);
    }
    Element rootManagerElement = JDomSerializationUtil.findOrCreateComponentElement(moduleSettings.getRootElement(),
                                                                                    ModuleSettings.MODULE_ROOT_MANAGER_COMPONENT);
    rootManagerElement.addContent(orderEntriesToAdd);

    Element componentElement =
      JDomSerializationUtil.findOrCreateComponentElement(moduleSettings.getRootElement(), FlexBuildConfigurationManagerImpl.COMPONENT_NAME);
    addContent(ConversionHelper.serialize(configurationManager), componentElement);

    ignoreInapplicableFacets(moduleSettings);
  }

  private void ignoreInapplicableFacets(ModuleSettings module) {
    boolean allowFlexFacets = isJavaModule(module);
    final Element facetManager = module.getComponentElement(FacetManagerImpl.COMPONENT_NAME);
    for (Element facet : JDOMUtil.getChildren(facetManager, JpsFacetSerializer.FACET_TAG)) {
      String type = facet.getAttributeValue(JpsFacetSerializer.TYPE_ATTRIBUTE);
      if (allowFlexFacets && FLEX_FACET_TYPE_ID.equals(type)) {
        continue;
      }
      String name = facet.getAttributeValue(JpsFacetSerializer.NAME_ATTRIBUTE);
      myParams.ignoreInvalidFacet(module.getModuleName(), type, name);
    }
  }

  private void processConfiguration(@Nullable FlexBuildConfiguration oldConfiguration,
                                    ModifiableFlexBuildConfiguration newBuildConfiguration,
                                    final ModuleSettings module,
                                    boolean facet,
                                    @Nullable String facetSdkName,
                                    Set<String> usedSdksNames,
                                    Collection<Element> orderEntriesToAdd,
                                    Set<Element> usedModuleLibrariesEntries) throws CannotConvertException {
    if (oldConfiguration == null) {
      newBuildConfiguration.setOutputType(OutputType.Application);
    }
    else {
      if (FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE)) {
        newBuildConfiguration.setOutputType(OutputType.Library);
      }
      else {
        newBuildConfiguration.setOutputType(OutputType.Application);
      }

      if (newBuildConfiguration.getOutputType() == OutputType.Application) {
        newBuildConfiguration.setMainClass(oldConfiguration.MAIN_CLASS);
        myParams.addAppModuleAndBCName(module.getModuleName(), newBuildConfiguration.getName());
      }
      newBuildConfiguration.setOutputFileName(oldConfiguration.OUTPUT_FILE_NAME);
      newBuildConfiguration.setSkipCompile(!oldConfiguration.DO_BUILD);

      final ModifiableCompilerOptions newCompilerOptions = newBuildConfiguration.getCompilerOptions();
      newCompilerOptions.setAllOptions(convertCompilerOptions(oldConfiguration, module, newCompilerOptions));
    }

    String outputFolder;
    if (facet && oldConfiguration != null && oldConfiguration.USE_FACET_COMPILE_OUTPUT_PATH) {
      outputFolder = PathUtil.getCanonicalPath(module.expandPath(oldConfiguration.FACET_COMPILE_OUTPUT_PATH));
    }
    else {
      outputFolder = getOutputFolder(module);
    }
    newBuildConfiguration.setOutputFolder(outputFolder);

    Collection<Element> orderEntriesToRemove = new ArrayList<Element>();
    // TODO filter out java libraries and remove their order entries
    for (Element orderEntry : module.getOrderEntries()) {
      String orderEntryType = orderEntry.getAttributeValue(OrderEntryFactory.ORDER_ENTRY_TYPE_ATTR);
      if (ModuleLibraryOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        Element library = orderEntry.getChild(LibraryImpl.ELEMENT);
        if (!isApplicableLibrary(library, new Function<String, String>() {
          @Override
          public String fun(final String s) {
            return module.expandPath(s);
          }
        })) {
          // ignore non-flex module library
          orderEntriesToRemove.add(orderEntry);
          continue;
        }

        if (facet && isAutogeneratedLibrary(library)) {
          orderEntriesToRemove.add(orderEntry);
          continue;
        }

        Element libraryProperties;
        if (!usedModuleLibrariesEntries.add(orderEntry)) {
          // this library is already used by another build configuration, create new entry with new library
          Element newEntry = (Element)orderEntry.clone();
          orderEntriesToAdd.add(newEntry);
          library = orderEntry.getChild(LibraryImpl.ELEMENT);
          libraryProperties = library.getChild(LibraryImpl.PROPERTIES_ELEMENT);
        }
        else {
          library.setAttribute(LibraryImpl.LIBRARY_TYPE_ATTR, FlexLibraryType.FLEX_LIBRARY.getKindId());
          libraryProperties = new Element(LibraryImpl.PROPERTIES_ELEMENT);
          //noinspection unchecked
          library.getChildren().add(0, libraryProperties);
        }
        String libraryId = FlexLibraryIdGenerator.generateId();
        XmlSerializer.serializeInto(new FlexLibraryProperties(libraryId), libraryProperties);

        ModifiableModuleLibraryEntry moduleLibraryEntry = ConversionHelper.createModuleLibraryEntry(libraryId);
        convertDependencyType(orderEntry, moduleLibraryEntry.getDependencyType());
        newBuildConfiguration.getDependencies().getModifiableEntries().add(moduleLibraryEntry);
      }
      else if ("library".equals(orderEntryType)) {
        String libraryName = orderEntry.getAttributeValue("name");
        String libraryLevel = orderEntry.getAttributeValue("level");
        if (myParams.libraryExists(libraryName, libraryLevel)) {
          myParams.changeLibraryTypeToFlex(libraryName, libraryLevel);
          ModifiableSharedLibraryEntry sharedLibraryEntry = ConversionHelper.createSharedLibraryEntry(libraryName, libraryLevel);
          convertDependencyType(orderEntry, sharedLibraryEntry.getDependencyType());
          newBuildConfiguration.getDependencies().getModifiableEntries().add(sharedLibraryEntry);
        }
        else {
          orderEntriesToRemove.add(orderEntry);
        }
      }
      else if (ModuleOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        String moduleName = orderEntry.getAttributeValue(ModuleOrderEntryImpl.MODULE_NAME_ATTR);
        Collection<String> bcNames = myParams.getBcNamesForDependency(moduleName, newBuildConfiguration.getNature());
        for (String bcName : bcNames) {
          ModifiableBuildConfigurationEntry bcEntry = ConversionHelper.createBuildConfigurationEntry(moduleName, bcName);
          convertDependencyType(orderEntry, bcEntry.getDependencyType());
          newBuildConfiguration.getDependencies().getModifiableEntries().add(bcEntry);
        }
        if (bcNames.isEmpty()) {
          orderEntriesToRemove.add(orderEntry);
        }
      }
      else if (ModuleJdkOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        if (!facet) {
          String sdkName = orderEntry.getAttributeValue(ModuleJdkOrderEntryImpl.JDK_NAME_ATTR);
          String newSdkName = processSdkEntry(newBuildConfiguration, oldConfiguration, sdkName);
          ContainerUtil.addIfNotNull(usedSdksNames, newSdkName);
        }
        orderEntriesToRemove.add(orderEntry);
      }
      else if (InheritedJdkOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        if (!facet) {
          String newSdkName = processSdkEntry(newBuildConfiguration, oldConfiguration, myParams.projectSdkName);
          ContainerUtil.addIfNotNull(usedSdksNames, newSdkName);
        }
        orderEntriesToRemove.add(orderEntry);
      }
    }

    if (facetSdkName != null) {
      String newSdkName = processSdkEntry(newBuildConfiguration, oldConfiguration, facetSdkName);
      ContainerUtil.addIfNotNull(usedSdksNames, newSdkName);
    }

    if (!orderEntriesToRemove.isEmpty()) {
      module.getOrderEntries().removeAll(orderEntriesToRemove);
    }

    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(newBuildConfiguration) &&
        oldConfiguration != null && !oldConfiguration.CSS_FILES_LIST.isEmpty()) {
      final Collection<String> cssFilesToCompile = new ArrayList<String>();
      for (String cssPath : oldConfiguration.CSS_FILES_LIST) {
        cssFilesToCompile.add(PathUtil.getCanonicalPath(module.expandPath(cssPath)));
      }
      newBuildConfiguration.setCssFilesToCompile(cssFilesToCompile);
    }
  }

  public static boolean isAutogeneratedLibrary(Element libraryElement) {
    String libraryName = libraryElement.getAttributeValue(LibraryImpl.LIBRARY_NAME_ATTR);
    return libraryName != null && libraryName.startsWith("AUTOGENERATED library equal to Flex SDK ");
  }

  private static Map<String, String> convertCompilerOptions(final FlexBuildConfiguration oldConfig,
                                                            final ModuleSettings module,
                                                            final ModifiableCompilerOptions newCompilerOptions) {
    if (oldConfig.USE_CUSTOM_CONFIG_FILE) {
      final String customConfigFilePath = PathUtil.getCanonicalPath(module.expandPath(oldConfig.CUSTOM_CONFIG_FILE));
      newCompilerOptions.setAdditionalConfigFilePath(customConfigFilePath);
    }
    // todo may be parse options, replace "-a b" to "-a=b", may be move some options to dedicated fields
    newCompilerOptions.setAdditionalOptions(oldConfig.ADDITIONAL_COMPILER_OPTIONS);

    final Map<String, String> options = new THashMap<String, String>(newCompilerOptions.getAllOptions());

    if (oldConfig.USE_LOCALE_SETTINGS) {
      options.put("compiler.locale", oldConfig.LOCALE.replace(",", CompilerOptionInfo.LIST_ENTRIES_SEPARATOR));
    }

    if (!oldConfig.CONDITIONAL_COMPILATION_DEFINITION_LIST.isEmpty()) {
      final StringBuilder b = new StringBuilder();
      for (FlexBuildConfiguration.ConditionalCompilationDefinition def : oldConfig.CONDITIONAL_COMPILATION_DEFINITION_LIST) {
        if (b.length() > 0) b.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
        b.append(def.NAME).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).append(def.VALUE);
      }
      options.put("compiler.define", b.toString());
    }

    if (!oldConfig.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.isEmpty()) {
      final StringBuilder b = new StringBuilder();
      for (FlexBuildConfiguration.NamespaceAndManifestFileInfo info : oldConfig.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST) {
        if (b.length() > 0) b.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
        b.append(info.NAMESPACE).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR)
          .append(PathUtil.getCanonicalPath(module.expandPath(info.MANIFEST_FILE_PATH)));
      }
      options.put("compiler.namespaces.namespace", b.toString());
    }

    if (!oldConfig.PATH_TO_SERVICES_CONFIG_XML.isEmpty()) {
      options.put("compiler.services", oldConfig.PATH_TO_SERVICES_CONFIG_XML);
      options.put("compiler.context-root", oldConfig.CONTEXT_ROOT);
    }
    return options;
  }

  static boolean isApplicableLibrary(final Element library, Function<String, String> pathExpander) {
    String libraryType = library.getAttributeValue(LibraryImpl.LIBRARY_TYPE_ATTR);
    if (FlexLibraryType.FLEX_LIBRARY.getKindId().equals(libraryType)) {
      return true;
    }

    if (libraryType != null) {
      return false; // ignore explicit non-Flex libraries
    }

    final Element rootChild = library.getChild(OrderRootType.CLASSES.name());
    if (rootChild == null) {
      return false; // corrupted element
    }

    JarDirectories jarDirectories = new JarDirectories();
    try {
      jarDirectories.readExternal(library);
    }
    catch (InvalidDataException ignored) {
      jarDirectories.clear();
    }

    List<Element> classesRoots = rootChild.getChildren(LibraryImpl.ROOT_PATH_ELEMENT);
    if (classesRoots.isEmpty()) {
      // no classes root
      return false;
    }

    for (Element root : classesRoots) {
      final String url = root.getAttributeValue("url");
      if (url == null) {
        continue; // corrupted element
      }

      final String path = getPathFromUrl(url);
      if (path == null) {
        continue;
      }

      if (path.toLowerCase().endsWith(".swc")) {
        return true;
      }
      else {
        if (jarDirectories.contains(OrderRootType.CLASSES, url)) {
          String expanded = pathExpander.fun(path);
          File file = new File(expanded);
          if (!file.isDirectory()) {
            return true;
          }

          String[] allChildren = file.list();
          if (allChildren == null || allChildren.length == 0) {
            return true;
          }

          File[] swcs = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
              return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".swc");
            }
          });
          if (swcs != null && swcs.length > 0) {
            return true;
          }

          File[] jars = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
              return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
            }
          });
          if (jars != null && jars.length > 0) {
            continue;
          }
          return true;
        }
        else if (!path.toLowerCase().endsWith(".jar")) {
          return true;
        }
      }
    }
    return false;
  }

  @Nullable
  private static String getPathFromUrl(String url) {
    if (url.startsWith(JarFileSystem.PROTOCOL_PREFIX) && url.endsWith(JarFileSystem.JAR_SEPARATOR)) {
      return url.substring(JarFileSystem.PROTOCOL_PREFIX.length(), url.length() - JarFileSystem.JAR_SEPARATOR.length());
    }
    else if (url.startsWith(LocalFileSystem.PROTOCOL_PREFIX)) {
      return url.substring(LocalFileSystem.PROTOCOL_PREFIX.length());
    }
    else {
      LOG.warn("Unknown url type: " + url);
      return null;
    }
  }

  private static void convertDependencyType(Element orderEntry, ModifiableDependencyType dependencyType) {
    DependencyScope scope = DependencyScope.readExternal(orderEntry);
    boolean isExported = orderEntry.getAttribute(ModuleLibraryOrderEntryImpl.EXPORTED_ATTR) != null;
    dependencyType.setLinkageType(FlexUtils.convertLinkageType(scope, isExported));
  }

  private static String getOutputFolder(final ModuleSettings moduleSettings) {
    final Element rootManagerElement = moduleSettings.getComponentElement(ModuleSettings.MODULE_ROOT_MANAGER_COMPONENT);
    if (rootManagerElement != null) {
      final boolean inheritOutput = "true".equals(rootManagerElement.getAttributeValue("inherit-compiler-output"));
      if (!inheritOutput) {
        final Element outputElement = rootManagerElement.getChild("output");
        final String outputUrl = outputElement == null ? null : outputElement.getAttributeValue("url");
        if (outputUrl != null) {
          String path = PathUtil.getCanonicalPath(VfsUtilCore.urlToPath(moduleSettings.expandPath(outputUrl)));
          return moduleSettings.collapsePath(path);
        }
      }
    }

    final String projectOutputUrl = moduleSettings.getProjectOutputUrl();
    String path = projectOutputUrl == null ? "" :
                  VfsUtilCore.urlToPath(moduleSettings.expandPath(projectOutputUrl) +
                                        "/" + CompilerModuleExtension.PRODUCTION + "/" + moduleSettings.getModuleName());
    return moduleSettings.collapsePath(path);
  }

  /**
   * @return SDK name if found
   */
  @Nullable
  private static String processSdkEntry(ModifiableFlexBuildConfiguration bc,
                                        @Nullable FlexBuildConfiguration oldConfiguration,
                                        String ideaSdkName) {
    if (ideaSdkName == null) {
      bc.setTargetPlatform(TargetPlatform.Web);
      return null;
    }

    Sdk oldSdk = ProjectJdkTable.getInstance().findJdk(ideaSdkName);
    final String sdkTypeName;
    if (oldSdk == null ||
        oldSdk.getHomePath() == null ||
        !ArrayUtil.contains((sdkTypeName = oldSdk.getSdkType().getName()), ConversionParams.OLD_SDKS_TYPES)) {
      bc.setTargetPlatform(TargetPlatform.Web);
      return null;
    }

    if (ConversionParams.OLD_AIR_MOBIE_SDK_TYPE_NAME.equals(sdkTypeName)) {
      bc.setTargetPlatform(TargetPlatform.Mobile);
      if (bc.getOutputType() == OutputType.Application) {
        bc.getAndroidPackagingOptions().setEnabled(true);
        final String fileName = FileUtil.getNameWithoutExtension(bc.getOutputFileName());
        bc.getAndroidPackagingOptions().setPackageFileName(fileName);
        bc.getIosPackagingOptions().setPackageFileName(fileName);
      }
    }
    else if (ConversionParams.OLD_AIR_SDK_TYPE_NAME.equals(sdkTypeName)) {
      bc.setTargetPlatform(TargetPlatform.Desktop);
      if (bc.getOutputType() == OutputType.Application) {
        bc.getAirDesktopPackagingOptions().setPackageFileName(FileUtil.getNameWithoutExtension(bc.getOutputFileName()));
      }
    }
    else {
      bc.setTargetPlatform(TargetPlatform.Web);
      final String targetPlayer = TargetPlayerUtils.getTargetPlayer(oldConfiguration == null ? null :
                                                                    oldConfiguration.TARGET_PLAYER_VERSION, oldSdk.getHomePath());
      bc.getDependencies().setTargetPlayer(targetPlayer);
    }

    Sdk sdk = ConversionParams.findNewSdk(oldSdk.getHomePath());
    if (sdk != null) {
      SdkEntry sdkEntry = Factory.createSdkEntry(sdk.getName());
      // TODO roots dependencies types
      bc.getDependencies().setSdkEntry(sdkEntry);
      return sdk.getName();
    }
    else {
      return null;
    }
  }

  private static void addContent(Element source, Element target) {
    final List attributes = source.getAttributes();
    for (Object attribute : attributes) {
      target.setAttribute((Attribute)((Attribute)attribute).clone());
    }
    for (Object child : source.getChildren()) {
      target.addContent((Element)((Element)child).clone());
    }
  }

  public static String generateModuleBcName(ModuleSettings module) {
    return module.getModuleName();
  }

  public static String generateFacetBcName(List<Element> facets, Element facet) {
    // TODO cache names for module
    List<String> names = FlexBuildConfigurationManagerImpl.generateUniqueNames(ContainerUtil.map(facets, new Function<Element, String>() {
      @Override
      public String fun(Element element) {
        return element.getAttributeValue(JpsFacetSerializer.NAME_ATTRIBUTE);
      }
    }));
    return names.get(facets.indexOf(facet));
  }

  private static void readNamespaceAndManifestInfoList(final Element element,
                                                       final FlexBuildConfiguration oldConfig) throws InvalidDataException {
    final List<FlexBuildConfiguration.NamespaceAndManifestFileInfo> namespaceAndManifestFileInfoList =
      new ArrayList<FlexBuildConfiguration.NamespaceAndManifestFileInfo>();

    final Element namespaceAndManifestFileInfoListElement = element.getChild(NAMESPACE_AND_MANIFEST_FILE_INFO_LIST_ELEMENT_NAME);
    if (namespaceAndManifestFileInfoListElement != null) {
      for (final Object namespaceAndManifestFileInfoElement : namespaceAndManifestFileInfoListElement
        .getChildren(FlexBuildConfiguration.NamespaceAndManifestFileInfo.class.getSimpleName())) {
        final FlexBuildConfiguration.NamespaceAndManifestFileInfo
          namespaceAndManifestFileInfo = new FlexBuildConfiguration.NamespaceAndManifestFileInfo();
        DefaultJDOMExternalizer.readExternal(namespaceAndManifestFileInfo, (Element)namespaceAndManifestFileInfoElement);
        namespaceAndManifestFileInfoList.add(namespaceAndManifestFileInfo);
      }
    }
    oldConfig.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST = namespaceAndManifestFileInfoList;
  }

  private static void readConditionalCompilerDefinitionList(final Element element,
                                                            final FlexBuildConfiguration oldConfig) throws InvalidDataException {
    final List<FlexBuildConfiguration.ConditionalCompilationDefinition> conditionalCompilationDefinitionList =
      new ArrayList<FlexBuildConfiguration.ConditionalCompilationDefinition>();

    final Element conditionalCompilerDefinitionListElement = element.getChild(CONDITIONAL_COMPILER_DEFINITION_LIST_ELEMENT_NAME);
    if (conditionalCompilerDefinitionListElement != null) {
      for (final Object conditionalCompilerDefinitionElement : conditionalCompilerDefinitionListElement
        .getChildren(FlexBuildConfiguration.ConditionalCompilationDefinition.class.getSimpleName())) {
        final FlexBuildConfiguration.ConditionalCompilationDefinition
          conditionalCompilationDefinition = new FlexBuildConfiguration.ConditionalCompilationDefinition();
        DefaultJDOMExternalizer.readExternal(conditionalCompilationDefinition, (Element)conditionalCompilerDefinitionElement);
        conditionalCompilationDefinitionList.add(conditionalCompilationDefinition);
      }
    }
    oldConfig.CONDITIONAL_COMPILATION_DEFINITION_LIST = conditionalCompilationDefinitionList;
  }

  private static void readCssFilesList(final Element element,
                                       final FlexBuildConfiguration oldConfig) throws InvalidDataException {
    final List<String> cssFilesList = new ArrayList<String>();

    final Element cssFilesListElement = element.getChild(CSS_FILES_LIST_ELEMENT_NAME);
    if (cssFilesListElement != null) {
      //noinspection unchecked
      for (Element conditionalCompilerDefinitionElement : (Iterable<Element>)cssFilesListElement.getChildren(FILE_PATH_ELEMENT_NAME)) {
        cssFilesList.add(conditionalCompilerDefinitionElement.getValue());
      }
    }
    oldConfig.CSS_FILES_LIST = cssFilesList;
  }
}
