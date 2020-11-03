package org.jetbrains.idea.perforce.perforce.jobs;

import java.awt.*;

final class DefaultGb {
  public static GridBagConstraints create() {
    return new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                                                         new Insets(2,2,2,2), 0,0);
  }
}
