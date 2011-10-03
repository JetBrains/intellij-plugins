package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.SocketDataHandler;
import com.intellij.flex.uiDesigner.SocketManager;

import flash.display.Sprite;

import org.flyti.plexus.PlexusContainer;
import org.flyti.plexus.PlexusManager;
import org.jetbrains.ApplicationManager;

public class Main extends Sprite {
  public function Main() {
    init();
  }

  private static function init():void {
    new ComponentSet();
    ApplicationManager.instance.unitTestMode = true;
    var container:PlexusContainer = PlexusManager.instance.container;
    var socketManager:SocketManager = SocketManager(container.lookup(SocketManager));
    socketManager.addSocketDataHandler(TestSocketDataHandler.CLASS, SocketDataHandler(container.lookup(TestSocketDataHandler)));
  }
}
}