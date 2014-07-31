package com.jetbrains.lang.dart.dartium;

import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import gnu.trove.THashMap;
import junit.framework.TestCase;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DartiumTest extends TestCase {

  private static void doTest(@Nullable final String dartFlagsBefore,
                             final boolean checkedMode,
                             @Nullable final String expectedDartFlagsAfter) {
    final Map<String, String> envVars = new THashMap<String, String>();
    if (dartFlagsBefore != null) {
      envVars.put("DART_FLAGS", dartFlagsBefore);
    }

    DartiumUtil.setCheckedMode(envVars, checkedMode);

    assertEquals(expectedDartFlagsAfter, envVars.get("DART_FLAGS"));
  }

  public void testCheckedMode() throws Exception {
    doTest(null, false, null);
    doTest("foo bar", false, "foo bar");
    doTest("--checked", false, null);
    doTest("  --checked  ", false, null);
    doTest("--checked foo", false, "foo");
    doTest("foo --checked", false, "foo");
    doTest("foo --checked bar", false, "foo bar");
    doTest(null, true, "--checked");
    doTest("--checked", true, "--checked");
    doTest("foo", true, "foo --checked");
  }
}
