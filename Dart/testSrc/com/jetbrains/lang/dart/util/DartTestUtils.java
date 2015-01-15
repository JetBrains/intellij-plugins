package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartTestUtils {

  public static final String BASE_TEST_DATA_PATH = findTestDataPath();
  public static final String SDK_HOME_PATH = BASE_TEST_DATA_PATH + "/sdk";

  private static String findTestDataPath() {
    final File f = new File("testData"); // launched from 'Dart-plugin' project
    if (f.isDirectory()) return FileUtil.toSystemIndependentName(f.getAbsolutePath());

    return FileUtil.toSystemIndependentName(PathManager.getHomePath() + "/contrib/Dart/testData");
  }

  public static void configureDartSdk(final @NotNull Module module) {
    final String dartSdkGlobalLibName;
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk != null) {
      dartSdkGlobalLibName = sdk.getGlobalLibName();
    }
    else {
      dartSdkGlobalLibName = ApplicationManager.getApplication().runWriteAction(new Computable<String>() {
        public String compute() {
          return DartSdkGlobalLibUtil.createDartSdkGlobalLib(module.getProject(), SDK_HOME_PATH);
        }
      });
    }

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        DartSdkGlobalLibUtil.configureDependencyOnGlobalLib(module, dartSdkGlobalLibName);
      }
    });
  }

  public static List<CaretPositionInfo> extractPositionMarkers(final @NotNull Project project, final @NotNull Document document) {
    final Pattern caretPattern = Pattern.compile(
      "<caret(?: expected=\'([^\']*)\')?(?: completionEquals=\'([^\']*)\')?(?: completionIncludes=\'([^\']*)\')?(?: completionExcludes=\'([^\']*)\')?>");
    final List<CaretPositionInfo> result = new ArrayList<CaretPositionInfo>();

    WriteCommandAction.runWriteCommandAction(null, new Runnable() {
      @Override
      public void run() {
        while (true) {
          Matcher m = caretPattern.matcher(document.getImmutableCharSequence());
          if (m.find()) {
            document.deleteString(m.start(), m.end());

            final int caretOffset = m.start();

            final String expected = m.group(1);

            final String completionEqualsRaw = m.group(2);
            final List<String> completionEqualsList = completionEqualsRaw == null ? null : StringUtil.split(completionEqualsRaw, ",");

            final String completionIncludesRaw = m.group(3);
            final List<String> completionIncludesList = completionIncludesRaw == null ? null : StringUtil.split(completionIncludesRaw, ",");

            final String completionExcludesRaw = m.group(4);
            final List<String> completionExcludesList = completionExcludesRaw == null ? null : StringUtil.split(completionExcludesRaw, ",");

            result.add(new CaretPositionInfo(caretOffset, expected, completionEqualsList, completionIncludesList, completionExcludesList));
          }
          else {
            break;
          }
        }
      }
    });

    if (!result.isEmpty()) {
      PsiDocumentManager.getInstance(project).commitDocument(document);
    }
    return result;
  }
}
