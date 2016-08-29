package com.jetbrains.lang.dart.ide.runner.server;

import com.intellij.execution.ConsoleFolding;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.ide.runner.DartConsoleFilter;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.sdk.DartSdkUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class DartConsoleFolding extends ConsoleFolding {

  public static final String DART_MARKER = SystemInfo.isWindows ? "\\bin\\dart.exe " : "/bin/dart ";

  @Override
  public boolean shouldFoldLine(@NotNull final String line) {
    // fold Dart VM command line created in DartCommandLineRunningState.createCommandLine() together with the following "Observatory listening on ..." message
    if (line.startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) return true;

    if (!line.contains(DART_MARKER)) return false;

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    return sdk != null && line.startsWith(FileUtil.toSystemDependentName(DartSdkUtil.getDartExePath(sdk)) + " ");
  }

  @Nullable
  @Override
  public String getPlaceholderText(@NotNull final List<String> lines) {
    // C:\dart-sdk\bin\dart.exe --checked --pause_isolates_on_start --enable-vm-service:55465 C:\dart_projects\DartSample\bin\file1.dart arg
    // is collapsed to "dart file1.dart arg"

    // depending on the Moon phase (well, on initialization speed) we may get lines.size() == 2 where first line is Dart VM startup and 2nd line is Observatory URL)
    // but more frequently we get these 2 lines one by one

    if (lines.size() == 1 && lines.get(0).startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) {
      return " [Observatory: " + lines.get(0).substring(DartConsoleFilter.OBSERVATORY_LISTENING_ON.length()) + "]";
    }

    final String fullText = StringUtil.join(lines, "\n");
    if (!lines.get(0).contains(DART_MARKER) ||
        lines.size() == 2 && !lines.get(1).startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON) ||
        lines.size() > 2) {
      // unexpected text
      return fullText;
    }

    final String line = lines.get(0);
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

    final StringBuilder b = new StringBuilder();
    b.append("dart ");
    b.append(PathUtil.getFileName(filePath));

    while (tok.hasMoreTokens()) {
      b.append(" ").append(tok.nextToken()); // program arguments
    }

    if (lines.size() == 2 && lines.get(1).startsWith(DartConsoleFilter.OBSERVATORY_LISTENING_ON)) {
      b.append(" [Observatory: ").append(lines.get(1).substring(DartConsoleFilter.OBSERVATORY_LISTENING_ON.length())).append("]");
    }

    return b.toString();
  }
}
