package com.intellij.flex.uiDesigner {
import flash.utils.ByteArray;
import flash.utils.Dictionary;

public class DocumentFactory implements SerializedDocumentDataProvider, DocumentReaderContext {
  public var module:Module;
  
  // not subdocument, only Document as tab in our UI
  public var document:Document;

  private const objectDeclarationPositionMap:Dictionary = new Dictionary(true);
  
  public function DocumentFactory(id:int, data:ByteArray, file:VirtualFile, className:String, module:Module) {
    _id = id;
    _data = data;
    _file = file;
    _className = className;
    this.module = module;
  }
  
  private var _users:Vector.<DocumentFactory>;
  public function get users():Vector.<DocumentFactory> {
    return _users;
  }
  
  public function addUser(user:DocumentFactory):void {
    if (_users == null) {
      _users = new Vector.<DocumentFactory>();
    }
    
    _users[_users.length] = user;
  }

  public function deleteUser(user:DocumentFactory):Boolean {
    var index:int = _users == null ? -1 : _users.indexOf(user);
    if (index == -1) {
      return false;
    }
    else {
      _users.splice(index, 1);
      return true;
    }
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

  public function get hasUsers():Boolean {
    return _users != null && _users.length > 0;
  }

  public function registerObjectDeclarationPosition(object:Object, textOffset:int):void {
    objectDeclarationPositionMap[object] = textOffset;
  }

  public function getObjectDeclarationPosition(object:Object):int {
    var r:* = objectDeclarationPositionMap[object];
    if (r === undefined) {
      return -1;
    }
    else {
      return r;
    }
  }
}
}
