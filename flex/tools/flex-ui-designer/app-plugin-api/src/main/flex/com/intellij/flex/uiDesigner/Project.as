package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import org.flyti.plexus.LocalEventMap;
import org.flyti.plexus.PlexusContainer;

public class Project {
  public var name:String;
  public var window:DocumentWindow;

  public function Project(name:String, map:LocalEventMap) {
    this.name = name;

    _map = map;
    _map.initializeContainer();
    _plexusContainer = _map.container;
  }

  private var _plexusContainer:PlexusContainer;
  public function get plexusContainer():PlexusContainer {
    return _plexusContainer;
  }

  private var _map:LocalEventMap;
  public function get map():LocalEventMap {
    return _map;
  }
}
}