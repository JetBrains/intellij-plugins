package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.io.AmfUtil;

import flash.net.Socket;
import flash.net.registerClassAlias;
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
  
  public function set socket(socket:Socket):void {
  }

  public function handleSockedData(method:int, data:IDataInput):void {
    trace("default socket handler: method " + method);
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

  private function openDocument(data:IDataInput):void {
    var module:Module = moduleManager.getById(data.readInt());
    projectManager.project = module.project;
    var documentManager:DocumentManager = DocumentManager(module.project.plexusContainer.lookup(DocumentManager));
    documentManager.open(module, data);
  }

  private function registerModule(data:IDataInput):void {
    stringRegistry.readStringTable(data);
    moduleManager.register(new Module(data.readInt(), projectManager.getById(data.readInt()), libraryManager.idsToInstancesAndMarkAsUsed(data.readObject()), data.readObject()));
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
    return method == ClientMethod.openDocument;
  }
}
}

final class ClientMethod {
  public static const openProject:int = 0;
  public static const closeProject:int = 1;
  
  public static const registerLibrarySet:int = 2;
  public static const registerModule:int = 3;
  public static const openDocument:int = 4;

  //noinspection JSUnusedGlobalSymbols
  public static const qualifyExternalInlineStyleSource:int = 5;
  public static const initStringRegistry:int = 6;
}
