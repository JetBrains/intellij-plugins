package com.intellij.flex.uiDesigner;

import java.io.IOException;
import java.io.OutputStream;

class TestClient extends Client {
  public TestClient(OutputStream output) {
    super(output);
  }

  public void assertStates(String filename, String parentFilename) throws IOException {
    out.write(1);
    out.writeAmfUTF(filename, false);
    char c = parentFilename.charAt(0);
    out.write(c == 's' ? 1 : c == 'i' ? 2 : (c == 'c' ? 4 : 0));

    flush();
  }
}
