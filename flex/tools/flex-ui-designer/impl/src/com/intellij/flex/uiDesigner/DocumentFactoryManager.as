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

  public function register(factory:DocumentFactory):void {
    EntityLists.add(factories, factory);
  }
  
  public function unregister(document:Document):void {
    var factory:DocumentFactory = document.documentFactory;
    factory.document = null;
    if (document.displayManager != null) {
      document.displayManager.removeEventHandlers();
    }

    if (isReferenced(factory.id)) {
      return;
    }

    var unregistered:Vector.<int> = new Vector.<int>();
    unregister2(factory, unregistered);
    server.unregisterDocumentFactories(unregistered);
  }

  private function isReferenced(id:int):Boolean {
    for each (var otherFactory:DocumentFactory in factories) {
      if (otherFactory != null && otherFactory.isReferencedTo(id)) {
        // has other references, so, we don't unregister it
        return true;
      }
    }

    return false;
  }

  private function unregister2(factory:DocumentFactory, unregistered:Vector.<int>):void {
    unregistered[unregistered.length] = factory.id;
    factories[factory.id] = null;

    // clear module context document flex factory pool
    factory.module.context.removeDocumentFactory(factory.id);

    var documentReferences:Vector.<int> = factory.documentReferences;
    if (documentReferences == null || documentReferences.length == 0) {
      return;
    }

    // find factories, required only for this factory — we need delete them
    for each (var id:int in documentReferences) {
      var referenceFactory:DocumentFactory = factories.length > id ? factories[id] : null;
      if (referenceFactory != null && referenceFactory.document == null && !isReferenced(id)) {
        unregister2(referenceFactory, unregistered);
      }
    }
  }

  //noinspection JSMethodCanBeStatic
  public function findElementAddress(object:Object, document:Document):ElementAddress {
    var factory:DocumentFactory = document.documentFactory;
    const id:int = factory.getComponentDeclarationRangeMarkerId(object);
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
