package com.google.dart.server.utilities.instrumentation;

/**
 * The instrumentation recording level representing (1) recording {@link #EVERYTHING} recording of
 * all instrumentation data, (2) recording only {@link #METRICS} information, or (3) recording
 * turned {@link #OFF} in which case nothing is recorded.
 * 
 * @coverage dart.server.utilities
 */
public enum InstrumentationLevel {

  /** Recording all instrumented information */
  EVERYTHING,

  /** Recording only metrics */
  METRICS,

  /** Nothing recorded */
  OFF;

  public static InstrumentationLevel fromString(String str) {

    if (str.equals("EVERYTHING")) {
      return InstrumentationLevel.EVERYTHING;
    }

    if (str.equals("METRICS")) {
      return InstrumentationLevel.METRICS;
    }

    if (str.equals("OFF")) {
      return InstrumentationLevel.OFF;
    }

    throw new IllegalArgumentException("Unrecognised InstrumentationLevel");
  }

//  @Override
//  public String toString() {
//    if (this == InstrumentationLevel.EVERYTHING) {
//      return "EVERYTHING";
//    }
//
//    if (this == InstrumentationLevel.METRICS) {
//      return "METRICS";
//    }
//
//    if (this == InstrumentationLevel.OFF) {
//      return "OFF";
//    }
//
//    throw new IllegalStateException("InstrumentationLevel is in an invalid state");
//
//  }

}
