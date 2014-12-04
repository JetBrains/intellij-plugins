package com.jetbrains.lang.dart.analyzer;

import com.google.dart.server.AnalysisServerListener;
import com.google.dart.server.GetErrorsConsumer;
import com.google.dart.server.generated.types.*;
import com.google.dart.server.internal.remote.DebugPrintStream;
import com.google.dart.server.internal.remote.RemoteAnalysisServerImpl;
import com.google.dart.server.internal.remote.StdioServerSocket;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class DartAnalysisServerService {

  static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.DartAnalysisServerService");

  private final AnalysisServerListener myListener = new AnalysisServerListener() {
    /**
     * A new collection of completions have been computed for the given completion id.
     *
     * @param completionId the id associated with the completion
     * @param replacementOffset The offset of the start of the text to be replaced. This will be
     *          different than the offset used to request the completion suggestions if there was a
     *          portion of an identifier before the original offset. In particular, the
     *          replacementOffset will be the offset of the beginning of said identifier.
     * @param replacementLength The length of the text to be replaced if the remainder of the
     *          identifier containing the cursor is to be replaced when the suggestion is applied
     *          (that is, the number of characters in the existing identifier).
     * @param completions the completion suggestions being reported
     * @param isLast {@code true} if this is the last set of results that will be returned for the
     *          indicated completion
     */
    public void computedCompletion(String completionId, int replacementOffset, int replacementLength,
                                   List<CompletionSuggestion> completions, boolean isLast) {
    }

    /**
     * Reports the errors associated with a given file.
     *
     * @param file the file containing the errors
     * @param errors the errors contained in the file
     */
    public void computedErrors(String file, List<AnalysisError> errors) {

    }

    /**
     * A new collection of highlight regions has been computed for the given file. Each highlight
     * region represents a particular syntactic or semantic meaning associated with some range. Note
     * that the highlight regions that are returned can overlap other highlight regions if there is
     * more than one meaning associated with a particular region.
     *
     * @param file the file containing the highlight regions
     * @param highlights the highlight regions contained in the file
     */
    public void computedHighlights(String file, List<HighlightRegion> highlights) {
    }

    /**
     * New launch data has been computed.
     *
     * @param file the file for which launch data is being provided
     * @param kind the kind of the executable file, or {@code null} for non-Dart files
     * @param referencedFiles a list of the Dart files that are referenced by the file, or
     *          {@code null} for non-HTML files
     */
    public void computedLaunchData(String file, String kind, String[] referencedFiles) {
    }

    /**
     * A new collection of navigation regions has been computed for the given file. Each navigation
     * region represents a list of targets associated with some range. The lists will usually contain
     * a single target, but can contain more in the case of a part that is included in multiple
     * libraries or an Dart code that is compiled against multiple versions of a package. Note that
     * the navigation regions that are returned do not overlap other navigation regions.
     *
     * @param file the file containing the navigation regions
     * @param highlights the highlight regions associated with the source
     */
    public void computedNavigation(String file, List<NavigationRegion> targets) {
    }

    /**
     * A new collection of occurrences that been computed for the given file. Each occurrences object
     * represents a list of occurrences for some element in the file.
     *
     * @param file the file containing the occurrences
     * @param occurrencesArray the array of occurrences in the passed file
     */
    public void computedOccurrences(String file, List<Occurrences> occurrencesArray) {
    }

    /**
     * A new outline has been computed for the given file.
     *
     * @param file the file with which the outline is associated
     * @param outline the outline associated with the file
     */
    public void computedOutline(String file, Outline outline) {
    }

    /**
     * A new collection of overrides that have been computed for a given file. Each override array
     * represents a list of overrides for some file.
     *
     * @param file the file with which the outline is associated
     * @param overrides the overrides associated with the file
     */
    public void computedOverrides(String file, List<OverrideMember> overrides) {
    }

    /**
     * A new collection of search results have been computed for the given completion id.
     *
     * @param searchId the id associated with the search
     * @param results the search results being reported
     * @param last {@code true} if this is the last set of results that will be returned for the
     *          indicated search
     */
    public void computedSearchResults(String searchId, List<SearchResult> results, boolean last) {
    }

    /**
     * Reports that any analysis results that were previously associated with the given files should
     * be considered to be invalid because those files are no longer being analyzed, either because
     * the analysis root that contained it is no longer being analyzed or because the file no longer
     * exists.
     * <p>
     * If a file is included in this notification and at some later time a notification with results
     * for the file is received, clients should assume that the file is once again being analyzed and
     * the information should be processed.
     * <p>
     * It is not possible to subscribe to or unsubscribe from this notification.
     */
    public void flushedResults(List<String> files) {
    }

    /**
     * Reports that the server is running. This notification is issued once after the server has
     * started running to let the client know that it started correctly.
     */
    public void serverConnected() {
    }

    /**
     * An error happened in the {@link AnalysisServer}.
     *
     * @param isFatal {@code true} if the error is a fatal error, meaning that the server will
     *          shutdown automatically after sending this notification
     * @param message the error message indicating what kind of error was encountered
     * @param stackTrace the stack trace associated with the generation of the error, used for
     *          debugging the server
     */
    public void serverError(boolean isFatal, String message, String stackTrace) {
    }

    /**
     * Reports the current analysis status of the server.
     *
     * @param status the current analysis status of the server
     */
    public void serverStatus(AnalysisStatus status) {
    }
  };

  private final Application myApplication;
  private RemoteAnalysisServerImpl myServer;

  public DartAnalysisServerService() {
    myApplication = ApplicationManager.getApplication();
    startServer();
    Disposer.register(myApplication, new Disposable() {
      public void dispose() {
        stopServer();
      }
    });
  }

  @NotNull
  public static DartAnalysisServerService getInstance() {
    return ServiceManager.getService(DartAnalysisServerService.class);
  }

  public void updateContent(@Nullable final Map<String, Object> files) {
    if (files == null || files.isEmpty()) return;
    myServer.analysis_updateContent(files);
  }

  @NotNull
  public AnalysisError[] analysis_getErrors(@NotNull final PsiFile file) {
    final Ref<AnalysisError[]> resultError = new Ref<AnalysisError[]>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    myServer.analysis_getErrors(file.getOriginalFile().getVirtualFile().getPath(), new GetErrorsConsumer() {
      @Override
      public void computedErrors(final AnalysisError[] errors) {
        resultError.set(errors);
        countDownLatch.countDown();
      }
    });
    try {
      countDownLatch.await();
    }
    catch (InterruptedException e) {
      LOG.debug(e.getMessage(), e);
    }
    return resultError.get();
  }

  private void startServer() {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      LOG.error("No SDK");
      return;
    }

    final String sdkPath = sdk.getHomePath();
    final String runtimePath = sdkPath + "/bin/dart";
    final String analysisServerPath = sdkPath + "/bin/snapshots/analysis_server.dart.snapshot";
    final DebugPrintStream debugStream = new DebugPrintStream() {
      @Override
      public void println(String str) {
        //System.out.println("debugStream: " + str);
      }
    };

    final StdioServerSocket serverSocket =
      new StdioServerSocket(runtimePath, analysisServerPath, null, debugStream, new String[]{"--port=10000"}, false, false, 0, false);

    myServer = new RemoteAnalysisServerImpl(serverSocket);
    try {
      myServer.start();
    }
    catch (Exception e) {
      LOG.debug(e.getMessage(), e);
    }
    setAnalysisRoots(ProjectManager.getInstance().getOpenProjects());
    myServer.addAnalysisServerListener(myListener);
  }

  private void setAnalysisRoots(@NotNull final Project[] projects) {
    ArrayList<String> included = new ArrayList<String>();
    ArrayList<String> excluded = new ArrayList<String>();
    for (final Project project : projects) {
      for (final Module module : ModuleManager.getInstance(project).getModules()) {
        for (final VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
          included.add(contentRoot.getPath());
        }
        for (final VirtualFile excludedRoot : ModuleRootManager.getInstance(module).getExcludeRoots()) {
          excluded.add(excludedRoot.getPath());
        }
      }
    }
    myServer.analysis_setAnalysisRoots(included, excluded, null);
  }

  private void stopServer() {
    myServer.server_shutdown();
  }
}
