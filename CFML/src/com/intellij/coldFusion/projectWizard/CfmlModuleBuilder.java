package com.intellij.coldFusion.projectWizard;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.UI.runner.CfmlRunConfiguration;
import com.intellij.coldFusion.UI.runner.CfmlRunConfigurationType;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.WebModuleBuilder;
import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import icons.CFMLIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class CfmlModuleBuilder extends ModuleBuilder {

  private CfmlProjectWizardData myWizardData;

  @Override
  public String getBuilderId() {
    return getClass().getName();
  }


  @Override
  public String getName() {
    return CfmlBundle.message("cfml.module.title");
  }

  @Override
  public String getPresentableName() {
    return CfmlBundle.message("cfml.module.title");
  }

  @Override
  public String getDescription() {
    return CfmlBundle.message("cfml.module.description");
  }

  @Override
  public Icon getBigIcon() {
    return CFMLIcons.Cfml;
  }

  @Override
  public Icon getNodeIcon() {
    return CFMLIcons.Cfml;
  }

  @Override
  public ModuleType getModuleType() {
    return WebModuleType.getInstance();
  }

  @Override
  public String getParentGroup() {
    return WebModuleBuilder.GROUP_NAME;
  }

  @Nullable
  @Override
  public ModuleWizardStep getCustomOptionsStep(final WizardContext context, final Disposable parentDisposable) {
    final CfmlModuleWizardStep step = new CfmlModuleWizardStep(context);
    Disposer.register(parentDisposable, step);
    return step;
  }

  void setWizardData(final CfmlProjectWizardData wizardData) {
    myWizardData = wizardData;
  }

  @Override
  public void setupRootModel(final ModifiableRootModel modifiableRootModel) throws ConfigurationException {
    final ContentEntry contentEntry = doAddContentEntry(modifiableRootModel);
    final VirtualFile baseDir = contentEntry == null ? null : contentEntry.getFile();
    setRunConfig(myWizardData);
    if (baseDir != null) {
      setupProject(modifiableRootModel, baseDir, myWizardData);
    }
  }

  void setupProject(@NotNull final ModifiableRootModel modifiableRootModel,
                           @NotNull final VirtualFile baseDir,
                           @NotNull final CfmlProjectWizardData wizardData) {
    //final String templateName = wizardData.myTemplate == null ? "Empty project" : wizardData.myTemplate.getName();
    //UsageTrigger.trigger("CfmlProjectWizard." + templateName);
    //
    //if (wizardData.myTemplate != null) {
    try {
      final Collection<VirtualFile> filesToOpen =
        generateProject(modifiableRootModel.getModule(), baseDir);
      if (!filesToOpen.isEmpty()) {
        scheduleFilesOpening(modifiableRootModel.getModule(), filesToOpen);
      }
    }
    catch (IOException ignore) {/*unlucky*/}
    //      scheduleFilesOpening(modifiableRootModel.getModule(), filesToOpen);
    //    }
    //  }
  }

  //private static void setupSdkAndDartium(@NotNull final ModifiableRootModel modifiableRootModel,
  //                                       @NotNull final DartProjectWizardData wizardData) {
  //  // similar to DartConfigurable.apply()
  //  if (DartSdkUtil.isDartSdkHome(wizardData.dartSdkPath)) {
  //    final LibraryTable.ModifiableModel libraryTableModifiableModel =
  //      ModifiableModelsProvider.SERVICE.getInstance().getLibraryTableModifiableModel();
  //
  //    DartSdkGlobalLibUtil.ensureDartSdkConfigured(libraryTableModifiableModel, wizardData.dartSdkPath);
  //
  //    if (libraryTableModifiableModel.isChanged()) {
  //      libraryTableModifiableModel.commit();
  //    }
  //    else {
  //      ModifiableModelsProvider.SERVICE.getInstance().disposeLibraryTableModifiableModel(libraryTableModifiableModel);
  //    }
  //
  //    modifiableRootModel.addInvalidLibrary(DartSdk.DART_SDK_GLOBAL_LIB_NAME, LibraryTablesRegistrar.APPLICATION_LEVEL);
  //  }
  //
  //  DartiumUtil.applyDartiumSettings(FileUtilRt.toSystemIndependentName(wizardData.dartiumPath), wizardData.dartiumSettings);
  //}
  //
  //private static void scheduleFilesOpening(@NotNull final Module module, @NotNull final Collection<VirtualFile> files) {
  //  runWhenNonModalIfModuleNotDisposed(new Runnable() {
  //    public void run() {
  //      final FileEditorManager manager = FileEditorManager.getInstance(module.getProject());
  //      for (VirtualFile file : files) {
  //        manager.openFile(file, true);
  //
  //        if (PubspecYamlUtil.PUBSPEC_YAML.equals(file.getName())) {
  //          final AnAction pubGetAction = ActionManager.getInstance().getAction("Dart.pub.get");
  //          if (pubGetAction instanceof DartPubGetAction) {
  //            ((DartPubGetAction)pubGetAction).performPubAction(module, file, false);
  //          }
  //        }
  //      }
  //    }
  //  }, module);
  //}
  //
  //static void runWhenNonModalIfModuleNotDisposed(@NotNull final Runnable runnable, @NotNull final Module module) {
  //  StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
  //    @Override
  //    public void run() {
  //      if (ApplicationManager.getApplication().getCurrentModalityState() == ModalityState.NON_MODAL) {
  //        runnable.run();
  //      }
  //      else {
  //        ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL, new Condition() {
  //          @Override
  //          public boolean value(final Object o) {
  //            return module.isDisposed();
  //          }
  //        });
  //      }
  //    }
  //  });
  //}

  public Collection<VirtualFile> generateProject(@NotNull final Module module,
                                                        @NotNull final VirtualFile baseDir) throws IOException {
    final String projectTitle = StringUtil.toTitleCase(module.getName());
    final String lowercaseName = module.getName().toLowerCase(Locale.US);

    final VirtualFile indexfile = baseDir.createChildData(this, "index.cfm");
    indexfile.setBinaryContent(("<html>\n" +
                                "<head>\n" +
                                "<title></title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "<cfoutput>\n" +
                                " <!---Create your code here!--->\n" +
                                "</cfoutput>\n" +
                                "</body>\n" +
                                "</html>").getBytes(Charset.forName("UTF-8")));


    return Collections.singletonList(indexfile);
  }

  private void scheduleFilesOpening(@NotNull final Module module, @NotNull final Collection<VirtualFile> files) {
    runWhenNonModalIfModuleNotDisposed(new Runnable() {
      public void run() {
        final FileEditorManager manager = FileEditorManager.getInstance(module.getProject());
        for (VirtualFile file : files)
          manager.openFile(file, true);
      }
    }, module);
  }

  void runWhenNonModalIfModuleNotDisposed(@NotNull final Runnable runnable, @NotNull final Module module) {
    StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
      @Override
      public void run() {
        if (ApplicationManager.getApplication().getCurrentModalityState() == ModalityState.NON_MODAL) {
          runnable.run();
        }
        else {
          ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL, new Condition() {
            @Override
            public boolean value(final Object o) {
              return module.isDisposed();
            }
          });
        }
      }
    });
  }


  public void setRunConfig(@NotNull final CfmlProjectWizardData wizardData){

    ConfigurationFactory factory = CfmlRunConfigurationType.getInstance().getConfigurationFactories()[0];
    //factory.createTemplateConfiguration()
  }

}
