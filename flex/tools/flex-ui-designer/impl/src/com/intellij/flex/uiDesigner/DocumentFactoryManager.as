package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;

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

  public function getById(id:int):DocumentFactory {
    return factories[id];
  }

  public function register(factory:DocumentFactory):void {
    EntityLists.add(factories, factory);
  }

  public function unregister(document:Document):void {
    var unregistered:Vector.<int> = new Vector.<int>();
    unregister3(document.documentFactory, false, unregistered);
    if (unregistered.length > 0) {
      server.unregisterDocumentFactories(unregistered);
    }
  }
  
  private function unregister3(factory:DocumentFactory, evenIfReferenced:Boolean, unregistered:Vector.<int>):void {
    if (factory.document != null) {
      factory.document.displayManager.removeEventHandlers();
      factory.document = null;
    }

    if (evenIfReferenced) {
      var dependents:Vector.<DocumentFactory> = getDependents(factory);
      if (dependents != null) {
        for each (var dependent:DocumentFactory in dependents) {
          // can be already unregistered
          if (factories[factory.id] != null) {
            unregister3(dependent, true, unregistered);
          }
        }
      }
    }
    else if (isReferenced(factory.id)) {
      return;
    }

    unregister2(factory, unregistered);
  }

  public function getDependents(factory:DocumentFactory):Vector.<DocumentFactory> {
    const id:int = factory.id;
    var dependents:Vector.<DocumentFactory> = new Vector.<DocumentFactory>(factories.length);
    var i:int = 0;
    for each (var otherFactory:DocumentFactory in factories) {
      if (otherFactory != null && otherFactory.isReferencedTo(id)) {
        dependents[i++] = otherFactory;
      }
    }

    if (i == 0) {
      return null;
    }

    dependents.length = i;
    dependents.fixed = true;
    return dependents;
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
    var flexLibrarySet:FlexLibrarySet = factory.module.context.flexLibrarySet;
    if (flexLibrarySet != null) {
      flexLibrarySet.documentUnregistered(factory.id);
    }

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

  private function findComponentDeclarationRangeMarkerId(object:Object, factory:DocumentFactory):ElementAddress {
    var id:int = factory.getComponentDeclarationRangeMarkerId(object);
    if (id != -1) {
      return new ElementAddress(factory, id);
    }

    if (factory.documentReferences != null) {
      for each (var subDocumentId:int in factory.documentReferences) {
        var addres:ElementAddress = findComponentDeclarationRangeMarkerId(object, getById(subDocumentId));
        if (addres != null) {
          return addres;
        }
      }
    }

    return null;
  }

  //noinspection JSMethodCanBeStatic
  public function findElementAddress(object:Object, document:Document):ElementAddress {
    var addres:ElementAddress = findComponentDeclarationRangeMarkerId(object, document.documentFactory);
    if (addres == null) {
      UncaughtErrorManager.instance.logWarning("Can't find document for object " + object + " in document " + document.file.presentableUrl);
    }

    return addres;
  }

  public function jumpToObjectDeclaration(object:Object, document:Document, focus:Boolean):void {
    var elementAddress:ElementAddress = findElementAddress(object, document);
    if (elementAddress != null) {
      server.openDocument(elementAddress.factory.module, elementAddress.factory, elementAddress.id, focus);
    }
  }

  public function unregisterBelongToProject(project:Project):void {
    for (var i:int = 0, n:int = factories.length; i < n; i++) {
      var documentFactory:DocumentFactory = factories[i];
      if (documentFactory != null && documentFactory.module.project == project) {
        // don't worry about dependents — dependent may be only from the same project — so, it will be deleted in any case
        factories[i] = null;
      }
    }
  }

  public function unregisterBelongToModule(module:Module):Vector.<int> {
    var unregistered:Vector.<int> = new Vector.<int>();
    for (var i:int = 0, n:int = factories.length; i < n; i++) {
      var documentFactory:DocumentFactory = factories[i];
      if (documentFactory != null && documentFactory.module == module) {
        unregister3(documentFactory, true, unregistered);
      }
    }

    return unregistered;
  }
}
}
