package org.jetbrains.plugins.ruby.motion.run;

import com.jetbrains.cidr.execution.debugger.breakpoints.CidrExceptionBreakpointType;

/**
 * @author Dennis.Ushakov
 */
public class MotionExceptionBreakpointType extends CidrExceptionBreakpointType {
  public MotionExceptionBreakpointType() {
    super("RubyMotion Exception Breakpoints");
  }
}
