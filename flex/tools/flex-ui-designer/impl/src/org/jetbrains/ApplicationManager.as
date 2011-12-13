package org.jetbrains {
import cocoa.plaf.LookAndFeel;

import org.flyti.plexus.PlexusManager;

public final class ApplicationManager {
  public var unitTestMode:Boolean;
  public var laf:LookAndFeel;

  public static function get instance():ApplicationManager {
    return ApplicationManager(PlexusManager.instance.container.lookup(ApplicationManager));
  }
}
}