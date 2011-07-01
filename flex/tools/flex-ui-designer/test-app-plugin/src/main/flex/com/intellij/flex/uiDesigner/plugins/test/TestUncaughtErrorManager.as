package com.intellij.flex.uiDesigner.plugins.test {
import com.intellij.flex.uiDesigner.ServerMethod;
import com.intellij.flex.uiDesigner.SocketManager;
import com.intellij.flex.uiDesigner.UncaughtErrorManager;

import org.hamcrest.AssertionError;

public class TestUncaughtErrorManager extends UncaughtErrorManager {
  public function TestUncaughtErrorManager(socketManager:SocketManager) {
    super(socketManager);
  }
  
  override protected function buildErrorMessage(error:Error):String {
    if (error is AssertionError) {
      var message:String = error.message;
      var stack:Array = error.getStackTrace().split("\n");
      for (var i:int = 3; i < stack.length; i++) {
        var s:String = stack[i];
        if (s.indexOf("org/hamcrest") == -1) {
          message += "\n" + s;
          break;
        }
      }

      return message;
    }
    else {
      return super.buildErrorMessage(error);
    }
  }

  override protected function sendMessage(message:String, uiMessage:String = null):void {
    socket.writeByte(ServerMethod.SHOW_ERROR);
    socket.writeUTF(message);
    socket.flush();
  }
}
}
