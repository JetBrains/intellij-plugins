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
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.impl.*;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: ksafonov
 */
class FlexIdeModuleConverter extends ConversionProcessor<ModuleSettings> {

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
    if (FlexModuleType.MODULE_TYPE_ID.equals(moduleSettings.getModuleType())) {
      return true;
    }
    if (StdModuleTypes.JAVA.getId().equals(moduleSettings.getModuleType())) {
      Collection<? extends Element> facetElements = moduleSettings.getFacetElements(FlexFacet.ID.toString());
      return !facetElements.isEmpty();
    }
    return false;
  }

  @Override
  public void process(ModuleSettings moduleSettings) throws CannotConvertException {
    FlexBuildConfigurationManagerImpl configurationManager = ConversionHelper.createBuildConfigurationManager();

    if (FlexModuleType.MODULE_TYPE_ID.equals(moduleSettings.getModuleType())) {
      ModifiableFlexIdeBuildConfiguration newConfiguration =
        (ModifiableFlexIdeBuildConfiguration)configurationManager.getBuildConfigurations()[0];
      newConfiguration.setName(moduleSettings.getModuleName());

      Element oldConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
      FlexBuildConfiguration oldConfiguration =
        oldConfigurationElement != null ? XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class) : null;
      processConfiguration(oldConfiguration, newConfiguration, moduleSettings, false, null, null);
    }
    else {
      List<Element> flexFacets = new ArrayList<Element>(moduleSettings.getFacetElements(FlexFacet.ID.toString()));
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
        newConfiguration.setName(facet.getAttributeValue(FacetManagerImpl.NAME_ATTRIBUTE));
        Element oldConfigurationElement = facet.getChild(FacetManagerImpl.CONFIGURATION_ELEMENT);
        if (oldConfigurationElement != null) {
          FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class);
          final String facetSdkName = oldConfigurationElement.getAttributeValue(FlexFacetConfigurationImpl.FLEX_SDK_ATTR_NAME);
          processConfiguration(oldConfiguration, newConfiguration, moduleSettings, true, facetSdkName, sdkLibrariesIds);
        }
        else {
          processConfiguration(null, newConfiguration, moduleSettings, true, null, sdkLibrariesIds);
        }
      }
      moduleSettings.setModuleType(FlexModuleType.MODULE_TYPE_ID);
      moduleSettings.getComponentElement(FacetManagerImpl.COMPONENT_NAME).getChildren(FacetManagerImpl.FACET_ELEMENT).removeAll(flexFacets);
    }

    Element componentElement =
      JDomConvertingUtil.findOrCreateComponentElement(moduleSettings.getRootElement(), FlexBuildConfigurationManagerImpl.COMPONENT_NAME);
    addContent(ConversionHelper.serialize(configurationManager), componentElement);
  }

  private void processConfiguration(@Nullable FlexBuildConfiguration oldConfiguration,
                                    ModifiableFlexIdeBuildConfiguration newBuildConfiguration,
                                    ModuleSettings module,
                                    boolean facet,
                                    @Nullable String facetSdkName,
                                    @Nullable Set<String> sdkLibrariesIds) {
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
    Collection<Element> orderEntriesToAdd = new ArrayList<Element>();
    // TODO filter out java libraries and remove their order entries
    for (Element orderEntry : module.getOrderEntries()) {
      String orderEntryType = orderEntry.getAttributeValue(OrderEntryFactory.ORDER_ENTRY_TYPE_ATTR);
      if (ModuleLibraryOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        Element library = orderEntry.getChild(LibraryImpl.ELEMENT);
        if (facet && AutogeneratedLibraryUtils.isAutogeneratedLibrary(library)) {
          orderEntriesToRemove.add(orderEntry);
          continue;
        }

        library.setAttribute(LibraryImpl.LIBRARY_TYPE_ATTR, FlexLibraryType.FLEX_LIBRARY.getKindId());
        Element libraryProperties = new Element(LibraryImpl.PROPERTIES_ELEMENT);
        //noinspection unchecked
        library.getChildren().add(0, libraryProperties);
        String libraryId = FlexLibraryIdGenerator.generateId();
        XmlSerializer.serializeInto(new FlexLibraryProperties(libraryId), libraryProperties);

        ModifiableModuleLibraryEntry moduleLibraryEntry = ConversionHelper.createModuleLibraryEntry(libraryId);
        DependencyScope scope = DependencyScope.readExternal(orderEntry);
        boolean isExported = orderEntry.getAttribute(ModuleLibraryOrderEntryImpl.EXPORTED_ATTR) != null;
        if (scope == DependencyScope.PROVIDED) {
          moduleLibraryEntry.getDependencyType().setLinkageType(LinkageType.External);
        }
        else if (isExported) {
          moduleLibraryEntry.getDependencyType().setLinkageType(LinkageType.Include);
        }
        else {
          moduleLibraryEntry.getDependencyType().setLinkageType(LinkageType.Merged);
        }
        newBuildConfiguration.getDependencies().getModifiableEntries().add(moduleLibraryEntry);
      }
      else if (ModuleOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        String moduleName = orderEntry.getAttributeValue(ModuleOrderEntryImpl.MODULE_NAME_ATTR);
        if (myParams.isApplicableForDependency(moduleName)) {
          ModifiableBuildConfigurationEntry bcEntry = ConversionHelper.createBuildConfigurationEntry(moduleName, moduleName); // TODO
          newBuildConfiguration.getDependencies().getModifiableEntries().add(bcEntry);
        }
        else {
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
    final Element rootManagerElement = module.getComponentElement(ModuleSettings.MODULE_ROOT_MANAGER_COMPONENT);
    if (rootManagerElement != null) {
      for (Element entryToAdd : orderEntriesToAdd) {
        rootManagerElement.addContent(entryToAdd);
      }
    }

    if (newBuildConfiguration.getTargetPlatform() == TargetPlatform.Web) {
      final SdkEntry sdkEntry = newBuildConfiguration.getDependencies().getSdkEntry();
      if (sdkEntry != null) {
        final String sdkHome = PathUtil.getCanonicalPath(module.expandPath(sdkEntry.getHomePath()));
        newBuildConfiguration.getDependencies().setTargetPlayer(TargetPlayerUtils.getTargetPlayer(oldConfiguration.TARGET_PLAYER_VERSION,
                                                                                                  sdkHome));
      }
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
}
