// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.highlighting;

import com.intellij.flex.codeInsight.FlexNavigationTest;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.lang.javascript.JSDaemonAnalyzerTestCase;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ConstantConditions")
public class FlexScopeTest extends JSDaemonAnalyzerTestCase {

  private boolean myIsAddDirToTests = false;

  @Override
  public void setUp() throws Exception {
    FlexTestUtils.allowFlexVfsRootsFor(getTestRootDisposable(), "");
    super.setUp();
  }

  @NotNull
  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass(), getTestRootDisposable());
  }

  @Override
  protected String getBasePath() {
    return "/flex_scope/";
  }

  @NotNull
  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected String getExtension() {
    return "as";
  }

  public void testModuleLibrary() throws Exception {
    // classes from library are unresolved
    doHighlightingTest("_1");

    // add dependency on missing library
    FlexTestUtils.addFlexLibrary(false, myModule, "moduleLib", true, getTestDataPath() + getBasePath(), "Lib", null, null);
    doHighlightingTest("_2");

    // create another build configuration with no dependency on library
    FlexTestUtils.modifyConfigs(myProject, e -> {
      final ModifiableFlexBuildConfiguration bc = e.createConfiguration(myModule);
      bc.setName("Second");
    });
    doHighlightingTest("_2");

    // switch to it
    final FlexBuildConfigurationManager bcManager = FlexBuildConfigurationManager.getInstance(myModule);
    bcManager.setActiveBuildConfiguration(bcManager.findConfigurationByName("Second"));
    doHighlightingTest("_1");

    // switch back
    bcManager.setActiveBuildConfiguration(bcManager.findConfigurationByName("Unnamed"));
    doHighlightingTest("_2");
  }

  public void testBcDependency() throws Exception {
    // module uses classes from other (non-existing) module
    doHighlightingTest("_1");

    final Module module2 = FlexTestUtils.createModule(myProject, "module2", findVirtualFile(getBasePath() + "Module2"));

    // create other module, don't add dependency
    doHighlightingTest("_1");

    ModuleRootModificationUtil.addDependency(myModule, module2);
    doHighlightingTest("_1");

    // now add bc-to-bc dependency, highlighting should not change because app-on-app dependency is ignored
    WriteAction.run(() -> FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration dependentBc = editor.getConfigurations(myModule)[0];
      final ModifiableFlexBuildConfiguration dependencyBc = editor.getConfigurations(module2)[0];
      final ModifiableBuildConfigurationEntry dependencyEntry =
        editor.createBcEntry(dependentBc.getDependencies(), dependencyBc, null);
      dependentBc.getDependencies().getModifiableEntries().add(dependencyEntry);
    }));

    doHighlightingTest("_1");

    // dependency with linkage type "Loaded" doesn't change highlighting as well
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration dependentBc = editor.getConfigurations(myModule)[0];
      dependentBc.getDependencies().getModifiableEntries().get(0).getDependencyType().setLinkageType(LinkageType.LoadInRuntime);
      editor.getConfigurations(module2)[0].setOutputType(OutputType.RuntimeLoadedModule);
    });
    doHighlightingTest("_1");

    // app-on-lib dependency affects highlighting
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration dependentBc = editor.getConfigurations(myModule)[0];
      dependentBc.getDependencies().getModifiableEntries().get(0).getDependencyType().setLinkageType(LinkageType.External);
      editor.getConfigurations(module2)[0].setOutputType(OutputType.Library);
    });
    doHighlightingTest("_2");

    // create another build configuration with no dependency on second BC
    FlexTestUtils.modifyConfigs(myProject, e -> {
      final ModifiableFlexBuildConfiguration bc = e.createConfiguration(myModule);
      bc.setName("Second");
    });
    doHighlightingTest("_2");

    // switch to it
    final FlexBuildConfigurationManager bcManager = FlexBuildConfigurationManager.getInstance(myModule);
    bcManager.setActiveBuildConfiguration(bcManager.findConfigurationByName("Second"));
    doHighlightingTest("_1");

    // switch back
    bcManager.setActiveBuildConfiguration(bcManager.findConfigurationByName("Unnamed"));
    doHighlightingTest("_2");
  }

  @JSTestOptions(JSTestOption.WithFlexSdk)
  public void testBcDependencyInSameModule() throws Exception {
    WriteAction.run(() -> FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];

      final ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(myModule);
      bc2.setNature(new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Library));
      bc2.setName("2");

      final ModifiableBuildConfigurationEntry entry = editor.createBcEntry(bc1.getDependencies(), bc2, null);
      bc1.getDependencies().getModifiableEntries().add(entry);
    }));

    doHighlightingTest("");
  }

  private void doHighlightingTest(final String suffix) throws Exception {
    doHighlightingTest(suffix, "as");
  }

  private void doHighlightingTest(final String suffix, String extension) throws Exception {
    doTest(getBasePath() + getTestName(false) + suffix + "." + extension, true, false, true);
  }

  public void testNavigationElement() throws Exception {
    configureByFile(getBasePath() + getTestName(false) + ".as");

    Pair<Sdk, Sdk> sdks = prepareTwoSdks();

    final FlexBuildConfigurationManager bcManager = FlexBuildConfigurationManager.getInstance(myModule);

    bcManager.setActiveBuildConfiguration(bcManager.findConfigurationByName("1"));
    VirtualFile sourceFile1 =
      LocalFileSystem.getInstance()
        .findFileByPath(sdks.first.getHomePath() + "/frameworks/projects/spark/src/spark/components/Application.as");
    FlexNavigationTest.doTest(myEditor, sourceFile1, null, null, belongsToSdk(sdks.first));

    bcManager.setActiveBuildConfiguration(bcManager.findConfigurationByName("2"));
    VirtualFile sourceFile2 =
      LocalFileSystem.getInstance()
        .findFileByPath(sdks.second.getHomePath() + "/frameworks/projects/spark/src/spark/components/Application.as");
    FlexNavigationTest.doTest(myEditor, sourceFile2, null, null, belongsToSdk(sdks.second));
  }

  private static Consumer<PsiElement> belongsToSdk(final Sdk sdk) {
    return psiElement -> {
      VirtualFile containingFile = psiElement.getContainingFile().getVirtualFile();
      if (containingFile.getFileSystem() instanceof JarFileSystem) {
        containingFile = JarFileSystem.getInstance().getVirtualFileForJar(containingFile);
      }
      assertTrue(VfsUtilCore.isAncestor(sdk.getHomeDirectory(), containingFile, true));
    };
  }

  public void testGotoClass() {
    prepareTwoSdks();
    GotoClassModel2 model = new GotoClassModel2(myProject);
    Object[] elements = model.getElementsByName("Application", true, "");
    assertEquals(4, elements.length);
  }

  public Pair<Sdk, Sdk> prepareTwoSdks() {
    final Sdk sdk1 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    final Sdk sdk2 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, false, getTestRootDisposable());

    WriteAction.run(() -> FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      bc1.setName("1");
      FlexTestUtils.setSdk(bc1, sdk1);
      final ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(myModule);
      bc2.setNature(new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Application));
      bc2.setName("2");
      FlexTestUtils.setSdk(bc2, sdk2);
    }));
    return Pair.create(sdk1, sdk2);
  }

  public void testCircularDependency() throws Exception {
    Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, true, getTestRootDisposable());
    WriteAction.run(() -> {
      ModifiableModuleModel m1 = ModuleManager.getInstance(myProject).getModifiableModel();
      VirtualFile moduleDir = getOrCreateProjectBaseDir().createChildDirectory(this, "module2");
      Module module2 = m1.newModule(moduleDir.toNioPath(), FlexModuleType.getInstance().getId());
      m1.commit();

      PsiTestUtil.addSourceRoot(module2, moduleDir);

      FlexTestUtils.modifyConfigs(myProject, editor -> {
        final ModifiableFlexBuildConfiguration app1 = editor.getConfigurations(myModule)[0];
        app1.setName("app1");
        FlexTestUtils.setSdk(app1, sdk);

        final ModifiableFlexBuildConfiguration lib1 = editor.getConfigurations(module2)[0];
        lib1.setNature(new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Library));
        app1.setName("lib1");
        FlexTestUtils.setSdk(lib1, sdk);

        final ModifiableFlexBuildConfiguration lib2 = editor.createConfiguration(myModule);
        lib2.setName("lib2");
        lib2.setNature(new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Library));
        FlexTestUtils.setSdk(lib2, sdk);

        final ModifiableBuildConfigurationEntry dep1 = editor.createBcEntry(app1.getDependencies(), lib1, null);
        app1.getDependencies().getModifiableEntries().add(dep1);
        final ModifiableBuildConfigurationEntry dep2 = editor.createBcEntry(lib1.getDependencies(), lib2, null);
        lib1.getDependencies().getModifiableEntries().add(dep2);
      });
    });

    doHighlightingTest("");
  }

  public void testTransitiveDependency() throws Exception {
    final Module module2 = FlexTestUtils.createModule(myProject, "module2", findVirtualFile(getBasePath() + "m2"));
    final Module module3 = FlexTestUtils.createModule(myProject, "module3", findVirtualFile(getBasePath() + "m3"));

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      final ModifiableFlexBuildConfiguration bc2 = editor.getConfigurations(module2)[0];
      bc2.setOutputType(OutputType.Library);
      final ModifiableFlexBuildConfiguration bc3 = editor.getConfigurations(module3)[0];
      bc3.setOutputType(OutputType.Library);
      final ModifiableFlexBuildConfiguration bc3a = editor.createConfiguration(module3);
      bc3a.setOutputType(OutputType.Library);
      bc3a.setName("bc3a");

      bc1.getDependencies().getModifiableEntries().add(editor.createBcEntry(bc1.getDependencies(), bc2, null));
      bc2.getDependencies().getModifiableEntries().add(editor.createBcEntry(bc2.getDependencies(), bc3, null));
      bc2.getDependencies().getModifiableEntries().add(editor.createBcEntry(bc2.getDependencies(), bc3a, null));
    });

    FlexTestUtils
      .addFlexLibrary(false, module2, "Flex Lib", true, FlexTestUtils.getTestDataPath("flexlib/"), "flexlib.swc", null, null);

    doHighlightingTest("_1");

    class Test implements ThrowableRunnable<Exception> {
      private final LinkageType myLinkageType1;
      private final LinkageType myLinkageType2;
      private final String mySuffix;

      Test(LinkageType linkageType1, LinkageType linkageType2, String suffix) {
        myLinkageType1 = linkageType1;
        myLinkageType2 = linkageType2;
        mySuffix = suffix;
      }

      @Override
      public void run() throws Exception {
        FlexTestUtils.modifyConfigs(myProject, editor -> {
          final ModifiableFlexBuildConfiguration bc = editor.getConfigurations(module2)[0];
          final ModifiableDependencyEntry e1 = bc.getDependencies().getModifiableEntries().get(0);
          e1.getDependencyType().setLinkageType(myLinkageType1);
          final ModifiableDependencyEntry e2 = bc.getDependencies().getModifiableEntries().get(1);
          e2.getDependencyType().setLinkageType(myLinkageType2);
        });
        doHighlightingTest(mySuffix);
      }
    }

    new Test(LinkageType.Default, LinkageType.Default, "_1").run();
    new Test(LinkageType.External, LinkageType.Default, "_1").run();
    new Test(LinkageType.Include, LinkageType.Default, "_2").run();
    new Test(LinkageType.LoadInRuntime, LinkageType.Default, "_1").run();
    new Test(LinkageType.Merged, LinkageType.Default, "_1").run();
    new Test(LinkageType.RSL, LinkageType.Default, "_1").run();
    new Test(LinkageType.RSL, LinkageType.Include, "_2").run();
    new Test(LinkageType.Include, LinkageType.Include, "_2").run();

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc2 = editor.getConfigurations(module2)[0];
      final ModifiableDependencyEntry entry = bc2.getDependencies().getModifiableEntries().get(2);
      assert entry instanceof ModifiableModuleLibraryEntry; // FlexLib
      entry.getDependencyType().setLinkageType(LinkageType.Include);
    });

    new Test(LinkageType.Include, LinkageType.Include, "_3").run();
  }

  public void testTransitiveDependency2() throws Exception {
    final Module module2 = FlexTestUtils.createModule(myProject, "module2", findVirtualFile(getBasePath() + "m2"));
    final Module module3 = FlexTestUtils.createModule(myProject, "module3", findVirtualFile(getBasePath() + "m3"));

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1a = editor.getConfigurations(myModule)[0];
      bc1a.setName("1");
      final ModifiableFlexBuildConfiguration bc1b = editor.createConfiguration(myModule);
      bc1b.setName("2");

      final ModifiableFlexBuildConfiguration bc2a = editor.getConfigurations(module2)[0];
      bc2a.setOutputType(OutputType.Library);
      bc2a.setName("1");

      final ModifiableFlexBuildConfiguration bc2b = editor.createConfiguration(module2);
      bc2b.setOutputType(OutputType.Library);
      bc2b.setName("2");

      final ModifiableFlexBuildConfiguration bc3 = editor.getConfigurations(module3)[0];
      bc3.setOutputType(OutputType.Library);

      bc1a.getDependencies().getModifiableEntries().add(editor.createBcEntry(bc1a.getDependencies(), bc2a, null));
      bc1b.getDependencies().getModifiableEntries().add(editor.createBcEntry(bc1b.getDependencies(), bc2b, null));
      final ModifiableBuildConfigurationEntry entry1 = editor.createBcEntry(bc2a.getDependencies(), bc3, null);
      bc2a.getDependencies().getModifiableEntries().add(entry1);
      entry1.getDependencyType().setLinkageType(LinkageType.Include);
      final ModifiableBuildConfigurationEntry entry2 = editor.createBcEntry(bc2b.getDependencies(), bc3, null);
      bc2b.getDependencies().getModifiableEntries().add(entry2);
      entry2.getDependencyType().setLinkageType(LinkageType.Merged);
    });

    final FlexBuildConfigurationManager m = FlexBuildConfigurationManager.getInstance(myModule);
    m.setActiveBuildConfiguration(m.findConfigurationByName("1"));
    doHighlightingTest("_1");
    m.setActiveBuildConfiguration(m.findConfigurationByName("2"));
    doHighlightingTest("_2");
  }

  public void testMissingSdk() throws Exception {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.5"), null, true, getTestRootDisposable());
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      final ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      bc1.setName("1");
      FlexTestUtils.setSdk(bc1, sdk);
      final ModifiableFlexBuildConfiguration bc2 = editor.createConfiguration(myModule);
      bc2.setName("2");
      bc2.getDependencies().setSdkEntry(null);
    });
    final FlexBuildConfigurationManager m = FlexBuildConfigurationManager.getInstance(myModule);
    m.setActiveBuildConfiguration(m.findConfigurationByName("1"));
    doHighlightingTest("_1");
    m.setActiveBuildConfiguration(m.findConfigurationByName("2"));
    doHighlightingTest("_2");
    m.setActiveBuildConfiguration(m.findConfigurationByName("1"));
    doHighlightingTest("_1");
  }

  public void testTestScope() throws Exception {
    final Sdk sdk46 = FlexTestUtils.createSdk(FlexTestUtils.getPathToCompleteFlexSdk("4.6"), null, true, getTestRootDisposable());
    final Module module2 = FlexTestUtils.createModule(myProject, "module2", findVirtualFile(getBasePath() + "m2"));
    final Module module3 = FlexTestUtils.createModule(myProject, "module3", findVirtualFile(getBasePath() + "m3"));

    FlexTestUtils.addFlexLibrary(false, myModule, "Lib", true, getTestDataPath() + getBasePath(), "Lib", null, null, LinkageType.Test);
    FlexTestUtils.addFlexLibrary(false, myModule, "Lib2", true, getTestDataPath() + getBasePath(), "Lib2", null, null);
    FlexTestUtils.addFlexLibrary(true, myModule, "Lib3", true, getTestDataPath() + getBasePath(), "Lib3", null, null, LinkageType.Test);
    FlexTestUtils.addFlexLibrary(true, myModule, "Lib4", true, getTestDataPath() + getBasePath(), "Lib4", null, null);

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      FlexTestUtils.setSdk(bc1, sdk46);

      ModifiableFlexBuildConfiguration bc2 = editor.getConfigurations(module2)[0];
      bc2.setOutputType(OutputType.Library);

      ModifiableFlexBuildConfiguration bc3 = editor.getConfigurations(module3)[0];
      bc3.setOutputType(OutputType.Library);

      ModifiableBuildConfigurationEntry entry1 = editor.createBcEntry(bc1.getDependencies(), bc2, null);
      entry1.getDependencyType().setLinkageType(LinkageType.Test);
      bc1.getDependencies().getModifiableEntries().add(entry1);

      ModifiableBuildConfigurationEntry entry2 = editor.createBcEntry(bc2.getDependencies(), bc3, null);
      entry2.getDependencyType().setLinkageType(LinkageType.Test);
      bc2.getDependencies().getModifiableEntries().add(entry2);
    });

    doHighlightingTest("_1", "as", false);
    doHighlightingTest("_1", "mxml", false);
    doHighlightingTest("_2", "as", true);
    doHighlightingTest("_2", "mxml", true);
  }

  public void testLibraryScope() throws Exception {
    final Module module2 = FlexTestUtils.createModule(myProject, "module2", findVirtualFile(getBasePath() + "m2"));
    FlexTestUtils.addFlexLibrary(false, myModule, "Flex Lib", true, FlexTestUtils.getTestDataPath("flexlib"), "flexlib.swc", null, null);

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration bc1 = editor.getConfigurations(myModule)[0];
      bc1.setOutputType(OutputType.Library);

      ModifiableFlexBuildConfiguration bc2 = editor.getConfigurations(module2)[0];

      ModifiableBuildConfigurationEntry entry1 = editor.createBcEntry(bc2.getDependencies(), bc1, null);
      bc2.getDependencies().getModifiableEntries().add(entry1);
    });

    doHighlightingTest("");
  }

  private void doHighlightingTest(String suffix, String extension, boolean testFolder) throws Exception {
    boolean b = myIsAddDirToTests;
    try {
      myIsAddDirToTests = testFolder;
      doHighlightingTest(suffix, extension);
    }
    finally {
      myIsAddDirToTests = b;
    }
  }

  @Override
  protected boolean isAddDirToTests() {
    return myIsAddDirToTests;
  }
}

