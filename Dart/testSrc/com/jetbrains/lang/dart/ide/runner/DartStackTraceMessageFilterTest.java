package com.jetbrains.lang.dart.ide.runner;

import com.intellij.openapi.util.Trinity;
import junit.framework.TestCase;

/**
 * Created by fedorkorotkov.
 */
public class DartStackTraceMessageFilterTest extends TestCase {
  public void testWinOldFormat() {
    final Trinity<String, Integer, Integer> position = DartStackTraceMessageFilter.findPosition(
      "#0      Configuration.onDone (package:unittest/src/config.dart:213:9)"
    );
    assertNotNull(position);
    assertEquals("package:unittest/src/config.dart", position.getFirst());
    assertTrue(212 == position.getSecond());
    assertTrue(8 == position.getThird());
  }

  public void testWinFormat() {
    final Trinity<String, Integer, Integer> position = DartStackTraceMessageFilter.findPosition(
      "main.<anonymous closure>                file:///C:/WebstormProjects/dartUnitWin/MyTest.dart 8:39"
    );
    assertNotNull(position);
    assertEquals("file:///C:/WebstormProjects/dartUnitWin/MyTest.dart", position.getFirst());
    assertTrue(7 == position.getSecond());
    assertTrue(38 == position.getThird());
  }

  public void testUnixFormat() {
    final Trinity<String, Integer, Integer> position = DartStackTraceMessageFilter.findPosition(
      "Configuration.onExpectFailure                  package:unittest/src/config.dart 151:7"
    );
    assertNotNull(position);
    assertEquals("package:unittest/src/config.dart", position.getFirst());
    assertTrue(150 == position.getSecond());
    assertTrue(6 == position.getThird());
  }
}
