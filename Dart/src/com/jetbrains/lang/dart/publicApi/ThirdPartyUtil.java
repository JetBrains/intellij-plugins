package com.jetbrains.lang.dart.publicApi;

import com.google.dart.server.AnalysisServerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.actions.DartStyleAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This class is an access point for third party plugins to integrated Dart Plugin features in downstream plugins.
 */
@SuppressWarnings("unused")
public class ThirdPartyUtil {

  /**
   * Run dartfmt on the specified dart files.
   */
  public static void runDartfmt(@NotNull final Project project, @NotNull final List<VirtualFile> dartFiles) {
    DartStyleAction.runDartfmt(project, dartFiles);
  }

  /**
   * For some {@link Project}, add the {@link AnalysisServerListener}. If the {@link Project} has a running service, the listener is added
   * immediately, otherwise it is added when the server is started.
   */
  public static void addAnalysisServerListener(@NotNull final Project project,
                                               @NotNull final AnalysisServerListener serverListener) {
    DartAnalysisServerService.addAnalysisServerListener(project, serverListener);
  }

  /**
   * For some {@link Project}, remove the {@link AnalysisServerListener}. If the {@link Project} has a running service, the listener is
   * removed immediately.
   */
  public static void removeServerListener(@NotNull final Project project,
                                          @NotNull final AnalysisServerListener serverListener) {
    DartAnalysisServerService.removeServerListener(project, serverListener);
  }
}
