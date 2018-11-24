package com.google.jstestdriver.idea.server;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Consumer;

public class JstdServerUtils extends JstdServerUtilsRt {
  private JstdServerUtils() {}

  public static void asyncFetchServerInfo(final String serverUrl, final Consumer<JstdServerFetchResult> consumer) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        JstdServerFetchResult serverFetchResult;
        try {
          serverFetchResult = syncFetchServerInfo(serverUrl);
        } catch (Exception e) {
          serverFetchResult = new JstdServerFetchResult(null, "Internal error occurred");
        }
        consumer.consume(serverFetchResult);
      }
    });
  }
}
