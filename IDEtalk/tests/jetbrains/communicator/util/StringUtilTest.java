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
public class StringUtilTest extends TestCase {
  public static final int BUFLENGTH = 100000;

  public void testTwoDirection() throws Exception {
    assertEquals("Expect valid >< conversion",
        "ЖaЖcЖ", StringUtil.fromXMLSafeString(StringUtil.toXMLSafeString("ЖaЖcЖ")));
  }

  public void testBigConversion() throws Exception {

    WatchDog watchDog = new WatchDog("Xml String Convert");
    StringBuffer sb = new StringBuffer(BUFLENGTH);
    for (int i = 0; i < BUFLENGTH >> 1; i++) {
      sb.append("Л");
      sb.append('i');
    }

    String s = sb.toString();

    watchDog.watchAndReset("fill buffer");
    String converted = StringUtil.toXMLSafeString(s);
    watchDog.watch("before backconversion");
    String back = StringUtil.fromXMLSafeString(converted);
    watchDog.watchAndReset("backconverted");

    assertEquals("Sanity check", s, back);
  }

  public void testGetMsg() throws Exception {
    assertEquals("Should get resource from ResourceBundle",
        "some result", StringUtil.getMsg("test.line"));
  }

  public void testGetMsg2() throws Throwable {
    String msg = StringUtil.getMsg("ViewFilesCommand.fail", "userName", "");
    assertEquals(msg, -1, msg.indexOf('{'));
    System.out.println(msg);
  }
}
