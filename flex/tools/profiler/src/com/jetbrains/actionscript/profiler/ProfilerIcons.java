package com.jetbrains.actionscript.profiler;

import icons.FlexProfilerIcons;

import javax.swing.*;

public interface ProfilerIcons {
  Icon CALLER_SOLID_ARROW = FlexProfilerIcons.CallerArrow;
  Icon CALLER_DOTTED_ARROW = FlexProfilerIcons.CallerLeafArrow;
  Icon CALLEE_SOLID_ARROW = FlexProfilerIcons.CalleeArrow;
  Icon CALLEE_DOTTED_ARROW = FlexProfilerIcons.CalleeLeafArrow;

  Icon START_CPU = FlexProfilerIcons.StartCPU;
  Icon STOP_CPU = FlexProfilerIcons.StopCPU;
  Icon DO_GC = FlexProfilerIcons.GC;

  Icon SNAPSHOT_CPU = FlexProfilerIcons.SnapshotCPU;
  Icon LIVE_OBJECTS = FlexProfilerIcons.LiveObjects;
}
