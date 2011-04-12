package com.intellij.flex.uiDesigner {
import cocoa.util.StringUtil;

import flash.desktop.NativeApplication;
import flash.display.NativeWindow;
import flash.display.Sprite;
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

  public function listen(dispatcher:Sprite):void {
    dispatcher.loaderInfo.uncaughtErrorEvents.addEventListener(UncaughtErrorEvent.UNCAUGHT_ERROR, uncaughtErrorHandler);
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

  public function handleUiError(error:Error, object:Object):void {
    sendMessage(Capabilities.isDebugger ? buildErrorMessage(error) : Error(error).message);
  }

  public function readDocumentErrorHandler(error:Error):void {
    sendMessage(Capabilities.isDebugger ? buildErrorMessage(error) : error.message);
  }

  protected function sendMessage(message:String):void {
    var projectId:int = -1;
    var documentFactoryId:int = -1;
    try {
      var activeWindow:NativeWindow = NativeApplication.nativeApplication.activeWindow;
      if (activeWindow != null) {
        var project:Project = ProjectManager(PlexusManager.instance.container.lookup(ProjectManager)).project;
        if (project != null) {
          var document:Document = DocumentManager(project.getComponent(DocumentManager)).document;
          if (document != null) {
            projectId = project.id;
            documentFactoryId = document.documentFactory.id;
          }
        }
      }
    }
    catch (ignored:Error) {
    }

    socket.writeByte(ServerMethod.showError);
    socket.writeBoolean(documentFactoryId != -1);
    if (documentFactoryId != -1) {
      socket.writeShort(projectId);
      socket.writeShort(documentFactoryId);
    }
    socket.writeUTF(message);
    socket.flush();
  }
}
}
