package com.intellij.flex.uiDesigner {
import cocoa.util.StringUtil;

import flash.display.LoaderInfo;
import flash.events.ErrorEvent;
import flash.events.UncaughtErrorEvent;
import flash.net.Socket;
import flash.system.Capabilities;

import org.flyti.plexus.PlexusManager;

public class UncaughtErrorManager implements UiErrorHandler {
  protected var socket:Socket;

  public function UncaughtErrorManager(socketManager:SocketManager) {
    socket = socketManager.getSocket();
    assert(socket != null);
  }

  public static function get instance():UncaughtErrorManager {
    return UncaughtErrorManager(PlexusManager.instance.container.lookup(UncaughtErrorManager));
  }

  public function listen(loaderInfo:LoaderInfo):void {
    loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
  }

  private function uncaughtErrorHandler(event:UncaughtErrorEvent):void {
    var message:String;
    var error:Object = event.error;
    if (error is Error) {
      // must be only buildErrorMessage(event.error), without any cast (i.e. Error(error)) — invalid staktrace (AIR 2.6) (thanks, Adobe) otherwise
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

  public function handleError(error:Error, project:Project = null):void {
    sendMessage(Capabilities.isDebugger ? buildErrorMessage(error) : error.message, null, project);
  }

  public function handleUiError(error:Error, object:Object, userMessage:String):void {
    var documentFactory:DocumentFactory;
    if (object != null) {
      try {
        while (true) {
          if (object is DocumentDisplayManager) {
            documentFactory = DocumentFactory(DocumentDisplayManager(object).documentFactory);
            break;
          }
          else if ((object = object.parent) == null) {
            break;
          }
        }
      }
      catch (e:Error) {
        trace(e);
      }
    }

    if (documentFactory != null) {
      sendMessage2(Capabilities.isDebugger ? buildErrorMessage(error) : error.message, userMessage, documentFactory.id);
    }
    else {
      sendMessage(Capabilities.isDebugger ? buildErrorMessage(error) : error.message, userMessage);
    }
  }

  public function readDocumentErrorHandler(error:Error, documentFactory:DocumentFactory):void {
    sendMessage2(Capabilities.isDebugger ? buildErrorMessage(error) : error.message, null, documentFactory.id);
  }

  protected function sendMessage(message:String, userMessage:String = null, project:Project = null):void {
    var documentFactoryId:int = -1;
    try {
      if (project == null) {
        project = ProjectUtil.getProjectForActiveWindow();
      }
      if (project != null) {
        var document:Document = DocumentManager(project.getComponent(DocumentManager)).document;
        if (document != null) {
          documentFactoryId = document.documentFactory.id;
        }
      }
    }
    catch (ignored:Error) {
    }

    sendMessage2(message, userMessage, documentFactoryId);
  }

  protected function sendMessage2(message:String, userMessage:String, documentFactoryId:int):void {
    socket.writeByte(ServerMethod.SHOW_ERROR);

    socket.writeUTF(userMessage == null ? "" : userMessage);
    socket.writeUTF(message);

    socket.writeBoolean(documentFactoryId != -1);

    if (documentFactoryId != -1) {
      socket.writeShort(documentFactoryId);
    }

    socket.flush();
  }

  public function logWarning(message:String):void {
    socket.writeByte(ServerMethod.LOG_WARNING);
    socket.writeUTF(message);
    socket.flush();
  }

  public function logWarning3(message:String, cause:Error):void {
    logWarning(message + ":\n" + Capabilities.isDebugger ? buildErrorMessage(cause) : cause.message);
  }
}
}
