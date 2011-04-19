package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;

import flash.filesystem.File;
import flash.filesystem.FileMode;

import flash.filesystem.FileStream;

import flash.net.Socket;
import flash.system.System;
import flash.utils.getQualifiedClassName;
import flash.utils.getTimer;

public class Server {
  private var socket:Socket;

  private const resultFile:File = new File("/Users/develar/res");
  private const resReady:File = new File("/Users/develar/resReady");

 public static var F:Boolean;


  public function Server(socketManager:SocketManager) {
    socket = socketManager.getSocket();
    assert(socket != null);
  }

  public function goToClass(module:Module, className:String):void {
    //F = true;
    socket.writeByte(ServerMethod.goToClass);
    writeModuleId(module);
    socket.writeUTF(className);
    socket.flush();

    //while (SocketManagerImpl.ff() == 0) {
    //
    //}

    //trace(socket.readInt(), socket.readByte(), socket.readByte(), socket.readUTF());

    var time:int = getTimer();
    // fileStream.bytesAvailable is not update, i.e. we cannot while (fileStream.bytesAvailable == 0), so, we delete file after read
    while (!resReady.exists) {

    }

    var fileStream:FileStream = new FileStream();
    fileStream.open(resultFile, FileMode.READ);
    trace(fileStream.readUTF(), getTimer() - time);
    fileStream.close();
    resReady.deleteFile();
  }

  // navigation for inline style in external file (for example, ButtonSkin in sparkskins.swc) is not supported
  public function resolveExternalInlineStyleDeclarationSource(module:Module, parentFQN:String, elementFQN:String, targetStyleName:String, declarations:Vector.<CssDeclaration>):void {
    socket.writeByte(ServerMethod.resolveExternalInlineStyleDeclarationSource);
    writeModuleId(module);
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
    writeProjectId(module.project);
    socket.writeUTF(uri);
    socket.writeInt(textOffset);
    socket.flush();
  }

  public function unregisterDocumentFactories(module:Module, deleted:Vector.<int>):void {
    socket.writeByte(ServerMethod.unregisterDocumentFactories);
    writeProjectId(module.project);
    socket.writeObject(deleted);
    socket.flush();
  }

  private function writeModuleId(module:Module):void {
    socket.writeShort(module.id);
  }

  private function writeProjectId(project:Project):void {
    socket.writeShort(project.id);
  }
}
}
