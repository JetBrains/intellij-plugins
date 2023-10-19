package com.intellij.plugins.serialmonitor;

import org.jetbrains.annotations.Nls;

/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class SerialMonitorException extends Exception {

  public SerialMonitorException(@Nls String message) {
    super(message);
  }

  public SerialMonitorException(@Nls String message, Throwable cause) {
    super(message + ": " + cause.getLocalizedMessage(), cause);
  }
}
