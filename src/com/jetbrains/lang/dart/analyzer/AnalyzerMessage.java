package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class AnalyzerMessage {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.analyzer.AnalyzerMessage");

  private final VirtualFile virtualFile;
  private final int line;
  private final int offset;
  private final int length;
  private final Type type;
  private final String errorCode;
  private final String message;
  private final String subSystem;

  protected AnalyzerMessage(@NotNull VirtualFile file,
                            int line,
                            int offset,
                            int length,
                            Type type,
                            String system,
                            String code,
                            String message) {
    virtualFile = file;
    this.line = line;
    this.offset = offset;
    this.length = length;
    this.type = type;
    errorCode = code;
    this.message = message;
    subSystem = system;
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return virtualFile;
  }

  public int getLine() {
    return line;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public Type getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public String getSubSystem() {
    return subSystem;
  }

  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return "AnalyzerMessage{" +
           "virtualFile=" + virtualFile.getPath() +
           ", line=" + line +
           ", offset=" + offset +
           ", length=" + length +
           ", type=" + type +
           ", message='" + message + '\'' +
           ", subSystem=" + subSystem +
           ", errorCode='" + errorCode + '\'' +
           '}';
  }

  public static List<AnalyzerMessage> parseMessages(List<String> lines, @NotNull String libraryRootPath) {
    LOG.debug("library root: " + libraryRootPath);
    final List<AnalyzerMessage> result = new ArrayList<AnalyzerMessage>();
    for (String line : lines) {
      final AnalyzerMessage message = parseMessage(line, libraryRootPath);
      if (message != null) {
        result.add(message);
      }
    }
    return result;
  }

  private static boolean isMessage(String line) {
    // Message starts with path
    return !Character.isSpaceChar(line.charAt(0));
  }

  @Nullable
  public static AnalyzerMessage parseMessage(String line, @NotNull String libraryRootPath) {
    final String[] parts = line.split("\\|");
    try {
      final String errorSeverity = parts[0];
      final String subSystem = parts[1];
      final String errorCode = parts[2];
      String sourceUrl = FileUtil.toSystemIndependentName(parts[3]);
      if (sourceUrl.startsWith("file:")) {
        sourceUrl = VfsUtilCore.pathToUrl(sourceUrl.substring("file:".length()));
      }
      VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(sourceUrl));
      if (virtualFile == null && !ApplicationManager.getApplication().isUnitTestMode() && sourceUrl.contains(libraryRootPath)) {
        //see http://code.google.com/p/dart/issues/detail?id=3391
        virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(libraryRootPath));
        if (virtualFile == null) {
          LOG.debug("cannot find library root");
          return null;
        }
        virtualFile = virtualFile.getParent();
        final int index = sourceUrl.indexOf(libraryRootPath);
        final String relativePath = sourceUrl.substring(index + libraryRootPath.length() + 1);
        LOG.debug("relative path from lib: " + relativePath);
        virtualFile = VfsUtil.findRelativeFile(virtualFile, relativePath.split("/"));
        LOG.debug("fix source url: " + (virtualFile == null ? null : virtualFile.getPath()));
      }
      if (virtualFile == null) {
        LOG.debug("cannot find file: " + sourceUrl);
        return null;
      }
      final int lineNumber = Integer.parseInt(parts[4]) - 1;
      final int offset = Integer.parseInt(parts[5]) - 1;
      final int length = Integer.parseInt(parts[6]);
      final String message = StringUtil.unescapeStringCharacters(parts[7]).trim();
      if (lineNumber < 0 || offset < 0 || length <= 0) {
        return null;
      }
      return new AnalyzerMessage(virtualFile, lineNumber, offset, length, Type.valueOf(errorSeverity), subSystem, errorCode, message);
    }
    catch (Throwable th) {
      LOG.debug(line, th);
      return null;
    }
  }

  public enum Type {
    ERROR, WARNING, INFO
  }
}
