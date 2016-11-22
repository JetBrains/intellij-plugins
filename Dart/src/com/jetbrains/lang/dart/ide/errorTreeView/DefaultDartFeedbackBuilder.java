package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.Nullable;

import static com.intellij.ide.actions.SendFeedbackAction.getDescription;

public class DefaultDartFeedbackBuilder implements DartFeedbackBuilder {

  public String prompt() {
    return "Create issue on github?";
  }

  public void sendFeedback(@Nullable Project project) {
    final ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
    boolean eap = appInfo.isEAP();
    String ijBuild = eap ? appInfo.getBuild().asStringWithoutProductCode() : appInfo.getBuild().asString();
    String sdkVsn = getSdkVersion(project);
    String platDescr = getDescription();
    String template = DartBundle.message("dart.feedback.url.template", ijBuild, sdkVsn, platDescr);
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
