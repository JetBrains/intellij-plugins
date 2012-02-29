package com.intellij.flex.uiDesigner;

import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

class TestClient extends Client {
  private static final int CLASS = 1;
  
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

  public void test(@NotNull Module module, int specialClassId) throws IOException {
    assert specialClassId >= 120;

    blockOut.end();

    out.write(CLASS);
    out.write(0);
    out.write(specialClassId);
    writeId(module, out);

    flush();
  }
  
  public void test(@Nullable Module module, String filename, int classId) throws IOException {
    // method called only and only after openDocument and shouldn't be any calls between
    // in non-tests the same agreement, except must be flush after openDocument always
    blockOut.end();

    out.write(CLASS);
    out.write(0);
    out.write(classId);
    if (module != null) {
      writeId(module, out);
    }
    else {
      out.writeShort(-1);
    }
    out.writeAmfUtf(filename, false);

    flush();
  }
}
