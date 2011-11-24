package com.jetbrains.actionscript.profiler;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface ProfilerIcons {
  Icon CALLER_ARROW = IconLoader.getIcon("/icons/callerArrow.png");
  Icon CALLER_LEAF_ARROW = IconLoader.getIcon("/icons/callerLeafArrow.png");
  Icon CALLEE_ARROW = IconLoader.getIcon("/icons/calleeArrow.png");
  Icon CALLEE_LEAF_ARROW = IconLoader.getIcon("/icons/calleeLeafArrow.png");
}
