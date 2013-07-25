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
package com.intellij.coldFusion.mxunit;

import com.intellij.coldFusion.UI.editorActions.CfmlScriptNodeSuppressor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ResourceUtil;
import com.intellij.util.SystemProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;

public class CfmlUnitRemoteTestsRunner {
  private static final Logger LOG = Logger.getInstance(CfmlUnitRemoteTestsRunner.class.getName());

  public static String getLauncherText(String resourcePath) {
    try {
      return ResourceUtil.loadText(CfmlUnitRunConfiguration.class.getResource(resourcePath))
        .replaceFirst("\\Q/*system_delimiter*/\\E", ("" + File.separatorChar).replace("\\", "\\\\\\\\"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void deleteFile(Project project, final VirtualFile file) throws ExecutionException {
    final Ref<IOException> error = new Ref<IOException>();

    final Runnable runnable = new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              if (file.isValid()) {
                file.delete(this);
              }
            }
            catch (IOException e) {
              error.set(e);
            }
          }
        });
      }
    };

    if (ApplicationManager.getApplication().isDispatchThread()) {
      runnable.run();
    }
    else {
      ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();
      ApplicationManager.getApplication().invokeAndWait(runnable, pi != null ? pi.getModalityState() : ModalityState.NON_MODAL);
    }
    if (!error.isNull()) {
      //noinspection ThrowableResultOfMethodCallIgnored
      throw new ExecutionException(error.get().getMessage());
    }
  }

  public static VirtualFile createFile(Project project, final VirtualFile directory, final String fileName, final String fileText)
    throws ExecutionException {
    LOG.assertTrue(directory != null);
    final Ref<IOException> error = new Ref<IOException>();
    final Ref<VirtualFile> launcherFile = new Ref<VirtualFile>();

    final Runnable runnable = new Runnable() {
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              VirtualFile file = directory.findChild(fileName);
              if (file == null) {
                file = directory.createChildData(CfmlUnitRunConfiguration.class, fileName);
              }
              CfmlScriptNodeSuppressor.suppress(file);
              VfsUtil.saveText(file, fileText);
              launcherFile.set(file);
            }
            catch (IOException e) {
              error.set(e);
            }
          }
        });
      }
    };
    if (ApplicationManager.getApplication().isDispatchThread()) {
      runnable.run();
    }
    else {
      ProgressIndicator pi = ProgressManager.getInstance().getProgressIndicator();
      ApplicationManager.getApplication().invokeAndWait(runnable, pi != null ? pi.getModalityState() : ModalityState.NON_MODAL);
    }

    if (!error.isNull()) {
      //noinspection ThrowableResultOfMethodCallIgnored
      throw new ExecutionException(error.get().getMessage());
    }
    return launcherFile.get();
  }

  private static String generateUniqueName(String prefix, Project project) {
    return prefix +
           "_" +
           project.getName().replaceAll("[^\\p{Alnum}]", "_") +
           "_" +
           SystemProperties.getUserName().replaceAll("[^\\p{Alnum}]", "_") +
           ".cfc";
  }

  public static void executeScript(final CfmlUnitRunnerParameters params,
                                   final ProcessHandler processHandler/*final String webPath,
                                   final String componentFilePath,
                                   final String methodName,
                                   final ProcessHandler processHandler*/,
                                   final Project project) throws ExecutionException {
    final Ref<ExecutionException> ref = new Ref<ExecutionException>();

    ApplicationManager.getApplication().assertIsDispatchThread();

    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        try {
          final VirtualFile componentFile =
            LocalFileSystem.getInstance().refreshAndFindFileByPath(params.getPath());
          if (componentFile == null) {
            throw new ExecutionException("File " + params.getPath() + " not found");
          }

          // creating script files
          final VirtualFile directory = componentFile.getParent();
          final String launcherFileName = "mxunit-launcher.cfc";//generateUniqueName("mxunit-launcher", project);
          LOG.debug("Copying script file" + launcherFileName + " to component folder: " + directory);
          createFile(project, directory, launcherFileName, getLauncherText("/scripts/mxunit-launcher.cfc"));

          final String resultsFileName = "mxunit-result-capture.cfc";//generateUniqueName("mxunit-result-capture", project);
          LOG.debug("Copying results capture file " + resultsFileName + " to component folder: " + directory);
          createFile(project, directory, resultsFileName, getLauncherText("/scripts/mxunit-result-capture.cfc"));

          // retrieving data through URL
          String webPath = params.getWebPath();
          if (webPath.endsWith("/") || webPath.endsWith("\\")) {
            webPath = webPath.substring(0, webPath.length() - 1);
          }
          String agentPath = webPath.substring(0, webPath.lastIndexOf('/')) + "/" + launcherFileName;
          LOG.debug("Retrieving data from coldfusion server by " + agentPath + " URL");
          BufferedReader reader = null;
          String agentUrl;
          if (params.getScope() == CfmlUnitRunnerParameters.Scope.Directory) {
            agentUrl = agentPath + "?method=executeDirectory&directoryName=" + componentFile.getName();
          }
          else {
            agentUrl = agentPath + "?method=executeTestCase&componentName=" + componentFile.getNameWithoutExtension();
            if (params.getScope() == CfmlUnitRunnerParameters.Scope.Method) {
              agentUrl += "&methodName=" + params.getMethod();
            }
          }
          HttpMethod method = null;
          try {
            LOG.debug("Retrieving test results from: " + agentUrl);
            /*
            final FileObject httpFile = getManager().resolveFile(agentUrl);

            reader = new BufferedReader(new InputStreamReader(httpFile.getContent().getInputStream()));
            */
            HttpClient client = new HttpClient();
            method = new GetMethod(agentUrl);
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
              LOG.debug("Http request failed: " + method.getStatusLine());
              processHandler.notifyTextAvailable("Http request failed: " + method.getStatusLine(), ProcessOutputTypes.SYSTEM);
            }
            final InputStream responseStream = method.getResponseBodyAsStream();
            reader = new BufferedReader(new InputStreamReader(responseStream));
            String line;
            while (!processHandler.isProcessTerminating() && !processHandler.isProcessTerminated() && (line = reader.readLine()) != null) {
              if (!StringUtil.isEmptyOrSpaces(line)) {
                LOG.debug("MXUnit: " + line);
                processHandler.notifyTextAvailable(line + "\n", ProcessOutputTypes.SYSTEM);
              }
            }
          }
          catch (IOException e) {
            LOG.warn(e);
            processHandler
              .notifyTextAvailable("Failed to retrieve test results from the server at " + agentUrl + "\n", ProcessOutputTypes.SYSTEM);
          }
          finally {
            if (method != null) {
              method.releaseConnection();
            }
            if (reader != null) {
              try {
                reader.close();
              }
              catch (IOException e) {
                // ignore
              }
            }
          }
          LOG.debug("Cleaning temporary files");
          deleteFile(project, directory.findChild(launcherFileName));
          deleteFile(project, directory.findChild(resultsFileName));
          if (!processHandler.isProcessTerminated() && !processHandler.isProcessTerminating()) {
            processHandler.destroyProcess();
          }
        }
        catch (ExecutionException e) {
          ref.set(e);
        }
      }
    });
    if (!ref.isNull()) {
      throw ref.get();
    }
  }
}
