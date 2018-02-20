package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.SendFeedbackAction;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class DefaultDartFeedbackBuilder extends DartFeedbackBuilder {

  public String prompt() {
    return "Open issue submission form?";
  }

  public void sendFeedback(@NotNull Project project, @Nullable String errorMessage, @Nullable String serverLog) {
    final ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
    boolean isEAP = appInfo.isEAP();
    String ijBuild = isEAP ? appInfo.getBuild().asStringWithoutProductCode() : appInfo.getBuild().asString();
    String sdkVersion = getSdkVersion(project);
    String platformDescription = StringUtil.replace(SendFeedbackAction.getDescription(), ";", " ").trim();
    String urlTemplate = DartBundle.message("dart.feedback.url.template", ijBuild, sdkVersion, platformDescription);
    if (errorMessage != null) {
      errorMessage = "```\n" + errorMessage + "```";
      try {
        File file = FileUtil.createTempFile("report", ".txt");
        FileUtil.writeToFile(file, errorMessage);
        if (serverLog != null) {
          // Assume serverLog is never long enough that opening and closing the file is cheaper than copying it.
          FileUtil.writeToFile(file, "\n\n" + serverLog, true);
        }
        String potentialTemplate =
          urlTemplate + "\n\n" + DartBundle.message("dart.error.file.instructions", file.getAbsolutePath()) + "\n\n" + errorMessage;
        urlTemplate = potentialTemplate.substring(0, Math.min(potentialTemplate.length(), MAX_URL_LENGTH));
      }
      catch (IOException e) {
        // ignore it
      }
    }
    openBrowserOnFeedbackForm(urlTemplate, project);
  }

  public static void openBrowserOnFeedbackForm(@NotNull String urlTemplate, @Nullable Project project) {
    BrowserUtil.browse(urlTemplate, project);
  }

  protected String getSdkVersion(@NotNull Project project) {
    DartSdk sdk = DartSdk.getDartSdk(project);
    return sdk == null ? "<NO SDK>" : sdk.getVersion();
  }
}
