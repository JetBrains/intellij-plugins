package java.text;

import java.util.Date;

// Workaround for this class not being present in any of our mock JDKs.
public class SimpleDateFormat {
  public SimpleDateFormat(String pattern) {
  }

  public Date parse(String source) {
    return null;
  }
}