package com.intellij.lang.javascript;

import com.intellij.flex.FlexTestUtils;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.ModuleTestCase;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

/**
 * User: ksafonov
 */
public class FlexProjectConfigTest extends ModuleTestCase {

  @Override
  protected void tearDown() throws Exception {
    Disposer.dispose(getTestRootDisposable());
    super.tearDown();
  }

  @Override
  public void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void testModuleLibrary() throws ConfigurationException {
    assertEquals(1, FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations().length);

    final String libraryId = createModuleLibrary();

    FlexTestUtils.modifyConfigs(myProject, configEditor -> {
      ModifiableFlexBuildConfiguration[] configurations = configEditor.getConfigurations(myModule);
      assertEquals(1, configurations.length);
      ModifiableFlexBuildConfiguration c = configurations[0];
      configEditor.createModuleLibraryEntry(c.getDependencies(), libraryId);
    });

    OrderEntry libraryEntry = findLibraryEntry(myModule, libraryId);
    assertNull(libraryEntry);
  }

  public void testModuleLibrary2() throws ConfigurationException {
    assertEquals(1, FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations().length);

    final String libraryId = createModuleLibrary();

    FlexTestUtils.modifyConfigs(myProject, configEditor -> {
      ModifiableFlexBuildConfiguration[] configurations = configEditor.getConfigurations(myModule);
      assertEquals(1, configurations.length);
      ModifiableFlexBuildConfiguration c = configurations[0];
      final ModifiableModuleLibraryEntry e = configEditor.createModuleLibraryEntry(c.getDependencies(), libraryId);
      c.getDependencies().getModifiableEntries().add(e);
    });

    OrderEntry libraryEntry = findLibraryEntry(myModule, libraryId);
    assertNotNull(libraryEntry);
  }

  public void testBcDependency() throws ConfigurationException {
    final Module module2 = createModule("module2");
    assertFalse(doesDepend(myModule, module2));
    assertFalse(doesDepend(module2, myModule));
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration module1Config = editor.getConfigurations(myModule)[0];
      ModifiableFlexBuildConfiguration module2Config = editor.getConfigurations(module2)[0];
      ModifiableBuildConfigurationEntry entry = editor.createBcEntry(module1Config.getDependencies(), module2Config, null);
      editor.setEntries(module1Config.getDependencies(), Collections.singletonList(entry));
    });
    assertTrue(doesDepend(myModule, module2));
    assertFalse(doesDepend(module2, myModule));

    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration module1Config = editor.getConfigurations(myModule)[0];
      editor.setEntries(module1Config.getDependencies(), new ArrayList<>());
    });
    assertFalse(doesDepend(myModule, module2));
    assertFalse(doesDepend(module2, myModule));

    final Module module3 = createModule("module3");
    final Module module4 = createModule("module4");
    FlexTestUtils.modifyConfigs(myProject, editor -> {
      ModifiableFlexBuildConfiguration m1bc1 = editor.getConfigurations(myModule)[0];
      ModifiableFlexBuildConfiguration m1bc2 = createConfiguration(editor, myModule);
      ModifiableFlexBuildConfiguration m1bc3 = createConfiguration(editor, myModule);
      ModifiableFlexBuildConfiguration m1bc4 = createConfiguration(editor, myModule);

      ModifiableFlexBuildConfiguration m2bc1 = editor.getConfigurations(module2)[0];
      ModifiableFlexBuildConfiguration m3bc1 = editor.getConfigurations(module3)[0];
      ModifiableFlexBuildConfiguration m2bc2 = createConfiguration(editor, module2);

      ModifiableBuildConfigurationEntry e1 = editor.createBcEntry(m1bc1.getDependencies(), m2bc1, null);
      editor.setEntries(m1bc1.getDependencies(), Collections.singletonList(e1));

      ModifiableBuildConfigurationEntry e2 = editor.createBcEntry(m1bc2.getDependencies(), m2bc2, null);
      ModifiableBuildConfigurationEntry e3 = editor.createBcEntry(m1bc2.getDependencies(), m3bc1, null);
      editor.setEntries(m1bc1.getDependencies(), Arrays.asList(e2, e3));
    });
    assertTrue(doesDepend(myModule, module2));
    assertTrue(doesDepend(myModule, module3));
    assertFalse(doesDepend(myModule, module4));
  }

  public void testSdkListener() {
    final Sdk sdk = FlexTestUtils.createSdk(FlexTestUtils.getPathToMockFlex(getClass(), getTestName(false)), "4.6.0");
    RootProvider.RootSetChangedListener listener = wrapper -> {
    };
    // See IDEA-140175: if different BCs of the same module have different SDKs then the listener may be first set by FlexCompositeSdk (when
    // 'multi-SDK module is being loaded) and then the same listener via standard way (when loading the next Flash module with a single BC)
    sdk.getRootProvider().addRootSetChangedListener(listener);
    sdk.getRootProvider().addRootSetChangedListener(listener);
  }


  private static ModifiableFlexBuildConfiguration createConfiguration(FlexProjectConfigurationEditor editor, Module module) {
    int i = 1;
    String name = "Config";
    while (true) {
      boolean found = false;
      for (ModifiableFlexBuildConfiguration configuration : editor.getConfigurations(module)) {
        if (configuration.getName().equals(name = "Config " + String.valueOf(i))) {
          found = true;
          break;
        }
      }
      if (found) {
        i++;
      }
      else {
        break;
      }
    }
    ModifiableFlexBuildConfiguration c = editor.createConfiguration(module);
    c.setName(name);
    return c;
  }

  private static boolean doesDepend(Module dependant, final Module dependency) {
    return !ContainerUtil.process(ModuleRootManager.getInstance(dependant).getOrderEntries(),
                                  orderEntry -> !(orderEntry instanceof ModuleOrderEntry) || ((ModuleOrderEntry)orderEntry).getModule() != dependency);
  }

  @Nullable
  private static OrderEntry findLibraryEntry(Module module, final String libraryId) {
    return ContainerUtil.find(ModuleRootManager.getInstance(module).getOrderEntries(), orderEntry -> {
      if (!(orderEntry instanceof LibraryOrderEntry)) {
        return false;
      }
      Library library = ((LibraryOrderEntry)orderEntry).getLibrary();
      if (!(library instanceof LibraryEx)) {
        return false;
      }

      return libraryId == ((FlexLibraryProperties)((LibraryEx)library).getProperties()).getId();
    });
  }

  private String createModuleLibrary() {
    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
    final LibraryTable.ModifiableModel libraryTable = modifiableModel.getModuleLibraryTable().getModifiableModel();
    LibraryEx library = (LibraryEx)libraryTable.createLibrary("test", FlexLibraryType.FLEX_LIBRARY);
    String libraryId = UUID.randomUUID().toString();
    ((FlexLibraryProperties)library.getProperties()).setId(libraryId);
    ApplicationManager.getApplication().runWriteAction(() -> {
      libraryTable.commit();
      modifiableModel.commit();
    });
    return libraryId;
  }
}
