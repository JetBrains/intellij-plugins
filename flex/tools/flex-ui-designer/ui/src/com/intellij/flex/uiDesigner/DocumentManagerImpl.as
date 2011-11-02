package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.css.CssReader;
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.css.StyleValueResolverImpl;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;
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

  private var _documentChanged:ISignal;
  public function get documentChanged():ISignal {
    if (_documentChanged == null) {
      _documentChanged = new Signal();
    }
    return _documentChanged;
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
      if (_documentChanged != null) {
        _documentChanged.dispatch();
      }
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
    var context:ModuleContextEx = documentFactory.module.context;
    if (context.imageAssetContainerClassPool.filling) {
      context.imageAssetContainerClassPool.filled.addOnce(function():void {
        open(documentFactory, documentOpened);
      });
      return;
    }
    if (context.swfAssetContainerClassPool.filling) {
      context.swfAssetContainerClassPool.filled.addOnce(function():void {
        open(documentFactory, documentOpened);
      });
      return;
    }

    if (documentFactory.document == null) {
      if (context.librariesResolved) {
        createAndOpen(documentFactory, documentOpened);
      }
      else {
        libraryManager.resolve(documentFactory.module.librarySets, doOpenAfterResolveLibraries, documentFactory, documentOpened);
      }
    }
    else if (doOpen(documentFactory, documentFactory.document, documentOpened)) {
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
    if (createAndOpen(documentFactory, documentOpened) && !ApplicationManager.instance.unitTestMode) {
      if (NativeWindow.supportsNotification) {
        module.project.window.notifyUser(NotificationType.INFORMATIONAL);
      }
      else {
        var dockIcon:DockIcon = NativeApplication.nativeApplication.icon as DockIcon;
        if (dockIcon != null) {
          dockIcon.bounce(NotificationType.INFORMATIONAL);
        }
      }
    }
  }

  private function createAndOpen(documentFactory:DocumentFactory, documentOpened:Function):Boolean {
    var document:Document = new Document(documentFactory);
    var module:Module = documentFactory.module;
    if (!documentFactory.isPureFlash) {
      createStyleManager(document, module);
    }

    createDocumentDisplayManager(document, module, documentFactory.isPureFlash);

    if (doOpen(documentFactory, document, documentOpened)) {
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

  private function doOpen(documentFactory:DocumentFactory, document:Document, documentOpened:Function):Boolean {
    if (documentOpened != null) {
      try {
        documentOpened();
      }
      catch (e:Error) {
        UncaughtErrorManager.instance.handleError(e, documentFactory.module.project);
      }
    }

    try {
      try {
        // IDEA-72499
        document.displayManager.setStyleManagerForTalentAdobeEngineers(true);
        var object:DisplayObject = DisplayObject(documentReader.read(documentFactory.data, documentFactory, document.styleManager));
        document.uiComponent = object;
        document.displayManager.setUserDocument(object);

        if (documentFactory.isPureFlash) {
          var pv3dViewClass:Class = documentFactory.module.context.getClassIfExists("org.papervision3d.view.AbstractView");
          if (pv3dViewClass != null && object is pv3dViewClass) {
            object["startRendering"]();
          }
        }
      }
      finally {
        document.displayManager.setStyleManagerForTalentAdobeEngineers(false);
      }
      
      documentReader.createDeferredMxContainersChildren(documentFactory.module.context.applicationDomain);
      var viewNavigatorApplicationBaseClass:Class = documentFactory.module.context.viewNavigatorApplicationBaseClass;
      if (viewNavigatorApplicationBaseClass != null && object is viewNavigatorApplicationBaseClass) {
        var navigator:Object = Object(object).navigator;
        if (navigator != null && navigator.activeView != null && !navigator.activeView.isActive) {
          navigator.activeView.setActive(true);
        }
      }
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.readDocumentErrorHandler(e, documentFactory);
      return false;
    }

    NativeApplication.nativeApplication.activate(document.module.project.window);
    return true;
  }

  private static function createStyleManager(document:Document, module:Module):void {
    if (module.context.styleManager == null) {
      createStyleManagerForContext(module.context);
    }

    if (module.localStyleHolders == null) {
      return;
    }

    if (module.isApp && document.documentFactory.isApp) {
      createStyleManagerForAppDocument(document, module);
    }
    else if (!module.hasOwnStyleManager) {
      createStyleManagerForModule(module);
    }
  }

  private static function createCssReader(context:ModuleContextEx, styleManager:StyleManagerEx):CssReader {
    var c:Class = context.getClass("com.intellij.flex.uiDesigner.css.CssReaderImpl");
    var cssReader:CssReader = new c();
    cssReader.styleManager = styleManager;
    return cssReader;
  }

  private static function createChildStyleManager(context:ModuleContextEx):StyleManagerEx {
    var c:Class = context.getClass("com.intellij.flex.uiDesigner.css.ChildStyleManager");
    return new c(context.styleManager);
  }
  
  private static function createStyleManagerForContext(context:ModuleContextEx):void {
    var inheritingStyleMapList:Vector.<Dictionary> = new Vector.<Dictionary>();
    var styleManagerClass:Class = context.getClass("com.intellij.flex.uiDesigner.css.RootStyleManager");
    context.styleManager = new styleManagerClass(inheritingStyleMapList, new StyleValueResolverImpl(context));
    var cssReader:CssReader = createCssReader(context, context.styleManager);
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

  private static function createStyleManagerForModule(module:Module):void {
    var styleManager:StyleManagerEx = createChildStyleManager(module.context);
    module.styleManager = styleManager;
    var cssReader:CssReader = createCssReader(module.context, styleManager);

    var localStyleHolder:LocalStyleHolder = module.localStyleHolders[0];
    cssReader.read(localStyleHolder.getStylesheet(module.project).rulesets, localStyleHolder.file);
    cssReader.finalizeRead();
  }

  private static function createStyleManagerForAppDocument(document:Document, module:Module):void {
    var styleManager:StyleManagerEx = createChildStyleManager(module.context);
    document.styleManager = styleManager;
    var cssReader:CssReader = createCssReader(module.context, styleManager);

    var localStyleHolder:LocalStyleHolder;
    for each (localStyleHolder in module.localStyleHolders) {
      if (localStyleHolder.isApplicable(document.documentFactory)) {
        cssReader.read(localStyleHolder.getStylesheet(module.project).rulesets, localStyleHolder.file);
      }
    }

    cssReader.finalizeRead();
  }

  private function createDocumentDisplayManager(document:Document, module:Module, isPureFlash:Boolean):void {
    var flexModuleFactoryClass:Class = isPureFlash ? null :  module.getClass("com.intellij.flex.uiDesigner.flex.FlexModuleFactory");
    var systemManagerClass:Class = isPureFlash ? FlashDocumentDisplayManager : module.getClass("com.intellij.flex.uiDesigner.flex.SystemManager");
    var window:DocumentWindow = module.project.window;
    var systemManager:DocumentDisplayManager = new systemManagerClass();
    document.displayManager = systemManager;

    if (!systemManager.sharedInitialized) {
      systemManager.initShared(window.stage, module.project, server, UncaughtErrorManager.instance);
    }
    systemManager.init(isPureFlash ? null : new flexModuleFactoryClass(document.styleManager, module.context.applicationDomain), UncaughtErrorManager.instance,
                       MainFocusManagerSB(module.project.window.focusManager), document.documentFactory);
    document.container = new DocumentContainer(Sprite(systemManager));
  }
}
}