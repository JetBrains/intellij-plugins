package com.intellij.flex.uiDesigner {
import flash.events.ProgressEvent;
import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.utils.Dictionary;

registerClassAlias("s", String);

public class SocketManagerImpl implements SocketManager {
  protected var socket:Socket;

  private var deferredMessageSize:int;
  private var unreadSocketRemainder:int;

  private const socketDataHandlers:Dictionary = new Dictionary();
  
  // for debug only
  private var totalBytes:int;
  //noinspection JSUnusedGlobalSymbols
  public final function geTotalBytesExceptLast(event:ProgressEvent):int {
    return totalBytes - event.bytesLoaded;
  }
  //noinspection JSUnusedGlobalSymbols
  public final function get currentPosition():int {
    return totalBytes - socket.bytesAvailable;
  }

  public function getSocket():Socket {
    return socket;
  }

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

  private function socketDataHandler(event:ProgressEvent):void {
    if (event != null) {
      totalBytes += event.bytesLoaded;
      //trace("socket data handler: bytesLoaded " + event.bytesLoaded + " socket bytesAvailable " + socket.bytesAvailable + " last unread " + (socket.bytesAvailable - event.bytesLoaded));
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
        trace(clientMethodClass + ":" + method);
        handler.handleSockedData(messageSize - 2, method, socket);
        trace(clientMethodClass + ":" + method + " processed");
        if (messageSize != (position - socket.bytesAvailable)) {
          if (handler.pendingReadIsAllowable(method)) {
            unreadSocketRemainder = socket.bytesAvailable;
            trace("allowed unread socket remainder: " + unreadSocketRemainder + " for method " + method);
          }
          else {
            throw new Error("prohibited unread socket remainder: " + socket.bytesAvailable + " for method " + method + " with message size " + messageSize + " at position " + position);
          }
          return;
        }
      }
      else {
        throw new ArgumentError("unknown class: " + clientMethodClass);
      }
    }
  }
}
}