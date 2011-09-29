package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionProcessor;
import com.intellij.conversion.ModuleSettings;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.ConversionHelper;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.sdk.AirMobileSdkType;
import com.intellij.lang.javascript.flex.sdk.AirSdkType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.impl.*;
import com.intellij.openapi.roots.impl.libraries.LibraryImpl;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xmlb.SkipDefaultValuesSerializationFilters;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Attribute;
import org.jdom.Element;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.UUID;

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
    if (!FlexModuleType.MODULE_TYPE_ID.equals(moduleSettings.getModuleType())) {
      return false;
    }

    return moduleSettings.getComponentElement(FlexBuildConfigurationManagerImpl.COMPONENT_NAME) == null;
  }

  @Override
  public void process(ModuleSettings moduleSettings) throws CannotConvertException {
    FlexBuildConfigurationManagerImpl configurationManager = ConversionHelper.createBuildConfigurationManager();
    ModifiableFlexIdeBuildConfiguration buildConfiguration =
      (ModifiableFlexIdeBuildConfiguration)configurationManager.getBuildConfigurations()[0];
    buildConfiguration.setName(moduleSettings.getModuleName());

    Element flexBuildConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
    FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(flexBuildConfigurationElement, FlexBuildConfiguration.class);
    if (oldConfiguration == null) {
      buildConfiguration.setOutputType(OutputType.Application);
    }
    else {
      if (FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE)) {
        buildConfiguration.setOutputType(OutputType.Library);
      }
      else {
        buildConfiguration.setOutputType(OutputType.Application);
      }

      if (buildConfiguration.getOutputType() == OutputType.Application) {
        buildConfiguration.setMainClass(oldConfiguration.MAIN_CLASS);
      }
      buildConfiguration.setOutputFileName(oldConfiguration.OUTPUT_FILE_NAME);
      //buildConfiguration.setOutputFolder(VfsUtil.urlToPath(CompilerModuleExtension));
    }

    // TODO filter out java libraries
    for (Element orderEntry : moduleSettings.getOrderEntries()) {
      String orderEntryType = orderEntry.getAttributeValue(OrderEntryFactory.ORDER_ENTRY_TYPE_ATTR);
      if (ModuleLibraryOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        Element library = orderEntry.getChild(LibraryImpl.ELEMENT);
        library.setAttribute(LibraryImpl.LIBRARY_TYPE_ATTR, FlexLibraryType.FLEX_LIBRARY.getKindId());
        Element libraryProperties = new Element(LibraryImpl.PROPERTIES_ELEMENT);
        library.getChildren().add(0, libraryProperties);
        String libraryId = UUID.randomUUID().toString();
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
        buildConfiguration.getDependencies().getModifiableEntries().add(moduleLibraryEntry);
      }
      else if (ModuleOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        String moduleName = orderEntry.getAttributeValue(ModuleOrderEntryImpl.MODULE_NAME_ATTR);
        ModifiableBuildConfigurationEntry bcEntry = ConversionHelper.createBuildConfigurationEntry(moduleName, "Unnamed"); // TODO name
        buildConfiguration.getDependencies().getModifiableEntries().add(bcEntry);
      }
      else if (ModuleJdkOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        String sdkName = orderEntry.getAttributeValue(ModuleJdkOrderEntryImpl.JDK_NAME_ATTR);
        String sdkType = orderEntry.getAttributeValue(ModuleJdkOrderEntryImpl.JDK_TYPE_ATTR);
        processSdkEntry(buildConfiguration, sdkName, sdkType);
      }
      else if (InheritedJdkOrderEntryImpl.ENTRY_TYPE.equals(orderEntryType)) {
        processSdkEntry(buildConfiguration, myParams.projectSdkName, myParams.projectSdkType);
      }
    }

    if (buildConfiguration.getTargetPlatform() == TargetPlatform.Web) {
      final SdkEntry sdkEntry = buildConfiguration.getDependencies().getSdkEntry();
      if (sdkEntry != null) {
        final String sdkHome = PathMacroManager.getInstance(ApplicationManager.getApplication()).expandPath(sdkEntry.getHomePath());
        buildConfiguration.getDependencies().setTargetPlayer(getTargetPlayer(oldConfiguration, sdkHome));
      }
    }

    Element componentElement =
      JDomConvertingUtil.findOrCreateComponentElement(moduleSettings.getRootElement(), FlexBuildConfigurationManagerImpl.COMPONENT_NAME);
    Element e = XmlSerializer.serialize(configurationManager.getState(), new SkipDefaultValuesSerializationFilters());
    addContent(e, componentElement);
  }

  private static String getTargetPlayer(final FlexBuildConfiguration oldConfiguration, final String sdkHome) {
    String targetPlayer = null;
    final String[] targetPlayers = getTargetPlayers(sdkHome);
    if (oldConfiguration != null) {
      final Pair<String, String> majorMinor = TargetPlayerUtils.getPlayerMajorMinorVersion(oldConfiguration.TARGET_PLAYER_VERSION);
      if (ArrayUtil.contains(majorMinor.first, targetPlayers)) {
        targetPlayer = majorMinor.first;
      }
      else if (ArrayUtil.contains(majorMinor.first + "." + majorMinor.second, targetPlayers)) {
        targetPlayer = majorMinor.first + "." + majorMinor.second;
      }
    }

    if (targetPlayer == null) {
      targetPlayer = targetPlayers.length > 0 ? targetPlayers[0] : "";
    }
    return targetPlayer;
  }

  private static String[] getTargetPlayers(final String sdkHome) {
    final File playerFolder = new File(sdkHome + "/frameworks/libs/player");
    if (playerFolder.isDirectory()) {
      return playerFolder.list(new FilenameFilter() {
        public boolean accept(final File dir, final String name) {
          return new File(playerFolder, name + "/playerglobal.swc").isFile();
        }
      });
    }

    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  private void processSdkEntry(ModifiableFlexIdeBuildConfiguration buildConfiguration, String sdkName, String sdkType) {
    if (AirMobileSdkType.NAME.equals(sdkType)) {
      buildConfiguration.setTargetPlatform(TargetPlatform.Mobile);
    }
    else if (AirSdkType.NAME.equals(sdkType)) {
      buildConfiguration.setTargetPlatform(TargetPlatform.Desktop);
    }
    else {
      buildConfiguration.setTargetPlatform(TargetPlatform.Web);
    }

    if (sdkName != null) {
      String homePath = myParams.getIdeaSdkHomePath(sdkName);
      if (homePath != null) {
        String sdkId = myParams.getExistingFlexIdeSdkId(homePath);
        if (sdkId == null) {
          sdkId = myParams.requireFlexIdeSdk(homePath);
        }
        SdkEntry sdkEntry = Factory.createSdkEntry(sdkId, homePath);
        // TODO roots dependencies types
        buildConfiguration.getDependencies().setSdkEntry(sdkEntry);
      }
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
