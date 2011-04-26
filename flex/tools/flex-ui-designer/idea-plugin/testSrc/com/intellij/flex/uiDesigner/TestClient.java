package com.intellij.flex.uiDesigner;

import java.io.IOException;
import java.io.OutputStream;

class TestClient extends Client {
  public TestClient(OutputStream out) {
    setOut(out);
  }

  public void test(String filename, String parentFilename) throws IOException {
    char c = parentFilename.charAt(0);
    test(filename, c == 's' ? 1 : c == 'i' ? 2 : (c == 'c' ? 4 : 0));
  }
  
  public void test(String filename, int c) throws IOException {
    // method only and only after openDocument and shouldn't be any calls between 
    // in non-tests the same agreement, except must be flush after openDocument always
    blockOut.end();
    
    out.write(1);
    out.writeAmfUtf(filename, false);
    out.write(c);
    
    flush();
  }
}
