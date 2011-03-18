package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.utils.ByteArray;
import flash.utils.IDataInput;

registerClassAlias("lsh", LocalStyleHolder);

public class DefaultSocketDataHandler implements SocketDataHandler {
  private var projectManager:ProjectManager;
  private var libraryManager:LibraryManager;
  private var moduleManager:ModuleManager;
  private var stringRegistry:StringRegistry;
  
  private var documentFactoryManager:DocumentFactoryManager;

  public function DefaultSocketDataHandler(libraryManager:LibraryManager, projectManager:ProjectManager, moduleManager:ModuleManager, stringRegistry:StringRegistry, documentFactoryManager:DocumentFactoryManager) {
    this.libraryManager = libraryManager;
    this.projectManager = projectManager;
    this.moduleManager = moduleManager;
    this.stringRegistry = stringRegistry;
    this.documentFactoryManager = documentFactoryManager;
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

      case ClientMethod.openDocument:
        openDocument(data);
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
  
  private function registerDocumentFactory(data:IDataInput, messageSize:int):void {
    var bytes:ByteArray = new ByteArray();
    var prevBytesAvailable:int = data.bytesAvailable;
    var documentFactory:DocumentFactory = new DocumentFactory(bytes, VirtualFileImpl.create(data), moduleManager.getById(data.readInt()));
    documentFactoryManager.register(data.readShort(), documentFactory);
    
    stringRegistry.readStringTable(data);
 
    i++;
    data.readBytes(bytes, 0, messageSize - (prevBytesAvailable - data.bytesAvailable));
  }
  
  private static var i:int;

  private function openDocument(data:IDataInput):void {
    var documentFactory:DocumentFactory = documentFactoryManager.get(data.readUnsignedShort());
    projectManager.project = documentFactory.module.project;
    DocumentManager(documentFactory.module.project.plexusContainer.lookup(DocumentManager)).open(documentFactory);
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
  public static const openDocument:int = 5;

  //noinspection JSUnusedGlobalSymbols
  public static const qualifyExternalInlineStyleSource:int = 6;
  public static const initStringRegistry:int = 7;
}