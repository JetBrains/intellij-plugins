package com.intellij.flex;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlexTestUtils {

  @NotNull
  public static String getTestDataPath(@NotNull final String relativePath) {
    return PathManager.getHomePath() + "/flex/flex-tests/testData/" + relativePath;
  }

  public static String getPathToCompleteFlexSdk(final String version) {
    return PathManager.getHomePath() + "/flex/tools/flex-ui-designer/idea-plugin/testData/lib/flex-sdk/" + version;
  }

  public static void setupFlexLib(final Project project, final Class clazz, final String testName) {
    if (JSTestUtils.testMethodHasOption(clazz, testName, JSTestOption.WithFlexLib)) {
      Module[] modules = ModuleManager.getInstance(project).getModules();

      for (Module module : modules) {
        JSTestUtils.addFlexLibrary(false, module, "Flex Lib", true, getTestDataPath("flexlib"), "flexlib.swc", null, null);
      }
    }
  }

  public static String getPathToMockFlex(@NotNull Class clazz, @NotNull String testName) {
    if (JSTestUtils.testMethodHasOption(JSTestUtils.getTestMethod(clazz, testName), JSTestOption.WithGumboSdk)) {
      return getTestDataPath("MockFlexSdk4");
    }
    return getTestDataPath("MockFlexSdk3");
  }

  public static String getPathToMockFlex(JSTestUtils.TestDescriptor testDescriptor) {
    return getPathToMockFlex(testDescriptor.first, testDescriptor.second);
  }

  public static void setupFlexSdk(@NotNull final Module module,
                                  @NotNull String testName,
                                  @NotNull Class clazz,
                                  String pathToFlexSdk,
                                  boolean air) {
    boolean withFlexSdk = JSTestUtils
      .testMethodHasOption(JSTestUtils.getTestMethod(clazz, testName), JSTestOption.WithFlexSdk, JSTestOption.WithGumboSdk,
                           JSTestOption.WithFlexFacet);
    if (withFlexSdk) {
      doSetupFlexSdk(module, pathToFlexSdk, air, getSdkVersion(testName, clazz));
    }
    else {
      JSTestUtils.setupPredefinedLibrary(module, testName, clazz);
    }
  }

  public static Sdk getSdk(JSTestUtils.TestDescriptor testDescriptor) {
    return createSdk(getPathToMockFlex(testDescriptor), getSdkVersion(testDescriptor));
  }

  private static String getSdkVersion(JSTestUtils.TestDescriptor testDescriptor) {
    return getSdkVersion(testDescriptor.second, testDescriptor.first);
  }

  private static String getSdkVersion(String testName, Class clazz) {
    return JSTestUtils.testMethodHasOption(JSTestUtils.getTestMethod(clazz, testName), JSTestOption.WithGumboSdk) ? "4.0.0" : "3.4.0";
  }

  public static void setupFlexSdk(@NotNull final Module module, @NotNull String testName, @NotNull Class clazz) {
    setupFlexSdk(module, testName, clazz, getPathToMockFlex(clazz, testName), false);
  }

  public static void addASDocToSdk(final Module module, final Class clazz, final String testName) {
    AccessToken l = WriteAction.start();
    try {
      final Sdk flexSdk = FlexUtils.getSdkForActiveBC(module);
      final SdkModificator sdkModificator = flexSdk.getSdkModificator();
      VirtualFile docRoot = LocalFileSystem.getInstance().findFileByPath(getPathToMockFlex(clazz, testName) + "/asdoc");
      sdkModificator.addRoot(docRoot, JavadocOrderRootType.getInstance());
      sdkModificator.commitChanges();
    }
    finally {
      l.finish();
    }
  }

  public static void doSetupFlexSdk(final Module module,
                                    final String flexSdkRootPath,
                                    final boolean air,
                                    final String sdkVersion) {
    AccessToken l = WriteAction.start();
    try {
      final Sdk sdk = createSdk(flexSdkRootPath, sdkVersion);

      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        JSTestUtils.modifyBuildConfiguration(module, new Consumer<ModifiableFlexBuildConfiguration>() {
          public void consume(final ModifiableFlexBuildConfiguration bc) {
            bc.setNature(new BuildConfigurationNature(air ? TargetPlatform.Desktop : TargetPlatform.Web, false, OutputType.Application));
            bc.getDependencies().setSdkEntry(Factory.createSdkEntry(sdk.getName()));
          }
        });
      }

      Disposer.register(module, new Disposable() {
        @Override
        public void dispose() {
          AccessToken l = WriteAction.start();
          try {
            final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
            projectJdkTable.removeJdk(sdk);
          }
          finally {
            l.finish();
          }
        }
      });
    }
    finally {
      l.finish();
    }
  }

  public static Sdk createSdk(final String flexSdkRootPath, @Nullable String sdkVersion) {
    return createSdk(flexSdkRootPath, sdkVersion, true);
  }

  public static Sdk createSdk(final String flexSdkRootPath, @Nullable String sdkVersion, final boolean removeExisting) {
    Sdk sdk = WriteCommandAction.runWriteCommandAction(null, new Computable<Sdk>() {
      public Sdk compute() {
        final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
        if (removeExisting) {
          final List<Sdk> existingFlexSdks = projectJdkTable.getSdksOfType(FlexSdkType2.getInstance());
          for (Sdk existingFlexSdk : existingFlexSdks) {
            projectJdkTable.removeJdk(existingFlexSdk);
          }
        }

        final FlexSdkType2 sdkType = FlexSdkType2.getInstance();
        final Sdk sdk = new ProjectJdkImpl(sdkType.suggestSdkName(null, flexSdkRootPath), sdkType, flexSdkRootPath, "");
        sdkType.setupSdkPaths(sdk);
        projectJdkTable.addJdk(sdk);
        return sdk;
      }
    });

    final SdkModificator modificator = sdk.getSdkModificator();
    if (sdkVersion != null) {
      modificator.setVersionString(sdkVersion);
    }
    if (sdk.getHomeDirectory() == null) {
      throw new IllegalArgumentException("Could not find a Flex SDK at " + flexSdkRootPath);
    }
    modificator.addRoot(sdk.getHomeDirectory(), OrderRootType.CLASSES);
    modificator.addRoot(JSTestUtils.getPredefinedLibraryRoot(), OrderRootType.CLASSES);
    modificator.commitChanges();
    return sdk;
  }
}
