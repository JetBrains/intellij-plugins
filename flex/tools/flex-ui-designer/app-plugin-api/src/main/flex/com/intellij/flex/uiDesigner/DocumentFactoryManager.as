package com.intellij.flex.uiDesigner {
public class DocumentFactoryManager {
  private const factories:Vector.<DocumentFactory> = new Vector.<DocumentFactory>();
  
  private var server:Server;

  public function DocumentFactoryManager(server:Server) {
    this.server = server;
  }

  public function get(id:int):DocumentFactory {
    return factories[id];
  }
  
  public function get2(id:int, requestor:DocumentFactory):DocumentFactory {
    var documentFactory:DocumentFactory = factories[id];
    documentFactory.addUser(requestor);
    return documentFactory;
  }

  public function register(id:int, factory:DocumentFactory):void {
    assert(id == factories.length || (id < factories.length && factories[id] == null));
    
    factories[id] = factory;
  }
  
  public function unregister(factory:DocumentFactory):void {
    factory.document = null;

    var deleted:Vector.<int> = new Vector.<int>();
    var id:int = unregister2(factory, deleted);
    if (id == -1) {
      return;
    }

    for each (var deletedIndex:int in deleted) {
      factories[deletedIndex] = null;
    }
    
    server.unregisterDocumentFactories(factory.module, deleted);
  }

  private function unregister2(factory:DocumentFactory, deleted:Vector.<int>):int {
    if (factory.hasUsers) {
      return -1;
    }

    var id:int;
    // find factories, required only for this factory — we need delete them
    for (var i:int = 0, n:int = factories.length; i < n; i++) {
      var f:DocumentFactory = factories[i];
      if (f == factory) {
        id = i;
      }
      else if (f != null) {
        if (f.deleteUser(factory) && f.document == null) {
          unregister2(f, deleted);
        }
      }
    }

    assert(id != -1);
    deleted[deleted.length] = id;
    return id;
  }
}
}
