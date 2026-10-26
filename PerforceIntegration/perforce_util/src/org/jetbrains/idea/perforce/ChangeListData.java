package org.jetbrains.idea.perforce;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class ChangeListData {
  private static final Logger LOG = Logger.getInstance(ChangeListData.class);

  public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.US).withZone(ZoneId.systemDefault());
  public static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.US).withZone(ZoneId.systemDefault());

  public static @NotNull Date parseChangelistDate(@NotNull String date) {
    try {
      // Perforce before 2003.1 did not include time in 'p4 changes' output
      DateTimeFormatter format = date.indexOf(':') >= 0 ? DATE_FORMAT : DATE_ONLY_FORMAT;
      return Date.from(Instant.from(format.parse(date)));
    }
    catch (DateTimeException e) {
      LOG.error(e);
      return new Date();
    }
  }

  public long NUMBER;
  public String DESCRIPTION;
  public String USER;
  public String CLIENT;
  public String DATE;

  @Override
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

  @Override
  public int hashCode() {
    int result;
    result = Long.hashCode(NUMBER);
    result = 31 * result + (DESCRIPTION != null ? DESCRIPTION.hashCode() : 0);
    result = 31 * result + (USER != null ? USER.hashCode() : 0);
    result = 31 * result + (CLIENT != null ? CLIENT.hashCode() : 0);
    result = 31 * result + (DATE != null ? DATE.hashCode() : 0);
    return result;
  }
}
