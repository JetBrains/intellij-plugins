/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.communicator.util;

import junit.framework.TestCase;

/**
 * @author kir
 */
public class CommunicatorStringsTest extends TestCase {
  public static final int BUFLENGTH = 100000;

  public void testTwoDirection() {
    assertEquals("Expect valid >< conversion",
                 "\u0416a\u0416c\u0416", CommunicatorStrings.fromXMLSafeString(CommunicatorStrings.toXMLSafeString("\u0416a\u0416c\u0416")));
  }

  public void testBigConversion() {

    WatchDog watchDog = new WatchDog("Xml String Convert");
    StringBuilder sb = new StringBuilder(BUFLENGTH);
    for (int i = 0; i < BUFLENGTH >> 1; i++) {
      sb.append('\u041b');
      sb.append('i');
    }

    String s = sb.toString();

    watchDog.watchAndReset("fill buffer");
    String converted = CommunicatorStrings.toXMLSafeString(s);
    watchDog.watch("before backconversion");
    String back = CommunicatorStrings.fromXMLSafeString(converted);
    watchDog.watchAndReset("backconverted");

    assertEquals("Sanity check", s, back);
  }

  public void testGetMsg() {
    assertEquals("Should get resource from ResourceBundle",
                 "some result", CommunicatorStrings.getMsg("test.line"));
  }

  public void testGetMsg2() {
    String msg = CommunicatorStrings.getMsg("ViewFilesCommand.fail", "userName", "");
    assertEquals(msg, -1, msg.indexOf('{'));
  }
}
