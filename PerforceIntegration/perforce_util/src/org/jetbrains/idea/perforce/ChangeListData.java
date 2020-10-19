package org.jetbrains.idea.perforce;

import com.intellij.util.text.SyncDateFormat;
import org.jetbrains.annotations.NonNls;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChangeListData {
  public long NUMBER;
  public String DESCRIPTION;
  public String USER;
  public String CLIENT;
  public String DATE;
  @NonNls private static final String PATTERN = "yyyy/MM/dd HH:mm:ss";
  public static final SyncDateFormat DATE_FORMAT = new SyncDateFormat(new SimpleDateFormat(PATTERN, Locale.US));
  @NonNls public static final SyncDateFormat DATE_ONLY_FORMAT = new SyncDateFormat(new SimpleDateFormat("yyyy/MM/dd", Locale.US));

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final ChangeListData that = (ChangeListData)o;

    if (NUMBER != that.NUMBER) return false;
    if (CLIENT != null ? !CLIENT.equals(that.CLIENT) : that.CLIENT != null) return false;
    if (DATE != null ? !DATE.equals(that.DATE) : that.DATE != null) return false;
    if (DESCRIPTION != null ? !DESCRIPTION.equals(that.DESCRIPTION) : that.DESCRIPTION != null) return false;
    if (USER != null ? !USER.equals(that.USER) : that.USER != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (int)(NUMBER ^ (NUMBER >>> 32));
    result = 31 * result + (DESCRIPTION != null ? DESCRIPTION.hashCode() : 0);
    result = 31 * result + (USER != null ? USER.hashCode() : 0);
    result = 31 * result + (CLIENT != null ? CLIENT.hashCode() : 0);
    result = 31 * result + (DATE != null ? DATE.hashCode() : 0);
    return result;
  }
}
