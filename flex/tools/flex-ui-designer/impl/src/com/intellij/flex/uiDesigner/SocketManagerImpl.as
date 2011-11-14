package com.intellij.flex.uiDesigner {
import flash.desktop.NativeApplication;
import flash.events.Event;
import flash.events.ProgressEvent;
import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.utils.ByteArray;
import flash.utils.Dictionary;

registerClassAlias("s", String);

public class SocketManagerImpl implements SocketManager {
  protected var socket:Socket;
  protected var errorSocket:Socket;

  private var messageId:uint = uint.MAX_VALUE;
  private var deferredMessageSize:int;
  private var unreadSocketRemainder:int;

  private var omitCount:int;
  private var omitMessages:Dictionary;

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

  public function connect(host:String, port:int, errorPort:int):void {
    if (errorPort != 0) {
      errorSocket = new Socket();
      errorSocket.addEventListener(ProgressEvent.SOCKET_DATA, errorSocketDataHandler);
      errorSocket.connect(host, errorPort);
    }

    socket = new Socket();
    socket.addEventListener(ProgressEvent.SOCKET_DATA, socketDataHandler);
    socket.addEventListener(Event.CLOSE, socketCloseHandler);
    socket.connect(host, port);
    
    for each (var handler:SocketDataHandler in socketDataHandlers) {
      handler.socket = socket;
    }
  }

  private static function socketCloseHandler(event:Event):void {
    trace("flash ui designer server close connection, so, exit");
    // IDEA-73550
    NativeApplication.nativeApplication.exit(57323);
  }

  private function socketDataHandler(event:ProgressEvent):void {
    if (event != null) {
      totalBytes += event.bytesLoaded;
      //trace("socket data handler: bytesLoaded " + event.bytesLoaded + " socket bytesAvailable " + socket.bytesAvailable + " last unread " + (socket.bytesAvailable - event.bytesLoaded) + " deferredMessageSize " + deferredMessageSize);
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

    if (checkOmit()) {
      return;
    }

    var messageSize:int;
    while (socket.bytesAvailable > 0) {
      if (deferredMessageSize == 0) {
        messageSize = socket.readInt();
        messageId = socket.readUnsignedInt();
        if (omitMessages != null && messageId in omitMessages) {
          omitCount = omitMessages[messageId];
          delete omitMessages[messageId];
          if (checkOmit()) {
            return;
          }
          else {
            continue;
          }
        }

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
        messageId = uint.MAX_VALUE;
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

  private function errorSocketDataHandler(event:ProgressEvent):void {
    const invalidMessageId:uint = errorSocket.readUnsignedInt();
    const count:uint = errorSocket.readUnsignedInt();
    trace("error, omit: " + count + " deferredMessageSize: " + deferredMessageSize);
    if (invalidMessageId == messageId) {
      deferredMessageSize = 0;
      omitCount = count;
      if (socket.bytesAvailable > 0) {
        socketDataHandler(null);
      }
    }
    else {
      if (omitMessages == null) {
        omitMessages = new Dictionary();
      }

      omitMessages[invalidMessageId] = count;
    }
  }

  private function checkOmit():Boolean {
    if (omitCount == 0) {
      return false;
    }
    
    var r:int = omitCount - socket.bytesAvailable;
    socket.readBytes(new ByteArray(), 0, r < 0 ? omitCount : 0);
    if (r <= 0) {
      omitCount = 0;
      return false;
    }
    else {
      omitCount = r;
      return true;
    }
  }
}
}