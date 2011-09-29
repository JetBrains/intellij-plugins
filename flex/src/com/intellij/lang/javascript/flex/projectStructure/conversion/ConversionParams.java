package com.intellij.lang.javascript.flex.projectStructure.conversion;

import com.intellij.conversion.CannotConvertException;
import com.intellij.conversion.ConversionContext;
import com.intellij.conversion.ModuleSettings;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public class ConversionParams {
  public String projectSdkName;
  public String projectSdkType;

  private final FlexProjectConfigurationEditor myEditor;
  private final LibraryTable.ModifiableModel myGlobalLibrariesModifiableModel;
  private final ConversionContext myContext;

  public ConversionParams(ConversionContext context) {
    myContext = context;
    myGlobalLibrariesModifiableModel = ApplicationLibraryTable.getApplicationTable().getModifiableModel();
    myEditor = new FlexProjectConfigurationEditor(null, new FlexProjectConfigurationEditor.ProjectModifiableModelProvider() {
      @Override
      public Module[] getModules() {
        return new Module[0];
      }

      @Override
      public ModifiableRootModel getModuleModifiableModel(Module module) {
        return null;
      }

      @Override
      public void addListener(FlexIdeBCConfigurator.Listener listener, Disposable parentDisposable) {
      }

      @Override
      public void commitModifiableModels() throws ConfigurationException {
      }

      @Override
      public LibraryTableBase.ModifiableModelEx getGlobalLibrariesModifiableModel() {
        return (LibraryTableBase.ModifiableModelEx)myGlobalLibrariesModifiableModel;
      }
    });
  }

  public void saveGlobalLibraries() throws CannotConvertException {
    if (myEditor.isModified()) {
      try {
        myEditor.commit();
      }
      catch (ConfigurationException e) {
        throw new CannotConvertException(e.getMessage(), e);
      }
    }

    if (myGlobalLibrariesModifiableModel.isChanged()) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        @Override
        public void run() {
          myGlobalLibrariesModifiableModel.commit();
        }
      });
    }
  }

  @Nullable
  public static String getIdeaSdkHomePath(@NotNull String name) {
    Sdk sdk = ProjectJdkTable.getInstance().findJdk(name);
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

    return sdk.getHomePath();
  }

  @NotNull
  public FlexSdk getOrCreateFlexIdeSdk(@NotNull final String homePath) {
    FlexSdk sdk = myEditor.findOrCreateSdk(homePath);
    myEditor.setSdkLibraryUsed(new Object(), (LibraryEx)sdk.getLibrary());
    return sdk;
  }

  public boolean isApplicableForDependency(String moduleName) {
    ModuleSettings moduleSettings = myContext.getModuleSettings(moduleName);
    if (moduleSettings == null) return false; // module is missing
    if (!FlexIdeModuleConverter.isFlexModule(moduleSettings)) return false; // non-Flex module
    Element flexBuildConfigurationElement = moduleSettings.getComponentElement(FlexBuildConfiguration.COMPONENT_NAME);
    if (flexBuildConfigurationElement == null) return false;
    FlexBuildConfiguration oldConfiguration = XmlSerializer.deserialize(flexBuildConfigurationElement, FlexBuildConfiguration.class);
    return oldConfiguration != null && FlexBuildConfiguration.LIBRARY.equals(oldConfiguration.OUTPUT_TYPE);
  }
}
