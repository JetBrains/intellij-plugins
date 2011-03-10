package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;

import flash.net.Socket;

public interface SocketManager {
  function connect(host:String, port:int):void;

  function goToClass(module:Module, className:String):void;
  function openFile(module:Module, uri:String, textOffset:int):void;

  function resolveExternalInlineStyleDeclarationSource(module:Module, parentFQN:String, elementFQN:String, targetStyleName:String, declarations:Vector.<CssDeclaration>):void;

  function addSocketDataHandler(classId:int, handler:SocketDataHandler):void;

  function checkData():void;
  
  function getSocket():Socket;
}
}