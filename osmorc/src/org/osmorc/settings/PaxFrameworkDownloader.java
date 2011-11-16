package org.osmorc.settings;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.DefaultJavaProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaAwareProjectJdkTableImpl;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathsList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.frameworkintegration.impl.AbstractPaxBasedFrameworkRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class which downloads an OSGi fraemwork using PAX Runner.
 */
public class PaxFrameworkDownloader {
  private static final Logger LOG = Logger.getInstance("#org.osmorc.settings.PaxFrameworkDownloader");

  private Pattern errorMessagePattern = Pattern.compile("^ */[ _]{2}/ *(.*)");
  private String myFrameworkType;
  private String myVersion;
  private String myTargetFolder;
  private String myProfiles;
  private DownloaderCallback myCallback;
  private boolean myClearDownloadFolder;
  private DefaultJavaProcessHandler myProcessHandler;
  private boolean myIsCancelled;
  private boolean myIsSuccessful;
  private String myErrorMessage;

  public PaxFrameworkDownloader(@NotNull String frameworkType,
                                @NotNull String version,
                                @NotNull String targetFolder,
                                @NotNull String profiles,
                                boolean clearDownloadFolder, @NotNull DownloaderCallback callback) {
    myFrameworkType = frameworkType;
    myVersion = version;
    myTargetFolder = targetFolder;
    myProfiles = profiles;
    myCallback = callback;
    myClearDownloadFolder = clearDownloadFolder;
  }

  /**
   * Performs the actual download.
   */
  public void download() {

    ProgressManager.getInstance().run(new Task.Modal(null, "Downloading OSGi framework", true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        perform(indicator);
        myCallback.downloadFinished(myIsSuccessful, myErrorMessage);
      }
    });
  }

  private void perform(final ProgressIndicator indicator) {
    indicator.setText("Downloading OSGi framework");
    indicator.setIndeterminate(true);

    try {
      myProcessHandler =
        new DefaultJavaProcessHandler(createJavaParameters()) {
          public void notifyTextAvailable(String text, Key outputType) {
            updateProgress(indicator, text);
          }
        };
    }
    catch (ExecutionException e) {
      myIsSuccessful = false;
      myErrorMessage = "Error when starting pax runner: " + e.getMessage();
      return;
    }

    start();
    readProcessOutput();
    stop();
    int i = myProcessHandler.getProcess().exitValue();
    if (i != 0) {
      myIsSuccessful = false;
    }
    else {
      myIsSuccessful = true;
    }
  }

  private JavaParameters createJavaParameters() throws ExecutionException {
    JavaParameters parameters = new JavaParameters();
    Sdk sdk = JavaAwareProjectJdkTableImpl.getInstanceEx().getInternalJdk();
    if (sdk == null) {
      throw new ExecutionException("No Java SDK available.");
    }
    parameters.setJdk(sdk);
    parameters.setMainClass(AbstractPaxBasedFrameworkRunner.PaxRunnerMainClass);
    PathsList classpath = parameters.getClassPath();
    for (VirtualFile libraryFile : AbstractPaxBasedFrameworkRunner.getPaxLibraries()) {
      classpath.add(libraryFile);
    }

    ParametersList parametersList = parameters.getProgramParametersList();
    parametersList.add("--p=" + myFrameworkType);
    if (!StringUtil.isEmpty(myVersion)) {
      parametersList.add("--v=" + myVersion);
    }
    parametersList.add("--nologo=true");
    parametersList.add("--executor=noop");
    parametersList.add("--workingDirectory=" + myTargetFolder);
    if ( myClearDownloadFolder ) {
      parametersList.add("--clean");
    }
    if (!StringUtil.isEmpty(myProfiles)) {
      parametersList.add("--profiles=" + myProfiles);
    }
    return parameters;
  }

  private void start() {
    myIsCancelled = false;
  }

  void stop() {
    if (myProcessHandler != null) {
      myProcessHandler.destroyProcess();
      myProcessHandler.waitFor();
    }
  }

  private void readProcessOutput() {
    myProcessHandler.startNotify();
    myProcessHandler.waitFor();
  }

  private void cancel() {
    myIsCancelled = true;
    stop();
  }

  private void updateProgress(final ProgressIndicator indicator, final String text) {
    LOG.info("PAX output: " + text.replace("\n", ""));
    Matcher matcher = errorMessagePattern.matcher(text.trim());
    // save error message for later..
    if (matcher.matches()) {
      String message = matcher.group(1).trim();
      if (!StringUtil.isEmpty(message) && !message.contains("--log=debug") && !message.contains("Oops")) {
        myErrorMessage =  (myErrorMessage != null ? myErrorMessage : "") +  message + "\n";
      }
    }
    if (indicator != null) {
      if (indicator.isCanceled()) {
        if (!myIsCancelled) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              cancel();
            }
          });
        }
      }
      indicator.setText2(text);
    }
  }

  public interface DownloaderCallback {
    /**
     * This is called when the download is finished.
     *
     * @param successful   true if the download was successfull
     * @param errorMessage the error message, if any. this is null when the download was not successful.
     */
    void downloadFinished(boolean successful, @Nullable String errorMessage);
  }
}
