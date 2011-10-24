package com.intellij.flex.uiDesigner {
import flash.display.Sprite;

import org.flyti.plexus.PlexusContainer;
import org.flyti.plexus.PlexusManager;
import org.jetbrains.ApplicationManager;

public class TestPlugin extends Sprite {
  public function TestPlugin() {
    init();
  }

  private static function init():void {
    new TestComponentSet();
    ApplicationManager.instance.unitTestMode = true;
    var container:PlexusContainer = PlexusManager.instance.container;
    var socketManager:SocketManager = SocketManager(container.lookup(SocketManager));
    socketManager.addSocketDataHandler(TestSocketDataHandler.CLASS, SocketDataHandler(container.lookup(TestSocketDataHandler)));
  }
}
}