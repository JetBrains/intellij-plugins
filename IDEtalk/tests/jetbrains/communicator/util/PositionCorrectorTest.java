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

import jetbrains.communicator.mock.MockIDEFacade;
import junit.framework.TestCase;

/**
 * @author kir
 */
public class PositionCorrectorTest extends TestCase {

  public void testNoContents() {
    assertPositionCorrection(null, null, 5, 5);
    assertPositionCorrection("adfadsf", null, 5, 5);
    assertPositionCorrection(null, "ssss", 5, 5);
  }

  public void testOutsideBoundary() {
    assertPositionCorrection("aaa", "ssss", -1, 0);
    assertPositionCorrection("aaa", "ssss", 5, 0);
  }

  public void testLinesChanges() {
    assertPositionCorrection("aaa", "ssss", 0, 0);
    assertPositionCorrection("aaa\nddd", "bbb\nddd", 0, 0);
    assertPositionCorrection("aaa\nddd", "bbb\nddd", 1, 1);
  }

  public void testLineAdded() {
    assertPositionCorrection("aaa", "aaa\nbbb", 0, 0);
    assertPositionCorrection("aaa", "bbb\naaa", 0, 1);
    assertPositionCorrection("aaa\rbbb", "ccc\naaa\nbbb\naaa", 1, 2);
    assertPositionCorrection("aaa\rbbb", "ccc\naaa\nqqqq\nbbb", 1, 3);
  }

  public void testLineDeleted() {
    assertPositionCorrection("aaa\nbbb", "aaa", 0, 0);
    assertPositionCorrection("aaa\nbbb", "aaa", 1, 0);

    assertPositionCorrection("aaa\nbbb", "bbb", 1, 0);
    assertPositionCorrection("aaa\nbbb\nccc\nddd", "bbb\nccc", 2, 1);
    assertPositionCorrection("aaa\nccc\nddd", "ccc", 1, 0);
    assertPositionCorrection("aaa\nbbb", "bbb", 0, 0);

    assertPositionCorrection("aaa\nbbb\naaa", "bbb", 1, 0);

    assertPositionCorrection("aaa\nbbb\naaa", "", 1, 0);

    assertPositionCorrection("ccc\naaa\nbbb\naaa", "aaa\naaa", 3, 1);
    assertPositionCorrection("ccc\naaa\nbbb\nzzz", "zzz\naaa", 3, 0);
  }

  public void testChange() {
    assertPositionCorrection("aaa\nbbb", "ccc\nbbb", 0, 0);
    assertPositionCorrection("aaa\nbbb", "ccc\nbbb", 1, 1);
  }

  public void testDeleteAndInsert() {
    String src =
      "aaa\n" +
      "bbb\n" +
      "ccc\n" +
      "ddd\n" +
      "eee\n";
    String dst =
        "aaa\n" +
        "1111\n" +
        "bbb\n" +
        "eee\n";

    assertPositionCorrection(src, dst, 0, 0);
    assertPositionCorrection(src, dst, 1, 2);
    assertPositionCorrection(src, dst, 2, 1);
    assertPositionCorrection(src, dst, 3, 2);
    assertPositionCorrection(src, dst, 4, 3);

  }

  public static void assertPositionCorrection(String src, String dest, int srcLine, int expectedLine) {
    PositionCorrector positionCorrector = new PositionCorrector(new MockIDEFacade(), src, dest);
    assertEquals("Invalid correction for line " + srcLine + '\n' + src + "\n-----------\n" + dest + '\n',
        expectedLine, positionCorrector.getCorrectedLine(srcLine));
  }
}
