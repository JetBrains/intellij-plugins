package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.EventDispatcher;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EventListener;

public class FlexUnitConnection extends ServerConnectionBase {

  public interface Listener extends EventListener {
    void statusChanged(ConnectionStatus status);

    void onData(String line);

    void onFinish();
  }

  private static final String TERMINATE_MARKER = "Finish";

  private static final Logger LOG = Logger.getInstance(FlexUnitConnection.class.getName());
  protected final EventDispatcher<Listener> myDispatcher = EventDispatcher.create(Listener.class);
  //private OutputStreamWriter myWriter;

  protected void run(InputStream inputStream) throws IOException {
    DataInputStream dataInputStream = new DataInputStream(inputStream);

    String line;
    while (!isStopped() && !StringUtil.isEmpty(line = dataInputStream.readUTF())) {
      LOG.debug(line);
      if (TERMINATE_MARKER.equals(line)) {
        //log("sending terminate command");
        //write(TERMINATE_MARKER);
        close();
        myDispatcher.getMulticaster().onFinish();
        return;
      }
      else {
        myDispatcher.getMulticaster().onData(line);
      }
    }
  }

  public void addListener(Listener listener) {
    myDispatcher.addListener(listener);
  }

  @Override
  protected void setStatus(ConnectionStatus status) {
    super.setStatus(status);
    myDispatcher.getMulticaster().statusChanged(status);
  }
}
