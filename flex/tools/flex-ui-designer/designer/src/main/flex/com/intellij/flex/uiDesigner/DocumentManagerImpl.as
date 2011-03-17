package com.intellij.flex.uiDesigner {
import cocoa.DocumentWindow;

import com.intellij.flex.uiDesigner.css.CssReader;
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.flex.DocumentContainer;
import com.intellij.flex.uiDesigner.flex.DocumentReader;
import com.intellij.flex.uiDesigner.flex.ProjectView;
import com.intellij.flex.uiDesigner.flex.StyleValueResolverImpl;

import flash.display.DisplayObject;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.Dictionary;

public class DocumentManagerImpl extends EventDispatcher implements DocumentManager {
  private const pathMap:Dictionary = new Dictionary();

  private var libraryManager:LibraryManager;

  private var documentReader:DocumentReader;

  public function DocumentManagerImpl(libraryManager:LibraryManager, documentReader:DocumentReader) {
    this.libraryManager = libraryManager;
    this.documentReader = documentReader;
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
    var file:VirtualFile = documentFactory.file;
    var document:Document = pathMap[file];
    if (document == null) {
      libraryManager.resolve(documentFactory.module.librarySets, doOpenAfterResolveLibraries, documentFactory);
    }
    else {
      doOpen(documentFactory, document);
      document.container.invalidateDisplayList();
    }
  }

  private function doOpenAfterResolveLibraries(documentFactory:DocumentFactory):void {
    var document:Document = new Document(documentFactory);
    createStyleManager(documentFactory.module);
    createSystemManager(document, documentFactory.module);
    pathMap[documentFactory.file] = document;

    doOpen(documentFactory, document);
  }

  private function doOpen(documentFactory:DocumentFactory, document:Document):void {
    var object:Object = documentReader.read(documentFactory.data, document.file, document.styleManager, document.module.context);
    document.uiComponent = object;
    document.systemManager.setUserDocument(DisplayObject(object));

    documentReader.createDeferredMxContainersChildren(document.module.context.applicationDomain);
    this.document = document;
  }

  private function createStyleManager(module:Module):void {
    if (module.context.styleManager == null) {
      createStyleManagerForContext(module.context);
    }
    if (!module.hasOwnStyleManager && module.localStyleHolders != null) {
      createStyleManagerForModule(module);
    }
  }
  
  private function createStyleManagerForContext(context:ModuleContextEx):void {
    var inheritingStyleMapList:Vector.<Dictionary> = new Vector.<Dictionary>();
    var styleManagerClass:Class = context.getClass("com.intellij.flex.uiDesigner.css.RootStyleManager");
    context.styleManager = new styleManagerClass(inheritingStyleMapList, new StyleValueResolverImpl(context.applicationDomain));
    var cssReaderClass:Class = context.getClass("com.intellij.flex.uiDesigner.css.CssReaderImpl");
    var cssReader:CssReader = new cssReaderClass();
    cssReader.styleManager = context.styleManager; 
    // FakeObjectProxy/FakeBooleanSetProxy/MergedCssStyleDeclaration ищут в списке от 0 к концу, поэтому мы добавляем в список в обратном порядке
    // (потому что библиотека с индексом 4 переопределяет значение библиотеки с индексом 2)
    var librarySets:Vector.<LibrarySet> = context.librarySets;
    for (var i:int = librarySets.length - 1; i > -1; i--) {
      var libraries:Vector.<Library> = librarySets[i].libraries;
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

    cssReader.finalizeRead();
    inheritingStyleMapList.fixed = true;
  }
  
  private function createStyleManagerForModule(module:Module):void {
    var styleManagerClass:Class = module.getClass("com.intellij.flex.uiDesigner.css.ChildStyleManager");
    module.styleManager = new styleManagerClass(module.context.styleManager);
    var cssReaderClass:Class = module.getClass("com.intellij.flex.uiDesigner.css.CssReaderImpl");
    var cssReader:CssReader = new cssReaderClass();
    cssReader.styleManager = module.styleManager;
    var localStyleHolder:LocalStyleHolder = module.localStyleHolders[0];
    cssReader.read(localStyleHolder.stylesheet.rulesets, localStyleHolder.file);
    cssReader.finalizeRead();
  }

  private function createSystemManager(document:Document, module:Module):void {
    var project:Project = module.project;
    var window:DocumentWindow = project.window;
    var projectView:ProjectView;
    if (window == null) {
      window = new DocumentWindow(new ProjectView(), module.project.map);
      window.title = project.name;
      project.window = window;
    }
    
    projectView = ProjectView(window.contentView);

    var flexModuleFactoryClass:Class = module.getClass("com.intellij.flex.uiDesigner.flex.FlexModuleFactory");
    var systemManagerClass:Class = module.getClass("com.intellij.flex.uiDesigner.flex.SystemManager");
    document.systemManager = new systemManagerClass(new flexModuleFactoryClass(module.styleManager, module.context.applicationDomain));
    document.container = new DocumentContainer(Sprite(document.systemManager));
    projectView.addDocument(document);
  }
}
}