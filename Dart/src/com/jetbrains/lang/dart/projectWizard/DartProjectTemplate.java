// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.browsers.impl.WebBrowserServiceImpl;
import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.intellij.util.Url;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfigurationType;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import com.jetbrains.lang.dart.projectWizard.Stagehand.StagehandDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DartProjectTemplate {

  private static final Stagehand STAGEHAND = new Stagehand();
  private static List<DartProjectTemplate> ourTemplateCache;

  private static final Logger LOG = Logger.getInstance(DartProjectTemplate.class.getName());

  @NotNull private final String myName;
  @NotNull private final String myDescription;

  public DartProjectTemplate(@NotNull final String name, @NotNull final String description) {
    myName = name;
    myDescription = description;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getDescription() {
    return myDescription;
  }

  public abstract Collection<VirtualFile> generateProject(@NotNull final String sdkRoot,
                                                          @NotNull final Module module,
                                                          @NotNull final VirtualFile baseDir)
    throws IOException;


  /**
   * Must be called in pooled thread without read action; {@code templatesConsumer} will be invoked in EDT
   */
  public static void loadTemplatesAsync(final String sdkRoot, @NotNull final Consumer<List<DartProjectTemplate>> templatesConsumer) {
    if (ApplicationManager.getApplication().isReadAccessAllowed()) {
      LOG.error("DartProjectTemplate.loadTemplatesAsync() must be called in pooled thread without read action");
    }

    final List<DartProjectTemplate> templates = new ArrayList<>();
    try {
      templates.addAll(getStagehandTemplates(sdkRoot));
    }
    finally {
      if (templates.isEmpty()) {
        templates.add(new WebAppTemplate());
        templates.add(new CmdLineAppTemplate());
      }

      ApplicationManager.getApplication().invokeLater(() -> templatesConsumer.consume(templates), ModalityState.any());
    }
  }

  @NotNull
  private static List<DartProjectTemplate> getStagehandTemplates(@NotNull final String sdkRoot) {
    if (ourTemplateCache != null) {
      return ourTemplateCache;
    }

    STAGEHAND.install(sdkRoot);

    final List<StagehandDescriptor> templates = STAGEHAND.getAvailableTemplates(sdkRoot);

    ourTemplateCache = new ArrayList<>();

    for (StagehandDescriptor template : templates) {
      ourTemplateCache.add(new StagehandTemplate(STAGEHAND, template));
    }

    return ourTemplateCache;
  }

  static void createWebRunConfiguration(final @NotNull Module module, final @NotNull VirtualFile htmlFile) {
    DartModuleBuilder.runWhenNonModalIfModuleNotDisposed(() -> {
      final Url url = WebBrowserServiceImpl.getDebuggableUrl(PsiManager.getInstance(module.getProject()).findFile(htmlFile));
      if (url == null) return;

      final RunManager runManager = RunManager.getInstance(module.getProject());
      try {
        final RunnerAndConfigurationSettings settings =
          runManager.createConfiguration("", JavascriptDebugConfigurationType.getTypeInstance());

        ((JavaScriptDebugConfiguration)settings.getConfiguration()).setUri(url.toDecodedForm());
        settings.setName(((JavaScriptDebugConfiguration)settings.getConfiguration()).suggestedName());

        runManager.addConfiguration(settings);
        runManager.setSelectedConfiguration(settings);
      }
      catch (Throwable t) {/* ClassNotFound in IDEA Community or if JS Debugger plugin disabled */}
    }, module);
  }

  static void createCmdLineRunConfiguration(final @NotNull Module module, final @NotNull VirtualFile mainDartFile) {
    DartModuleBuilder.runWhenNonModalIfModuleNotDisposed(() -> {
      final RunManager runManager = RunManager.getInstance(module.getProject());
      final RunnerAndConfigurationSettings settings =
        runManager.createConfiguration("", DartCommandLineRunConfigurationType.getInstance().getConfigurationFactories()[0]);

      final DartCommandLineRunConfiguration runConfiguration = (DartCommandLineRunConfiguration)settings.getConfiguration();
      runConfiguration.getRunnerParameters().setFilePath(mainDartFile.getPath());
      runConfiguration.getRunnerParameters()
        .setWorkingDirectory(DartCommandLineRunnerParameters.suggestDartWorkingDir(module.getProject(), mainDartFile));

      settings.setName(runConfiguration.suggestedName());

      runManager.addConfiguration(settings);
      runManager.setSelectedConfiguration(settings);
    }, module);
  }
}
