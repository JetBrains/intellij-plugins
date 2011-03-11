package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.ProjectManager;

import flash.display.NativeWindow;
import flash.geom.Point;
import flash.geom.Rectangle;
import flash.net.Socket;

public class UITest extends BaseTestCase {
  public function UITest() {
    // disable unused inspection
    //noinspection ConstantIfStatementJS
    if (false) {
      styleNavigation();
    }
  }
  
  override public function init(projectManager:ProjectManager, socket:Socket):void {
    super.init(projectManager, socket);
    
    socket.writeByte(1);
    
    var nativeWindow:NativeWindow = projectManager.project.window.nativeWindow;
    var point:Point = nativeWindow.globalToScreen(new Point(0, 0));  
    socket.writeShort(point.x);
    socket.writeShort(point.y);
    
    var bounds:Rectangle = nativeWindow.bounds;
    bounds.width = 1280;
    bounds.height = 770;
    nativeWindow.bounds = bounds;
  }
  
  public function styleNavigation():void {
  }  
}
}