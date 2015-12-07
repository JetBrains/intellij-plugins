package com.google.dart.server.utilities.instrumentation;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Method;

/**
 * The class {@code HealthReporter} acts as a general mechanism for reporting system state
 * information to the instrumentation system
 */
public class HealthUtils {

  //Copied from org.eclipse.ui.internal.HeapStatus
  public static long getMaxMem() {
    long max = Long.MAX_VALUE;
    try {
      // Must use reflect to allow compilation against JCL/Foundation
      Method maxMemMethod = Runtime.class.getMethod("maxMemory", new Class[0]); //$NON-NLS-1$
      Object o = maxMemMethod.invoke(Runtime.getRuntime(), new Object[0]);
      if (o instanceof Long) {
        max = ((Long) o).longValue();
      }
    } catch (Exception e) {
      // ignore if method missing or if there are other failures trying to determine the max
    }
    return max;
  }

  public static void logMemory(InstrumentationBuilder instrumentation) {

    instrumentation.metric("MexMemory-FeedbackUtils", getMaxMem());
    instrumentation.metric("TotalMemory", Runtime.getRuntime().totalMemory());
    instrumentation.metric("FreeMemory", Runtime.getRuntime().freeMemory());

  }

  /**
   * Log data about threads to the provided instrumentation builder
   * 
   * @param instrumentation builder to log data to
   */
  public static void logThreads(InstrumentationBuilder instrumentation) {
    java.lang.management.ThreadMXBean th = ManagementFactory.getThreadMXBean();
    ThreadInfo[] thInfos = th.getThreadInfo(th.getAllThreadIds(), Integer.MAX_VALUE);

    instrumentation.metric("threads-count", thInfos.length);

    for (ThreadInfo thInfo : thInfos) {
      if (thInfo == null) {
        instrumentation.metric("Thread-Name", "<unknown>");
        continue;
      }
      instrumentation.metric("Thread-Name", thInfo.getThreadName());
      instrumentation.metric("Thread-ID", thInfo.getThreadId());
      instrumentation.metric("Thread-State", thInfo.getThreadState().toString());

      instrumentation.metric("Blocked-Count", thInfo.getBlockedCount());
      instrumentation.metric("Blocked-Time", thInfo.getBlockedTime());

      instrumentation.metric("Waited-Count", thInfo.getWaitedCount());
      instrumentation.metric("Waited-Time", thInfo.getWaitedTime());

      instrumentation.data(
          "Thread-ST",
          Base64.encodeBytes(stackTraceToString(thInfo.getStackTrace()).getBytes()));

    }
  }

  /**
   * Report the health state of the editor to instrumentation The method can be run on any thread
   * and will return immediately, performing all of its work async
   * 
   * @param reason what was the cause of the health check being reported
   */
  public static void ReportHealth(final String reason) {

    //Async create and dispatch the thread
    new Thread("HealthReporter") {
      @Override
      public void run() {
        ReportHealthImpl(reason);
      }
    }.start();

  }

  /**
   * Health reporter implementation, this must be implemented so that it can run on any thread
   * 
   * @param reason
   */
  private static void ReportHealthImpl(String reason) {
    InstrumentationBuilder instrumentation = Instrumentation.builder("HealthReport");

    try {
      instrumentation.metric("Reason", reason);

      logMemory(instrumentation);
      logThreads(instrumentation);
    } finally {
      instrumentation.log();
    }
  }

  private static String stackTraceToString(StackTraceElement[] elements) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : elements) {
      sb.append(element.toString());
      sb.append("\n");
    }
    return sb.toString();
  }
}
