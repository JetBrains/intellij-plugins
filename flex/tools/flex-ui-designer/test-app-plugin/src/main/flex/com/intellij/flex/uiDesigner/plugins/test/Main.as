package com.intellij.flex.uiDesigner.plugins.test {
import cocoa.ApplicationManager;

import com.intellij.flex.uiDesigner.SocketDataHandler;
import com.intellij.flex.uiDesigner.SocketManager;

import flash.display.Sprite;

import org.flyti.plexus.PlexusContainer;
import org.flyti.plexus.PlexusManager;

public class Main extends Sprite {
  public function Main() {
    init();
  }

  private function init():void {
    new ComponentSet();
    ApplicationManager.instance.unitTestMode = false;
    var container:PlexusContainer = PlexusManager.instance.container;
    var socketManager:SocketManager = SocketManager(container.lookup(SocketManager));
    socketManager.addSocketDataHandler(TestSocketDataHandler.CLASS, SocketDataHandler(container.lookup(TestSocketDataHandler)));
  }
}
}