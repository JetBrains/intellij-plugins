package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;

import flash.events.ProgressEvent;
import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.system.Capabilities;
import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;

registerClassAlias("s", String);

public class SocketManagerImpl implements SocketManager {
  protected var socket:Socket;

  private var deferredMessageSize:int;
  private var unreadSocketRemainder:int;

  private const socketDataHandlers:Dictionary = new Dictionary();

  public function addSocketDataHandler(classId:int, handler:SocketDataHandler):void {
    assert(!(classId in socketDataHandlers));
    socketDataHandlers[classId] = handler;
    if (socket != null) {
      handler.socket = socket;
    }
  }

  public function connect(host:String, port:int):void {
    socket = new Socket();
    socket.addEventListener(ProgressEvent.SOCKET_DATA, socketDataHandler);
    socket.connect(host, port);
    
    for each (var handler:SocketDataHandler in socketDataHandlers) {
      handler.socket = socket;
    }
  }

  public function checkData():void {
    socketDataHandler(null);
    if (socket.bytesAvailable > 0 && Capabilities.isDebugger) {
      trace("WARNING! Unread document data, at least " + socket.bytesAvailable);
//      var bytes:ByteArray = new ByteArray();
//      socket.readBytes(bytes, 0, socket.bytesAvailable);
//      FileUtil.writeBytes("/Users/develar/r", bytes);
    }
  }

  private function socketDataHandler(event:ProgressEvent):void {
    if (event != null) {
      trace("socket data handler: bytesLoaded " + event.bytesLoaded + " socket bytesAvailable " + socket.bytesAvailable + " last unread " + (socket.bytesAvailable - event.bytesLoaded));
    }
    
    if (unreadSocketRemainder != 0) {
      if (event != null) {
        unreadSocketRemainder += event.bytesLoaded;
      }
      if (socket.bytesAvailable == unreadSocketRemainder) {
        return;
      }
      else {
        unreadSocketRemainder = 0;
      }
    }

    var messageSize:int;
    while (socket.bytesAvailable > 0) {
      if (deferredMessageSize == 0) {
        messageSize = socket.readInt();
        if (messageSize > socket.bytesAvailable) {
          deferredMessageSize = messageSize;
          break;
        }
      }
      else if (deferredMessageSize > socket.bytesAvailable) {
        break;
      }
      else {
        messageSize = deferredMessageSize;
        deferredMessageSize = 0;
      }

      var clientMethodClass:int = socket.readByte();
      var handler:SocketDataHandler = socketDataHandlers[clientMethodClass];
      if (handler != null) {
        var position:int = socket.bytesAvailable + 1 /* method class size */;
        const method:int = socket.readByte();
        handler.handleSockedData(method, socket);
        if (messageSize != (position - socket.bytesAvailable)) {
          if (handler.pendingReadIsAllowable(method)) {
            unreadSocketRemainder = socket.bytesAvailable;
            trace("allowed unread socket remainder: " + unreadSocketRemainder + " for method " + method);
          }
          else {
            trace("prohibited unread socket remainder: " + unreadSocketRemainder + " for method " + method);
          }
          return;
        }
      }
      else {
        throw new ArgumentError("unknown class: " + clientMethodClass);
      }
    }
  }

  public function goToClass(module:Module, className:String):void {
    socket.writeByte(ServerMethod.goToClass);
    socket.writeInt(module.id);
    socket.writeUTF(className);
    socket.flush();
  }

  // navigation for inline style in external file (for example, ButtonSkin in sparkskins.swc) is not supported
  public function resolveExternalInlineStyleDeclarationSource(module:Module, parentFQN:String, elementFQN:String, targetStyleName:String, declarations:Vector.<CssDeclaration>):void {
    socket.writeByte(ServerMethod.resolveExternalInlineStyleDeclarationSource);
    socket.writeInt(module.id);
    socket.writeUTF(parentFQN);
    socket.writeUTF(elementFQN);
    socket.writeUTF(targetStyleName);
    socket.writeShort(declarations.length);
    for each (var declaration:CssDeclaration in declarations) {
      if (declaration.fromAs || declaration.value === undefined) {
        socket.writeShort(0);
        continue;
      }

      socket.writeUTF(declaration.name);
      if (declaration.value is Class) {
        socket.writeUTF(getQualifiedClassName(declaration.value).replace("::", "."));
      }
      else {
        socket.writeUTF(declaration.value.toString());
      }
    }
  }

  public function openFile(module:Module, uri:String, textOffset:int):void {
    socket.writeByte(ServerMethod.openFile);
    socket.writeInt(module.id);
    socket.writeUTF(uri);
    socket.writeInt(textOffset);
    socket.flush();
  }

  public function getSocket():Socket {
    return socket;
  }
}
}