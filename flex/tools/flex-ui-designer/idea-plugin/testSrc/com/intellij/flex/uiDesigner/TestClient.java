package com.intellij.flex.uiDesigner;

import java.io.IOException;
import java.io.OutputStream;

class TestClient extends Client {
  public TestClient(OutputStream output) {
    super(output);
  }

  public void test(String filename, String parentFilename) throws IOException {
    char c = parentFilename.charAt(0);
    test(filename, c == 's' ? 1 : c == 'i' ? 2 : (c == 'c' ? 4 : 0));
  }
  
  public void test(String filename, int c) throws IOException {
    // метод только и только после openDocument и никаких вызовов между ними быть не должно
    // не в тестах такое же соглашение, только там сразу после openDocument должен быть flush.
//    blockOut.end();
    
    out.write(1);
    out.writeAmfUTF(filename, false);
    out.write(c);
    
    flush();
  }
}
