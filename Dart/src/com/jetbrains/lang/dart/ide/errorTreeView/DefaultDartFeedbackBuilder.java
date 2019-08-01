// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DefaultDartFeedbackBuilder extends DartFeedbackBuilder {

  @Override
  public String prompt() {
    return "Open issue submission form?";
  }

  @Override
  public void sendFeedback(@NotNull Project project, @Nullable String errorMessage, @Nullable String serverLog) {
    final ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
    final boolean isEAP = appInfo.isEAP();
    final String intellijBuild = isEAP ? appInfo.getBuild().asStringWithoutProductCode() : appInfo.getBuild().asString();
    final String sdkVersion = getSdkVersion(project);
    final String platformDescription = StringUtil.replace(SendFeedbackAction.getDescription(project), ";", " ").trim();

    final String url = DartBundle.message("dart.feedback.url");
    String body = DartBundle.message("dart.feedback.template", intellijBuild, sdkVersion, platformDescription);

    if (errorMessage != null) {
      // The github servers don't like urls longer than 8k chars; we limit our exception text to 5k
      // in order to stay under the url limit.
      body += DartBundle.message("dart.feedback.error", limitTextTo(errorMessage, 5 * 1024).trim());
    }

    if (serverLog != null) {
      try {
        final File file = FileUtil.createTempFile("report", ".txt");
        FileUtil.writeToFile(file, errorMessage == null ? "" : errorMessage.trim());
        FileUtil.writeToFile(file, "\n\n" + serverLog, true);
        body += DartBundle.message("dart.feedback.file", file.getAbsolutePath());
      }
      catch (IOException e) {
        // ignore it
      }
    }

    openBrowserOnFeedbackForm(url + urlEncode(body), project);
  }

  /**
   * Return a string that is no longer than the given length.
   * <p>
   * If the string is longer than maxLength, return the longest substring of the string that we can,
   * with a trailing "..." appended to the last line.
   */
  @SuppressWarnings("SameParameterValue")
  private static String limitTextTo(@NotNull String string, int maxLength) {
    if (string.length() > maxLength) {
      return StringUtil.trimTrailing(string.substring(0, maxLength - 4)) + "...\n";
    }
    else {
      return string;
    }
  }

  public static void openBrowserOnFeedbackForm(@NotNull String urlTemplate, @Nullable Project project) {
    BrowserUtil.browse(urlTemplate, project);
  }

  protected String getSdkVersion(@NotNull Project project) {
    DartSdk sdk = DartSdk.getDartSdk(project);
    return sdk == null ? "<NO SDK>" : sdk.getVersion();
  }

  private static String urlEncode(String input) {
    try {
      return URLEncoder.encode(input, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      // Unreachable - UTF-8 is always supported.
      return input;
    }
  }
}
