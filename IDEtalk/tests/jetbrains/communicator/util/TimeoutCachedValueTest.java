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
 * @author Kir
 */
public class TimeoutCachedValueTest extends TestCase {
  public void testNoCache() throws Exception {
    final int[] counter = new int[1];

    final TimeoutCachedValue<Integer> cachedValue = new TimeoutCachedValue<Integer>(0) {
      @Override
      protected Integer calculate() {
        return counter[0] ++;
      }
    };

    assertEquals(0, cachedValue.getValue().intValue());
    Thread.sleep(10);
    assertEquals(1, cachedValue.getValue().intValue());
  }

  public void testTimeout() throws Exception {
    final int[] counter = new int[1];

    final TimeoutCachedValue<Integer> cachedValue = new TimeoutCachedValue<Integer>(50) {
      @Override
      protected Integer calculate() {
        return counter[0] ++;
      }
    };

    assertEquals(0, cachedValue.getValue().intValue());
    assertEquals(0, cachedValue.getValue().intValue());

    Thread.sleep(50);

    assertEquals(1, cachedValue.getValue().intValue());
  }
}
