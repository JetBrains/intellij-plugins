package com.intellij.flex.uiDesigner {
import cocoa.ClassFactory;
import cocoa.DocumentWindow;
import cocoa.pane.PaneItem;
import cocoa.toolWindow.ToolWindowManager;

import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.io.AmfUtil;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.mxml.MxmlReader;
import com.intellij.flex.uiDesigner.ui.ProjectEventMap;
import com.intellij.flex.uiDesigner.ui.ProjectView;
import com.intellij.flex.uiDesigner.ui.inspectors.propertyInspector.PropertyInspector;
import com.intellij.flex.uiDesigner.ui.inspectors.styleInspector.StyleInspector;

import flash.desktop.NativeApplication;
import flash.display.BitmapData;
import flash.geom.Rectangle;
import flash.net.Socket;
import flash.net.registerClassAlias;
import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.IDataInput;
import flash.utils.describeType;

import net.miginfocom.layout.MigConstants;

import org.jetbrains.ApplicationManager;
import org.jetbrains.util.ActionCallback;

registerClassAlias("lsh", LocalStyleHolder);

// SocketDataHandler is extension component (i.e. DefaultSocketDataHandler as role, lookup must be container.lookup(DefaultSocketDataHandler))
internal class DefaultSocketDataHandler implements SocketDataHandler {
  private var projectManager:ProjectManager;
  private var libraryManager:LibraryManager;
  private var moduleManager:ModuleManager;
  private var stringRegistry:StringRegistry;

  public function DefaultSocketDataHandler(libraryManager:LibraryManager, projectManager:ProjectManager, moduleManager:ModuleManager, stringRegistry:StringRegistry) {
    this.libraryManager = libraryManager;
    this.projectManager = projectManager;
    this.moduleManager = moduleManager;
    this.stringRegistry = stringRegistry;

    moduleManager.moduleUnregistered.add(moduleUnregistered);
  }

  private function moduleUnregistered(module:Module):void {
    libraryManager.unregister(module.librarySet);
  }

  public function set socket(value:Socket):void {
  }

  public function handleSockedData(messageSize:int, method:int, callbackId:int, input:IDataInput):void {
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

      case ClientMethod.unregisterModule:
        unregisterModule(input, callbackId);
        break;
      
      case ClientMethod.registerDocumentFactory:
        registerDocumentFactory(input, messageSize);
        break;
      
      case ClientMethod.updateDocumentFactory:
        updateDocumentFactory(input, messageSize);
        break;

      case ClientMethod.renderDocument:
        renderDocument(input, callbackId);
        break;
      
      case ClientMethod.renderDocumentsAndDependents:
        renderDocumentsAndDependents(input, callbackId);
        break;
      
      case ClientMethod.initStringRegistry:
        stringRegistry.initTable(input);
        break;

      case ClientMethod.updateStringRegistry:
        stringRegistry.readTable(input);
        break;

      case ClientMethod.fillImageClassPool:
        fillClassPool(input, messageSize, FlexLibrarySet.IMAGE_POOL);
        break;

      case ClientMethod.fillSwfClassPool:
        fillClassPool(input, messageSize, FlexLibrarySet.SWF_POOL);
        break;

      case ClientMethod.fillViewClassPool:
        fillClassPool(input, messageSize, FlexLibrarySet.VIEW_POOL);
        break;

      case ClientMethod.selectComponent:
        selectComponent(input);
        break;

      case ClientMethod.getDocumentImage:
        getDocumentImage(input, callbackId);
        break;

      case ClientMethod.updatePropertyOrStyle:
        updatePropertyOrStyle(input, callbackId);
        break;
    }
  }

  private function fillClassPool(input:IDataInput, messageSize:int, id:String):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var librarySet:FlexLibrarySet = FlexLibrarySet(libraryManager.getById(input.readUnsignedShort()));
    const classCount:int = input.readUnsignedShort();
    var data:ByteArray = new ByteArray();
    input.readBytes(data, 0, messageSize - (prevBytesAvailable - input.bytesAvailable));
    librarySet.getClassPool(id).fill(classCount, data, libraryManager);
  }

  private function openProject(input:IDataInput):void {
    var project:Project = new Project(input.readUnsignedShort(), AmfUtil.readString(input), new ProjectEventMap());
    var projectWindowBounds:Rectangle;
    if (input.readBoolean()) {
      projectWindowBounds = new Rectangle(input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort(), input.readUnsignedShort());
    }
    var projectView:ProjectView = new ProjectView();
    projectView.laf = ApplicationManager.instance.laf;
    var documentWindow:DocumentWindow = new DocumentWindow(projectView, new MainFocusManager());
    projectView.dataContext = projectManager.open(project, documentWindow);

    var toolWindowManager:ToolWindowManager = ToolWindowManager(project.getComponent(ToolWindowManager));
    toolWindowManager.container = projectView;
    toolWindowManager.registerToolWindow(PaneItem.create("Style", new ClassFactory(StyleInspector)), MigConstants.RIGHT, true);
    toolWindowManager.registerToolWindow(PaneItem.create("Properties", new ClassFactory(PropertyInspector)), MigConstants.RIGHT, true);

    documentWindow.init(project.map, projectWindowBounds);
  }

  private function registerModule(input:IDataInput):void {
    stringRegistry.readTable(input);
    moduleManager.register(new Module(input.readUnsignedShort(), projectManager.getById(input.readUnsignedShort()),
                                      libraryManager.getById(input.readUnsignedShort()), input.readBoolean(), input.readObject()));
  }

  private function unregisterModule(input:IDataInput, callbackId:int):void {
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var unregisteredDocuments:Vector.<int> = DocumentFactoryManager.getInstance().unregisterBelongToModule(module);
    moduleManager.unregister(module);
    Server.instance.unregisterDocumentFactories(unregisteredDocuments, callbackId);
  }

  private function registerDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var module:Module = moduleManager.getById(input.readUnsignedShort());
    var bytes:ByteArray = new ByteArray();
    var documentFactory:DocumentFactory = new DocumentFactory(input.readUnsignedShort(), bytes, VirtualFileImpl.create(input),
                                                              AmfUtil.readString(input), input.readUnsignedByte(), module);

    documentFactory.documentReferences = input.readObject();
    getDocumentFactoryManager().register(documentFactory);

    stringRegistry.readTable(input);
    input.readBytes(bytes, 0, messageSize - (prevBytesAvailable - input.bytesAvailable));
  }
  
  private function updateDocumentFactory(input:IDataInput, messageSize:int):void {
    const prevBytesAvailable:int = input.bytesAvailable;
    var documentFactory:DocumentFactory = getDocumentFactoryManager().getById(input.readUnsignedShort());
    AmfUtil.readString(input);
    input.readUnsignedByte(); // todo isApp update document styleManager

    documentFactory.documentReferences = input.readObject();
    
    stringRegistry.readTable(input);

    const length:int = messageSize - (prevBytesAvailable - input.bytesAvailable);
    var bytes:ByteArray = documentFactory.data;
    bytes.position = 0;
    bytes.length = length;
    input.readBytes(bytes, 0, length);
  }

  private static function getDocumentFactoryManager():DocumentFactoryManager {
    return DocumentFactoryManager.getInstance();
  }
  
  private static function getDocumentManager(module:Module):DocumentManager {
    return DocumentManager(module.project.getComponent(DocumentManager));
  }

  private static function renderDocument(input:IDataInput, callbackId:int):void {
    var documentFactory:DocumentFactory = getDocumentFactoryManager().getById(input.readUnsignedShort());
    var callback:ActionCallback = getDocumentManager(documentFactory.module).render(documentFactory);
    Server.instance.asyncCallback(callback, callbackId);
  }

  private static function getDocumentImage(input:IDataInput, callbackId:int):void {
    var documentFactoryManager:DocumentFactoryManager = getDocumentFactoryManager();
    var documentFactory:DocumentFactory = documentFactoryManager.getById(input.readUnsignedShort());
    var documentManager:DocumentManager = DocumentManager(documentFactory.module.project.getComponent(DocumentManager));
    if (documentFactory.document == null) {
      var callback:ActionCallback = documentManager.render(documentFactory);
      callback.doWhenDone(getDocumentImageDoneHandler, documentFactory, callbackId);
      callback.doWhenRejected(Server.instance.callback, callbackId, false);
    }
    else {
      getDocumentImageDoneHandler(documentFactory, callbackId);
    }
  }

  private static function getDocumentImageDoneHandler(documentFactory:DocumentFactory, callbackId:int):void {
    var document:Document = documentFactory.document;
    var bitmapData:BitmapData = document.displayManager.getSnapshot(document.container == null);
    var server:Server = Server.instance;
    server.callback(callbackId, true, false);
    server.writeDocumentImage(bitmapData);
  }

  private function updatePropertyOrStyle(input:IDataInput, callbackId:int):void {
    const documentId:int = AmfUtil.readUInt29(input);
    const componentId:int = AmfUtil.readUInt29(input);
    stringRegistry.readTable(input);
    const isStyle:Boolean = input.readBoolean();
    const propertyName:String = stringRegistry.readNotNull(input);
    const propertyValue:Object = MxmlReader.readPrimitive(input.readByte(), input, stringRegistry);

    var documentFactory:DocumentFactory = getDocumentFactoryManager().getById(documentId);
    var component:Object = documentFactory.getComponent(componentId);
    if (component == null) {
      UncaughtErrorManager.instance.logWarning("Can't find target component " + documentFactory.id + ":" + componentId);
    }
    else if (isStyle) {
      component.setStyle(propertyName, propertyValue);
    }
    else {
      component[propertyName] = propertyValue;
    }

    Server.instance.callback(callbackId);
  }

  private static function renderDocumentsAndDependents(input:IDataInput, callbackId:int):void {
    var documentFactoryManager:DocumentFactoryManager = getDocumentFactoryManager();
    var n:int = AmfUtil.readUInt29(input);
    var processed:Dictionary = new Dictionary();
    var callbacks:Vector.<ActionCallback> = new Vector.<ActionCallback>();
    while (n-- > 0) {
      var documentFactory:DocumentFactory = documentFactoryManager.getById(AmfUtil.readUInt29(input));
      doRenderDocumentAndDependents(documentFactory, getDocumentManager(documentFactory.module), documentFactoryManager, processed, callbacks);
    }

    if (callbacks.length == 0) {
      Server.instance.callback(callbackId, true);
      return;
    }

    var totalCallback:ActionCallback = new ActionCallback(callbacks.length);
    Server.instance.asyncCallback(totalCallback, callbackId);
    for each (var callback:ActionCallback in callbacks) {
      callback.notify(totalCallback);
    }
  }

  private static function doRenderDocumentAndDependents(documentFactory:DocumentFactory,
                                                        documentManager:DocumentManager,
                                                        documentFactoryManager:DocumentFactoryManager,
                                                        processed:Dictionary,
                                                        callbacks:Vector.<ActionCallback>):void {
    if (processed[documentFactory]) {
      return;
    }

    processed[documentFactory] = true;

    if (documentFactory.document != null) {
      var callback:ActionCallback = documentManager.render(documentFactory);
      if (!callback.isProcessed) {
        callbacks[callbacks.length] = callback;
      }
    }

    var dependents:Vector.<DocumentFactory> = documentFactoryManager.getDependents(documentFactory);
    if (dependents != null) {
      for each (var dependent:DocumentFactory in dependents) {
        doRenderDocumentAndDependents(dependent, documentManager, documentFactoryManager, processed, callbacks);
      }
    }
  }

  private function registerLibrarySet(data:IDataInput):void {
    const id:int = AmfUtil.readUInt29(data);
    const isFlexLibrarySet:Boolean = data.readBoolean();
    var parentId:int = data.readShort();
    var parent:LibrarySet = parentId == -1 ? null : libraryManager.getById(parentId);
    var librarySet:LibrarySet = isFlexLibrarySet ? new FlexLibrarySet(id, parent) : new LibrarySet(id, parent);
    librarySet.readExternal(data);
    libraryManager.register(librarySet);
  }

  private static function selectComponent(input:IDataInput):void {
    const documentId:int = input.readUnsignedShort();
    const componentId:int = input.readUnsignedShort();
    var documentFactory:DocumentFactory = DocumentFactoryManager.getInstance().getById(documentId);

    var documentManager:DocumentManager = DocumentManager(documentFactory.module.project.getComponent(DocumentManager));
    if (documentFactory.document == null) {
      documentManager.render(documentFactory).doWhenDone(selectDocumentTab, documentFactory, componentId);
    }
    else {
      selectDocumentTab(documentFactory, componentId);
    }
  }

  private static function selectDocumentTab(documentFactory:DocumentFactory, componentId:int):void {
    var component:Object = documentFactory.getComponent(componentId);
    var project:Project = documentFactory.module.project;

    var w:DocumentWindow = project.window;
    var projectView:ProjectView = ProjectView(w.contentView);
    projectView.selectEditorTab(documentFactory.document);
    DocumentManager(project.getComponent(DocumentManager)).document = documentFactory.document;
    ComponentManager(project.getComponent(ComponentManager)).component = component;

    if (component == null) {
      UncaughtErrorManager.instance.logWarning("Can't find target component " + documentFactory.id + ":" + componentId);
    }

    var window:DocumentWindow = project.window;
    if (!window.visible) {
      window.visible = true;
    }
    NativeApplication.nativeApplication.activate(window);
  }

  private static var methodIdToName:Vector.<String>;

  public function describeMethod(methodId:int):String {
    if (methodIdToName == null) {
      var names:XMLList = describeType(ClientMethod).constant.@name;
      methodIdToName = new Vector.<String>(names.length(), true);
      for each (var name:String in names) {
        methodIdToName[ClientMethod[name]] = name;
      }
    }

    return methodIdToName[methodId];
  }
}
}

final class ClientMethod {
  public static const openProject:int = 0;
  public static const closeProject:int = 1;
  
  public static const registerLibrarySet:int = 2;

  public static const registerModule:int = 3;
  public static const unregisterModule:int = 4;

  public static const registerDocumentFactory:int = 5;
  public static const updateDocumentFactory:int = 6;
  public static const renderDocument:int = 7;
  public static const renderDocumentsAndDependents:int = 8;

  public static const initStringRegistry:int = 9;
  public static const updateStringRegistry:int = 10;

  public static const fillImageClassPool:int = 11;
  public static const fillSwfClassPool:int = 12;
  public static const fillViewClassPool:int = 13;

  public static const selectComponent:int = 14;
  public static const getDocumentImage:int = 15;
  public static const updatePropertyOrStyle:int = 16;
}