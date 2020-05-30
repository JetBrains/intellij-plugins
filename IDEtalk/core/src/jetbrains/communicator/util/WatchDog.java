// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

/**
 * @author Kir
 */
public class WatchDog {
  private long myStarted;
  private final String myName;
  private final StringBuffer myBuffer = new StringBuffer(10);
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
