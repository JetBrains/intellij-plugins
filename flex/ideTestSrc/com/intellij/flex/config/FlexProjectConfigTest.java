package com.intellij.flex.config;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.ModuleTestCase;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User: ksafonov
 */
public class FlexProjectConfigTest extends ModuleTestCase {

  @Override
  protected void tearDown() throws Exception {
    Disposer.dispose(myTestRootDisposable);
    super.tearDown();
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void testModuleLibrary() throws ConfigurationException {
    assertEquals(1, FlexBuildConfigurationManager.getInstance(myModule).getBuildConfigurations().length);

    final String libraryId = createModuleLibrary();

    modifyConfig(new Consumer<FlexProjectConfigurationEditor>() {
      @Override
      public void consume(FlexProjectConfigurationEditor configEditor) {
        ModifiableFlexIdeBuildConfiguration[] configurations = configEditor.getConfigurations(myModule);
        assertEquals(1, configurations.length);
        ModifiableFlexIdeBuildConfiguration c = configurations[0];
        configEditor.createModuleLibraryEntry(c.getDependencies(), libraryId);
      }
    });

    OrderEntry libraryEntry = findLibraryEntry(myModule, libraryId);
    assertNotNull(libraryEntry);
  }

  public void testBcDependency() throws ConfigurationException {
    final Module module2 = createModule("module2");
    assertFalse(doesDepend(myModule, module2));
    assertFalse(doesDepend(module2, myModule));
    modifyConfig(new Consumer<FlexProjectConfigurationEditor>() {
      @Override
      public void consume(FlexProjectConfigurationEditor editor) {
        ModifiableFlexIdeBuildConfiguration module1Config = editor.getConfigurations(myModule)[0];
        ModifiableFlexIdeBuildConfiguration module2Config = editor.getConfigurations(module2)[0];
        ModifiableBuildConfigurationEntry entry = editor.createBcEntry(module1Config.getDependencies(), module2Config);
        editor.setEntries(module1Config.getDependencies(), Collections.singletonList(entry));
      }
    });
    assertTrue(doesDepend(myModule, module2));
    assertFalse(doesDepend(module2, myModule));

    modifyConfig(new Consumer<FlexProjectConfigurationEditor>() {
      @Override
      public void consume(FlexProjectConfigurationEditor editor) {
        ModifiableFlexIdeBuildConfiguration module1Config = editor.getConfigurations(myModule)[0];
        editor.setEntries(module1Config.getDependencies(), new ArrayList<ModifiableDependencyEntry>());
      }
    });
    assertFalse(doesDepend(myModule, module2));
    assertFalse(doesDepend(module2, myModule));

    final Module module3 = createModule("module3");
    final Module module4 = createModule("module4");
    modifyConfig(new Consumer<FlexProjectConfigurationEditor>() {
      @Override
      public void consume(FlexProjectConfigurationEditor editor) {
        ModifiableFlexIdeBuildConfiguration m1bc1 = editor.getConfigurations(myModule)[0];
        ModifiableFlexIdeBuildConfiguration m1bc2 = createConfiguration(editor, myModule);
        ModifiableFlexIdeBuildConfiguration m1bc3 = createConfiguration(editor, myModule);
        ModifiableFlexIdeBuildConfiguration m1bc4 = createConfiguration(editor, myModule);

        ModifiableFlexIdeBuildConfiguration m2bc1 = editor.getConfigurations(module2)[0];
        ModifiableFlexIdeBuildConfiguration m3bc1 = editor.getConfigurations(module3)[0];
        ModifiableFlexIdeBuildConfiguration m2bc2 = createConfiguration(editor, module2);

        ModifiableBuildConfigurationEntry e1 = editor.createBcEntry(m1bc1.getDependencies(), m2bc1);
        editor.setEntries(m1bc1.getDependencies(), Collections.singletonList(e1));

        ModifiableBuildConfigurationEntry e2 = editor.createBcEntry(m1bc2.getDependencies(), m2bc2);
        ModifiableBuildConfigurationEntry e3 = editor.createBcEntry(m1bc2.getDependencies(), m3bc1);
        editor.setEntries(m1bc1.getDependencies(), Arrays.asList(e2, e3));
      }
    });
    assertTrue(doesDepend(myModule, module2));
    assertTrue(doesDepend(myModule, module3));
    assertFalse(doesDepend(myModule, module4));
  }

  public void testSdkDependency() throws ConfigurationException {
    ProjectLibraryTable.getInstance(myProject).createLibrary();
    modifyConfig(new Consumer<FlexProjectConfigurationEditor>() {
      @Override
      public void consume(FlexProjectConfigurationEditor editor) {
        ModifiableFlexIdeBuildConfiguration c1 = editor.getConfigurations(myModule)[0];
        //SdkEntry sdk = Factory.createSdkEntry();
        //c1.getDependencies().setSdkEntry(sdk);
      }
    });
  }


  private static ModifiableFlexIdeBuildConfiguration createConfiguration(FlexProjectConfigurationEditor editor, Module module) {
    int i = 1;
    String name = "Config";
    while (true) {
      boolean found = false;
      for (ModifiableFlexIdeBuildConfiguration configuration : editor.getConfigurations(module)) {
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
    ModifiableFlexIdeBuildConfiguration c = editor.createConfiguration(module);
    c.setName(name);
    return c;
  }

  private void modifyConfig(Consumer<FlexProjectConfigurationEditor> modificator) throws ConfigurationException {
    Module[] modules = ModuleManager.getInstance(myProject).getModules();
    FlexProjectConfigurationEditor configEditor = createConfigEditor(myProject, myTestRootDisposable, modules);
    modificator.consume(configEditor);
    configEditor.commit();
    Disposer.dispose(configEditor);
  }

  private static boolean doesDepend(Module dependant, final Module dependency) {
    return !ContainerUtil.process(ModuleRootManager.getInstance(dependant).getOrderEntries(), new Processor<OrderEntry>() {
      @Override
      public boolean process(OrderEntry orderEntry) {
        return !(orderEntry instanceof ModuleOrderEntry) || ((ModuleOrderEntry)orderEntry).getModule() != dependency;
      }
    });
  }

  @Nullable
  private static OrderEntry findLibraryEntry(Module module, final String libraryId) {
    return ContainerUtil.find(ModuleRootManager.getInstance(module).getOrderEntries(), new Condition<OrderEntry>() {
      @Override
      public boolean value(OrderEntry orderEntry) {
        if (!(orderEntry instanceof LibraryOrderEntry)) {
          return false;
        }
        Library library = ((LibraryOrderEntry)orderEntry).getLibrary();
        if (!(library instanceof LibraryEx)) {
          return false;
        }

        return libraryId == ((FlexLibraryProperties)((LibraryEx)library).getProperties()).getId();
      }
    });
  }

  private String createModuleLibrary() {
    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(myModule).getModifiableModel();
    final LibraryTableBase.ModifiableModelEx libraryTable =
      (LibraryTableBase.ModifiableModelEx)modifiableModel.getModuleLibraryTable();
    LibraryEx library = (LibraryEx)libraryTable.createLibrary("test", FlexLibraryType.getInstance());
    String libraryId = UUID.randomUUID().toString();
    ((FlexLibraryProperties)library.getProperties()).setId(libraryId);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        libraryTable.commit();
        modifiableModel.commit();
      }
    });
    return libraryId;
  }

  private static FlexProjectConfigurationEditor createConfigEditor(Project project,
                                                                   final Disposable parentDisposable,
                                                                   final Module... modules) {
    final Map<Module, ModifiableRootModel> models = new HashMap<Module, ModifiableRootModel>();

    return new FlexProjectConfigurationEditor(project, new FlexProjectConfigurationEditor.ProjectModifiableModelProvider() {

      @Override
      public Module[] getModules() {
        return modules;
      }

      @Override
      public ModifiableRootModel getModuleModifiableModel(Module module) {
        ModifiableRootModel result = models.get(module);
        if (result == null) {
          result = ModuleRootManager.getInstance(module).getModifiableModel();
          models.put(module, result);
          final ModifiableRootModel result_ = result;
          Disposer.register(parentDisposable, new Disposable() {
            @Override
            public void dispose() {
              if (!result_.isDisposed()) result_.dispose();
            }
          });
        }
        return result;
      }

      @Override
      public void addListener(FlexIdeBCConfigurator.Listener listener, Disposable parentDisposable) {
        // ignore
      }

      @Override
      public void commitModifiableModels() throws ConfigurationException {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            for (ModifiableRootModel model : models.values()) {
              model.commit();
            }
          }
        });
      }

      @Override
      public LibraryTableBase.ModifiableModelEx getGlobalLibrariesModifiableModel() {
        return (LibraryTableBase.ModifiableModelEx)ApplicationLibraryTable.getApplicationTable().getModifiableModel();
      }
    });
  }
}
