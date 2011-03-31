package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.display.BitmapData;
import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.utils.ByteArray;
import flash.utils.IDataInput;

import org.flyti.plexus.PlexusManager;

registerClassAlias("lsh", LocalStyleHolder);

public class DefaultSocketDataHandler implements SocketDataHandler {
  private var projectManager:ProjectManager;
  private var libraryManager:LibraryManager;
  private var moduleManager:ModuleManager;
  private var stringRegistry:StringRegistry;
  
  private var bitmapDataManager:BitmapDataManager;
  private var swfDataManager:SwfDataManager;

  public function DefaultSocketDataHandler(libraryManager:LibraryManager, projectManager:ProjectManager, moduleManager:ModuleManager, stringRegistry:StringRegistry) {
    this.libraryManager = libraryManager;
    this.projectManager = projectManager;
    this.moduleManager = moduleManager;
    this.stringRegistry = stringRegistry;
  }
  
  public function set socket(socket:Socket):void {
  }

  public function handleSockedData(messageSize:int, method:int, data:IDataInput):void {
    switch (method) {
      case ClientMethod.openProject:
        projectManager.open(data.readInt(), new Project(data.readUTFBytes(AmfUtil.readUInt29(data)), new ProjectEventMap()));
        break;
      
      case ClientMethod.closeProject:
        closeProject(data.readInt());
        break;

      case ClientMethod.registerLibrarySet:
        registerLibrarySet(data);
        break;

      case ClientMethod.registerModule:
        registerModule(data);
        break;
      
      case ClientMethod.registerDocumentFactory:
        registerDocumentFactory(data, messageSize);
        break;
      
      case ClientMethod.updateDocumentFactory:
        updateDocumentFactory(data, messageSize);
        break;

      case ClientMethod.openDocument:
        openDocument(data);
        break;
      
      case ClientMethod.updateDocuments:
        updateDocuments(data);
        break;
      
      case ClientMethod.registerBitmap:
        registerBitmap(data);
        break;
      
      case ClientMethod.registerSwf:
        registerSwf(data);
        break;
      
      case ClientMethod.initStringRegistry:
        stringRegistry.initStringTable(data);
        break;
    }
  }
  
  private function closeProject(id:int):void {
    var project:Project = projectManager.close(id);
    for each (var module:Module in moduleManager.remove(project)) {
      libraryManager.remove(module.librarySets);
    }
  }
  
  private function registerModule(data:IDataInput):void {
    stringRegistry.readStringTable(data);
    moduleManager.register(new Module(data.readInt(), projectManager.getById(data.readInt()), libraryManager.idsToInstancesAndMarkAsUsed(data.readObject()), data.readObject()));
  }
  
  private var bitmapWorkaroundByteArray:ByteArray;
  
  private function registerDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var module:Module = moduleManager.getById(input.readInt());
    var bytes:ByteArray = new ByteArray();
    var documentFactory:DocumentFactory = new DocumentFactory(bytes, VirtualFileImpl.create(input), input.readUTFBytes(AmfUtil.readUInt29(input)), module);
    getDocumentFactoryManager(module).register(input.readUnsignedShort(), documentFactory);
    
    stringRegistry.readStringTable(input);
 
    input.readBytes(bytes, 0, messageSize - (prevBytesAvailable - input.bytesAvailable));
  }
  
  private function updateDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var module:Module = moduleManager.getById(input.readInt());
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
    if (bitmapWorkaroundByteArray == null) {
      bitmapWorkaroundByteArray = new ByteArray();
    }
    input.readBytes(bitmapWorkaroundByteArray, 0, bitmapData.width * bitmapData.height * 4);
    bitmapData.setPixels(bitmapData.rect, bitmapWorkaroundByteArray);
    bitmapWorkaroundByteArray.clear();

    if (bitmapDataManager == null) {
      bitmapDataManager = BitmapDataManager(PlexusManager.instance.container.lookup(BitmapDataManager));
    }

    bitmapDataManager.register(id, bitmapData);
  }
  
  private function registerSwf(input:IDataInput):void {
    var id:int = input.readShort();
    // we cannot change Loader API
    if (bitmapWorkaroundByteArray == null) {
      bitmapWorkaroundByteArray = new ByteArray();
    }

    var length:int = input.readInt();
    input.readBytes(bitmapWorkaroundByteArray, 0, length);
    
    if (swfDataManager == null) {
      swfDataManager = SwfDataManager(PlexusManager.instance.container.lookup(SwfDataManager));
    }
    
    swfDataManager.load(id, bitmapWorkaroundByteArray);
    bitmapWorkaroundByteArray.clear();
  }
  
  private function getDocumentFactoryManager(module:Module):DocumentFactoryManager {
    return module.context.documentFactoryManager;
  }
  
  private function getDocumentManager(module:Module):DocumentManager {
    return DocumentManager(module.project.getComponent(DocumentManager));
  }

  private function openDocument(input:IDataInput):void {
    var module:Module = moduleManager.getById(input.readInt());
    var documentFactory:DocumentFactory = getDocumentFactoryManager(module).get(input.readUnsignedShort());
    projectManager.project = module.project;
    getDocumentManager(module).open(documentFactory);
  }
  
  private function updateDocuments(input:IDataInput):void {
    var module:Module = moduleManager.getById(input.readInt());
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
    librarySet.readExternal(data);
    libraryManager.register(librarySet);
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
  public static const registerSwf:int = 11;
}