package com.intellij.flex.uiDesigner {
import flash.net.Socket;

public interface SocketManager {
  function connect(host:String, port:int):void;

  function addSocketDataHandler(classId:int, handler:SocketDataHandler):void;
  
  function getSocket():Socket;
}
}