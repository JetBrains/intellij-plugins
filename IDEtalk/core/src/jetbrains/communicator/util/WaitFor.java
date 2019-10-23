// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

/**
 * @author Kir Maximov
 */
public abstract class WaitFor {

  protected WaitFor() {
    this(60 * 1000);
  }

  protected WaitFor(long timeout) {
    long started = System.currentTimeMillis();
    try {
      //noinspection AbstractMethodCallInConstructor
      while(!condition() && (System.currentTimeMillis() - started < timeout)) {
        //noinspection BusyWait
        Thread.sleep(10);
      }
    }
    catch(InterruptedException e) {
      // ignore
    }
  }

  protected abstract boolean condition();
}
