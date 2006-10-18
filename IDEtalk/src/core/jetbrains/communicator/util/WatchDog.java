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
public class WatchDog {
  private long myStarted;
  private String myName;
  private StringBuffer myBuffer = new StringBuffer(10);
  public static final int SEC = 1000;

  public WatchDog(String prefix) {
    myName = prefix;
    myStarted = System.currentTimeMillis();
    watchAndReset("start");
  }

  public final void watch(String s) {
    System.out.println(myName + ' ' + s + ": " + _diff());
  }

  public final void stop() {
    watch("stopped");
  }

  public final void watchAndReset(String s) {
    watch(s);
    myStarted = System.currentTimeMillis();
  }

  private String _diff() {
    long diff = diff();
    myBuffer.delete(0, myBuffer.length());
    if (diff >= SEC) {
      long secs = diff / SEC;
      myBuffer.append(secs);
      myBuffer.append(" sec ");
      diff -= secs * SEC;
    }
    myBuffer.append(diff);
    myBuffer.append(" msec");

    return myBuffer.toString();
  }

  public long diff() {
    return System.currentTimeMillis() - myStarted;
  }

}
