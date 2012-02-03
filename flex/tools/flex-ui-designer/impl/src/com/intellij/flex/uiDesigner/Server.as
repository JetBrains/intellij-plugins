package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.display.BitmapData;
import flash.filesystem.File;
import flash.filesystem.FileMode;
import flash.filesystem.FileStream;
import flash.geom.Rectangle;
import flash.net.Socket;
import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;
import flash.utils.getTimer;

import org.flyti.plexus.PlexusManager;
import org.jetbrains.actionSystem.DataContext;

public class Server implements ResourceBundleProvider {
  // we cannot use File.applicationDirectory.nativePath directly  http://juick.com/develar/1485063
  private static const APP_DIR_PATH:String = File.applicationDirectory.nativePath + "/";

  // http://exaflood.de/syrotech/air-securityerror-filewriteresource/
  private const resultFile:File = new File(APP_DIR_PATH + "r");

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

  public function unregisterLibrarySets(unregistered:Vector.<int>):void {
    socket.writeByte(ServerMethod.UNREGISTER_LIBRARY_SETS);
    socket.writeObject(unregistered);
    socket.flush();
  }

  public function unregisterDocumentFactories(unregistered:Vector.<int>):void {
    socket.writeByte(ServerMethod.UNREGISTER_DOCUMENT_FACTORIES);
    socket.writeObject(unregistered);
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

  private static var flashWorkaroundByteArray:ByteArray;

  public function getBitmapData(id:int, project:Project):BitmapData {
    var resultReadyFile:File = null;
    try {
      resultReadyFile = sendSyncAssetMessage(ServerMethod.GET_BITMAP_DATA, id);
      while (!resultReadyFile.exists) {
      }

      var fileStream:FileStream = new FileStream();
      fileStream.open(resultFile, FileMode.READ);
      try {
        if (fileStream.bytesAvailable == 0) {
          return null;
        }
        
        var bitmapData:BitmapData = new BitmapData(fileStream.readUnsignedShort(), fileStream.readUnsignedShort(), fileStream.readBoolean(), 0);
        if (flashWorkaroundByteArray == null) {
          flashWorkaroundByteArray = new ByteArray();
        }
        fileStream.readBytes(flashWorkaroundByteArray);
        bitmapData.setPixels(bitmapData.rect, flashWorkaroundByteArray);
        return bitmapData;
      }
      finally {
        fileStream.close();
        if (flashWorkaroundByteArray != null) {
          flashWorkaroundByteArray.clear();
        }
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.handleError(e, project);
    }
    finally {
      postCheckSyncMessaging(resultReadyFile, project);
    }

    return null;
  }

  private function sendSyncAssetMessage(method:int, id:int):File {
    const resultReadyFilename:String = generateResultReadyFilename();
    socket.writeByte(method);
    socket.writeUTF(resultReadyFilename);
    socket.writeShort(id);
    socket.flush();
    return new File(APP_DIR_PATH + resultReadyFilename);
  }

  public function getAssetInfo(id:int, project:Project, isSwf:Boolean):AssetInfo {
    var resultReadyFile:File = null;
    try {
      resultReadyFile = sendSyncAssetMessage(isSwf ? ServerMethod.GET_EMBED_SWF_ASSET_INFO : ServerMethod.GET_EMBED_IMAGE_ASSET_INFO, id);
      while (!resultReadyFile.exists) {
      }

      var fileStream:FileStream = new FileStream();
      fileStream.open(resultFile, FileMode.READ);
      try {
        if (fileStream.bytesAvailable == 0) {
          return null;
        }
        else {
          return new AssetInfo(VirtualFileImpl.create(fileStream), AmfUtil.readNullableString(fileStream));
        }
      }
      finally {
        fileStream.close();
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.handleError(e, project);
    }
    finally {
      postCheckSyncMessaging(resultReadyFile, project);
    }

    return null;
  }

  public function getSwfData(id:int, cacheItem:SwfAssetCacheItem, project:Project):ByteArray {
    var resultReadyFile:File = null;
    try {
      resultReadyFile = sendSyncAssetMessage(ServerMethod.GET_SWF_DATA, id);
      while (!resultReadyFile.exists) {
      }

      var fileStream:FileStream = new FileStream();
      fileStream.open(resultFile, FileMode.READ);
      try {
        cacheItem.bounds = new Rectangle(fileStream.readInt() / 20, fileStream.readInt() / 20, fileStream.readInt() / 20,
                                         fileStream.readInt() / 20);
        var bytes:ByteArray = new ByteArray();
        fileStream.readBytes(bytes);
        return bytes;
      }
      finally {
        fileStream.close();
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.handleError(e, project);
    }
    finally {
      postCheckSyncMessaging(resultReadyFile, project);
    }

    //noinspection UnreachableCodeJS
    throw new Error("Burn in hell, Adobe.");
  }

  public var moduleForGetResourceBundle:Module;

  public function getResourceBundle(locale:String, bundleName:String):Dictionary {
    var module:Module = moduleForGetResourceBundle;
    if (module == null) {
      var rootDataContext:DataContext = ProjectUtil.getRootDataContext();
      var document:Document = rootDataContext == null ? null : PlatformDataKeys.DOCUMENT.getData(rootDataContext);
      if (document == null) {
        UncaughtErrorManager.instance.logWarning("Cannot find resource bundle " + bundleName + " for locale " + locale +
                                                 " due to null document");
        return null;
      }

      module = document.module;
    }

    var resultReadyFile:File = null;
    var result:Dictionary;
    try {
      const resultReadyFilename:String = generateResultReadyFilename();
      socket.writeByte(ServerMethod.GET_RESOURCE_BUNDLE);
      socket.writeUTF(resultReadyFilename);
      writeModuleId(module);
      socket.writeUTF(locale);
      socket.writeUTF(bundleName);
      socket.flush();

      resultReadyFile = new File(APP_DIR_PATH + resultReadyFilename);
      // fileStream.bytesAvailable is not update, i.e. we cannot while (fileStream.bytesAvailable == 0), so, we delete file after read
      while (!resultReadyFile.exists) {
      }

      var fileStream:FileStream = new FileStream();
      fileStream.open(resultFile, FileMode.READ);
      try {
        result = fileStream.readObject();
      }
      finally {
        fileStream.close();
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.handleError(e);
    }
    finally {
      postCheckSyncMessaging(resultReadyFile, null);
    }

    if (result == null) {
      UncaughtErrorManager.instance.logWarning("Cannot find resource bundle " + bundleName + " for locale " + locale);
    }
    return result;
  }

  private static function generateResultReadyFilename():String {
    return (getTimer() + Math.random()).toString();
  }

  private static function postCheckSyncMessaging(resultReadyFile:File, project:Object):void {
    try {
      if (resultReadyFile != null && resultReadyFile.exists) {
        resultReadyFile.deleteFileAsync();
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.handleError(e, Project(project));
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

    //var displayObject:DisplayObject = DisplayObject(document.uiComponent);
    //var bitmapData:BitmapData = new BitmapData(displayObject.width, displayObject.height, false);
    //bitmapData.draw(displayObject);
    //
    //socket.writeShort(bitmapData.width);
    //socket.writeShort(bitmapData.height);
    //socket.writeBytes(bitmapData.getPixels(bitmapData.rect));

    socket.flush();
  }
}
}
