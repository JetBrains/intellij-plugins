package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.io.AmfUtil;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.ui.ProjectEventMap;
import com.intellij.flex.uiDesigner.ui.ProjectView;

import flash.geom.Rectangle;
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

  public function DefaultSocketDataHandler(libraryManager:LibraryManager, projectManager:ProjectManager, moduleManager:ModuleManager, stringRegistry:StringRegistry) {
    this.libraryManager = libraryManager;
    this.projectManager = projectManager;
    this.moduleManager = moduleManager;
    this.stringRegistry = stringRegistry;
  }

  public function set socket(value:Socket):void {
  }

  public function handleSockedData(messageSize:int, method:int, input:IDataInput):void {
    switch (method) {
      case ClientMethod.openProject:
        openProject(input);
        break;
      
      case ClientMethod.closeProject:
        projectManager.close(input.readUnsignedShort());
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
      
      case ClientMethod.initStringRegistry:
        stringRegistry.initStringTable(input);
        break;

      case ClientMethod.updateStringRegistry:
        stringRegistry.readStringTable(input);
        break;

      case ClientMethod.fillImageClassPool:
        fillAssetCClassPool(input, messageSize, false);
        break;

      case ClientMethod.fillSwfClassPool:
        fillAssetCClassPool(input, messageSize, true);
        break;
    }
  }

  private function fillAssetCClassPool(input:IDataInput, messageSize:int, isSwf:Boolean):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var context:ModuleContextEx = moduleManager.getById(input.readUnsignedShort()).context;
    const classCount:int = input.readUnsignedShort();
    var swfData:ByteArray = new ByteArray();
    input.readBytes(swfData, 0, messageSize - (prevBytesAvailable - input.bytesAvailable));
    (isSwf ? context.swfAssetContainerClassPool : context.imageAssetContainerClassPool).fill(classCount, swfData, context, libraryManager);
  }

  private function openProject(input:IDataInput):void {
    var project:Project = new Project(input.readUnsignedShort(), AmfUtil.readString(input), new ProjectEventMap());
    var projectWindowBounds:Rectangle;
    if (input.readBoolean()) {
      projectWindowBounds = new Rectangle(input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort());
    }
    projectManager.open(project, new DocumentWindow(new ProjectView(), project.map, projectWindowBounds, new MainFocusManager()));
  }

  private function registerModule(input:IDataInput):void {
    const imageCount:int = input.readUnsignedShort();
    const swfCount:int = input.readUnsignedShort();
    stringRegistry.readStringTable(input);
    var module:Module = new Module(input.readUnsignedShort(), projectManager.getById(input.readUnsignedShort()),
                                   libraryManager.idsToInstancesAndMarkAsUsed(input.readObject()), input.readObject());
    moduleManager.register(module);

    if (imageCount != 0) {
      module.context.imageAssetContainerClassPool.fillFromLibraries(imageCount);
    }
    if (swfCount != 0) {
      module.context.swfAssetContainerClassPool.fillFromLibraries(swfCount);
    }
  }
  
  private function registerDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var bytes:ByteArray = new ByteArray();
    var documentFactory:DocumentFactory = new DocumentFactory(input.readUnsignedShort(), bytes, VirtualFileImpl.create(input), AmfUtil.readString(input), module);
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
  
  private static function getDocumentFactoryManager(module:Module):DocumentFactoryManager {
    return DocumentFactoryManager.getInstance(module.project);
  }
  
  private static function getDocumentManager(module:Module):DocumentManager {
    return DocumentManager(module.project.getComponent(DocumentManager));
  }

  private function openDocument(input:IDataInput):void {
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var documentFactory:DocumentFactory = getDocumentFactoryManager(module).get(input.readUnsignedShort());
    projectManager.project = module.project;

    var documentOpened:Function;
    if (input.readBoolean()) {
      documentOpened = Server.instance.documentOpened;
    }

    getDocumentManager(module).open(documentFactory, documentOpened);
  }
  
  private function updateDocuments(input:IDataInput):void {
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var documentFactory:DocumentFactory = getDocumentFactoryManager(module).get(input.readUnsignedShort());
    var documentManager:DocumentManager = getDocumentManager(module);
    // not set projectManager.project — current project is not changed (opposite to openDocument)
    openDocumentsForFactory(documentFactory, documentManager);
  }

  private static function openDocumentsForFactory(documentFactory:DocumentFactory, documentManager:DocumentManager):void {
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

    var n:int = AmfUtil.readUInt29(data);
    var librarySet:LibrarySet = new LibrarySet(id, n == 0 ? null : libraryManager.getById(data.readUTFBytes(n)));
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
  public static const updateStringRegistry:int = 10;
  public static const fillImageClassPool:int = 11;
  public static const fillSwfClassPool:int = 12;
}