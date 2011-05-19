package com.intellij.flex.uiDesigner {
import cocoa.ApplicationManager;
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.css.CssReader;
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.css.StyleValueResolverImpl;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;
import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.ui.DocumentContainer;
import com.intellij.flex.uiDesigner.ui.ProjectView;

import flash.desktop.DockIcon;
import flash.desktop.NativeApplication;
import flash.desktop.NotificationType;
import flash.display.DisplayObject;
import flash.display.NativeWindow;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.Dictionary;

public class DocumentManagerImpl extends EventDispatcher implements DocumentManager {
  private var libraryManager:LibraryManager;

  private var documentReader:DocumentReader;
  private var server:Server;

  public function DocumentManagerImpl(libraryManager:LibraryManager, documentReader:DocumentReader, server:Server) {
    this.libraryManager = libraryManager;
    this.documentReader = documentReader;
    this.server = server;
  }

  private var _document:Document;
  [Bindable(event="documentChanged")]
  public function get document():Document {
    return _document;
  }

  public function set document(value:Document):void {
    if (value != document) {
      _document = value;
      dispatchEvent(new Event("documentChanged"));
    }
  }

  public function open(documentFactory:DocumentFactory):void {
    if (documentFactory.document == null) {
      var context:ModuleContextEx = documentFactory.module.context;
      if (context.librariesResolved) {
        createAndOpen(documentFactory);
      }
      else {
        libraryManager.resolve(documentFactory.module.librarySets, doOpenAfterResolveLibraries, documentFactory);
      }
    }
    else if (doOpen(documentFactory, documentFactory.document)) {
      documentFactory.document.container.invalidateDisplayList();
      this.document = documentFactory.document;
    }
  }

  private function doOpenAfterResolveLibraries(documentFactory:DocumentFactory):void {
    var module:Module = documentFactory.module;
    module.context.librariesResolved = true;
    if (createAndOpen(documentFactory) && !ApplicationManager.instance.unitTestMode) {
      var w:DocumentWindow = module.project.window;
      if (NativeWindow.supportsNotification) {
        w.nativeWindow.notifyUser(NotificationType.INFORMATIONAL);
      }
      else {
        var dockIcon:DockIcon = NativeApplication.nativeApplication.icon as DockIcon;
        if (dockIcon != null) {
          dockIcon.bounce(NotificationType.INFORMATIONAL);
        }
      }
    }
  }

  private function createAndOpen(documentFactory:DocumentFactory):Boolean {
    var document:Document = new Document(documentFactory);
    var module:Module = documentFactory.module;
    createStyleManager(module, documentFactory);
    createSystemManager(document, module);

    if (doOpen(documentFactory, document)) {
      documentFactory.document = document;
      var w:DocumentWindow = module.project.window;
      ProjectView(w.contentView).addDocument(document);
      this.document = document;
      return true;
    }
    else {
      return false;
    }
  }

  private function doOpen(documentFactory:DocumentFactory, document:Document):Boolean {
    try {
      var object:Object = documentReader.read(documentFactory.data, documentFactory, document.styleManager);
      document.uiComponent = object;
      document.systemManager.setUserDocument(DisplayObject(object));
      documentReader.createDeferredMxContainersChildren(documentFactory.module.context.applicationDomain);
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.readDocumentErrorHandler(e);
      return false;
    }

    return true;
  }

  private function createStyleManager(module:Module, documentFactory:DocumentFactory):void {
    if (module.context.styleManager == null) {
      createStyleManagerForContext(module.context);
    }
    if (!module.hasOwnStyleManager && module.localStyleHolders != null) {
      createStyleManagerForModule(module, documentFactory);
    }
  }
  
  private function createStyleManagerForContext(context:ModuleContextEx):void {
    var inheritingStyleMapList:Vector.<Dictionary> = new Vector.<Dictionary>();
    var styleManagerClass:Class = context.getClass("com.intellij.flex.uiDesigner.css.RootStyleManager");
    context.styleManager = new styleManagerClass(inheritingStyleMapList, new StyleValueResolverImpl(context.applicationDomain));
    var cssReaderClass:Class = context.getClass("com.intellij.flex.uiDesigner.css.CssReaderImpl");
    var cssReader:CssReader = new cssReaderClass();
    cssReader.styleManager = context.styleManager; 
    // FakeObjectProxy/FakeBooleanSetProxy/MergedCssStyleDeclaration find in list from 0 to end, then we add in list in reverse order
    // (because the library with index 4 overrides the library with index 2)
    var librarySets:Vector.<LibrarySet> = context.librarySets;
    for (var i:int = librarySets.length - 1; i > -1; i--) {
      var librarySet:LibrarySet = librarySets[i];
      do {
        var libraries:Vector.<LibrarySetItem> = librarySet.items;
        for (var j:int = libraries.length - 1; j > -1; j--) {
          var library:LibrarySetItem = libraries[j];
          if (library.inheritingStyles != null) {
            inheritingStyleMapList.push(library.inheritingStyles);
          }

          if (library.defaultsStyle != null) {
            var virtualFile:VirtualFileImpl = VirtualFileImpl(library.file.createChild("defaults.css"));
            virtualFile.stylesheet = library.defaultsStyle;
            cssReader.read(library.defaultsStyle.rulesets, virtualFile);
          }
        }
      }
      while ((librarySet = librarySet.parent) != null);
    }

    cssReader.finalizeRead();
    inheritingStyleMapList.fixed = true;
  }
  
  private function createStyleManagerForModule(module:Module, documentFactory:DocumentFactory):void {
    var styleManagerClass:Class = module.getClass("com.intellij.flex.uiDesigner.css.ChildStyleManager");
    module.styleManager = new styleManagerClass(module.context.styleManager);
    var cssReaderClass:Class = module.getClass("com.intellij.flex.uiDesigner.css.CssReaderImpl");
    var cssReader:CssReader = new cssReaderClass();
    cssReader.styleManager = module.styleManager;
    
    var suitableLocalStyleHolder:LocalStyleHolder = module.localStyleHolders[0];
    for each (var localStyleHolder:LocalStyleHolder in module.localStyleHolders) {
      if (localStyleHolder.file.url == documentFactory.file.url) {
        suitableLocalStyleHolder = localStyleHolder;
        break;
      }
    }

    cssReader.read(suitableLocalStyleHolder.getStylesheet().rulesets, suitableLocalStyleHolder.file);
    cssReader.finalizeRead();
  }

  private function createSystemManager(document:Document, module:Module):void {
    var flexModuleFactoryClass:Class = module.getClass("com.intellij.flex.uiDesigner.flex.FlexModuleFactory");
    var systemManagerClass:Class = module.getClass("com.intellij.flex.uiDesigner.flex.SystemManager");
    var window:DocumentWindow = module.project.window;
    var systemManager:SystemManagerSB = new systemManagerClass();
    document.systemManager = systemManager;
    systemManager.init(new flexModuleFactoryClass(module.styleManager, module.context.applicationDomain), window.nativeWindow.stage,
                       UncaughtErrorManager.instance, server);
    document.container = new DocumentContainer(Sprite(systemManager));
  }
}
}