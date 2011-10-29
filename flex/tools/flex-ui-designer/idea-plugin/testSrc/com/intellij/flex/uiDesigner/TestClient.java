package com.intellij.flex.uiDesigner;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

class TestClient extends Client {
  private static final int COMMON_TEST_CLASS_ID = 0;
  private static final int MX_TEST_CLASS_ID = 6;
  private static final int MOBILE_TEST_CLASS_ID = 7;
  private static final int STATES_TEST_CLASS_ID = 1;
  private static final int INJECTED_AS_TEST_CLASS_ID = 2;
  private static final int STYLE_TEST_CLASS_ID = 4;

  // MxmlTest on idea side splitted as MxmlTest, StatesTest and InjectedAsTest on client side.
  public void test(Module module, String filename, String parentFilename) throws IOException {
    test(module, filename, charToTestId(parentFilename.charAt(0), parentFilename.length()));
  }
  
  private static int charToTestId(char c, int l) {
    switch (c) {
      case 's':
        return STATES_TEST_CLASS_ID;
      case 'i':
        return INJECTED_AS_TEST_CLASS_ID;
      case 'm':
        return l == 2 ? MX_TEST_CLASS_ID : MOBILE_TEST_CLASS_ID;
      default:
        return c == 'A' || l == 3 ? STYLE_TEST_CLASS_ID : COMMON_TEST_CLASS_ID;
    }
  }
  
  public void test(Module module, String filename, int classId) throws IOException {
    test(this, module, filename, classId);
  }

  public static void test(Client client, @Nullable Module module, String filename, int c) throws IOException {
    // method called only and only after openDocument and shouldn't be any calls between
    // in non-tests the same agreement, except must be flush after openDocument always
    client.blockOut.end();

    client.out.write(1);
    client.out.writeAmfUtf(filename, false);
    if (module != null) {
      client.writeId(module, client.out);
    }
    else {
      client.out.writeShort(-1);
    }
    client.out.write(c);

    client.flush();
  }
}
