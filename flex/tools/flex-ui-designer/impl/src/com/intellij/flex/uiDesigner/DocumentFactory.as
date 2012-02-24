package com.intellij.flex.uiDesigner {
import flash.utils.ByteArray;
import flash.utils.Dictionary;

import org.jetbrains.Identifiable;

public final class DocumentFactory implements SerializedDocumentDataProvider, DocumentReaderContext, Identifiable {
  public var module:Module;
  
  // not subdocument, only Document as tab in our UI
  public var document:Document;

  private const componentToRangeMarkerId:Dictionary = new Dictionary(true);

  public function DocumentFactory(id:int, data:ByteArray, file:VirtualFile, className:String, flags:int, module:Module) {
    _id = id;
    _data = data;
    _file = file;
    _className = className;
    _isApp = flags == 1;
    _isPureFlash = flags == 2;
    this.module = module;
  }

  private var _documentReferences:Vector.<int>;
  public function get documentReferences():Vector.<int> {
    return _documentReferences;
  }

  public function set documentReferences(value:Vector.<int>):void {
    _documentReferences = value;
  }

  public function isReferencedTo(id:int):Boolean {
    return documentReferences != null && documentReferences.indexOf(id) != -1;
  }

  private var _isApp:Boolean;
  public function get isApp():Boolean {
    return _isApp;
  }

  private var _isPureFlash:Boolean;
  public function get isPureFlash():Boolean {
    return _isPureFlash;
  }

  private var _id:int;
  public function get id():int {
    return _id;
  }
  
  private var _className:String;
  public function get className():String {
    return _className;
  }

  private var _data:ByteArray;
  public function get data():ByteArray {
    return _data;
  }

  private var _file:VirtualFile;
  public function get file():VirtualFile {
    return _file;
  }

  public function get moduleContext():ModuleContext {
    return module.context;
  }

  public function registerComponentDeclarationRangeMarkerId(component:Object, id:int):void {
    componentToRangeMarkerId[component] = id;
  }

  public function getComponent(id:int):Object {
    for (var component:Object in componentToRangeMarkerId) {
      if (componentToRangeMarkerId[component] == id) {
        return component;
      }
    }

    return null;
  }

  public function getComponentDeclarationRangeMarkerId(object:Object):int {
    var r:* = componentToRangeMarkerId[object];
    return r === undefined ? -1 : r;
  }

  public function registerObjectWithId(id:String, object:Object):void {
    // empty, actual only for FlexDocumentFactory
  }

  public function get instanceForRead():Object {
    return null;
  }
}
}
