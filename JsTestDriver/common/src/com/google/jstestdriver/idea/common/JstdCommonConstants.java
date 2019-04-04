package com.google.jstestdriver.idea.common;

public class JstdCommonConstants {

  public static final String EVENT_PREFIX = "##intellij-event[";
  public static final String EVENT_SUFFIX = "]\n";
  public static final String SERVER_STARTED = "server_started";
  public static final String SERVER_STOPPED = "server_stopped";
  public static final String BROWSER_CAPTURED = "browser_captured";
  public static final String BROWSER_PANICKED = "browser_panicked";

  public static final String EVENT_TYPE = "type";
  public static final String BROWSER_INFO = "browser_info";
  public static final String BROWSER_INFO_ID = "id";
  public static final String BROWSER_INFO_NAME = "name";
  public static final String BROWSER_INFO_OS = "os";

  private JstdCommonConstants() {}
}
