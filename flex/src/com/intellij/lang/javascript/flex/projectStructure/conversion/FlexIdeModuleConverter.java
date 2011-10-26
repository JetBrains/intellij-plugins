package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.ModuleSettings;
import com.intellij.facet.FacetManagerImpl;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.impl.*;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.Function;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import gnu.trove.THashMap;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: ksafonov
 */
public class FlexIdeModuleConverter extends ConversionProcessor<ModuleSettings> {

  private final ConversionParams myParams;

  public FlexIdeModuleConverter(ConversionParams params) {
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
    return new ArrayList<Element>(module.getFacetElements(FlexFacet.ID.toString()));
  }

  @Override
  public void process(ModuleSettings moduleSettings) throws CannotConvertException {
    FlexBuildConfigurationManagerImpl configurationManager = ConversionHelper.createBuildConfigurationManager();

    Collection<Element> orderEntriesToAdd = new ArrayList<Element>();
    if (isFlexModule(moduleSettings)) {
      ModifiableFlexIdeBuildConfiguration newConfiguration =
        (ModifiableFlexIdeBuildConfiguration)configurationManager.getBuildConfigurations()[0];
      newConfiguration.setName(generateModuleBcName(moduleSettings));

      Element oldConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
      FlexBuildConfiguration oldConfiguration =
        oldConfigurationElement != null ? XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class) : null;
      processConfiguration(oldConfiguration, newConfiguration, moduleSettings, false, null, null, orderEntriesToAdd);
    }
    else {
      List<Element> flexFacets = getFlexFacets(moduleSettings);
      Set<String> sdkLibrariesIds = new HashSet<String>();
      for (int i = 0; i < flexFacets.size(); i++) {
        Element facet = flexFacets.get(i);
        ModifiableFlexIdeBuildConfiguration newConfiguration;
        if (i == 0) {
          newConfiguration = (ModifiableFlexIdeBuildConfiguration)configurationManager.getBuildConfigurations()[0];
        }
        else {
          newConfiguration = ConversionHelper.createBuildConfiguration(configurationManager);
        }
        newConfiguration.setName(generateFacetBcName(flexFacets, facet));
        Element oldConfigurationElement = facet.getChild(FacetManagerImpl.CONFIGURATION_ELEMENT);
        if (oldConfigurationElement != null) {
          FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class);
          final String facetSdkName = oldConfigurationElement.getAttributeValue(FlexFacetConfigurationImpl.FLEX_SDK_ATTR_NAME);
          processConfiguration(oldConfiguration, newConfiguration, moduleSettings, true, facetSdkName, sdkLibrariesIds, orderEntriesToAdd);
        }
        else {
          processConfiguration(null, newConfiguration, moduleSettings, true, null, sdkLibrariesIds, orderEntriesToAdd);
        }
      }
      moduleSettings.setModuleType(FlexModuleType.MODULE_TYPE_ID);
      moduleSettings.getComponentElement(FacetManagerImpl.COMPONENT_NAME).getChildren(FacetManagerImpl.FACET_ELEMENT).removeAll(flexFacets);
    }

    Element rootManagerElement = JDomConvertingUtil.findOrCreateComponentElement(moduleSettings.getRootElement(),
                                                                                 ModuleSettings.MODULE_ROOT_MANAGER_COMPONENT);
    rootManagerElement.addContent(orderEntriesToAdd);

    Element componentElement =
      JDomConvertingUtil.findOrCreateComponentElement(moduleSettings.getRootElement(), FlexBuildConfigurationManagerImpl.COMPONENT_NAME);
    addContent(ConversionHelper.serialize(configurationManager), componentElement);

    ignoreInapplicableFacets(moduleSettings);
  }

  private void ignoreInapplicableFacets(ModuleSettings module) {
    boolean allowFlexFacets = isJavaModule(module);
    final Element facetManager = module.getComponentElement(FacetManagerImpl.COMPONENT_NAME);
    for (Element facet : JDOMUtil.getChildren(facetManager, FacetManagerImpl.FACET_ELEMENT)) {
      String type = facet.getAttributeValue(FacetManagerImpl.TYPE_ATTRIBUTE);
      if (allowFlexFacets && FlexFacet.ID.toString().equals(type)) {
        continue;
      }
      String name = facet.getAttributeValue(FacetManagerImpl.NAME_ATTRIBUTE);
      myParams.ignoreInvalidFacet(module.getModuleName(), type, name);
    }
  }

  private void processConfiguration(@Nullable FlexBuildConfiguration oldConfiguration,
                                    ModifiableFlexIdeBuildConfiguration newBuildConfiguration,
                                    ModuleSettings module,
                                    boolean facet,
                                    @Nullable String facetSdkName,
                                    @Nullable Set<String> sdkLibrariesIds,
                                    Collection<Element> orderEntriesToAdd) throws CannotConvertException {
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

      final Map<String, String> options = new THashMap<String, String>(newBuildConfiguration.getCompilerOptions().getAllOptions());
      
      if (oldConfiguration.USE_LOCALE_SETTINGS) {
        options.put("compiler.locale", oldConfiguration.LOCALE.replace(',', CompilerOptionInfo.LIST_ENTRIES_SEPARATOR));
      }
      
      if (!oldConfiguration.CONDITIONAL_COMPILATION_DEFINITION_LIST.isEmpty()) {
        final StringBuilder b = new StringBuilder();
        for (FlexBuildConfiguration.ConditionalCompilationDefinition def : oldConfiguration.CONDITIONAL_COMPILATION_DEFINITION_LIST) {
          if (b.length() > 0) b.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
          b.append(def.NAME).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).append(def.VALUE);
        }
        options.put("compiler.define", b.toString());
      }
      
      if (!oldConfiguration.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.isEmpty()) {
        final StringBuilder b = new StringBuilder();
        for (FlexBuildConfiguration.NamespaceAndManifestFileInfo info : oldConfiguration.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST) {
          if (b.length() > 0) b.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
          b.append(info.NAMESPACE).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR).append(info.MANIFEST_FILE_PATH);
          // todo import info.INCLUDE_IN_SWC
        }
        options.put("compiler.namespaces.namespace", b.toString());
      }
      
      if (!oldConfiguration.PATH_TO_SERVICES_CONFIG_XML.isEmpty()) {
        options.put("compiler.services", oldConfiguration.PATH_TO_SERVICES_CONFIG_XML);
        options.put("compiler.context-root", oldConfiguration.CONTEXT_ROOT);
      }

      newBuildConfiguration.getCompilerOptions().setAllOptions(options);
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
        if (facet && AutogeneratedLibraryUtils.isAutogeneratedLibrary(library)) {
          orderEntriesToRemove.add(orderEntry);
          continue;
        }

        Element libraryProperties;
        if (facet && FlexLibraryType.FLEX_LIBRARY.getKindId().equals(library.getAttributeValue(LibraryImpl.LIBRARY_TYPE_ATTR))) {
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
        Collection<String> bcNames = myParams.getBcNamesForDependency(moduleName);
        for (String bcName : bcNames) {
          ModifiableBuildConfigurationEntry bcEntry =
            ConversionHelper.createBuildConfigurationEntry(moduleName, bcName);
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
          String sdkType = orderEntry.getAttributeValue(ModuleJdkOrderEntryImpl.JDK_TYPE_ATTR);
          Element entryToAdd = processSdkEntry(newBuildConfiguration, sdkName, sdkType, null);
          ContainerUtil.addIfNotNull(entryToAdd, orderEntriesToAdd);
        }
        orderEntriesToRemove.add(orderEntry);
      }
      else if (InheritedJdkOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        if (!facet) {
          Element entryToAdd = processSdkEntry(newBuildConfiguration, myParams.projectSdkName, myParams.projectSdkType, null);
          ContainerUtil.addIfNotNull(entryToAdd, orderEntriesToAdd);
        }
        orderEntriesToRemove.add(orderEntry);
      }
    }

    if (facetSdkName != null) {
      Element entryToAdd = processSdkEntry(newBuildConfiguration, facetSdkName, null, sdkLibrariesIds);
      ContainerUtil.addIfNotNull(entryToAdd, orderEntriesToAdd);
    }

    if (!orderEntriesToRemove.isEmpty()) {
      module.getOrderEntries().removeAll(orderEntriesToRemove);
    }

    if (newBuildConfiguration.getTargetPlatform() == TargetPlatform.Web) {
      final SdkEntry sdkEntry = newBuildConfiguration.getDependencies().getSdkEntry();
      if (sdkEntry != null) {
        final String sdkHome = PathUtil.getCanonicalPath(module.expandPath(sdkEntry.getHomePath()));
        final String targetPlayer =
          TargetPlayerUtils.getTargetPlayer(oldConfiguration == null ? null : oldConfiguration.TARGET_PLAYER_VERSION, sdkHome);
        newBuildConfiguration.getDependencies().setTargetPlayer(targetPlayer);
      }
    }
  }

  private static void convertDependencyType(Element orderEntry, ModifiableDependencyType dependencyType) {
    DependencyScope scope = DependencyScope.readExternal(orderEntry);
    boolean isExported = orderEntry.getAttribute(ModuleLibraryOrderEntryImpl.EXPORTED_ATTR) != null;
    if (scope == DependencyScope.PROVIDED) {
      dependencyType.setLinkageType(LinkageType.External);
    }
    else if (isExported) {
      dependencyType.setLinkageType(LinkageType.Include);
    }
    else {
      dependencyType.setLinkageType(LinkageType.Merged);
    }
  }

  private static String getOutputFolder(final ModuleSettings moduleSettings) {
    final Element rootManagerElement = moduleSettings.getComponentElement(ModuleSettings.MODULE_ROOT_MANAGER_COMPONENT);
    if (rootManagerElement != null) {
      final boolean inheritOutput = "true".equals(rootManagerElement.getAttributeValue("inherit-compiler-output"));
      if (!inheritOutput) {
        final Element outputElement = rootManagerElement.getChild("output");
        final String outputUrl = outputElement == null ? null : outputElement.getAttributeValue("url");
        if (outputUrl != null) {
          String path = PathUtil.getCanonicalPath(VfsUtil.urlToPath(moduleSettings.expandPath(outputUrl)));
          return moduleSettings.collapsePath(path);
        }
      }
    }

    final String projectOutputUrl = moduleSettings.getProjectOutputUrl();
    String path = projectOutputUrl == null ? "" :
                  VfsUtil.urlToPath(moduleSettings.expandPath(projectOutputUrl) +
                                    "/" + CompilerModuleExtension.PRODUCTION + "/" + moduleSettings.getModuleName());
    return moduleSettings.collapsePath(path);
  }

  /**
   * @return order entry element to be added to the module
   */
  @Nullable
  private Element processSdkEntry(ModifiableFlexIdeBuildConfiguration buildConfiguration,
                                  String ideaSdkName,
                                  @Nullable String ideaSdkType,
                                  @Nullable Set<String> existingSdkLibrariesIds) {
    if (ideaSdkName == null) {
      buildConfiguration.setTargetPlatform(TargetPlatform.Web);
      return null;
    }

    Pair<String, IFlexSdkType.Subtype> homePathAndSubtype = ConversionParams.getIdeaSdkHomePathAndSubtype(ideaSdkName, ideaSdkType);
    if (homePathAndSubtype == null) {
      buildConfiguration.setTargetPlatform(TargetPlatform.Web);
      return null;
    }

    if (IFlexSdkType.Subtype.AIRMobile == homePathAndSubtype.second) {
      buildConfiguration.setTargetPlatform(TargetPlatform.Mobile);
      buildConfiguration.getAndroidPackagingOptions().setEnabled(true);
    }
    else if (IFlexSdkType.Subtype.AIR == homePathAndSubtype.second) {
      buildConfiguration.setTargetPlatform(TargetPlatform.Desktop);
    }
    else {
      buildConfiguration.setTargetPlatform(TargetPlatform.Web);
    }

    FlexSdk sdk = myParams.getOrCreateFlexIdeSdk(homePathAndSubtype.first);
    SdkEntry sdkEntry = Factory.createSdkEntry(sdk.getLibraryId(), sdk.getHomePath());
    // TODO roots dependencies types
    buildConfiguration.getDependencies().setSdkEntry(sdkEntry);

    if (existingSdkLibrariesIds == null || existingSdkLibrariesIds.add(sdk.getLibraryId())) {
      Element orderEntryElement = new Element(OrderEntryFactory.ORDER_ENTRY_ELEMENT_NAME);
      orderEntryElement.setAttribute(OrderEntryFactory.ORDER_ENTRY_TYPE_ATTR, "library");
      orderEntryElement.setAttribute("name", sdk.getLibrary().getName());
      orderEntryElement.setAttribute("level", LibraryTablesRegistrar.APPLICATION_LEVEL);
      return orderEntryElement;
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
        return element.getAttributeValue(FacetManagerImpl.NAME_ATTRIBUTE);
      }
    }));
    return names.get(facets.indexOf(facet));
  }
}
