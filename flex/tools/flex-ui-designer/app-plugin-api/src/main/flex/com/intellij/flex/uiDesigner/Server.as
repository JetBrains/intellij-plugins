package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;

import flash.display.BitmapData;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.geom.Rectangle;
import flash.net.Socket;
import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;

import org.flyti.plexus.PlexusManager;

public class Server implements ResourceBundleProvider {
  private var socket:Socket;

  public function Server(socketManager:SocketManager) {
    socket = socketManager.getSocket();
    assert(socket != null);
  }

  public static function get instance():Server {
    return Server(PlexusManager.instance.container.lookup(Server));
  }

  public function goToClass(module:Module, className:String):void {
    socket.writeByte(ServerMethod.GO_TO_CLASS);
    writeModuleId(module);
    socket.writeUTF(className);
    socket.flush();
  }

  // navigation for inline style in external file (for example, ButtonSkin in sparkskins.swc) is not supported
  public function resolveExternalInlineStyleDeclarationSource(module:Module, parentFQN:String, elementFQN:String, targetStyleName:String, declarations:Vector.<CssDeclaration>):void {
    socket.writeByte(ServerMethod.RESOLVE_EXTERNAL_INLINE_STYLE_DECLARATION_SOURCE);
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
    socket.writeByte(ServerMethod.OPEN_FILE);
    writeProjectId(module.project);
    socket.writeUTF(uri);
    socket.writeInt(textOffset);
    socket.flush();
  }

  public function openFileAndFindXmlAttributeOrTag(module:Module, uri:String, textOffset:int, elementName:String):void {
      socket.writeByte(ServerMethod.OPEN_FILE_AND_FIND_XML_ATTRIBUTE_OR_TAG);
      writeProjectId(module.project);
      socket.writeUTF(uri);
      socket.writeInt(textOffset);
      socket.writeUTF(elementName);
      socket.flush();
    }

  public function openDocument(module:Module, factory:DocumentFactory, textOffset:int):void {
    socket.writeByte(ServerMethod.OPEN_DOCUMENT);
    writeProjectId(module.project);
    socket.writeShort(factory.id);
    socket.writeInt(textOffset);
    socket.flush();
  }

  public function unregisterDocumentFactories(module:Module, deleted:Vector.<int>):void {
    socket.writeByte(ServerMethod.UNREGISTER_DOCUMENT_FACTORIES);
    writeProjectId(module.project);
    socket.writeObject(deleted);
    socket.flush();
  }

  public function closeProject(project:Project):void {
    socket.writeByte(ServerMethod.CLOSE_PROJECT);
    writeProjectId(project);
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
    preCheckSyncMessaging();
    try {
      socket.writeByte(ServerMethod.GET_BITMAP_DATA);
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
    catch (e:Error) {
      UncaughtErrorManager.instance.handleError(e);
    }
    finally {
      postCheckSyncMessaging();
    }

    //noinspection UnreachableCodeJS
    throw new Error("Burn in hell, Adobe.");
  }

  public function getResourceBundle(project:Object, locale:String, bundleName:String):Dictionary {
    preCheckSyncMessaging();
    try {
      socket.writeByte(ServerMethod.GET_RESOURCE_BUNDLE);
      writeProjectId(Project(project));
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
      UncaughtErrorManager.instance.handleError(e);
    }
    finally {
      postCheckSyncMessaging();
    }

    return null;
  }

  private function preCheckSyncMessaging():void {
    // windows only issue
    // http://youtrack.jetbrains.net/issue/IDEA-71568
    if (resultReadyFile.exists) {
      resultReadyFile.deleteFile();
    }
  }

  private function postCheckSyncMessaging():void {
    if (resultReadyFile.exists) {
      try {
        resultReadyFile.deleteFile();
      }
      catch (e:Error) {
        UncaughtErrorManager.instance.handleError(e);
      }
    }
  }

  public function saveProjectWindowBounds(project:Project, bounds:Rectangle):void {
    socket.writeByte(ServerMethod.SAVE_PROJECT_WINDOW_BOUNDS);
    writeProjectId(project);
    socket.writeShort(bounds.x);
    socket.writeShort(bounds.y);
    socket.writeShort(bounds.width);
    socket.writeShort(bounds.height);
    socket.flush();
  }

  public function documentOpened():void {
    socket.writeByte(ServerMethod.DOCUMENT_OPENED);
    socket.flush();
  }
}
}
