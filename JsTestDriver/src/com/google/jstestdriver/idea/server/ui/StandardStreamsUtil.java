package com.google.jstestdriver.idea.server.ui;

import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Sergey Simonchik
 */
public class StandardStreamsUtil {

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public synchronized static <T> Pair<T, CapturedStreams> captureStandardStreams(
    @NotNull NullableComputable<T> computable
  ) {
    PrintStream oldOut = System.out;
    PrintStream oldErr = System.err;
    ByteArrayOutputStream baOut = new ByteArrayOutputStream();
    ByteArrayOutputStream baErr = new ByteArrayOutputStream();
    final T result;
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    PrintStream out = new PrintStream(baOut, true);
    PrintStream err = new PrintStream(baErr, true);
    try {
      System.setOut(out);
      System.setErr(err);
      result = computable.compute();
    }
    finally {
      System.setOut(oldOut);
      System.setErr(oldErr);
      out.close();
      err.close();
    }
    CapturedStreams capturedStreams = new CapturedStreams(
      new String(baOut.toByteArray()),
      new String(baErr.toByteArray())
    );
    return Pair.create(result, capturedStreams);
  }

  public static class CapturedStreams {
    private final String myStdOut;
    private final String myStdErr;

    public CapturedStreams(@NotNull String stdOut, @NotNull String stdErr) {
      myStdErr = stdErr;
      myStdOut = stdOut;
    }

    @NotNull
    public String getStdOut() {
      return myStdOut;
    }

    @NotNull
    public String getStdErr() {
      return myStdErr;
    }
  }
}
