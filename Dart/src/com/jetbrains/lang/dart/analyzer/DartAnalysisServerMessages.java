package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class to send and receive Dart analysis busy messages on the project message bus.
 */
public class DartAnalysisServerMessages {
  public static final Topic<DartAnalysisNotifier> DART_ANALYSIS_TOPIC = Topic.create("dart.analysisBusy", DartAnalysisNotifier.class);

  public interface DartAnalysisNotifier {
    void analysisStarted();

    void analysisFinished();
  }

  public static void sendAnalysisStarted(@NotNull Project project, boolean isStarting) {
    final MessageBus bus = project.getMessageBus();
    final DartAnalysisNotifier publisher = bus.syncPublisher(DART_ANALYSIS_TOPIC);
    if (isStarting) {
      publisher.analysisStarted();
    }
    else {
      publisher.analysisFinished();
    }
  }
}
