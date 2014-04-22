package com.jetbrains.lang.dart.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.containers.hash.LinkedHashMap;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkGlobalLibUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

  public static LinkedHashMap<Integer, String> extractPositionMarkers(final @NotNull Project project, final @NotNull Document document) {
    final Pattern caretPattern = Pattern.compile("<caret expected=\'([^\']*)\'>");
    final LinkedHashMap<Integer, String> result = new LinkedHashMap<Integer, String>();
    WriteCommandAction.runWriteCommandAction(null, new Runnable() {
      @Override
      public void run() {
        while (true) {
          Matcher m = caretPattern.matcher(document.getText());
          if (m.find()) {
            document.deleteString(m.start(), m.end());
            result.put(m.start() < document.getTextLength() - 1 ? m.start() : m.start() - 1, m.group(1));
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
