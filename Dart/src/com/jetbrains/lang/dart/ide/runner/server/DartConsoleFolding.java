// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ConsoleFolding;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class DartConsoleFolding extends ConsoleFolding {

  private static final String DART_MARKER = SystemInfo.isWindows ? "\\bin\\dart.exe " : "/bin/dart ";
  private static final String WEBDEV_RUNNER_MARKER = SystemInfo.isWindows
                                                     ? "\\bin\\pub.bat global run webdev daemon " : "/bin/pub global run webdev daemon ";
  private static final String TEST_RUNNER_MARKER = SystemInfo.isWindows
                                                   ? "\\bin\\pub.bat run test -r json " : "/bin/pub run test -r json ";
  private static final int MIN_FRAME_DISPLAY_COUNT = 8;

  private int myFrameCount = 0;

  @Override
  public boolean shouldFoldLine(@NotNull Project project, @NotNull final String line) {
    // check for a stack trace
    if (isFrameLine(line)) {
      myFrameCount++;

      // Show the first n frames and fold the rest.
      return myFrameCount > MIN_FRAME_DISPLAY_COUNT;
    }

    myFrameCount = 0;

    // fold Dart VM command line created in DartCommandLineRunningState.createCommandLine() together with the following "Observatory listening on ..." message
    if (line.startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) return true;

    int index = line.indexOf(DART_MARKER);
    if (index < 0) index = line.indexOf(TEST_RUNNER_MARKER);
    if (index < 0) index = line.indexOf(WEBDEV_RUNNER_MARKER);
    if (index < 0) return false;

    final String probablySdkPath = line.substring(0, index);
    return DartSdkUtil.isDartSdkHome(probablySdkPath);
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull Project project, @NotNull final List<String> lines) {
    // C:\dart-sdk\bin\dart.exe --checked --pause_isolates_on_start --enable-vm-service:55465 C:\dart_projects\DartSample\bin\file1.dart arg
    // is collapsed to "dart file1.dart arg"

    // depending on the Moon phase (well, on initialization speed) we may get lines.size() == 2 where first line is Dart VM startup and 2nd line is Observatory URL)
    // but more frequently we get these 2 lines one by one

    if (lines.size() == 1 && lines.get(0).startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) {
      return " [Debug service available at " + lines.get(0).substring(DartConsoleFilter.OBSERVATORY_LISTENING_ON.length()) + "]";
    }

    if (lines.size() == 1 && lines.get(0).contains(TEST_RUNNER_MARKER)) {
      return foldTestRunnerCommand(lines.get(0));
    }

    if (lines.size() == 1 && lines.get(0).contains(WEBDEV_RUNNER_MARKER)) {
      return foldWebdevCommand(lines.get(0));
    }

    // exception folding
    if (isFrameLine(lines.get(0))) {
      return " [" + lines.size() + " more...]";
    }

    final String fullText = StringUtil.join(lines, "\n");
    if (lines.size() > 2 ||
        lines.size() == 2 && !lines.get(1).startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON) ||
        !lines.get(0).contains(DART_MARKER) && !lines.get(0).contains(TEST_RUNNER_MARKER)) {
      // unexpected text
      return fullText;
    }

    final StringBuilder b = new StringBuilder();

    final String line = lines.get(0);
    if (line.contains(DART_MARKER)) {
      int index = line.indexOf(' ');
      assert index > 0 && line.substring(0, index + 1).endsWith(DART_MARKER) : line;

      while (line.length() > index + 1 && line.charAt(index + 1) == '-') {
        index = line.indexOf(" ", index + 1);
      }

      if (index < 0) return fullText; // can't happen

      final CommandLineTokenizer tok = new CommandLineTokenizer(line.substring(index));
      if (!tok.hasMoreTokens()) return fullText; // can't happen

      final String filePath = tok.nextToken();
      if (!filePath.contains(File.separator)) return fullText; // can't happen

      b.append("dart ");
      b.append(PathUtil.getFileName(filePath));

      while (tok.hasMoreTokens()) {
        b.append(" ").append(tok.nextToken()); // program arguments
      }
    }
    else if (line.contains(TEST_RUNNER_MARKER)) {
      b.append(foldTestRunnerCommand(line));
    }
    else if (line.contains(WEBDEV_RUNNER_MARKER)) {
      b.append(foldWebdevCommand(line));
    }
    else {
      return fullText; // can't happen
    }

    if (lines.size() == 2 && lines.get(1).startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) {
      b.append(" [Debug service available at ").append(lines.get(1).substring(DartConsoleFilter.OBSERVATORY_LISTENING_ON.length()))
        .append("]");
    }

    return b.toString();
  }

  private boolean isFrameLine(@NotNull final String line) {
    // Handle the "..." ellipses in the middle of a stack overflow trace.
    if (myFrameCount > 0 && line.equals("...")) return true;

    // Handle "<asynchronous suspension>".
    if (myFrameCount > 0 && line.startsWith("<async") && line.endsWith(">")) return true;

    //  #1      main (file:///Users/foo/projects/bar/tool/generate.dart:30:3)
    if (!line.startsWith("#") || !line.endsWith(")")) return false;
    if (line.length() < "#1234567x (x)".length()) return false;
    if (line.charAt(8) == ' ') return false;

    try {
      Integer.parseInt(line.substring(1, 8).trim());
      return true;
    }
    catch (Throwable t) {
      return false;
    }
  }

  private static String foldTestRunnerCommand(@NotNull final String line) {
    // C:\dart-sdk\bin\pub.bat run test -r json --concurrency=4 C:/MyProject/test/main_test.dart -n "group1 test21|group1 test22"
    // folded to
    // pub run test main_test.dart -n "group1 test21|group1 test22"
    int index = line.indexOf(TEST_RUNNER_MARKER);
    index += TEST_RUNNER_MARKER.length();
    index = StringUtil.toLowerCase(line).indexOf(".dart", index);
    if (index < 0) return line;

    int tailIndex = index + (line.substring(index + ".dart".length()).startsWith("\"") ? ".dart\"".length() : ".dart".length());

    int slashIndex = FileUtil.toSystemIndependentName(line.substring(0, index)).lastIndexOf('/');
    if (slashIndex < 0) return line;

    return "pub run test " + line.substring(slashIndex + 1, index) + ".dart" + line.substring(tailIndex);
  }

  private static String foldWebdevCommand(@NotNull final String line) {
    // /<path-to-sdk>/bin/pub global run webdev daemon web:53322 --launch-app=web/index.html
    // folded to
    // webdev serve web:53322 --launch-app=web/index.html
    int index = line.indexOf(WEBDEV_RUNNER_MARKER);
    if (index >= 0) {
      return "webdev serve " + line.substring(index + WEBDEV_RUNNER_MARKER.length());
    }
    return line;
  }
}
