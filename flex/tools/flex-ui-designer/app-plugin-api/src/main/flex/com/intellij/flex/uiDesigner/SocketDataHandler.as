package com.intellij.flex.uiDesigner {
import flash.net.Socket;
import flash.utils.IDataInput;

public interface SocketDataHandler {
  function handleSockedData(method:int, data:IDataInput):void;

  function set socket(socket:Socket):void;

  function pendingReadIsAllowable(method:int):Boolean;
}
}
