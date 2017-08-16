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
 * @author Kir Maximov
 */
public class WaitForTest extends TestCase {

  public void testWaitFor() {
    final long l = System.currentTimeMillis();

    new WaitFor() {
      @Override
      protected boolean condition() {
        return System.currentTimeMillis() > l + 200;
      }
    };

    long now = System.currentTimeMillis();
    assertEquals("Should wait 200 msecs: " + (now - l - 200),
        l + 200d, now, 50d);
  }

  public void testWaitForTimeout() {
    final long l = System.currentTimeMillis();

    new WaitFor(500) {
      @Override
      protected boolean condition() {
        return System.currentTimeMillis() > l + 2000;
      }
    };

    long now = System.currentTimeMillis();
    assertEquals("Should wait 100 msecs: " + (now - l - 500),
        l + 500d, now, 150d);
  }

}
