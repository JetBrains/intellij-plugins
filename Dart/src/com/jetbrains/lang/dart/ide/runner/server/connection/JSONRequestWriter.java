package com.jetbrains.lang.dart.ide.runner.server.connection;

import com.intellij.util.io.socketConnection.RequestWriter;

import java.io.IOException;
import java.io.Writer;

public class JSONRequestWriter implements RequestWriter<JSONCommand> {
  private final Writer myWriter;

  public JSONRequestWriter(Writer writer) {
    myWriter = writer;
  }

  @Override
  public void writeRequest(JSONCommand request) throws IOException {
    request.sendCommand(myWriter);
  }
}
