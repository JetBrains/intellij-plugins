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

/**
 * @author Kir
 */
public abstract class TimeoutCachedValue <T> implements CachedValue<T> {
  private final long myTimeout;
  private long myLastCalcTime;
  private T myCache;

  public TimeoutCachedValue(long timeout) {
    myTimeout = timeout;
  }

  public T getValue() {
    if (myTimeout <= System.currentTimeMillis() - myLastCalcTime) {
      myLastCalcTime = System.currentTimeMillis();
      myCache = calculate();
    }
    return myCache;
  }

  protected abstract T calculate();
}
