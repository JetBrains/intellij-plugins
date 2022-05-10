// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.bc;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.FlexCompilerConfigFileUtilBase;
import com.intellij.flex.model.bc.ComponentSet;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.CompilerConfigGenerator;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitPrecompileTask;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.impl.RootProviderBaseImpl;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.serialization.PathMacroUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FlexCompilerConfigTest extends HeavyPlatformTestCase {

  private static final String[] SDK_3_ROOTS = new String[]{
    "/frameworks/libs/player/9/playerglobal.swc",
    "/frameworks/libs/player/10/playerglobal.swc",

    "/frameworks/libs/air/airglobal.swc",

    "/frameworks/libs/air/airframework.swc",
    "/frameworks/libs/air/applicationupdater.swc",
    "/frameworks/libs/air/applicationupdater_ui.swc",
    "/frameworks/libs/air/servicemonitor.swc",

    "/frameworks/libs/flex.swc",
    "/frameworks/libs/framework.swc",
    "/frameworks/libs/rpc.swc",
    "/frameworks/libs/utilities.swc",
  };

  @Override
  protected void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "compilerConfig");
    super.setUp();
  }

  private static final String[] SDK_40_ROOTS = new String[]{
    "/frameworks/libs/player/10.0/playerglobal.swc",

    "/frameworks/libs/air/airglobal.swc",

    "/frameworks/libs/air/airframework.swc",
    "/frameworks/libs/air/airspark.swc",
    "/frameworks/libs/air/applicationupdater.swc",
    "/frameworks/libs/air/applicationupdater_ui.swc",
    "/frameworks/libs/air/servicemonitor.swc",

    "/frameworks/libs/datavisualization.swc",
    "/frameworks/libs/flash-integration.swc",
    "/frameworks/libs/flex.swc",
    "/frameworks/libs/framework.swc",
    "/frameworks/libs/osmf.swc",
    "/frameworks/libs/rpc.swc",
    "/frameworks/libs/spark.swc",
    "/frameworks/libs/sparkskins.swc",
    "/frameworks/libs/textLayout.swc",
    "/frameworks/libs/utilities.swc",
  };

  private static final String[] SDK_45_ROOTS = new String[]{
    "/frameworks/libs/player/10.0/playerglobal.swc",
    "/frameworks/libs/player/10.1/playerglobal.swc",
    "/frameworks/libs/player/10.2/playerglobal.swc",
    "/frameworks/libs/player/10.3/playerglobal.swc",
    "/frameworks/libs/player/11/playerglobal.swc",
    "/frameworks/libs/player/11.1/playerglobal.swc",

    "/frameworks/libs/air/airglobal.swc",

    "/frameworks/libs/air/aircore.swc",
    "/frameworks/libs/air/airframework.swc",
    "/frameworks/libs/air/airspark.swc",
    "/frameworks/libs/air/applicationupdater.swc",
    "/frameworks/libs/air/applicationupdater_ui.swc",
    "/frameworks/libs/air/servicemonitor.swc",

    "/frameworks/libs/advancedgrids.swc",
    "/frameworks/libs/authoringsupport.swc",
    "/frameworks/libs/charts.swc",
    "/frameworks/libs/core.swc",
    "/frameworks/libs/flash-integration.swc",
    "/frameworks/libs/framework.swc",
    "/frameworks/libs/osmf.swc",
    "/frameworks/libs/rpc.swc",
    "/frameworks/libs/spark.swc",
    "/frameworks/libs/spark_dmv.swc",
    "/frameworks/libs/sparkskins.swc",
    "/frameworks/libs/textLayout.swc",

    "/frameworks/libs/mobile/mobilecomponents.swc",

    "/frameworks/libs/mx/mx.swc",
  };

  private static final String[] SDK_46_ROOTS = SDK_45_ROOTS;

  private static final String[] AIR_SDK_ROOTS = new String[]{
    "/frameworks/libs/player/11.6/playerglobal.swc",

    "/frameworks/libs/air/airglobal.swc",

    "/frameworks/libs/air/aircore.swc",
    "/frameworks/libs/air/airframework.swc",
    "/frameworks/libs/air/applicationupdater.swc",
    "/frameworks/libs/air/applicationupdater_ui.swc",
    "/frameworks/libs/air/servicemonitor.swc",

    "/frameworks/libs/asc-support.swc",
    "/frameworks/libs/core.swc",
  };

  private static final String TEST_FLEX_SDK_NAME = "Test Flex SDK";

  private static String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("compilerConfig") + "/";
  }

  @NotNull
  @Override
  protected ModuleType<?> getModuleType() {
    return FlexModuleType.getInstance();
  }

  @NotNull
  @Override
  protected Module createMainModule() throws IOException {
    Module module = super.createMainModule();
    WriteCommandAction.writeCommandAction(myProject).run(() -> {
      Path dir = module.getModuleNioFile().getParent().resolve("src");
      Files.createDirectories(dir);
      VirtualFile src = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(dir);
      PsiTestUtil.addContentRoot(module, src.getParent());
      PsiTestUtil.addSourceRoot(module, src);
    });
    return module;
  }

  private static ModifiableFlexBuildConfiguration createBuildConfiguration(final TargetPlatform targetPlatform,
                                                                           final boolean isPureAS,
                                                                           final OutputType outputType,
                                                                           final String targetPlayer) {
    final ModifiableFlexBuildConfiguration bc = Factory.createBuildConfiguration();

    bc.setTargetPlatform(targetPlatform);
    bc.setPureAs(isPureAS);
    bc.setOutputType(outputType);
    bc.setMainClass("Main");
    setOutputPaths(bc);

    bc.getDependencies().setSdkEntry(Factory.createSdkEntry(TEST_FLEX_SDK_NAME));
    bc.getDependencies().setTargetPlayer(targetPlayer);
    bc.getDependencies().setComponentSet(ComponentSet.SparkAndMx);
    bc.getDependencies().setFrameworkLinkage(LinkageType.Default);

    return bc;
  }

  private static void setOutputPaths(final ModifiableFlexBuildConfiguration bc) {
    bc.setOutputFileName(bc.getName() + (bc.getOutputType() == OutputType.Library ? ".swc" : ".swf"));
    bc.setOutputFolder("output/folder");
  }

  private void doTest(final String sdkVersion, final FlexBuildConfiguration bc) throws Exception {
    doTest(sdkVersion, bc, Factory.createCompilerOptions(), Factory.createCompilerOptions(), "", null);
  }

  private void doTest(final String sdkVersion, final FlexBuildConfiguration bc,
                      final CompilerOptions moduleLevelOptions, final CompilerOptions projectLevelOptions,
                      final String suffix,
                      @Nullable Map<String, String> additionalMacros) throws Exception {
    WriteCommandAction.runWriteCommandAction(null, () -> ProjectJdkTable.getInstance().addJdk(createTestSdk(sdkVersion), getTestRootDisposable()));

    final Constructor<CompilerConfigGenerator> constructor =
      CompilerConfigGenerator.class.getDeclaredConstructor(Module.class, FlexBuildConfiguration.class,
                                                           CompilerOptions.class, CompilerOptions.class);
    constructor.setAccessible(true);

    final CompilerConfigGenerator configGenerator = constructor.newInstance(getModule(), bc, moduleLevelOptions, projectLevelOptions);

    final Method method = CompilerConfigGenerator.class.getDeclaredMethod("generateConfigFileText");
    method.setAccessible(true);
    String text = (String)method.invoke(configGenerator);

    if (bc.isTempBCForCompilation()) {
      text = FlexCompilerConfigFileUtilBase
        .mergeWithCustomConfigFile(text, bc.getCompilerOptions().getAdditionalConfigFilePath(), true, false);
    }

    VirtualFile expectedFile = getVirtualFile(getTestName(false) + suffix + ".xml");
    String expectedText = StringUtil.convertLineSeparators(VfsUtilCore.loadText(expectedFile));
    assertEquals(expectedFile.getName(), replaceMacros(expectedText, bc.getSdk(), additionalMacros), text);
  }

  private static Sdk createTestSdk(final String sdkVersion) {
    return new MySdk(sdkVersion);
  }

  private static Map<String, String> createMap(String... keysAndValues) {
    final Map<String, String> result = new HashMap<>();
    for (int i = 0; i < keysAndValues.length; i++) {
      //noinspection AssignmentToForLoopParameter
      result.put(keysAndValues[i], keysAndValues[++i]);
    }
    return result;
  }

  public void testFlexAppSdk45() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Application, "11.1");
    bc.getCompilerOptions().setAllOptions(createMap("compiler.locale", "ru_RU\nen_US\nfr_FR",
                                                    "compiler.context-root", "context-root",
                                                    "compiler.define", "a\tb\nc\td",
                                                    "compiler.namespaces.namespace", "A\tB"));
    doTest("4.5.1.21328", bc);
  }

  public void testFlexLibSdk45() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Library, "11");
    bc.getCompilerOptions().setAllOptions(createMap("compiler.theme", "custom",
                                                    "compiler.namespaces.namespace", "A\tB"));
    doTest("4.5.0", bc);
  }

  public void testASAppSdk45() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, true, OutputType.Application, "10.3");
    final ModifiableCompilerOptions moduleLevelOptions = Factory.createCompilerOptions();
    final ModifiableCompilerOptions projectLevelOptions = Factory.createCompilerOptions();

    bc.getCompilerOptions().setAllOptions(createMap("compiler.debug.swc", "false",
                                                    "compiler.services", "BC",
                                                    "compiler.defaults-css-files", "BC",
                                                    "compiler.defaults-css-url", "BC"));

    moduleLevelOptions.setAllOptions(createMap("compiler.services", "MODULE",
                                               "compiler.context-root", "MODULE",
                                               "compiler.defaults-css-files", "MODULE",
                                               "compiler.keep-as3-metadata", "MODULE"));

    projectLevelOptions.setAllOptions(createMap("compiler.services", "PROJECT",
                                                "compiler.context-root", "PROJECT",
                                                "compiler.defaults-css-url", "PROJECT",
                                                "compiler.keep-generated-actionscript", "PROJECT"));

    doTest("4.5.0", bc, moduleLevelOptions, projectLevelOptions, "", null);
  }

  public void testAirAppSdk45() throws Exception {
    doTest("4.6.0", createBuildConfiguration(TargetPlatform.Desktop, false, OutputType.Application, "10.2"));
  }

  public void testMobileAppSdk45() throws Exception {
    doTest("4.5.0", createBuildConfiguration(TargetPlatform.Mobile, false, OutputType.Application, "10.1"));
  }

  public void testFlexAppSdk41() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Application, "10.0");
    final ModifiableCompilerOptions projectLevelOptions = Factory.createCompilerOptions();
    projectLevelOptions.setAllOptions(createMap("compiler.debug.swf", "false"));
    bc.getCompilerOptions().setAllOptions(createMap("compiler.locale", "",
                                                    "metadata.creator", "<><>"));
    doTest("4.1.0.16076", bc, Factory.createCompilerOptions(), projectLevelOptions, "", null);
  }

  public void testFlexLibSdk40() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Library, "10.0");
    bc.getDependencies().setFrameworkLinkage(LinkageType.Merged);
    bc.getCompilerOptions().setAllOptions(createMap("compiler.debug.swc", "false",
                                                    "compiler.namespaces.namespace", "A\tB\nC\tD"));
    doTest("4.0.0.14159", bc);
  }

  public void testFlexLibMxOnlySdk40() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Library, "10.0");
    bc.getDependencies().setFrameworkLinkage(LinkageType.Merged);
    bc.getDependencies().setComponentSet(ComponentSet.MxOnly);
    doTest("4.0.0.14159", bc);
  }

  public void testFlexAppSparkOnlySdk46() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Application, "10.0");
    bc.getDependencies().setFrameworkLinkage(LinkageType.Merged);
    bc.getDependencies().setComponentSet(ComponentSet.SparkOnly);
    doTest("4.6.0", bc);
  }

  public void testFlexAppSdk3() throws Exception {
    doTest("3.6.0.16995", createBuildConfiguration(TargetPlatform.Web, false, OutputType.Application, "9"));
  }

  public void testFlexAppSdk3Rsl() throws Exception {
    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Application, "9");
    bc.getDependencies().setFrameworkLinkage(LinkageType.RSL);
    bc.getCompilerOptions().setAdditionalOptions("-services=some_path");
    doTest("3.6.0.16995", bc);
  }

  public void testMergeWithAdditionalConfigFile() throws Exception {
    final String sdkVersion = "4.6.0";

    final ModifiableFlexBuildConfiguration bc = createBuildConfiguration(TargetPlatform.Web, false, OutputType.Application, "11.1");
    VirtualFile f = getVirtualFile(getTestName(false) + "_config.xml");
    ApplicationManager.getApplication().runWriteAction(() -> {
      VirtualFile additionalConfigFile;
      try {
        additionalConfigFile = FlexUtils.addFileWithContent(f.getName(),
                                                            replaceMacros(VfsUtilCore.loadText(f), createTestSdk(sdkVersion), null),
                                                            LocalFileSystem.getInstance().refreshAndFindFileByNioFile(myModule.getModuleNioFile().getParent()));
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      bc.setOutputFileName("SetInBC.swf");
      bc.getCompilerOptions().setAdditionalConfigFilePath(additionalConfigFile.getPath());
    });

    bc.getCompilerOptions().setAllOptions(createMap("compiler.locale", "en_US\nja_JP",
                                                    "compiler.services", "services"));
    doTest(sdkVersion, Factory.getTemporaryCopyForCompilation(bc));
  }

  private String replaceMacros(String text, final Sdk sdk, @Nullable Map<String, String> additionalMacros) {
    text = text.replace(PathMacroUtil.DEPRECATED_MODULE_DIR, FileUtil.toSystemIndependentName(myModule.getModuleNioFile().getParent().toString()));
    text = text.replace("$FLEX_SDK$", sdk.getHomePath());
    if (additionalMacros != null) {
      for (String key : additionalMacros.keySet()) {
        text = text.replace(key, additionalMacros.get(key));
      }
    }
    return text;
  }

  public void testTestDependencies() throws Exception {
    final Module module2 = FlexTestUtils.createModule(myProject, "module2", getVirtualFile("m2"));
    final Module module3 = FlexTestUtils.createModule(myProject, "module3", getVirtualFile("m3"));

    VirtualFile moduleDir = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(myModule.getModuleNioFile().getParent());
    FlexTestUtils.addFlexLibrary(false, myModule, "Lib", true, getTestDataPath(), "Lib", null, null, LinkageType.Test, moduleDir);
    FlexTestUtils.addFlexLibrary(false, myModule, "Lib2", true, getTestDataPath(), "Lib2", null, null, LinkageType.Merged, moduleDir);
    FlexTestUtils.addFlexLibrary(true, myModule, "Lib3", true, getTestDataPath(), "Lib3", null, null, LinkageType.Test, moduleDir);
    FlexTestUtils.addFlexLibrary(true, myModule, "Lib4", true, getTestDataPath(), "Lib4", null, null, LinkageType.Merged, moduleDir);

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      bc1.getDependencies().setSdkEntry(Factory.createSdkEntry(TEST_FLEX_SDK_NAME));
      bc1.setName("bc1");
      setOutputPaths(bc1);

      ModifiableFlexBuildConfiguration bc2 = editor.getConfigurations(module2)[0];
      bc2.setOutputType(OutputType.Library);
      bc2.setName("bc2");
      setOutputPaths(bc2);

      ModifiableFlexBuildConfiguration bc3 = editor.getConfigurations(module3)[0];
      bc3.setOutputType(OutputType.Library);
      bc2.setName("bc3");
      setOutputPaths(bc3);

      ModifiableBuildConfigurationEntry entry1 = editor.createBcEntry(bc1.getDependencies(), bc2, null);
      entry1.getDependencyType().setLinkageType(LinkageType.Test);
      bc1.getDependencies().getModifiableEntries().add(entry1);

      ModifiableBuildConfigurationEntry entry2 = editor.createBcEntry(bc2.getDependencies(), bc3, null);
      entry2.getDependencyType().setLinkageType(LinkageType.Test);
      bc2.getDependencies().getModifiableEntries().add(entry2);
    });

    final FlexBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(myModule).getActiveConfiguration();
    ModifiableFlexBuildConfiguration tempBc = Factory.getTemporaryCopyForCompilation(bc);
    doTest("4.5.1.21328", tempBc, Factory.createCompilerOptions(), Factory.createCompilerOptions(), "_1", null);

    tempBc.setMainClass(FlexCommonUtils.FLEX_UNIT_LAUNCHER);
    Map<String, String> map = new HashMap<>();
    map.put("$FLEX_UNIT_TEMP_FOLDER$", FlexUnitPrecompileTask.getPathToFlexUnitTempDirectory(myProject));
    map.put("$FLEX_DIR$", PathUtil.getParentPath(PathUtil.getParentPath(FlexTestUtils.getTestDataPath(""))));

    String path = FileUtil.toSystemIndependentName(FlexCommonUtils.getPathToBundledJar(""));
    VfsRootAccess.allowRootAccess(getTestRootDisposable(), path);
    doTest("4.5.1.21328", tempBc, Factory.createCompilerOptions(), Factory.createCompilerOptions(), "_2", map);
  }

  private static VirtualFile getVirtualFile(@NonNls String filePath) {
    String fullPath = getTestDataPath() + filePath;

    final VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(fullPath.replace(File.separatorChar, '/'));
    assertNotNull("file " + fullPath + " not found", vFile);
    return vFile;
  }

  public void testAirSdk() throws Exception {
    doTest("AIR SDK 3.6.0.5990", createBuildConfiguration(TargetPlatform.Web, true, OutputType.Application, "11.6"));
  }

  private static class MySdk implements Sdk {
    private final String mySdkVersion;

    private MySdk(String sdkVersion) { mySdkVersion = sdkVersion; }

    @Override
    @NotNull
    public SdkType getSdkType() {
      return FlexSdkType2.getInstance();
    }

    @Override
    @NotNull
    public String getName() {
      return TEST_FLEX_SDK_NAME;
    }

    @Override
    public String getVersionString() {
      return mySdkVersion;
    }

    @Override
    public String getHomePath() {
      return getTestDataPath() +
             (mySdkVersion.startsWith("AIR SDK ") ? "air_sdk" : "flex_sdk_" + mySdkVersion.substring(0, "0.0.0".length()));
    }

    @Override
    public VirtualFile getHomeDirectory() {
      return null;
    }

    @Override
    @NotNull
    public RootProvider getRootProvider() {
      return new MyRootProvider(mySdkVersion);
    }

    @Override
    @NotNull
    public SdkModificator getSdkModificator() {
      throw new UnsupportedOperationException();
    }

    @Override
    public SdkAdditionalData getSdkAdditionalData() {
      return null;
    }

    @Override
    public <T> T getUserData(@NotNull final Key<T> key) {
      return null;
    }

    @Override
    public <T> void putUserData(@NotNull final Key<T> key, @Nullable final T value) {
    }

    @Override
    @NotNull
    public Object clone() throws CloneNotSupportedException {
      return super.clone();
    }

    class MyRootProvider extends RootProviderBaseImpl implements Supplier<Sdk> {
      private final String mySdkVersion;

      private MyRootProvider(String sdkVersion) { mySdkVersion = sdkVersion; }

      @Override
      public String @NotNull [] getUrls(@NotNull final OrderRootType rootType) {
        final String[] relPaths = mySdkVersion.startsWith("AIR SDK ")
                                  ? AIR_SDK_ROOTS
                                  : mySdkVersion.startsWith("4.6")
                                    ? SDK_46_ROOTS
                                    : mySdkVersion.startsWith("4.5")
                                      ? SDK_45_ROOTS
                                      : mySdkVersion.startsWith("4")
                                        ? SDK_40_ROOTS
                                        : SDK_3_ROOTS;
        final String[] urls = new String[relPaths.length];
        for (int i = 0; i < relPaths.length; i++) {
          urls[i] = getHomePath() + relPaths[i];
        }
        return urls;
      }

      @Override
      public VirtualFile @NotNull [] getFiles(@NotNull final OrderRootType rootType) {
        return VirtualFile.EMPTY_ARRAY;
      }

      @Override
      public Sdk get() {
        return MySdk.this;
      }
    }
  }
}
