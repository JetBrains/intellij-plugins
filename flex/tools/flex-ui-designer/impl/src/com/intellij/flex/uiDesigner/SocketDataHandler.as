package com.intellij.flex.uiDesigner {
import flash.net.Socket;
import flash.utils.IDataInput;

public interface SocketDataHandler {
  /**
   * messageSize — logical (i.e. without 2 bytes for clientMethodClass (1) and clentMethod (1))
   */
  function handleSockedData(messageSize:int, method:int, callbackId:int, data:IDataInput):void;

  function set socket(socket:Socket):void;

  function pendingReadIsAllowable(method:int):Boolean;

  function describeMethod(methodId:int):String;
}
}
