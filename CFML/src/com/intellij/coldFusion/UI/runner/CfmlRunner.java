/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.UI.runner;

import com.intellij.coldFusion.mxunit.CfmlUnitRunConfiguration;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.jetbrains.annotations.NotNull;

public class CfmlRunner extends DefaultProgramRunner {
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final RunProfile runProfileRaw = env.getRunProfile();
    if (runProfileRaw instanceof CfmlRunConfiguration) {
      FileDocumentManager.getInstance().saveAllDocuments();
      final CfmlRunConfiguration runProfile = (CfmlRunConfiguration)runProfileRaw;
      final CfmlRunnerParameters params = runProfile.getRunnerParameters();
      String uuid = params.getNonDefaultBrowserUuid();
      WebBrowser webBrowser = (uuid.isEmpty() ? null : WebBrowserManager.getInstance().findBrowserById(uuid));
      BrowserLauncher.getInstance().browse(params.getUrl() + params.getPageUrl(), webBrowser, env.getProject());
      return null;
    }
    else {
      return super.doExecute(state, env);
    }
  }

  @Override
  @NotNull
  public String getRunnerId() {
    return "CfmlRunner";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof CfmlRunConfiguration || profile instanceof CfmlUnitRunConfiguration);
  }
}