package com.intellij.flex.uiDesigner {
import cocoa.util.StringUtil;

import flash.display.Sprite;
import flash.events.UncaughtErrorEvent;
import flash.net.Socket;

public class UncaughtErrorManager {
  protected var socket:Socket;

  public function UncaughtErrorManager(socketManager:SocketManager) {
    socket = socketManager.getSocket();
    assert(socket != null);
  }

  public function listen(dispatcher:Sprite):void {
    dispatcher.loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
  }

  private function uncaughtErrorHandler(event:UncaughtErrorEvent):void {
    var error:Error = event.error;
    event.preventDefault();
    event.stopImmediatePropagation();

    sendMessage(buildErrorMessage(error));
  }

  protected function buildErrorMessage(error:Error):String {
    var stackText:String = error.getStackTrace();
    if (StringUtil.startsWith(stackText, "Error: assert failed")) {
      return stackText.substr(stackText.indexOf("\n", 22) + 1);
    }
    else {
      return stackText;
    }
  }

  protected function sendMessage(message:String):void {
    socket.writeByte(ServerMethod.showError);
    socket.writeUTF(message);
    socket.flush();
  }
}
}
