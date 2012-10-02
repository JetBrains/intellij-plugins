package flex2.compiler.util;

import com.intellij.flex.compiler.SdkSpecificHandler;
import flex2.compiler.ILocalizableMessage;
import flex2.compiler.Logger;

public class ConsoleLogger extends AbstractLogger implements Logger {

  private Logger myLogger = SdkSpecificHandler.getLogger();

  public ConsoleLogger() {
    this(true, true, true, true);
  }

  public ConsoleLogger(boolean isInfoEnabled, boolean isDebugEnabled, boolean isWarningEnabled, boolean isErrorEnabled) {
  }

  public int errorCount() {
    return myLogger.errorCount();
  }

  public int warningCount() {
    return myLogger.warningCount();
  }

  public void logInfo(String info) {
    myLogger.logInfo(info);
  }

  public void logDebug(String debug) {
    myLogger.logDebug(debug);
  }

  public void logWarning(String warning) {
    myLogger.logWarning(warning);
  }

  public void logError(String error) {
    myLogger.logError(error);
  }

  public void logInfo(String path, String info) {
    myLogger.logInfo(path, info);
  }

  public void logDebug(String path, String debug) {
    myLogger.logDebug(path, debug);
  }

  public void logWarning(String path, String warning) {
    myLogger.logWarning(path, warning);
  }

  public void logWarning(String path, String warning, int errorCode) {
    myLogger.logWarning(path, warning, errorCode);
  }

  public void logError(String path, String error) {
    myLogger.logError(path, error);
  }

  public void logError(String path, String error, int errorCode) {
    myLogger.logError(path, error, errorCode);
  }

  public void logInfo(String path, int line, String info) {
    myLogger.logInfo(path, line, info);
  }

  public void logDebug(String path, int line, String debug) {
    myLogger.logDebug(path, line, debug);
  }

  public void logWarning(String path, int line, String warning) {
    myLogger.logWarning(path, line, warning);
  }

  public void logWarning(String path, int line, String warning, int errorCode) {
    myLogger.logWarning(path, line, warning, errorCode);
  }

  public void logError(String path, int line, String error) {
    myLogger.logError(path, line, error);
  }

  public void logError(String path, int line, String error, int errorCode) {
    myLogger.logError(path, line, error, errorCode);
  }

  public void logInfo(String path, int line, int col, String info) {
    myLogger.logInfo(path, line, col, info);
  }

  public void logDebug(String path, int line, int col, String debug) {
    myLogger.logDebug(path, line, col, debug);
  }

  public void logWarning(String path, int line, int col, String warning) {
    myLogger.logWarning(path, line, col, warning);
  }

  public void logError(String path, int line, int col, String error) {
    myLogger.logError(path, line, col, error);
  }

  public void logWarning(String path, int line, int col, String warning, String source) {
    myLogger.logWarning(path, line, col, warning, source);
  }

  public void logWarning(String path, int line, int col, String warning, String source, int errorCode) {
    myLogger.logWarning(path, line, col, warning, source, errorCode);
  }

  public void logError(String path, int line, int col, String error, String source) {
    myLogger.logError(path, line, col, error, source);
  }

  public void logError(String path, int line, int col, String error, String source, int errorCode) {
    myLogger.logError(path, line, col, error, source, errorCode);
  }

  public void log(ILocalizableMessage m) {
    myLogger.log(m);
  }

  public void log(ILocalizableMessage m, String source) {
    myLogger.log(m, source);
  }

  public void needsCompilation(String path, String reason) {
    myLogger.needsCompilation(path, reason);
  }

  public void includedFileUpdated(String path) {
    myLogger.includedFileUpdated(path);
  }

  public void includedFileAffected(String path) {
    myLogger.includedFileAffected(path);
  }

}
