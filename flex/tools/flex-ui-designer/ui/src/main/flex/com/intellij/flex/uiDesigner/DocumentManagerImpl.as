package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.css.CssReader;
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.css.StyleValueResolverImpl;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;
import com.intellij.flex.uiDesigner.flex.SystemManagerSB;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.libraries.LibrarySetItem;
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

import org.jetbrains.ApplicationManager;
import org.osflash.signals.ISignal;
import org.osflash.signals.Signal;

public class DocumentManagerImpl extends EventDispatcher implements DocumentManager {
  private var libraryManager:LibraryManager;

  private var documentReader:DocumentReader;
  private var server:Server;

  public function DocumentManagerImpl(libraryManager:LibraryManager, documentReader:DocumentReader, server:Server) {
    this.libraryManager = libraryManager;
    this.documentReader = documentReader;
    this.server = server;
  }

  private var _documentUpdated:ISignal;
  public function get documentUpdated():ISignal {
    if (_documentUpdated == null) {
      _documentUpdated = new Signal();
    }
    return _documentUpdated;
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
      if (_document != null) {
        adjustElementSelection();
      }
    }
  }

  // IDEA-71781, IDEA-71779
  private function adjustElementSelection():void {
    ElementManager(_document.module.project.getComponent(ElementManager)).element = null;
  }

  public function open(documentFactory:DocumentFactory, documentOpened:Function = null):void {
    if (documentFactory.document == null) {
      var context:ModuleContextEx = documentFactory.module.context;
      if (context.librariesResolved) {
        createAndOpen(documentFactory);
      }
      else {
        libraryManager.resolve(documentFactory.module.librarySets, doOpenAfterResolveLibraries, documentFactory, documentOpened);
      }
    }
    else if (doOpen(documentFactory, documentFactory.document)) {
      if (documentFactory.document == document) {
        adjustElementSelection();
        if (_documentUpdated != null) {
          _documentUpdated.dispatch();
        }
      }
      document.container.invalidateDisplayList();
    }
  }

  private function doOpenAfterResolveLibraries(documentFactory:DocumentFactory, documentOpened:Function):void {
    var module:Module = documentFactory.module;
    module.context.librariesResolved = true;

    if (documentOpened != null) {
      documentOpened();
    }
    
    if (createAndOpen(documentFactory) && !ApplicationManager.instance.unitTestMode) {
      var w:DocumentWindow = module.project.window;
      if (NativeWindow.supportsNotification) {
        w.notifyUser(NotificationType.INFORMATIONAL);
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
      var projectView:ProjectView = ProjectView(w.contentView);
      projectView.addDocument(document);
      this.document = document;
      projectView.selectEditorTab(document);
      return true;
    }
    else {
      return false;
    }
  }

  private function doOpen(documentFactory:DocumentFactory, document:Document):Boolean {
    try {
      var object:Object = documentReader.read(documentFactory.data, documentFactory);
      document.uiComponent = object;
      document.systemManager.setUserDocument(DisplayObject(object));
      documentReader.createDeferredMxContainersChildren(documentFactory.module.context.applicationDomain);
      var viewNavigatorApplicationBaseClass:Class = documentFactory.module.context.viewNavigatorApplicationBaseClass;
      if (viewNavigatorApplicationBaseClass != null && object is viewNavigatorApplicationBaseClass) {
        var navigator:Object = object.navigator;
        if (navigator != null && navigator.activeView != null && !navigator.activeView.isActive) {
          navigator.activeView.setActive(true);
        }
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.readDocumentErrorHandler(e);
      return false;
    }

    return true;
  }

  private static function createStyleManager(module:Module, documentFactory:DocumentFactory):void {
    if (module.context.styleManager == null) {
      createStyleManagerForContext(module.context);
    }
    if (!module.hasOwnStyleManager && module.localStyleHolders != null) {
      createStyleManagerForModule(module, documentFactory);
    }
  }
  
  private static function createStyleManagerForContext(context:ModuleContextEx):void {
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
  
  private static function createStyleManagerForModule(module:Module, documentFactory:DocumentFactory):void {
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

    cssReader.read(suitableLocalStyleHolder.getStylesheet(module.project).rulesets, suitableLocalStyleHolder.file);
    cssReader.finalizeRead();
  }

  private function createSystemManager(document:Document, module:Module):void {
    var flexModuleFactoryClass:Class = module.getClass("com.intellij.flex.uiDesigner.flex.FlexModuleFactory");
    var systemManagerClass:Class = module.getClass("com.intellij.flex.uiDesigner.flex.SystemManager");
    var window:DocumentWindow = module.project.window;
    var systemManager:SystemManagerSB = new systemManagerClass();
    document.systemManager = systemManager;

    if (!systemManager.sharedInitialized) {
      systemManager.initShared(window.stage, module.project, server, UncaughtErrorManager.instance);
    }
    systemManager.init(new flexModuleFactoryClass(module.styleManager, module.context.applicationDomain), UncaughtErrorManager.instance,
                       MainFocusManagerSB(module.project.window.focusManager));
    document.container = new DocumentContainer(Sprite(systemManager));
  }
}
}