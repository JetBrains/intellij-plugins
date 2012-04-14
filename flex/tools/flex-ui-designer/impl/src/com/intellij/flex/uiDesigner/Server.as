package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssDeclaration;
import com.intellij.flex.uiDesigner.flex.ResourceBundle;
import com.intellij.flex.uiDesigner.io.AmfUtil;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;

import flash.display.BitmapData;
import flash.display.DisplayObject;
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
import org.jetbrains.util.ActionCallback;

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

  public function openDocument(module:Module, factory:DocumentFactory, textOffset:int, focus:Boolean):void {
    socket.writeByte(ServerMethod.OPEN_DOCUMENT);
    writeProjectId(module.project);
    socket.writeShort(factory.id);
    socket.writeInt(textOffset);
    socket.writeBoolean(focus);
    socket.flush();
  }

  public function unregisterLibrarySets(unregistered:Vector.<int>):void {
    socket.writeByte(ServerMethod.UNREGISTER_LIBRARY_SETS);
    socket.writeObject(unregistered);
    socket.flush();
  }

  public function unregisterDocumentFactories(unregistered:Vector.<int>, callbackId:int = -1):void {
    if (callbackId == -1) {
      socket.writeByte(ServerMethod.UNREGISTER_DOCUMENT_FACTORIES);
    }
    else {
       callback(callbackId, true, false);
    }

    writeIds(unregistered);
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

  private function findResourceBundle(bundles:Vector.<ResourceBundle>, locale:String, bundleName:String):ResourceBundle {
    if (bundles == null) {
      return null;
    }

    for each (var bundle:ResourceBundle in bundles) {
      if (bundle.bundleName == bundleName && bundle.locale == locale) {
        return bundle;
      }
    }

    return null;
  }

  private function findResourceBundle2(librarySet:LibrarySet, locale:String, bundleName:String):ResourceBundle {
    var resourceBundle:ResourceBundle;
    do {
      resourceBundle = findResourceBundle(librarySet.resourceBundles, locale, bundleName);
    }
    while (resourceBundle == null && (librarySet = librarySet.parent) != null);
    return resourceBundle;
  }

  public function getResourceBundle(locale:String, bundleName:String, bundleClass:Class):ResourceBundle {
    var module:Module = moduleForGetResourceBundle;
    if (module == null) {
      var rootDataContext:DataContext = ProjectUtil.getRootDataContext();
      module = rootDataContext == null ? null : PlatformDataKeys.MODULE.getData(rootDataContext);
      if (module == null) {
        UncaughtErrorManager.instance.logWarning("Cannot find resource bundle " + bundleName + " for locale " + locale +
                                                 " due to null document");
        return null;
      }
    }

    var moduleResolveResult:int = -1;
    const moduleResolveResultKey:String = locale + ":" + bundleName;
    if (module.resourceBundleResolveResult == null) {
      module.resourceBundleResolveResult = new Dictionary();
    }
    else {
      if (moduleResolveResultKey in module.resourceBundleResolveResult) {
        moduleResolveResult = module.resourceBundleResolveResult[moduleResolveResultKey] ? 1 : 0;
      }
    }

    var resourceBundle:ResourceBundle = moduleResolveResult != 1 ? null : findResourceBundle(module.resourceBundles, locale, bundleName);
    if (moduleResolveResult == -1) {
      resourceBundle = doGetResourceBundle(true, module, locale, bundleName, bundleClass);
      if ((module.resourceBundleResolveResult[moduleResolveResultKey] = resourceBundle != null)) {
        if (module.resourceBundles == null) {
          module.resourceBundles = new Vector.<ResourceBundle>();
        }
        module.resourceBundles[module.resourceBundles.length] = resourceBundle;
      }
    }

    if (resourceBundle == null) {
      resourceBundle = findResourceBundle2(module.librarySet, locale, bundleName);
      if (resourceBundle == null) {
        resourceBundle = doGetResourceBundle(false, module, locale, bundleName, bundleClass);
        if (resourceBundle != null) {
          var librarySet:LibrarySet = LibraryManager(module.project.getComponent(LibraryManager)).getById(resourceBundle.content["__$ls_id__"]);
          if (librarySet.resourceBundles == null) {
            librarySet.resourceBundles = new Vector.<ResourceBundle>();
          }
          librarySet.resourceBundles[librarySet.resourceBundles.length] = resourceBundle;
        }
      }
    }

    return resourceBundle;
  }

  private function doGetResourceBundle(inSource:Boolean, module:Module, locale:String, bundleName:String, bundleClass:Class):ResourceBundle {
    var resultReadyFile:File = null;
    try {
      const resultReadyFilename:String = generateResultReadyFilename();
      socket.writeByte(ServerMethod.GET_RESOURCE_BUNDLE);
      socket.writeUTF(resultReadyFilename);
      socket.writeBoolean(inSource);
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
        var sourceId:int = fileStream.readUnsignedShort();
        if (sourceId == 0) {
          return null;
        }
        else {
          var content:Dictionary = fileStream.readObject();
          if (!inSource) {
            content["__$ls_id__"] = sourceId - 1;
          }
          return new bundleClass(locale, bundleName, content);
        }
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

    return null;
  }

  private static function generateResultReadyFilename():String {
    return (getTimer() + Math.random()).toString();
  }

  private static function postCheckSyncMessaging(resultReadyFile:File, project:Object):void {
    try {
      if (resultReadyFile != null && resultReadyFile.exists) {
        try {
          resultReadyFile.deleteFile();
        }
        catch (e:Error) {
          if (resultReadyFile.exists) {
            resultReadyFile.deleteFile();
          }
        }
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

  public function writeIds(ids:Vector.<int>):void {
    socket.writeObject(ids);
    socket.flush();
  }

  public function writeDocumentImage(bitmapData:BitmapData):void {
    if (bitmapData == null) {
      socket.writeShort(0);
    }
    else {
      var argb:ByteArray = bitmapData.getPixels(bitmapData.rect);
      socket.writeShort(bitmapData.width);
      socket.writeShort(bitmapData.height);
      socket.writeBytes(argb);
    }

    socket.flush();
  }

  public function asyncCallback(result:ActionCallback, callbackId:int):void {
    result.doWhenDone(callback, callbackId, true);
    result.doWhenRejected(callback, callbackId, false);
  }

  public function callback(callbackId:int, success:Boolean = true, flush:Boolean = true):void {
    socket.writeByte(ServerMethod.CALLBACK);
    socket.writeByte(callbackId);
    socket.writeBoolean(success);
    if (flush) {
      socket.flush();
    }
  }
}
}