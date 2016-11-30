package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

import static com.intellij.ide.actions.SendFeedbackAction.getDescription;

public class DefaultDartFeedbackBuilder implements DartFeedbackBuilder {
  private String myMessage;

  public String prompt() {
    return "Create issue on github?";
  }

  public void setMessage(String message) {
    myMessage = message;
  }

  public void sendFeedback(@Nullable Project project) {
    final ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
    boolean eap = appInfo.isEAP();
    String ijBuild = eap ? appInfo.getBuild().asStringWithoutProductCode() : appInfo.getBuild().asString();
    String sdkVsn = getSdkVersion(project);
    String platDescr = getDescription().replaceAll(";", " ");
    String template = DartBundle.message("dart.feedback.url.template", ijBuild, sdkVsn, platDescr);
    if (myMessage != null) {
      myMessage = "```dart\n" + myMessage + "```";
      String potentialTemplate = template + "\n\n" + myMessage;
      if (potentialTemplate.length() <= MAX_URL_LENGTH) {
        template = potentialTemplate;
      } else {
        try {
          File file = FileUtil.createTempFile("report", ".txt");
          FileUtil.writeToFile(file, myMessage);
          potentialTemplate = template + "\n\n" + DartBundle.message("dart.error.file.instructions", file.getAbsolutePath()) + "\n\n" + myMessage;
          template = potentialTemplate.substring(0, MAX_URL_LENGTH);
        }
        catch (IOException e) {
          // ignore it
        }
      }
    }
    openBrowserOnFeedbackForm(template, project);
  }

  public static void openBrowserOnFeedbackForm(String urlTemplate, Project project) {
    BrowserUtil.browse(urlTemplate, project);
  }

  protected String getSdkVersion(@Nullable Project project) {
    DartSdk sdk = getSdk(project);
    return sdk == null ? "<NO SDK>" : sdk.getVersion();
  }

  protected DartSdk getSdk(@Nullable Project project) {
    return project == null ? DartSdk.getGlobalDartSdk() : DartSdk.getDartSdk(project);
  }
}
