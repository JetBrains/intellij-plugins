package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.CssReader;
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;
import com.intellij.flex.uiDesigner.css.StyleValueResolverImpl;
import com.intellij.flex.uiDesigner.flex.MainFocusManagerSB;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.flex.uiDesigner.libraries.LibrarySet;
import com.intellij.flex.uiDesigner.libraries.QueueLoader;
import com.intellij.flex.uiDesigner.mxml.FlexMxmlReader;
import com.intellij.flex.uiDesigner.mxml.MxmlReader;
import com.intellij.flex.uiDesigner.ui.DocumentContainer;

import flash.desktop.DockIcon;
import flash.desktop.NativeApplication;
import flash.desktop.NotificationType;
import flash.display.DisplayObject;
import flash.display.NativeWindow;
import flash.display.Stage;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.Dictionary;

import org.jetbrains.ApplicationManager;
import org.jetbrains.util.ActionCallback;
import org.osflash.signals.ISignal;
import org.osflash.signals.Signal;

public class DocumentManagerImpl extends EventDispatcher implements DocumentManager {
  private var libraryManager:LibraryManager;

  private var server:Server;

  public function DocumentManagerImpl(libraryManager:LibraryManager, server:Server) {
    this.libraryManager = libraryManager;
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
    ComponentManager(_document.module.project.getComponent(ComponentManager)).component = null;
  }

  public function render(documentFactory:DocumentFactory):ActionCallback {
    var result:ActionCallback = new ActionCallback();
    render2(documentFactory, result);
    return result;
  }

  private function render2(documentFactory:DocumentFactory, result:ActionCallback):void {
    var module:Module = documentFactory.module;
    if (!documentFactory.isPureFlash) {
      var fillCallback:ActionCallback = module.flexLibrarySet.createFillCallback();
      if (fillCallback != null) {
        fillCallback.doWhenDone(render2, documentFactory, result);
        trace("as fillCallback");
        return;
      }
    }

    if (documentFactory.document == null) {
      if (module.librarySet.isLoaded) {
        trace("as librariesResolved");
        createAndRender(documentFactory, result);
      }
      else {
        trace("as resolve");
        libraryManager.resolve(documentFactory.module.librarySet, doOpenAfterResolveLibraries, documentFactory, result);
      }
    }
    else if (doRender(documentFactory, documentFactory.document, result)) {
      if (documentFactory.document == document) {
        adjustElementSelection();
        if (_documentUpdated != null) {
          _documentUpdated.dispatch();
        }
      }

      DocumentContainer(documentFactory.document.container).documentUpdated();
    }
  }

  private function doOpenAfterResolveLibraries(documentFactory:DocumentFactory, result:ActionCallback):void {
    var module:Module = documentFactory.module;
    createAndRender(documentFactory, result);
    if (result.isDone && !ApplicationManager.instance.unitTestMode) {
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

  private function createAndRender(documentFactory:DocumentFactory, result:ActionCallback):void {
    var document:Document = new Document(documentFactory);
    var module:Module = documentFactory.module;
    if (!documentFactory.isPureFlash) {
      createStyleManager(document, module);
    }

    createDocumentDisplayManager(document, module, documentFactory.isPureFlash);
    doRender(documentFactory, document, result);
  }

  private function doRender(documentFactory:DocumentFactory, document:Document, result:ActionCallback):Boolean {
    var documentReader:DocumentReader = documentFactory.isPureFlash ? new MxmlReader() : new FlexMxmlReader(document.displayManager);
    try {
      server.moduleForGetResourceBundle = documentFactory.module;
      // IDEA-72499
      document.displayManager.setStyleManagerForTalentAdobeEngineers(true);
      var object:DisplayObject = DisplayObject(documentReader.read(documentFactory.data, documentFactory, document.styleManager));
      document.uiComponent = object;
      document.displayManager.setDocument(object);
    }
    catch (e:Error) {
      UncaughtErrorManager.instance.readDocumentErrorHandler(e, documentFactory);
      result.setRejected();
      return false;
    }
    finally {
      server.moduleForGetResourceBundle = null;
      document.displayManager.setStyleManagerForTalentAdobeEngineers(false);
    }

    documentFactory.document = document;
    result.setDone();
    return true;
  }

  private static function createStyleManager(document:Document, module:Module):void {
    if (module.librarySet.styleManager == null) {
      createStyleManagerForLibrarySet(module);
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

  private static function createCssReader(context:Module, styleManager:StyleManagerEx):CssReader {
    var c:Class = context.getClass("com.intellij.flex.uiDesigner.css.CssReaderImpl");
    var cssReader:CssReader = new c();
    cssReader.styleManager = styleManager;
    return cssReader;
  }

  private static function createChildStyleManager(context:Module):StyleManagerEx {
    return new (context.getClass("com.intellij.flex.uiDesigner.css.ChildStyleManager"))(context.styleManager);
  }
  
  private static function createStyleManagerForLibrarySet(module:Module):void {
    var inheritingStyleMapList:Vector.<Dictionary> = new Vector.<Dictionary>();
    var styleManagerClass:Class = module.getClass("com.intellij.flex.uiDesigner.css.RootStyleManager");
    module.librarySet.styleManager = new styleManagerClass(inheritingStyleMapList, new StyleValueResolverImpl(module));
    var cssReader:CssReader = createCssReader(module, module.styleManager);
    // FakeObjectProxy/FakeBooleanSetProxy/MergedCssStyleDeclaration find in list from 0 to end, then we add in list in reverse order
    // (because the library with index 4 overrides the library with index 2)
    var librarySet:LibrarySet = module.librarySet;
    do {
      var libraries:Vector.<Library> = librarySet.items;
      for (var j:int = libraries.length - 1; j > -1; j--) {
        var library:Library = libraries[j];
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

    cssReader.finalizeRead();
    inheritingStyleMapList.fixed = true;
  }

  private static function createStyleManagerForModule(module:Module):void {
    var styleManager:StyleManagerEx = createChildStyleManager(module);
    module.styleManager = styleManager;
    var cssReader:CssReader = createCssReader(module, styleManager);

    var localStyleHolder:LocalStyleHolder = module.localStyleHolders[0];
    cssReader.read(localStyleHolder.getStylesheet(module.project).rulesets, localStyleHolder.file);
    cssReader.finalizeRead();
  }

  private static function createStyleManagerForAppDocument(document:Document, module:Module):void {
    var styleManager:StyleManagerEx = createChildStyleManager(module);
    document.styleManager = styleManager;
    var cssReader:CssReader = createCssReader(module, styleManager);

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
    var systemManagerClass:Class = isPureFlash ? FlashDocumentDisplayManager : module.getClass("com.intellij.flex.uiDesigner.flex.FlexDocumentDisplayManager");
    var systemManager:DocumentDisplayManager = new systemManagerClass();
    document.displayManager = systemManager;

    if (!systemManager.sharedInitialized) {
      const stageForAdobeDummies:Stage = QueueLoader.stageForAdobeDummies;
      assert(stageForAdobeDummies != null, "Stage for Adobe dummies cannot be null");
      systemManager.initShared(stageForAdobeDummies, server, UncaughtErrorManager.instance);
    }
    systemManager.init(module.project.window.stage, isPureFlash ? null : new flexModuleFactoryClass(document.styleManager, module.applicationDomain), UncaughtErrorManager.instance,
                       MainFocusManagerSB(module.project.window.focusManager), document.documentFactory);
    document.container = new DocumentContainer(systemManager);
  }
}
}