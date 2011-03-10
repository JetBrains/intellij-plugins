package com.intellij.flex.compiler;

import flash.localization.LocalizationManager;
import flex2.compiler.ILocalizableMessage;
import flex2.compiler.Logger;

public abstract class LoggerAdapter implements Logger {

  public static final String ERROR_LEVEL = "Error";
  public static final String WARNING_LEVEL = "Warning";
  public static final String INFO_LEVEL = "Info";
  public static final String DEBUG_LEVEL = "Debug";

  public abstract void log(final String level, final String path, final int line, final int column, final String message);

  public int errorCount() {
    return 0;
  }

  public int warningCount() {
    return 0;
  }

  public void logInfo(final String info) {
    log(INFO_LEVEL, null, -1, -1, info);
  }

  public void logDebug(final String debug) {
    log(DEBUG_LEVEL, null, -1, -1, debug);
  }

  public void logWarning(final String warning) {
    log(WARNING_LEVEL, null, -1, -1, warning);
  }

  public void logError(final String error) {
    log(ERROR_LEVEL, null, -1, -1, error);
  }

  public void logInfo(final String path, final String info) {
    log(INFO_LEVEL, path, -1, -1, info);
  }

  public void logDebug(final String path, final String debug) {
    log(DEBUG_LEVEL, path, -1, -1, debug);
  }

  public void logWarning(final String path, final String warning) {
    log(WARNING_LEVEL, path, -1, -1, warning);
  }

  public void logWarning(final String path, final String warning, final int errorCode) {
    log(WARNING_LEVEL, path, -1, -1, prependErrorCode(errorCode, warning));
  }

  public void logError(final String path, final String error) {
    log(ERROR_LEVEL, path, -1, -1, error);
  }

  public void logError(final String path, final String error, final int errorCode) {
    log(ERROR_LEVEL, path, -1, -1, prependErrorCode(errorCode, error));
  }

  public void logInfo(final String path, final int line, final String info) {
    log(INFO_LEVEL, path, line, -1, info + ", line: " + line);
  }

  public void logDebug(final String path, final int line, final String debug) {
    log(DEBUG_LEVEL, path, line, -1, debug);
  }

  public void logWarning(final String path, final int line, final String warning) {
    log(WARNING_LEVEL, path, line, -1, warning);
  }

  public void logWarning(final String path, final int line, final String warning, final int errorCode) {
    log(WARNING_LEVEL, path, line, -1, prependErrorCode(errorCode, warning));
  }

  public void logError(final String path, final int line, final String error) {
    log(ERROR_LEVEL, path, line, -1, error);
  }

  public void logError(final String path, final int line, final String error, final int errorCode) {
    log(ERROR_LEVEL, path, line, -1, prependErrorCode(errorCode, error));
  }

  public void logInfo(final String path, final int line, final int col, final String info) {
    log(INFO_LEVEL, path, line, col, info);
  }

  public void logDebug(final String path, final int line, final int col, final String debug) {
    log(DEBUG_LEVEL, path, line, col, debug);
  }

  public void logWarning(final String path, final int line, final int col, final String warning) {
    log(WARNING_LEVEL, path, line, col, warning);
  }

  public void logError(final String path, final int line, final int col, final String error) {
    log(ERROR_LEVEL, path, line, col, error);
  }

  public void logWarning(final String path, final int line, final int col, final String warning, final String source) {
    log(WARNING_LEVEL, path, line, col, appendSource(warning, source));
  }

  public void logWarning(final String path, final int line, final int col, final String warning, final String source, final int errorCode) {
    log(WARNING_LEVEL, path, line, col, appendSource(prependErrorCode(errorCode, warning), source));
  }

  public void logError(final String path, final int line, final int col, final String error, final String source) {
    log(ERROR_LEVEL, path, line, col, appendSource(error, source));
  }

  public void logError(final String path, final int line, final int col, final String error, final String source, final int errorCode) {
    log(ERROR_LEVEL, path, line, col, appendSource(prependErrorCode(errorCode, error), source));
  }

  public void log(final ILocalizableMessage m) {
    log(m.getLevel(), m.getPath(), m.getLine(), m.getColumn(), m.toString());
  }

  public void log(final ILocalizableMessage m, final String source) {
    log(m.getLevel(), m.getPath(), m.getLine(), m.getColumn(), appendSource(m.toString(), source));
  }

  public void needsCompilation(final String path, final String reason) {
    log(INFO_LEVEL, path, -1, -1, "Needs compilation, reason: " + reason);
  }

  public void includedFileUpdated(final String path) {
    log(INFO_LEVEL, path, -1, -1, "Included file updated");
  }

  public void includedFileAffected(final String path) {
    log(INFO_LEVEL, path, -1, -1, "Included file affected");
  }

  public void setLocalizationManager(final LocalizationManager mgr) {
  }

  private static String prependErrorCode(final int errorCode, final String message) {
    return "Error code: " + errorCode + ": " + message;
  }

  private static String appendSource(final String message, final String source) {
    return message + ", source" + source;
  }
}
