package com.google.jstestdriver.idea.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class JsErrorMessage {

  private static final String UNCAUGHT_PREFIX = "Uncaught ";

  private final File myFileWithError;
  private final int myLineNumber;
  private final Integer myColumnNumber;
  private final boolean myErrorNameWithUncaughtPrefix;
  private final String myParsedErrorName;
  private final int myHyperlinkStartInclusiveInd;
  private final int myHyperlinkEndExclusiveInd;

  public JsErrorMessage(@NotNull File fileWithError,
                        int lineNumber,
                        @Nullable Integer columnNumber,
                        boolean errorNameWithUncaughtPrefix,
                        @Nullable String parsedErrorName,
                        int hyperlinkStartInclusiveInd,
                        int hyperlinkEndExclusiveInd) {
    myFileWithError = fileWithError;
    myLineNumber = lineNumber;
    myColumnNumber = columnNumber;
    myErrorNameWithUncaughtPrefix = errorNameWithUncaughtPrefix;
    myParsedErrorName = parsedErrorName;
    myHyperlinkStartInclusiveInd = hyperlinkStartInclusiveInd;
    myHyperlinkEndExclusiveInd = hyperlinkEndExclusiveInd;
  }

  @NotNull
  public String getErrorName() {
    String errorName = myParsedErrorName;
    if (errorName != null && errorName.contains(" ")) {
      errorName = null;
    }
    if (errorName != null && !errorName.endsWith("Error")) {
      errorName = null;
    }
    if (errorName == null) {
      errorName = "Error";
    }
    if (myErrorNameWithUncaughtPrefix) {
      errorName = UNCAUGHT_PREFIX + errorName;
    }
    return errorName;
  }

  @NotNull
  public File getFileWithError() {
    return myFileWithError;
  }

  public int getLineNumber() {
    return myLineNumber;
  }

  @Nullable
  public Integer getColumnNumber() {
    return myColumnNumber;
  }

  public int getHyperlinkStartInclusiveInd() {
    return myHyperlinkStartInclusiveInd;
  }

  public int getHyperlinkEndExclusiveInd() {
    return myHyperlinkEndExclusiveInd;
  }

  @Nullable
  public static JsErrorMessage parseFromText(@NotNull String text, @NotNull File basePath) {
    String prefix = "error loading file: ";
    if (!text.startsWith(prefix)) {
      return null;
    }
    String pathAndOther = text.substring(prefix.length());
    int filePathEndInd = findFilePathEndColonIndex(pathAndOther, basePath);
    if (filePathEndInd < 0) {
      return null;
    }
    String filePath = pathAndOther.substring(0, filePathEndInd);
    File file = resolveFile(filePath, basePath);
    if (file == null) {
      return null;
    }
    int lineNumberEndInd = pathAndOther.indexOf(':', filePathEndInd + 1);
    if (lineNumberEndInd < 0) {
      return null;
    }
    String lineNumberStr = pathAndOther.substring(filePathEndInd + 1, lineNumberEndInd);
    Integer lineNumber = toInteger(lineNumberStr);
    if (lineNumber == null) {
      return null;
    }
    int columnNumberEndInd = pathAndOther.indexOf(':', lineNumberEndInd + 1);
    int textMessageStartInd = lineNumberEndInd + 1;
    Integer columnNumber = null;
    if (columnNumberEndInd > 0) {
      String columnNumberStr = pathAndOther.substring(lineNumberEndInd + 1, columnNumberEndInd);
      columnNumber = toInteger(columnNumberStr);
      if (columnNumber != null) {
        textMessageStartInd = columnNumberEndInd + 1;
      }
    }
    String errorNameAndOther = pathAndOther.substring(textMessageStartInd).trim();
    boolean uncaughtTextPresented = false;
    if (errorNameAndOther.startsWith(UNCAUGHT_PREFIX)) {
      errorNameAndOther = errorNameAndOther.substring(UNCAUGHT_PREFIX.length()).trim();
      uncaughtTextPresented = true;
    }
    String exceptionStr = "exception:";
    if (errorNameAndOther.startsWith(exceptionStr)) {
      errorNameAndOther = errorNameAndOther.substring(exceptionStr.length()).trim();
    }
    int detailsStartInd = errorNameAndOther.indexOf(':');
    if (detailsStartInd > 0) {
      errorNameAndOther = errorNameAndOther.substring(0, detailsStartInd);
    }
    return new JsErrorMessage(file, lineNumber, columnNumber, uncaughtTextPresented, errorNameAndOther,
                              prefix.length(), prefix.length() + textMessageStartInd - 1);
  }

  private static int findFilePathEndColonIndex(@NotNull String str, @NotNull File basePath) {
    int index = 0;
    int lastValidIndex = -1;
    while (true) {
      index = str.indexOf(':', index + 1);
      if (index < 0) {
        break;
      }
      File resolvedFile = resolveFile(str.substring(0, index), basePath);
      if (resolvedFile != null) {
        lastValidIndex = index;
      }
    }
    return lastValidIndex;
  }

  @Nullable
  private static Integer toInteger(String s) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  private static File resolveFile(@NotNull String testAndFilePath, @NotNull File basePath) {
    String prefix = "/test/";
    if (testAndFilePath.startsWith(prefix)) {
      String filePath = testAndFilePath.substring(prefix.length());
      if (filePath.isEmpty()) {
        return null;
      }
      File absoluteFile = new File(filePath);
      if (absoluteFile.isAbsolute() && absoluteFile.isFile()) {
        return absoluteFile;
      }
      File file = new File(basePath, filePath);
      if (file.isFile()) {
        return file;
      }
    }
    return null;
  }

}
