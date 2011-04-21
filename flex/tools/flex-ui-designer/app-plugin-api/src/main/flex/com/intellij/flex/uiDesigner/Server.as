package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;

import flash.display.BitmapData;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.net.Socket;
import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;

public class Server implements ResourceBundleProvider {
  private var socket:Socket;
  private var uncaughtErrorManager:UncaughtErrorManager;

  public function Server(socketManager:SocketManager, uncaughtErrorManager:UncaughtErrorManager) {
    socket = socketManager.getSocket();
    this.uncaughtErrorManager = uncaughtErrorManager;
    assert(socket != null);
  }

  public function goToClass(module:Module, className:String):void {
    socket.writeByte(ServerMethod.goToClass);
    writeModuleId(module);
    socket.writeUTF(className);
    socket.flush();
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

  // http://exaflood.de/syrotech/air-securityerror-filewriteresource/
  private const resultReadyFile:File = new File(File.applicationDirectory.nativePath + "/d");
  private const resultFile:File = new File(File.applicationDirectory.nativePath + "/r");

  private static var flashWorkaroundByteArray:ByteArray;

  public function getBitmapData(id:int):BitmapData {
    try {
      socket.writeByte(ServerMethod.getBitmapData);
      socket.writeShort(id);
      socket.flush();

      while (!resultReadyFile.exists) {
      }

      var fileStream:FileStream = new FileStream();
      fileStream.open(resultFile, FileMode.READ);
      try {
        var bitmapData:BitmapData = new BitmapData(fileStream.readUnsignedShort(), fileStream.readUnsignedShort(), fileStream.readBoolean(), 0);
        if (flashWorkaroundByteArray == null) {
          flashWorkaroundByteArray = new ByteArray();
        }
        fileStream.readBytes(flashWorkaroundByteArray);
        bitmapData.setPixels(bitmapData.rect, flashWorkaroundByteArray);
        flashWorkaroundByteArray.clear();
        return bitmapData;
      }
      finally {
        fileStream.close();
      }
    }
    finally {
      if (resultReadyFile.exists) {
        try {
          resultReadyFile.deleteFile();
        }
        catch (e:Error) {
          uncaughtErrorManager.handleError(e);
        }
      }
    }

    //noinspection UnreachableCodeJS
    throw new Error("Burn in hell, Adobe.");
  }

  public function getResourceBundle(locale:String, bundleName:String):Dictionary {
    try {
      socket.writeByte(ServerMethod.getResourceBundle);
      var project:Project = ProjectUtil.getProjectForActiveWindow();
      // todo MUST BE MODULE
      writeProjectId(project);
      socket.writeUTF(locale);
      socket.writeUTF(bundleName);
      socket.flush();

      // fileStream.bytesAvailable is not update, i.e. we cannot while (fileStream.bytesAvailable == 0), so, we delete file after read
      while (!resultReadyFile.exists) {
      }

      var fileStream:FileStream = new FileStream();
      fileStream.open(resultFile, FileMode.READ);
      try {
        return fileStream.readObject();
      }
      finally {
        fileStream.close();
      }
    }
    catch (e:Error) {
      uncaughtErrorManager.handleError(e);
    }
    finally {
      if (resultReadyFile.exists) {
        try {
          resultReadyFile.deleteFile();
        }
        catch (e:Error) {
          uncaughtErrorManager.handleError(e);
        }
      }
    }

    return null;
  }
}
}
