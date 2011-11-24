package com.jetbrains.actionscript.profiler.util;

public class MathUtil {
  private MathUtil() {
  }
  
  public static int sign(long n){
    if(n == 0L) return 0;
    return n > 0L ? 1 : -1;
  }
}
