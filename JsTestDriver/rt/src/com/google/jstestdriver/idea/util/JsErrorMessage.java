package com.google.jstestdriver.idea.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class JsErrorMessage {

  private final File myFileWithError;
  private int myLineNumber;
  private final Integer myColumnNumber;
  private final String myParsedErrorName;
  private final int myHyperlinkStartInclusiveInd;
  private final int myHyperlinkEndExclusiveInd;

  public JsErrorMessage(@NotNull File fileWithError,
                        int lineNumber,
                        @Nullable Integer columnNumber,
                        @Nullable String parsedErrorName,
                        int hyperlinkStartInclusiveInd,
                        int hyperlinkEndExclusiveInd) {
    myFileWithError = fileWithError;
    myLineNumber = lineNumber;
    myColumnNumber = columnNumber;
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
    return errorName != null ? errorName : "Error";
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
    int filePathEndInd = pathAndOther.indexOf(':');
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
    String uncaughtPrefix = "Uncaught ";
    if (errorNameAndOther.startsWith(uncaughtPrefix)) {
      errorNameAndOther = errorNameAndOther.substring(uncaughtPrefix.length()).trim();
    }
    String exceptionStr = "exception:";
    if (errorNameAndOther.startsWith(exceptionStr)) {
      errorNameAndOther = errorNameAndOther.substring(exceptionStr.length()).trim();
    }
    int detailsStartInd = errorNameAndOther.indexOf(':');
    if (detailsStartInd > 0) {
      errorNameAndOther = errorNameAndOther.substring(0, detailsStartInd);
    }
    return new JsErrorMessage(file, lineNumber, columnNumber, errorNameAndOther,
                              prefix.length(), prefix.length() + textMessageStartInd - 1);
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
  private static File resolveFile(@NotNull String filePath, @NotNull File basePath) {
    String prefix = "/test/";
    if (filePath.startsWith(prefix)) {
      String refinedPath = filePath.substring(prefix.length());
      File file = new File(basePath, refinedPath);
      if (file.isFile()) {
        return file;
      }
    }
    return null;
  }


}
