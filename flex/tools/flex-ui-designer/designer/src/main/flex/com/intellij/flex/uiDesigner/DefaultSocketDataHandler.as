package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.io.AmfUtil;
import com.intellij.flex.uiDesigner.ui.ProjectEventMap;
import com.intellij.flex.uiDesigner.ui.ProjectView;

import flash.desktop.NativeApplication;
import flash.display.BitmapData;
import flash.display.NativeWindow;
import flash.events.Event;
import flash.geom.Rectangle;
import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.system.System;
import flash.utils.ByteArray;
import flash.utils.IDataInput;

import org.flyti.plexus.PlexusManager;

registerClassAlias("lsh", LocalStyleHolder);

/**
 * WARNING: THIS CLASS MUST BE HERE: IntelliJ IDEA can debug classes only in designer module, but not in any other, like app-plugin-api
 */
public class DefaultSocketDataHandler implements SocketDataHandler {
  private var projectManager:ProjectManager;
  private var libraryManager:LibraryManager;
  private var moduleManager:ModuleManager;
  private var stringRegistry:StringRegistry;
  
  private var bitmapDataManager:BitmapDataManager;

  private var applicationExiting:Boolean;

  public function DefaultSocketDataHandler(libraryManager:LibraryManager, projectManager:ProjectManager, moduleManager:ModuleManager, stringRegistry:StringRegistry) {
    this.libraryManager = libraryManager;
    this.projectManager = projectManager;
    this.moduleManager = moduleManager;
    this.stringRegistry = stringRegistry;

    NativeApplication.nativeApplication.addEventListener(Event.EXITING, exitingHandler);
  }

  private var _embedSwfManager:EmbedSwfManager;
  private function get embedSwfManager():EmbedSwfManager {
    if (_embedSwfManager == null) {
      _embedSwfManager = EmbedSwfManager(PlexusManager.instance.container.lookup(EmbedSwfManager));
    }

    return _embedSwfManager;
  }

  private var _embedImageManager:EmbedImageManager;
  private function get embedImageManager():EmbedImageManager {
    if (_embedImageManager == null) {
      _embedImageManager = EmbedImageManager(PlexusManager.instance.container.lookup(EmbedImageManager));
    }

    return _embedImageManager;
  }

  private var _socket:Socket;
  public function set socket(value:Socket):void {
    _socket = value;
  }

  private function exitingHandler(event:Event):void {
    applicationExiting = true;
  }

  public function handleSockedData(messageSize:int, method:int, input:IDataInput):void {
    switch (method) {
      case ClientMethod.openProject:
        openProject(input);
        break;
      
      case ClientMethod.closeProject:
        closeProject(input.readUnsignedShort());
        break;

      case ClientMethod.registerLibrarySet:
        registerLibrarySet(input);
        break;

      case ClientMethod.registerModule:
        registerModule(input);
        break;
      
      case ClientMethod.registerDocumentFactory:
        registerDocumentFactory(input, messageSize);
        break;
      
      case ClientMethod.updateDocumentFactory:
        updateDocumentFactory(input, messageSize);
        break;

      case ClientMethod.openDocument:
        openDocument(input);
        break;
      
      case ClientMethod.updateDocuments:
        updateDocuments(input);
        break;
      
      case ClientMethod.registerBitmap:
        registerBitmap(input);
        break;
      
      case ClientMethod.registerBinaryFile:
        registerBinaryFile(input);
        break;
      
      case ClientMethod.initStringRegistry:
        stringRegistry.initStringTable(input);
        break;

      case ClientMethod.gg:
        //Server.RRR = AmfUtil.readUtf(input);
        System.resume();
        break;
    }
  }

  private function openProject(input:IDataInput):void {
    var project:Project = new Project(input.readUnsignedShort(), AmfUtil.readUtf(input), new ProjectEventMap());
    var projectWindowBounds:Rectangle;
    if (input.readBoolean()) {
      projectWindowBounds = new Rectangle(input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort());
    }
    projectManager.open(project);

    var window:DocumentWindow = new DocumentWindow(new ProjectView(), project.map, null, projectWindowBounds);
    window.nativeWindow.addEventListener(Event.CLOSING, closeHandler);
    window.title = project.name;
    project.window = window;
  }

  private function closeHandler(event:Event):void {
    if (applicationExiting) {

    }

    var window:NativeWindow = NativeWindow(event.target);
    var bounds:Rectangle = window.bounds;
    var project:Project = projectManager.getByNativeWindow(window);

    _socket.writeByte(ServerMethod.saveProjectWindowBounds);
    _socket.writeShort(project.id);
    _socket.writeShort(bounds.x);
    _socket.writeShort(bounds.y);
    _socket.writeShort(bounds.width);
    _socket.writeShort(bounds.height);
    _socket.flush();
  }
  
  private function closeProject(id:int):void {
    var project:Project = projectManager.close(id);
    for each (var module:Module in moduleManager.remove(project)) {
      libraryManager.remove(module.librarySets);
    }
  }
  
  private function registerModule(input:IDataInput):void {
    stringRegistry.readStringTable(input);
    moduleManager.register(new Module(input.readUnsignedShort(), projectManager.getById(input.readUnsignedShort()), libraryManager.idsToInstancesAndMarkAsUsed(input.readObject()), input.readObject()));
  }
  
  private var flashWorkaroundByteArray:ByteArray;
  
  private function registerDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var bytes:ByteArray = new ByteArray();
    var documentFactory:DocumentFactory = new DocumentFactory(input.readUnsignedShort(), bytes, VirtualFileImpl.create(input), AmfUtil.readUtf(input), module);
    getDocumentFactoryManager(module).register(documentFactory);
    
    stringRegistry.readStringTable(input);
 
    input.readBytes(bytes, 0, messageSize - (prevBytesAvailable - input.bytesAvailable));
  }
  
  private function updateDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var documentFactory:DocumentFactory = getDocumentFactoryManager(module).get(input.readUnsignedShort());
    
    stringRegistry.readStringTable(input);

    const length:int = messageSize - (prevBytesAvailable - input.bytesAvailable);
    var bytes:ByteArray = documentFactory.data;
    bytes.position = 0;
    bytes.length = length;
    input.readBytes(bytes, 0, length);
  }

  private function registerBitmap(input:IDataInput):void {
    var id:int = input.readShort();
    var bitmapData:BitmapData = new BitmapData(input.readShort(), input.readShort(), input.readBoolean(), 0);
    // we cannot change BitmapData API
    if (flashWorkaroundByteArray == null) {
      flashWorkaroundByteArray = new ByteArray();
    }
    input.readBytes(flashWorkaroundByteArray, 0, bitmapData.width * bitmapData.height * 4);
    bitmapData.setPixels(bitmapData.rect, flashWorkaroundByteArray);
    flashWorkaroundByteArray.clear();

    if (bitmapDataManager == null) {
      bitmapDataManager = BitmapDataManager(PlexusManager.instance.container.lookup(BitmapDataManager));
    }

    bitmapDataManager.register(id, bitmapData);
  }
  
  private function registerBinaryFile(input:IDataInput):void {
    var type:int = input.readByte();
    var id:int = input.readUnsignedShort();
    // we cannot change Loader API
    if (flashWorkaroundByteArray == null) {
      flashWorkaroundByteArray = new ByteArray();
    }

    var length:int = input.readInt();
    input.readBytes(flashWorkaroundByteArray, 0, length);

    (type == 1 ? embedSwfManager : embedImageManager).load(id, flashWorkaroundByteArray);
    flashWorkaroundByteArray.clear();
  }
  
  private function getDocumentFactoryManager(module:Module):DocumentFactoryManager {
    return module.context.documentFactoryManager;
  }
  
  private function getDocumentManager(module:Module):DocumentManager {
    return DocumentManager(module.project.getComponent(DocumentManager));
  }

  private function openDocument(input:IDataInput):void {
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var documentFactory:DocumentFactory = getDocumentFactoryManager(module).get(input.readUnsignedShort());
    projectManager.project = module.project;
    getDocumentManager(module).open(documentFactory);
  }
  
  private function updateDocuments(input:IDataInput):void {
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var documentFactory:DocumentFactory = getDocumentFactoryManager(module).get(input.readUnsignedShort());
    var documentManager:DocumentManager = getDocumentManager(module);
    // not set projectManager.project — current project is not changed (opposite to openDocument)
    openDocumentsForFactory(documentFactory, documentManager);
  }

  private function openDocumentsForFactory(documentFactory:DocumentFactory, documentManager:DocumentManager):void {
    if (documentFactory.document != null) {
      documentManager.open(documentFactory);
    }

    if (documentFactory.users != null) {
      for each (var user:DocumentFactory in documentFactory.users) {
        openDocumentsForFactory(user, documentManager);
      }
    }
  }

  private function registerLibrarySet(data:IDataInput):void {
    const id:String = data.readUTFBytes(AmfUtil.readUInt29(data));
    const parentId:int = data.readInt();
    StringRegistry.instance.readStringTable(data);
    var librarySet:LibrarySet = new LibrarySet(id, parentId == -1 ? null : libraryManager.getById(parentId));
    var assetLoadSemaphore:AssetLoadSemaphore = new AssetLoadSemaphore();
    librarySet.readExternal(data, assetLoadSemaphore);
    libraryManager.register(librarySet, assetLoadSemaphore);
  }

  public function pendingReadIsAllowable(method:int):Boolean {
    return false; // was for openDocument, but now (after implement factory concept) it is read immediately (sync read)
  }
}
}

final class ClientMethod {
  public static const openProject:int = 0;
  public static const closeProject:int = 1;
  
  public static const registerLibrarySet:int = 2;
  public static const registerModule:int = 3;
  public static const registerDocumentFactory:int = 4;
  public static const updateDocumentFactory:int = 5;
  public static const openDocument:int = 6;
  public static const updateDocuments:int = 7;

  //noinspection JSUnusedGlobalSymbols
  public static const qualifyExternalInlineStyleSource:int = 8;
  public static const initStringRegistry:int = 9;
  
  public static const registerBitmap:int = 10;
  public static const registerBinaryFile:int = 11;
  public static const gg:int = 12;
}