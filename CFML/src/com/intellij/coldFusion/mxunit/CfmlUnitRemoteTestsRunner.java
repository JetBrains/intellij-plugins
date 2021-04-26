// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.mxunit;

import com.intellij.coldFusion.UI.editorActions.CfmlScriptNodeSuppressor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ResourceUtil;
import com.intellij.util.io.HttpRequests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class CfmlUnitRemoteTestsRunner {
  private static final Logger LOG = Logger.getInstance(CfmlUnitRemoteTestsRunner.class.getName());

  public static String getLauncherText(String resourcePath) {
    try {
      return ResourceUtil.loadText(Objects.requireNonNull(CfmlUnitRunConfiguration.class.getClassLoader()
                                                            .getResourceAsStream(StringUtil.trimStart(resourcePath, "/"))))
        .replaceFirst("\\Q/*system_delimiter*/\\E", (String.valueOf(File.separatorChar)).replace("\\", "\\\\\\\\"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void deleteFile(final VirtualFile file) throws ExecutionException {
    final Ref<IOException> error = new Ref<>();

    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
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

    ApplicationManager.getApplication().invokeAndWait(runnable);
    if (!error.isNull()) {
      throw new ExecutionException(error.get().getMessage());
    }
  }

  public static void createFile(final VirtualFile directory, final String fileName, final String fileText)
    throws ExecutionException {
    LOG.assertTrue(directory != null);
    final Ref<IOException> error = new Ref<>();

    final Runnable runnable = () -> ApplicationManager.getApplication().runWriteAction(() -> {
      try {
        VirtualFile file = directory.findChild(fileName);
        if (file == null) {
          file = directory.createChildData(CfmlUnitRunConfiguration.class, fileName);
        }
        CfmlScriptNodeSuppressor.suppress(file);
        VfsUtil.saveText(file, fileText);
      }
      catch (IOException e) {
        error.set(e);
      }
    });
    ApplicationManager.getApplication().invokeAndWait(runnable);

    if (!error.isNull()) {
      throw new ExecutionException(error.get().getMessage());
    }
  }

  public static void executeScript(final CfmlUnitRunnerParameters params,
                                   final ProcessHandler processHandler/*final String webPath,
                                   final String componentFilePath,
                                   final String methodName,
                                   final ProcessHandler processHandler*/) throws ExecutionException {
    final Ref<ExecutionException> ref = new Ref<>();

    ApplicationManager.getApplication().assertIsDispatchThread();

    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        final VirtualFile componentFile =
          LocalFileSystem.getInstance().refreshAndFindFileByPath(params.getPath());
        if (componentFile == null) {
          throw new ExecutionException("File " + params.getPath() + " not found"); //NON-NLS
        }

        // creating script files
        final VirtualFile directory = componentFile.getParent();
        final String launcherFileName = "mxunit-launcher.cfc";//generateUniqueName("mxunit-launcher", project);
        LOG.debug("Copying script file" + launcherFileName + " to component folder: " + directory);
        createFile(directory, launcherFileName, getLauncherText("/scripts/mxunit-launcher.cfc"));

        final String resultsFileName = "mxunit-result-capture.cfc";//generateUniqueName("mxunit-result-capture", project);
        LOG.debug("Copying results capture file " + resultsFileName + " to component folder: " + directory);
        createFile(directory, resultsFileName, getLauncherText("/scripts/mxunit-result-capture.cfc"));

        // retrieving data through URL
        String webPath = params.getWebPath();
        if (webPath.endsWith("/") || webPath.endsWith("\\")) {
          webPath = webPath.substring(0, webPath.length() - 1);
        }
        String agentPath = webPath.substring(0, webPath.lastIndexOf('/')) + "/" + launcherFileName;
        LOG.debug("Retrieving data from coldfusion server by " + agentPath + " URL");
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
        try {
          LOG.debug("Retrieving test results from: " + agentUrl);
          /*
          final FileObject httpFile = getManager().resolveFile(agentUrl);

          reader = new BufferedReader(new InputStreamReader(httpFile.getContent().getInputStream()));
          */
          HttpRequests.request(agentUrl).connect(request -> {
            BufferedReader reader = request.getReader();
            String line;
            while (!processHandler.isProcessTerminating() &&
                   !processHandler.isProcessTerminated() &&
                   (line = reader.readLine()) != null) {
              if (!StringUtil.isEmptyOrSpaces(line)) {
                LOG.debug("MXUnit: " + line);
                processHandler.notifyTextAvailable(line + "\n", ProcessOutputTypes.SYSTEM);
              }
            }
            return null;
          });
        }
        catch (HttpRequests.HttpStatusException e) {
          LOG.debug("Http request failed: " + e.getMessage());
          processHandler.notifyTextAvailable("Http request failed: " + e.getMessage(), ProcessOutputTypes.SYSTEM);
        }
        catch (IOException e) {
          LOG.warn(e);
          processHandler
            .notifyTextAvailable("Failed to retrieve test results from the server at " + agentUrl + "\n", ProcessOutputTypes.SYSTEM);
        }
        LOG.debug("Cleaning temporary files");
        deleteFile(directory.findChild(launcherFileName));
        deleteFile(directory.findChild(resultsFileName));
        if (!processHandler.isProcessTerminated() && !processHandler.isProcessTerminating()) {
          processHandler.destroyProcess();
        }
      }
      catch (ExecutionException e) {
        ref.set(e);
      }
    });
    if (!ref.isNull()) {
      throw ref.get();
    }
  }
}
