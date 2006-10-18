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

import java.util.Date;
import java.util.Calendar;

/**
 * @author Kir
 */
public class TimeUtil {
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
