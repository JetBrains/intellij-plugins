package com.google.jstestdriver.idea.rt.server;

import com.google.gson.stream.JsonWriter;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.idea.common.JstdCommonConstants;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;

public class JstdIntellijServerListener implements ServerListener {

  private static final Handler NOTHING = new Handler() {
    @Override
    public void handle(@NotNull JsonWriter writer) {}
  };

  @Override
  public void serverStarted() {
    String json = formatJson(JstdCommonConstants.SERVER_STARTED, NOTHING);
    sendEvent(json);
  }

  @Override
  public void serverStopped() {
    String json = formatJson(JstdCommonConstants.SERVER_STOPPED, NOTHING);
    sendEvent(json);
  }

  @Override
  public void browserCaptured(BrowserInfo info) {
    String json = formatJson(JstdCommonConstants.BROWSER_CAPTURED, toJsonBrowserInfo(info));
    sendEvent(json);
  }

  @Override
  public void browserPanicked(BrowserInfo info) {
    String json = formatJson(JstdCommonConstants.BROWSER_PANICKED, toJsonBrowserInfo(info));
    sendEvent(json);
  }

  private static Handler toJsonBrowserInfo(@NotNull final BrowserInfo info) {
    return new Handler() {
      @Override
      public void handle(@NotNull JsonWriter writer) throws IOException {
        writer.name(JstdCommonConstants.BROWSER_INFO);
        writer.beginObject();
        writer.name(JstdCommonConstants.BROWSER_INFO_ID);
        writer.value(String.valueOf(info.getId()));
        writer.name(JstdCommonConstants.BROWSER_INFO_NAME);
        writer.value(info.getName());
        writer.name(JstdCommonConstants.BROWSER_INFO_OS);
        writer.value(info.getOs());
        writer.endObject();
      }
    };
  }

  @NotNull
  private static String formatJson(@NotNull String type, @NotNull Handler handler) {
    StringWriter buf = new StringWriter();
    JsonWriter writer = new JsonWriter(buf);
    writer.setLenient(false);
    try {
      writer.beginObject();
      writer.name(JstdCommonConstants.EVENT_TYPE);
      writer.value(type);
      handler.handle(writer);
      writer.endObject();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      try {
        writer.close();
      }
      catch (IOException e) {
        //noinspection CallToPrintStackTrace
        e.printStackTrace();
      }
    }
    return buf.toString();
  }

  private static void sendEvent(@NotNull String json) {
    //noinspection UseOfSystemOutOrSystemErr
    System.out.print(JstdCommonConstants.EVENT_PREFIX + json + JstdCommonConstants.EVENT_SUFFIX);
  }

  private interface Handler {
    void handle(@NotNull JsonWriter writer) throws IOException;
  }
}
