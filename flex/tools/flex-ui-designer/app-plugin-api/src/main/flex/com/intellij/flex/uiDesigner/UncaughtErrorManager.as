package com.intellij.flex.uiDesigner {
import cocoa.util.StringUtil;

import flash.display.Sprite;
import flash.events.ErrorEvent;
import flash.events.UncaughtErrorEvent;
import flash.net.Socket;
import flash.system.Capabilities;

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
    var message:String;
    var error:Object = event.error;
    if (error is Error) {
      message = Capabilities.isDebugger ? buildErrorMessage(event.error) : Error(error).message;
    }
    else {
      message = error is ErrorEvent ? ErrorEvent(error).text : error.toString();
    }
    
    event.preventDefault();
    event.stopImmediatePropagation();

    sendMessage(message);
  }

  protected function buildErrorMessage(error:Error):String {
    var message:String = error.getStackTrace();
    if (StringUtil.startsWith(message, "Error: assert failed")) {
      return message.substr(message.indexOf("\n", 22) + 1);
    }
    else {
      return message;
    }
  }

  protected function sendMessage(message:String):void {
    socket.writeByte(ServerMethod.showError);
    socket.writeUTF(message);
    socket.flush();
  }
}
}
