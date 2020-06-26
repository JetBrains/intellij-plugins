// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Kir
 */
public final class TimeUtil {
  private static int ourNow;

  private TimeUtil() {
  }

  public static long now() {
    if (ourNow < 0) {
      return System.currentTimeMillis();
    }
    return ourNow;
  }

  /** Set to negative to reset value */
  public static void setNow(int now) {
    ourNow = now;
  }

  public static Date getDay(Date when) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(when);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
  }
}
