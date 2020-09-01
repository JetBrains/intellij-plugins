// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.coldFusion.mxunit;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.UI.runner.CfmlRunConfiguration;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

public class CfmlUnitRunConfiguration extends LocatableConfigurationBase {
  private CfmlUnitRunnerParameters myRunnerParameters;

  protected CfmlUnitRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
    myRunnerParameters = createRunnerParametersInstance();
  }


  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new CfmlUnitRunConfigurationForm(getProject());
  }

  private static class MyProcessHandler extends ProcessHandler {
    @Override
    protected void destroyProcessImpl() {
      notifyProcessTerminated(0);
    }

    @Override
    protected void detachProcessImpl() {
      notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
      return true;
    }

    @Override
    public OutputStream getProcessInput() {
      return null;
    }
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment env) {
    return new RunProfileState() {
      @Override
      public ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
        final ProcessHandler processHandler = new MyProcessHandler();

        final ConsoleView console = createConsole(getProject(), processHandler, env, executor);
        console.addMessageFilter(new CfmlStackTraceFilterProvider(getProject()));
        // processHandler.startNotify();
        runTests(processHandler);
        return new DefaultExecutionResult(console, processHandler);
      }
    };
  }

  private static boolean isVirtualFileUnderSourceRoot(VirtualFile virtualFile, Project project) {
    ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
    if (virtualFile == null || index.getContentRootForFile(virtualFile) == null) {
      return false;
    }
    return true;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    final CfmlUnitRunnerParameters params = getRunnerParameters();
    final String path = params.getPath();

    switch (params.getScope()) {
      case Method:
      case Component: {
        if (StringUtil.isEmpty(path)) {
          throw new RuntimeConfigurationError(CfmlBundle.message("cfml.runconfig.file.name.empty", path));
        }
        final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        if (file == null || !file.isValid() || file.isDirectory()) {
          throw new RuntimeConfigurationError(CfmlBundle.message("cfml.runconfig.file.not.found", path));
        }
        if (!isVirtualFileUnderSourceRoot(file, getProject())) {
          throw new RuntimeConfigurationError(CfmlBundle.message("cfml.runconfig.file.not.in.project", path));
        }
        final PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);
        if (!(psiFile instanceof CfmlFile)) {
          throw new RuntimeConfigurationException("Incorrect file type"); //NON-NLS
        }
        /*
        final CfmlFile cfmlFile = (CfmlFile)psiFile;
        PsiTreeUtil.getChildrenOfType(cfmlFile, CfmlTagImpl);
        if ()
        */
      }
      break;
      case Directory: {
        if (StringUtil.isEmpty(path)) {
          throw new RuntimeConfigurationError(CfmlBundle.message("cfml.runconfig.directory.name.empty", path));
        }
        final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        if (file == null || !file.isValid() || !file.isDirectory()) {
          throw new RuntimeConfigurationError(CfmlBundle.message("cfml.runconfig.directory.not.found", path));
        }
        if (!isVirtualFileUnderSourceRoot(file, getProject())) {
          throw new RuntimeConfigurationError(CfmlBundle.message("cfml.runconfig.directory.not.in.project", path));
        }
      }
      break;
    }
    // check URL
    CfmlRunConfiguration.checkURL(params.getWebPath());
  }

  private ConsoleView createConsole(Project project, ProcessHandler processHandler, ExecutionEnvironment env, Executor executor)
    throws ExecutionException {
    final CfmlUnitRunConfiguration runConfiguration = (CfmlUnitRunConfiguration)env.getRunProfile();
    final CfmlUnitConsoleProperties consoleProps = new CfmlUnitConsoleProperties(runConfiguration, executor);
    consoleProps.addStackTraceFilter(new CfmlStackTraceFilterProvider(getProject()));

    BaseTestsOutputConsoleView testsOutputConsoleView = SMTestRunnerConnectionUtil.createAndAttachConsole("Cfml", processHandler, consoleProps);
    Disposer.register(project, testsOutputConsoleView);
    return testsOutputConsoleView;
  }

  private void runTests(ProcessHandler processHandler) throws ExecutionException {
    CfmlUnitRemoteTestsRunner.
      executeScript(getRunnerParameters(), processHandler/*webPath, componentFile,
                    params.getScope() == CfmlUnitRunnerParameters.Scope.Method ? params.getMethod() : "",
                    processHandler*/);
  }

  protected CfmlUnitRunnerParameters createRunnerParametersInstance() {
    return new CfmlUnitRunnerParameters();
  }

  public CfmlUnitRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @SuppressWarnings({"CloneDoesntCallSuperClone"})
  @Override
  public CfmlUnitRunConfiguration clone() {
    Element element = new Element("tmp");
    writeExternal(element);
    CfmlUnitRunConfiguration clone =
      new CfmlUnitRunConfiguration(getProject(), getFactory(), getName());
    clone.readExternal(element);
    return clone;
  }

  @Override
  public void readExternal(@NotNull Element element) throws InvalidDataException {
    super.readExternal(element);
    myRunnerParameters = createRunnerParametersInstance();
    XmlSerializer.deserializeInto(myRunnerParameters, element);
  }

  @Override
  public void writeExternal(@NotNull Element element) throws WriteExternalException {
    super.writeExternal(element);
    if (myRunnerParameters != null) {
      XmlSerializer.serializeInto(myRunnerParameters, element);
    }
  }
}