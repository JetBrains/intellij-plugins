package com.intellij.flex.uiDesigner;

import java.io.IOException;

class TestClient extends Client {
  private static final int MXML_TEST_CLASS_ID = 0;
  private static final int STATES_TEST_CLASS_ID = 1;
  private static final int INJECTED_AS_TEST_CLASS_ID = 2;
  private static final int STYLE_TEST_CLASS_ID = 4;

  public void test(String filename, String parentFilename) throws IOException {
    char c = parentFilename.charAt(0);
    int testClassId = c == 's' ? STATES_TEST_CLASS_ID : c == 'i' ? INJECTED_AS_TEST_CLASS_ID : (c == 'c' ? STYLE_TEST_CLASS_ID : MXML_TEST_CLASS_ID);
    if (testClassId == MXML_TEST_CLASS_ID && filename.contains("State")) {
      testClassId = STATES_TEST_CLASS_ID;
    }
    test(filename, testClassId);
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
