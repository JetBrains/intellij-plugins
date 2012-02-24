package com.intellij.flex.uiDesigner.mxml {
import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.events.Event;

public class FlexMxmlReader extends MxmlReader {
  private var systemManager:DocumentDisplayManager;

  public function FlexMxmlReader(systemManager:DocumentDisplayManager = null) {
    this.systemManager = systemManager;
  }

  override protected function beforeReadRootObjectProperties(object:Object, isDocumentLevel:Boolean):void {
    // perfomance, early set document, avoid recursive set later (see UIComponent.document setter)
    if (isDocumentLevel) {
      // may be spark.primitives.supportClasses.GraphicElement
      if ("document" in object) {
        object.document = object;
      }
    }

    rootObject = object;

    // see flex 4.5 Application splashScreenImage setter —
    // systemManager may be quiered while read (i. e. before documentDisplayManager.setDocument)
    if ("systemManager" in object) {
      object.systemManager = systemManager;
    }
  }

  private function getMxNs():Namespace {
    return Namespace(moduleContext.applicationDomain.getDefinition("mx.core.mx_internal"));
  }

  override protected function registerEffect(propertyName:String, object:Object):void {
    moduleContext.effectManagerClass[new QName(getMxNs(), "setStyle")](propertyName, object);
  }

  override protected function readChildrenMxContainer(container:DisplayObjectContainer):void {
    const length:int = input.readUnsignedShort();
    var array:Array = new Array(length);
    var mxNs:Namespace = getMxNs();
    container[new QName(mxNs, "setActualCreationPolicies")]("none");
    container[new QName(mxNs, "createdComponents")] = array;

    readArrayOrVector(array, length);

    container.addEventListener("preinitialize", mxContainerPreinitializeHandler);
  }

  private static function createMxContainerChildren(container:DisplayObjectContainer, createdComponentsQName:QName,
                                                    numChildrenCreatedQName:QName, flexEventClass:Class, controlBarClass:Class,
                                                    isPanel:Boolean):void {
    var chidlren:Array = container[createdComponentsQName];
    for (var i:int = 0, n:int = chidlren.length == 1 ? 1 : chidlren.length - 1; i < n; i++) {
      container.addChild(chidlren[i]);
    }

    if (chidlren.length > 1) {
      var lastChild:DisplayObject = chidlren[i];
      if (isPanel && lastChild is controlBarClass) {
        container["rawChildren"].addChild(lastChild);
        container["setControlBar"](lastChild);
      }
      else {
        container.addChild(lastChild);
      }
    }

    container["processedDescriptors"] = true;
    container[numChildrenCreatedQName] = chidlren.length;
    container.dispatchEvent(new flexEventClass("contentCreationComplete"));
  }

  private static function mxContainerPreinitializeHandler(event:Event):void {
    var container:DisplayObjectContainer = DisplayObjectContainer(event.currentTarget);
    container.removeEventListener("preinitialize", mxContainerPreinitializeHandler);
    var sm:DocumentDisplayManager = DocumentDisplayManager(Object(container).systemManager);
    var mxNs:Namespace = Namespace(sm.getDefinitionByName("mx.core.mx_internal"));
    createMxContainerChildren(container, new QName(mxNs, "createdComponents"), new QName(mxNs, "numChildrenCreated"),
                              Class(sm.getDefinitionByName("mx.events.FlexEvent")),
                              Class(sm.getDefinitionByName("mx.containers.ControlBar")),
                              container is Class(sm.getDefinitionByName("mx.containers.Panel")));
  }

}
}
