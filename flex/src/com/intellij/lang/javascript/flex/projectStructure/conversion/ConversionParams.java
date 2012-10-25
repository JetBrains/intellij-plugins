package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.ModuleSettings;
import com.intellij.facet.impl.invalid.InvalidFacetManagerImpl;
import com.intellij.facet.impl.invalid.InvalidFacetType;
import com.intellij.facet.pointers.FacetPointersManager;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationManagerImpl;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexBuildConfigurationState;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.peer.PeerFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.serialization.JDomSerializationUtil;
import org.jetbrains.jps.model.serialization.facet.JpsFacetSerializer;

import java.util.*;

/**
 * User: ksafonov
 */
public class ConversionParams {
  public static final String OLD_FLEX_SDK_TYPE_NAME = "Flex SDK Type";
  public static final String OLD_AIR_SDK_TYPE_NAME = "AIR SDK Type";
  public static final String OLD_AIR_MOBIE_SDK_TYPE_NAME = "AIR Mobile SDK Type";

  public static final String[] OLD_SDKS_TYPES = new String[]{OLD_FLEX_SDK_TYPE_NAME, OLD_AIR_SDK_TYPE_NAME, OLD_AIR_MOBIE_SDK_TYPE_NAME};

  public String projectSdkName;

  private final Collection<Pair<String, String>> myAppModuleAndBCNames = new ArrayList<Pair<String, String>>();

  private final ConversionContext myContext;
  private final Collection<String> myFacetsToIgnore = new HashSet<String>();
  private Collection<String> myProjectLibrariesNames;
  private Set<String> myProjectLibrariesToMakeFlex = new HashSet<String>();

  public ConversionParams(ConversionContext context) {
    myContext = context;
  }

  public void ignoreInvalidFacet(String moduleName, String type, String name) {
    // this facet will be of type 'invalid'
    myFacetsToIgnore.add(FacetPointersManager.constructId(moduleName, InvalidFacetType.TYPE_ID.toString(), name));
  }

  public void apply() throws CannotConvertException {
    ignoreInvalidFacets();
  }

  private void ignoreInvalidFacets() throws CannotConvertException {
    if (!myFacetsToIgnore.isEmpty()) {
      Element invalidFacetManager = JDomSerializationUtil.findOrCreateComponentElement(myContext.getWorkspaceSettings().getRootElement(),
                                                                                       InvalidFacetManagerImpl.COMPONENT_NAME);
      InvalidFacetManagerImpl.InvalidFacetManagerState state =
        XmlSerializer.deserialize(invalidFacetManager, InvalidFacetManagerImpl.InvalidFacetManagerState.class);
      state.getIgnoredFacets().addAll(myFacetsToIgnore);
      XmlSerializer.serializeInto(state, invalidFacetManager);
    }
  }

  public boolean libraryExists(final String libraryName, final String libraryLevel) throws CannotConvertException {
    if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryLevel)) {
      final LibraryEx library = (LibraryEx)ApplicationLibraryTable.getApplicationTable().getLibraryByName(libraryName);
      return library != null && isApplicableLibrary(library);
    }
    else if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryLevel)) {
      return myProjectLibrariesNames.contains(libraryName);
    }
    else {
      return false;
    }
  }

  public String expandPath(final String path) {
    return myContext.expandPath(path);
  }

  // keep old Flex SDKs for now, after IDEA 11.1 release we may decide to delete them
  public static void convertFlexSdks() {
    final ProjectJdkTable sdkTable = ProjectJdkTable.getInstance();
    final Sdk[] allSdks = sdkTable.getAllJdks();
    final FlexSdkType2 newSdkType = FlexSdkType2.getInstance();
    Map<String, Sdk> homePathToNewSdk = new HashMap<String, Sdk>();
    Collection<Sdk> sdksToAdd = new ArrayList<Sdk>();
    for (Sdk sdk : allSdks) {
      if (sdk.getSdkType() == newSdkType && sdk.getHomePath() != null) {
        homePathToNewSdk.put(sdk.getHomePath(), sdk);
      }
    }

    for (Sdk sdk : allSdks) {
      if (!ArrayUtil.contains(sdk.getSdkType().getName(), OLD_SDKS_TYPES)) {
        continue;
      }

      final String version = sdk.getVersionString();
      if (version == null || (!version.startsWith("3.") && !version.startsWith("4."))) {
        // ignore corrupt SDK
        continue;
      }

      final String homePath = sdk.getHomePath();
      if (homePath == null) {
        continue;
      }


      if (homePathToNewSdk.containsKey(homePath)) {
        continue;
      }

      String newSdkName = SdkConfigurationUtil.createUniqueSdkName(newSdkType, homePath, Arrays.asList(allSdks));
      Sdk newSdk = PeerFactory.getInstance().createProjectJdk(newSdkName, "", homePath, newSdkType);
      newSdkType.setupSdkPaths(newSdk);
      sdksToAdd.add(newSdk);
      homePathToNewSdk.put(homePath, newSdk);
    }

    final AccessToken l = WriteAction.start();
    try {
      for (Sdk sdk : sdksToAdd) {
        sdkTable.addJdk(sdk);
      }
    }
    finally {
      l.finish();
    }
  }

  private static boolean isApplicableLibrary(final LibraryEx library) {
    return library.getKind() == null || library.getKind() == FlexLibraryType.FLEX_LIBRARY;
  }

  public void changeLibraryTypeToFlex(final String libraryName, final String libraryLevel) throws CannotConvertException {
    if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryLevel)) {
      final Library library = ApplicationLibraryTable.getApplicationTable().getLibraryByName(libraryName);
      final LibraryEx.ModifiableModelEx model = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
      model.setKind(FlexLibraryType.FLEX_LIBRARY);
      model.setProperties(FlexLibraryType.FLEX_LIBRARY.createDefaultProperties());
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          model.commit();
        }
      });
    }
    else {
      myProjectLibrariesToMakeFlex.add(libraryName);
    }
  }

  public void setProjectLibrariesNames(final Collection<String> librariesNames) {
    myProjectLibrariesNames = librariesNames;
  }

  public Set<String> getProjectLibrariesToMakeFlex() {
    return myProjectLibrariesToMakeFlex;
  }

  @Nullable
  public static Sdk findNewSdk(@NotNull final String homePath) {
    final List<Sdk> sdks = ProjectJdkTable.getInstance().getSdksOfType(FlexSdkType2.getInstance());
    return ContainerUtil.find(sdks, new Condition<Sdk>() {
      @Override
      public boolean value(final Sdk sdk) {
        return homePath.equals(sdk.getHomePath());
      }
    });
  }

  /**
   * Run configurations will be created for these BCs
   */
  void addAppModuleAndBCName(final String moduleName, final String bcName) {
    myAppModuleAndBCNames.add(Pair.create(moduleName, bcName));
  }

  public Collection<Pair<String, String>> getAppModuleAndBCNames() {
    return myAppModuleAndBCNames;
  }

  public Collection<String> getBcNamesForDependency(String moduleName, final BuildConfigurationNature dependantNature) {
    ModuleSettings moduleSettings = myContext.getModuleSettings(moduleName);
    if (moduleSettings == null) return Collections.emptyList(); // module is missing

    if (FlexModuleConverter.isFlexModule(moduleSettings)) {
      Element flexBuildConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
      if (flexBuildConfigurationElement != null) {
        FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(flexBuildConfigurationElement, FlexBuildConfiguration.class);
        if (oldConfiguration != null && FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE)) {
          return Collections.singletonList(FlexModuleConverter.generateModuleBcName(moduleSettings));
        }
      }
      else {
        // this module might have already been processed
        Element buildConfigManagerElement = moduleSettings.getComponentElement(FlexBuildConfigurationManagerImpl.COMPONENT_NAME);
        if (buildConfigManagerElement != null) {
          FlexBuildConfigurationManagerImpl.State s =
            XmlSerializer.deserialize(buildConfigManagerElement, FlexBuildConfigurationManagerImpl.State.class);

          return ContainerUtil.mapNotNull(s.CONFIGURATIONS, new Function<FlexBuildConfigurationState, String>() {
            @Nullable
            @Override
            public String fun(final FlexBuildConfigurationState bcState) {
              return BCUtils.isApplicableForDependency(dependantNature, bcState.OUTPUT_TYPE) ? bcState.NAME : null;
            }
          });
        }
      }
      return Collections.emptyList();
    }

    final List<Element> facets = FlexModuleConverter.getFlexFacets(moduleSettings);
    return ContainerUtil.mapNotNull(facets, new Function<Element, String>() {
      @Nullable
      @Override
      public String fun(Element facet) {
        Element oldConfigurationElement = facet.getChild(JpsFacetSerializer.CONFIGURATION_TAG);
        if (oldConfigurationElement != null) {
          FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class);
          if (oldConfiguration != null && FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE)) {
            return FlexModuleConverter.generateFacetBcName(facets, facet);
          }
        }
        return null;
      }
    });
  }
}
