package com.intellij.flex.uiDesigner {
import org.flyti.plexus.PlexusManager;
import org.jetbrains.EntityLists;

public class DocumentFactoryManager {
  private const factories:Vector.<DocumentFactory> = new Vector.<DocumentFactory>();
  
  private var server:Server;

  public function DocumentFactoryManager(server:Server) {
    this.server = server;
  }

  public static function getInstance():DocumentFactoryManager {
    return DocumentFactoryManager(PlexusManager.instance.container.lookup(DocumentFactoryManager));
  }

  public function get(id:int):DocumentFactory {
    return factories[id];
  }
  
  public function get2(id:int, requestor:DocumentFactory):DocumentFactory {
    var documentFactory:DocumentFactory = factories[id];
    documentFactory.addUser(requestor);
    return documentFactory;
  }

  public function register(factory:DocumentFactory):void {
    EntityLists.add(factories, factory);
  }
  
  public function unregister(document:Document):void {
    var factory:DocumentFactory = document.documentFactory;
    factory.document = null;
    if (document.displayManager != null) {
      document.displayManager.removeEventHandlers();
    }

    var deleted:Vector.<int> = new Vector.<int>();
    var id:int = unregister2(factory, deleted);
    if (id == -1) {
      return;
    }

    for each (var deletedIndex:int in deleted) {
      factories[deletedIndex] = null;
    }

    server.unregisterDocumentFactories(factory.module.project, deleted);
  }

  private function unregister2(factory:DocumentFactory, unregistered:Vector.<int>):int {
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
      else if (f != null && f.deleteUser(factory) && f.document == null) {
        unregister2(f, unregistered);
      }
    }

    assert(id != -1);
    unregistered[unregistered.length] = id;

    // clear module context document flex factory pool
    factory.module.context.removeDocumentFactory(id);

    return id;
  }

  //noinspection JSMethodCanBeStatic
  public function findElementAddress(object:Object, document:Document):ElementAddress {
    var factory:DocumentFactory = document.documentFactory;
    var id:int = factory.getObjectDeclarationRangeMarkerId(object);
    if (id == -1) {
      UncaughtErrorManager.instance.logWarning("Can't find document for object " + object + " in document " + document.file.presentableUrl);
      return null;
    }
    else {
      return new ElementAddress(factory, id);
    }
  }

  public function jumpToObjectDeclaration(object:Object, document:Document):void {
    var elementAddress:ElementAddress = findElementAddress(object, document);
    if (elementAddress != null) {
      server.openDocument(elementAddress.factory.module, elementAddress.factory, elementAddress.id);
    }
  }

  public function unregisterBelongToProject(project:Project):void {
    for (var i:int = 0, n:int = factories.length; i < n; i++) {
      var documentFactory:DocumentFactory = factories[i];
      if (documentFactory != null && documentFactory.module.project == project) {
        factories[i] = null;
      }
    }
  }
}
}
