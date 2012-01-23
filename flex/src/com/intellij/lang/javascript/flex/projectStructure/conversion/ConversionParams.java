package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.ModuleSettings;
import com.intellij.facet.FacetManagerImpl;
import com.intellij.facet.impl.invalid.InvalidFacetManagerImpl;
import com.intellij.facet.impl.invalid.InvalidFacetType;
import com.intellij.facet.pointers.FacetPointersManager;
import com.intellij.ide.impl.convert.JDomConvertingUtil;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: ksafonov
 */
public class ConversionParams {
  public String projectSdkName;
  public String projectSdkType;

  private final Collection<Pair<String, String>> myAppModuleAndBCNames = new ArrayList<Pair<String, String>>();

  private final LibraryTable.ModifiableModel myGlobalLibrariesModifiableModel;
  private final ConversionContext myContext;
  private final Collection<String> myFacetsToIgnore = new HashSet<String>();
  private Collection<String> myProjectLibrariesNames;
  private Set<String> myProjectLibrariesToMakeFlex = new HashSet<String>();

  public ConversionParams(ConversionContext context) {
    myContext = context;
    myGlobalLibrariesModifiableModel = ApplicationLibraryTable.getApplicationTable().getModifiableModel();
  }

  private void saveGlobalLibraries() throws CannotConvertException {
    if (myGlobalLibrariesModifiableModel.isChanged()) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          myGlobalLibrariesModifiableModel.commit();
        }
      });
    }
  }

  public void ignoreInvalidFacet(String moduleName, String type, String name) {
    // in Flex IDE facet will be of type 'invalid'
    myFacetsToIgnore.add(FacetPointersManager.constructId(moduleName, InvalidFacetType.TYPE_ID.toString(), name));
  }

  public void apply() throws CannotConvertException {
    saveGlobalLibraries();
    ignoreInvalidFacets();
  }

  private void ignoreInvalidFacets() throws CannotConvertException {
    if (!myFacetsToIgnore.isEmpty()) {
      Element invalidFacetManager = JDomConvertingUtil.findOrCreateComponentElement(myContext.getWorkspaceSettings().getRootElement(),
                                                                                    InvalidFacetManagerImpl.COMPONENT_NAME);
      InvalidFacetManagerImpl.InvalidFacetManagerState state =
        XmlSerializer.deserialize(invalidFacetManager, InvalidFacetManagerImpl.InvalidFacetManagerState.class);
      state.getIgnoredFacets().addAll(myFacetsToIgnore);
      XmlSerializer.serializeInto(state, invalidFacetManager);
    }
  }

  public boolean libraryExists(final String libraryName, final String libraryLevel) throws CannotConvertException {
    if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryLevel)) {
      final LibraryEx library = (LibraryEx)myGlobalLibrariesModifiableModel.getLibraryByName(libraryName);
      return library != null && isApplicableLibrary(library);
    }
    else if (LibraryTablesRegistrar.PROJECT_LEVEL.equals(libraryLevel)) {
      return myProjectLibrariesNames.contains(libraryName);
    }
    else {
      return false;
    }
  }

  private static boolean isApplicableLibrary(final LibraryEx library) {
    return library.getType() == null || library.getType() == FlexLibraryType.getInstance();
  }

  public void changeLibraryTypeToFlex(final String libraryName, final String libraryLevel) throws CannotConvertException {
    if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(libraryLevel)) {
      final Library library = myGlobalLibrariesModifiableModel.getLibraryByName(libraryName);
      final LibraryEx.ModifiableModelEx model = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
      model.setType(FlexLibraryType.getInstance());
      model.setProperties(FlexLibraryType.getInstance().createDefaultProperties());
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
  public static Pair<String, IFlexSdkType.Subtype> getIdeaSdkHomePathAndSubtype(@NotNull String name, @Nullable String type) {
    Sdk sdk = type != null ? ProjectJdkTable.getInstance().findJdk(name, type) : ProjectJdkTable.getInstance().findJdk(name);
    if (sdk == null) {
      return null;
    }
    SdkType sdkType = sdk.getSdkType();
    if (!(sdkType instanceof IFlexSdkType)) {
      return null;
    }
    IFlexSdkType.Subtype subtype = ((IFlexSdkType)sdkType).getSubtype();
    if (subtype != IFlexSdkType.Subtype.Flex && subtype != IFlexSdkType.Subtype.AIR && subtype != IFlexSdkType.Subtype.AIRMobile) {
      return null;
    }

    return Pair.create(sdk.getHomePath(), subtype);
  }

  @NotNull
  public FlexSdk getOrCreateFlexIdeSdk(@NotNull final String homePath) {
    //FlexSdk sdk = myEditor.findOrCreateSdk(homePath);
    //myEditor.setSdkLibraryUsed(new Object(), (LibraryEx)sdk.getLibrary());
    return null;
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

  public Collection<String> getBcNamesForDependency(String moduleName) {
    ModuleSettings moduleSettings = myContext.getModuleSettings(moduleName);
    if (moduleSettings == null) return Collections.emptyList(); // module is missing

    if (FlexIdeModuleConverter.isFlexModule(moduleSettings)) {
      Element flexBuildConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
      if (flexBuildConfigurationElement != null) {
        FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(flexBuildConfigurationElement, FlexBuildConfiguration.class);
        if (oldConfiguration != null && FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE)) {
          return Collections.singletonList(FlexIdeModuleConverter.generateModuleBcName(moduleSettings));
        }
      }
      return Collections.emptyList();
    }

    final List<Element> facets = FlexIdeModuleConverter.getFlexFacets(moduleSettings);
    return ContainerUtil.mapNotNull(facets, new Function<Element, String>() {
      @Override
      public String fun(Element facet) {
        Element oldConfigurationElement = facet.getChild(FacetManagerImpl.CONFIGURATION_ELEMENT);
        if (oldConfigurationElement != null) {
          FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(oldConfigurationElement, FlexBuildConfiguration.class);
          if (oldConfiguration != null && FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE)) {
            return FlexIdeModuleConverter.generateFacetBcName(facets, facet);
          }
        }
        return null;
      }
    });
  }
}
