package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import org.flyti.plexus.LocalEventMap;
import org.flyti.plexus.PlexusContainer;

public class Project {
  public var name:String;
  public var window:DocumentWindow;

  public function Project(id:int, name:String, map:LocalEventMap) {
    _id = id;
    this.name = name;

    _map = map;
    _map.initializeContainer();
    plexusContainer = _map.container;
  }

  private var _id:int;
  public function get id():int {
    return _id;
  }

  private var plexusContainer:PlexusContainer;

  private var _map:LocalEventMap;
  public function get map():LocalEventMap {
    return _map;
  }
  
  public function getComponent(role:Class):Object {
    return plexusContainer.lookup(role);
  }
}
}