package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface ProfilerIcons {
  Icon CALLER_SOLID_ARROW = IconLoader.getIcon("/icons/callerArrow.png");
  Icon CALLER_DOTTED_ARROW = IconLoader.getIcon("/icons/callerLeafArrow.png");
  Icon CALLEE_SOLID_ARROW = IconLoader.getIcon("/icons/calleeArrow.png");
  Icon CALLEE_DOTTED_ARROW = IconLoader.getIcon("/icons/calleeLeafArrow.png");

  Icon START_CPU = IconLoader.getIcon("/icons/startCPU.png");
  Icon STOP_CPU = IconLoader.getIcon("/icons/stopCPU.png");
  Icon DO_GC = IconLoader.getIcon("/icons/gc.png");

  Icon SNAPSHOT_CPU = IconLoader.getIcon("/icons/snapshotCPU.png");
  Icon LIVE_OBJECTS = IconLoader.getIcon("/icons/liveObjects.png");
}
