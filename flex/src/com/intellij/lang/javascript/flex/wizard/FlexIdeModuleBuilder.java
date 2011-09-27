package com.intellij.lang.javascript.flex.wizard;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeModuleStructureExtension;
import com.intellij.lang.javascript.flex.projectStructure.FlexSdk;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class FlexIdeModuleBuilder extends ModuleBuilder {

  private TargetPlatform myTargetPlatform = TargetPlatform.Web;
  private boolean isPureActionScript = false;
  private OutputType myOutputType = OutputType.Application;
  private FlexSdk myFlexSdk;
  private String myTargetPlayer;
  private boolean myCreateMainClass;
  private String myMainClassName;
  private boolean myCreateHtmlWrapperTemplate;

  public ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  public void setTargetPlatform(final TargetPlatform targetPlatform) {
    myTargetPlatform = targetPlatform;
  }

  public void setPureActionScript(final boolean pureActionScript) {
    isPureActionScript = pureActionScript;
  }

  public void setOutputType(final OutputType outputType) {
    myOutputType = outputType;
  }

  public void setFlexSdk(final FlexSdk flexSdk) {
    myFlexSdk = flexSdk;
  }

  public void setTargetPlayer(final String targetPlayer) {
    myTargetPlayer = targetPlayer;
  }

  public void setCreateMainClass(final boolean createMainClass) {
    myCreateMainClass = createMainClass;
  }

  public void setMainClassName(final String mainClassName) {
    myMainClassName = mainClassName;
  }

  public void setCreateHtmlWrapperTemplate(final boolean createHtmlWrapperTemplate) {
    myCreateHtmlWrapperTemplate = createHtmlWrapperTemplate;
  }

  public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
    if (contentEntry == null) return;

    setupSourceRoots(contentEntry);

    final Module module = modifiableRootModel.getModule();
    final LibraryTableBase.ModifiableModelEx globalLibrariesModifiableModel;

    final FlexProjectConfigurationEditor currentFlexEditor =
      FlexIdeModuleStructureExtension.getInstance().getConfigurator().getConfigEditor();
    final boolean needToCommitFlexEditor = currentFlexEditor == null;

    final FlexProjectConfigurationEditor flexConfigEditor;

    if (currentFlexEditor != null) {
      globalLibrariesModifiableModel = null;
      flexConfigEditor = currentFlexEditor;
    }
    else {
      globalLibrariesModifiableModel =
        (LibraryTableBase.ModifiableModelEx)ApplicationLibraryTable.getApplicationTable().getModifiableModel();
      flexConfigEditor = createFlexConfigEditor(modifiableRootModel, globalLibrariesModifiableModel);
    }

    final ModifiableFlexIdeBuildConfiguration[] configurations = flexConfigEditor.getConfigurations(module);
    assert configurations.length == 1;
    setupBC(module, configurations[0]);

    commitIfNeeded(globalLibrariesModifiableModel, flexConfigEditor, needToCommitFlexEditor);
  }

  private static FlexProjectConfigurationEditor createFlexConfigEditor(final ModifiableRootModel modifiableRootModel,
                                                                       final LibraryTableBase.ModifiableModelEx globalLibrariesModifiableModel) {
    final Module module = modifiableRootModel.getModule();

    final FlexProjectConfigurationEditor.ProjectModifiableModelProvider provider =
      new FlexProjectConfigurationEditor.ProjectModifiableModelProvider() {
        public Module[] getModules() {
          return new Module[]{module};
        }

        public ModifiableRootModel getModuleModifiableModel(final Module moduleParam) {
          assert moduleParam == module;
          return modifiableRootModel;
        }

        public void addListener(final FlexIdeBCConfigurator.Listener listener,
                                final Disposable parentDisposable) {
          // modules and BCs are not removed here
        }

        public void commitModifiableModels() throws ConfigurationException {
          // commit will be performed outside of #setupRootModel()
        }

        public LibraryTableBase.ModifiableModelEx getGlobalLibrariesModifiableModel() {
          return globalLibrariesModifiableModel;
        }
      };

    return new FlexProjectConfigurationEditor(modifiableRootModel.getProject(), provider);
  }

  private static void commitIfNeeded(final @Nullable LibraryTableBase.ModifiableModelEx globalLibrariesModifiableModel,
                                     final FlexProjectConfigurationEditor flexConfigEditor,
                                     final boolean needToCommitFlexEditor) throws ConfigurationException {
    if (globalLibrariesModifiableModel != null || needToCommitFlexEditor) {
      final ConfigurationException exception =
        ApplicationManager.getApplication().runWriteAction(new NullableComputable<ConfigurationException>() {
          public ConfigurationException compute() {
            if (globalLibrariesModifiableModel != null) {
              globalLibrariesModifiableModel.commit();
            }

            if (needToCommitFlexEditor) {
              try {
                flexConfigEditor.commit();
              }
              catch (ConfigurationException e) {
                return e;
              }
            }

            return null;
          }
        });

      if (exception != null) {
        throw exception;
      }
    }
  }

  private void setupBC(final Module module, final ModifiableFlexIdeBuildConfiguration configuration) {
    configuration.setName(module.getName());
    configuration.setTargetPlatform(myTargetPlatform);
    configuration.setPureAs(isPureActionScript);
    configuration.setOutputType(myOutputType);
    if (myCreateMainClass) {
      configuration.setMainClass(myMainClassName);
      configuration.setOutputFileName(StringUtil.getShortName(myMainClassName) + (myOutputType == OutputType.Library ? ".swc" : ".swf"));
    }
    else {
      configuration.setOutputFileName(module.getName() + (myOutputType == OutputType.Library ? ".swc" : ".swf"));
    }
    configuration.setOutputFolder(VfsUtil.urlToPath(CompilerModuleExtension.getInstance(module).getCompilerOutputUrl()));

    configuration.getDependencies().setSdkEntry(Factory.createSdkEntry(myFlexSdk.getLibraryId(), myFlexSdk.getHomePath())); // todo correct?
    configuration.getDependencies().setTargetPlayer(myTargetPlayer);
  }

  private void setupSourceRoots(final ContentEntry contentEntry) {
    final VirtualFile contentRoot = contentEntry.getFile();
    if (contentRoot == null) return;

    VirtualFile sourceRoot = VfsUtil.findRelativeFile(contentRoot, "src");

    if (sourceRoot == null) {
      sourceRoot = ApplicationManager.getApplication().runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          try {
            return contentRoot.createChildDirectory(this, "src");
          }
          catch (IOException e) {
            return null;
          }
        }
      });
    }
    if (sourceRoot != null) {
      contentEntry.addSourceFolder(sourceRoot, false);
    }
  }
}
