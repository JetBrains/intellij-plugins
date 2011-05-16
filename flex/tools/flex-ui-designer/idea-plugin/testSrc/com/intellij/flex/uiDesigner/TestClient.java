package com.intellij.flex.uiDesigner;

import java.io.IOException;

class TestClient extends Client {
  public void test(String filename, String parentFilename) throws IOException {
    char c = parentFilename.charAt(0);
    test(filename, c == 's' ? 1 : c == 'i' ? 2 : (c == 'c' ? 4 : 0));
  }
  
  public void test(String filename, int c) throws IOException {
    test(this, filename, c);
  }

  public static void test(Client client, String filename, int c) throws IOException {
    // method only and only after openDocument and shouldn't be any calls between
    // in non-tests the same agreement, except must be flush after openDocument always
    client.blockOut.end();

    client.out.write(1);
    client.out.writeAmfUtf(filename, false);
    client.out.write(c);

    client.flush();
  }
}
