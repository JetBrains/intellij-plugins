package mx.managers {
import com.intellij.flex.uiDesigner.UiErrorHandler;

import flash.display.DisplayObject;
import flash.events.Event;
import flash.events.EventDispatcher;
import flash.utils.getDefinitionByName;

import mx.core.UIComponentGlobals;
import mx.core.mx_internal;
import mx.events.FlexEvent;
import mx.managers.layoutClasses.PriorityQueue;

use namespace mx_internal;

public class LayoutManager extends EventDispatcher implements ILayoutManager {
  private var uiErrorHandler:UiErrorHandler;
  private var displayDispatcher:DisplayObject;

  private var updateCompleteQueue:PriorityQueue = new PriorityQueue();
  private var invalidatePropertiesQueue:PriorityQueue = new PriorityQueue();

  private var invalidatePropertiesFlag:Boolean = false;
  private var invalidateClientPropertiesFlag:Boolean = false;

  private var invalidateSizeQueue:PriorityQueue = new PriorityQueue();
  private var invalidateSizeFlag:Boolean = false;
  private var invalidateClientSizeFlag:Boolean = false;

  private var invalidateDisplayListQueue:PriorityQueue = new PriorityQueue();
  private var invalidateDisplayListFlag:Boolean = false;

  private var waitedAFrame:Boolean = false;
  private var listenersAttached:Boolean = false;

  private var targetLevel:int = int.MAX_VALUE;

  private var currentObject:ILayoutManagerClient;

  private var fteTextFieldClass:Object;

  public function LayoutManager(displayDispatcher:DisplayObject, uiErrorHandler:UiErrorHandler):void {
    this.displayDispatcher = displayDispatcher;
    this.uiErrorHandler = uiErrorHandler;

    fteTextFieldClass = getDefinitionByName("mx.core.FTETextField") as Class;
    if (fteTextFieldClass != null) {
      fteTextFieldClass.staticHandlersAdded = true;
    }
  }

  public function getCurrentObject():Object {
    return currentObject;
  }

  public function waitFrame():void {
    waitedAFrame = false;
  }

  // // librarySet.applicationDomain.getDefinition("mx.core.UIComponentGlobals").layoutManager = null is not working
  public static function prepareToDie():void {
    LayoutManager(UIComponentGlobals.layoutManager).doPrepareToDie();
    UIComponentGlobals.layoutManager = null;
  }

  private function doPrepareToDie():void {
    if (listenersAttached) {
      if (!waitedAFrame) {
        displayDispatcher.removeEventListener(Event.ENTER_FRAME, waitAFrame);
      }
      else {
        removeListeners();
      }
      listenersAttached = true;
    }

    invalidatePropertiesFlag = false;
    invalidateClientPropertiesFlag = false;
    invalidateSizeFlag = false;
    invalidateDisplayListFlag = false;

    currentObject = null;
    displayDispatcher = null;
  }

  //noinspection JSUnusedGlobalSymbols
  public static function getInstance():LayoutManager {
    return LayoutManager(UIComponentGlobals.layoutManager);
  }

  public function get usePhasedInstantiation():Boolean {
    return false;
  }
  public function set usePhasedInstantiation(value:Boolean):void {
  }

  public function invalidateProperties(obj:ILayoutManagerClient):void {
    if (!invalidatePropertiesFlag) {
      invalidatePropertiesFlag = true;

      if (!listenersAttached) {
        attachListeners();
      }
    }

    if (targetLevel <= obj.nestLevel) {
      invalidateClientPropertiesFlag = true;
    }

    invalidatePropertiesQueue.addObject(obj, obj.nestLevel);
  }

  public function invalidateSize(obj:ILayoutManagerClient):void {
    if (!invalidateSizeFlag) {
      invalidateSizeFlag = true;

      if (!listenersAttached) {
        attachListeners();
      }
    }

    if (targetLevel <= obj.nestLevel) {
      invalidateClientSizeFlag = true;
    }

    invalidateSizeQueue.addObject(obj, obj.nestLevel);
  }

  public function invalidateDisplayList(obj:ILayoutManagerClient):void {
    if (!invalidateDisplayListFlag) {
      invalidateDisplayListFlag = true;

      if (!listenersAttached) {
        attachListeners();
      }
    }

    invalidateDisplayListQueue.addObject(obj, obj.nestLevel);
  }

  private function validateProperties():void {
    var obj:ILayoutManagerClient = ILayoutManagerClient(invalidatePropertiesQueue.removeSmallest());
    while (obj) {
      if (obj.nestLevel) {
        currentObject = obj;
        try {
        obj.validateProperties();

        if (!obj.updateCompletePendingFlag) {
          updateCompleteQueue.addObject(obj, obj.nestLevel);
          obj.updateCompletePendingFlag = true;
        }
      }
        catch (e:Error) {
          uiErrorHandler.handleUiError(e, obj, "Can't validate properties " + obj);
          invalidateSizeQueue.removeChild(obj);
          invalidateDisplayListQueue.removeChild(obj);
        }
      }

      // Once we start, don't stop.
      obj = ILayoutManagerClient(invalidatePropertiesQueue.removeSmallest());
    }

    if (invalidatePropertiesQueue.isEmpty()) {
      invalidatePropertiesFlag = false;
    }
  }

  private function validateSize():void {
    var obj:ILayoutManagerClient = ILayoutManagerClient(invalidateSizeQueue.removeLargest());
    while (obj) {
      if (obj.nestLevel) {
        currentObject = obj;
        try {
          obj.validateSize();

          if (!obj.updateCompletePendingFlag) {
            updateCompleteQueue.addObject(obj, obj.nestLevel);
            obj.updateCompletePendingFlag = true;
          }
        }
        catch (e:Error) {
          uiErrorHandler.handleUiError(e, currentObject, "Can't validate size " + obj);
          invalidateDisplayListQueue.removeChild(obj);
        }
      }

      obj = ILayoutManagerClient(invalidateSizeQueue.removeLargest());
    }

    if (invalidateSizeQueue.isEmpty()) {
      invalidateSizeFlag = false;
    }
  }

  private function validateDisplayList():void {
    var obj:ILayoutManagerClient = ILayoutManagerClient(invalidateDisplayListQueue.removeSmallest());
    while (obj) {
      if (obj.nestLevel) {
        currentObject = obj;
        try {
        obj.validateDisplayList();

        if (!obj.updateCompletePendingFlag) {
          updateCompleteQueue.addObject(obj, obj.nestLevel);
          obj.updateCompletePendingFlag = true;
        }
      }
        catch (e:Error) {
          uiErrorHandler.handleUiError(e, currentObject, "Can't validate display list " + obj);
        }
      }

      // Once we start, don't stop.
      obj = ILayoutManagerClient(invalidateDisplayListQueue.removeSmallest());
    }

    if (invalidateDisplayListQueue.isEmpty()) {
      invalidateDisplayListFlag = false;
    }
  }

  public function validateClient(target:ILayoutManagerClient, skipDisplayList:Boolean = false):void {
    var lastCurrentObject:ILayoutManagerClient = currentObject;

    var obj:ILayoutManagerClient;
    var done:Boolean = false;
    var oldTargetLevel:int = targetLevel;

    try {
    //  the theory here is that most things that get validated are deep in the tree
    // and so there won't be nested calls to validateClient.  However if there is,
    // we don't want to have a more sophisticated scheme of keeping track
    // of dirty flags at each level that is being validated, but we definitely
    // do not want to keep scanning the queues unless we're pretty sure that
    // something might be dirty so we just say that if something got dirty
    // during this call at a deeper nesting than the first call to validateClient
    // then we'll scan the queues.  So we only change targetLevel if we're the
    // outer call to validateClient and only that call restores it.
    if (targetLevel == int.MAX_VALUE) {
      targetLevel = target.nestLevel;
    }

    while (!done) {
      // assume we won't find anything
      done = true;

      // Keep traversing the invalidatePropertiesQueue until we've reached the end.
      // More elements may get added to the queue while we're in this loop, or a
      // a recursive call to this function may remove elements from the queue while
      // we're in this loop.
      obj = ILayoutManagerClient(invalidatePropertiesQueue.removeSmallestChild(target));
      while (obj) {
        // trace("LayoutManager calling validateProperties() on " + Object(obj) + " " + DisplayObject(obj).width + " " + DisplayObject(obj).height);

        if (obj.nestLevel) {
          currentObject = obj;
          obj.validateProperties();
          if (!obj.updateCompletePendingFlag) {
            updateCompleteQueue.addObject(obj, obj.nestLevel);
            obj.updateCompletePendingFlag = true;
          }
        }

        // Once we start, don't stop.
        obj = ILayoutManagerClient(invalidatePropertiesQueue.removeSmallestChild(target));
      }

      if (invalidatePropertiesQueue.isEmpty()) {
        invalidatePropertiesFlag = false;
        invalidateClientPropertiesFlag = false;
      }

      obj = ILayoutManagerClient(invalidateSizeQueue.removeLargestChild(target));
      while (obj) {
        // trace("LayoutManager calling validateSize() on " + Object(obj));

        if (obj.nestLevel) {
          currentObject = obj;
          obj.validateSize();
          if (!obj.updateCompletePendingFlag) {
            updateCompleteQueue.addObject(obj, obj.nestLevel);
            obj.updateCompletePendingFlag = true;
          }
        }

        if (invalidateClientPropertiesFlag) {
          // did any properties get invalidated while validating size?
          obj = ILayoutManagerClient(invalidatePropertiesQueue.removeSmallestChild(target));
          if (obj) {
            // re-queue it. we'll pull it at the beginning of the loop
            invalidatePropertiesQueue.addObject(obj, obj.nestLevel);
            done = false;
            break;
          }
        }

        obj = ILayoutManagerClient(invalidateSizeQueue.removeLargestChild(target));
      }

      if (invalidateSizeQueue.isEmpty()) {
        invalidateSizeFlag = false;
        invalidateClientSizeFlag = false;
      }

      if (!skipDisplayList) {
        obj = ILayoutManagerClient(invalidateDisplayListQueue.removeSmallestChild(target));
        while (obj) {
          if (obj.nestLevel) {
            currentObject = obj;
            obj.validateDisplayList();
            if (!obj.updateCompletePendingFlag) {
              updateCompleteQueue.addObject(obj, obj.nestLevel);
              obj.updateCompletePendingFlag = true;
            }
          }
          // trace("LayoutManager return from validateDisplayList on " + Object(obj) + " " + DisplayObject(obj).width + " " + DisplayObject(obj).height);

          if (invalidateClientPropertiesFlag) {
            // did any properties get invalidated while validating size?
            obj = ILayoutManagerClient(invalidatePropertiesQueue.removeSmallestChild(target));
            if (obj) {
              // re-queue it. we'll pull it at the beginning of the loop
              invalidatePropertiesQueue.addObject(obj, obj.nestLevel);
              done = false;
              break;
            }
          }

          if (invalidateClientSizeFlag) {
            obj = ILayoutManagerClient(invalidateSizeQueue.removeLargestChild(target));
            if (obj) {
              // re-queue it. we'll pull it at the beginning of the loop
              invalidateSizeQueue.addObject(obj, obj.nestLevel);
              done = false;
              break;
            }
          }

          // Once we start, don't stop.
          obj = ILayoutManagerClient(invalidateDisplayListQueue.removeSmallestChild(target));
        }

        if (invalidateDisplayListQueue.isEmpty()) {
          invalidateDisplayListFlag = false;
        }
      }
    }

    if (oldTargetLevel == int.MAX_VALUE) {
      targetLevel = int.MAX_VALUE;
      if (!skipDisplayList) {
        obj = ILayoutManagerClient(updateCompleteQueue.removeLargestChild(target));
        while (obj) {
          if (!obj.initialized) {
            obj.initialized = true;
          }

          if (obj.hasEventListener(FlexEvent.UPDATE_COMPLETE)) {
            obj.dispatchEvent(new FlexEvent(FlexEvent.UPDATE_COMPLETE));
          }
          obj.updateCompletePendingFlag = false;
          obj = ILayoutManagerClient(updateCompleteQueue.removeLargestChild(target));
        }
      }
    }
    }
    catch (e:Error) {
      uiErrorHandler.handleUiError(e, obj, "Can't validate " + obj);
    }

    currentObject = lastCurrentObject;
  }

  public function isInvalid():Boolean {
    return invalidatePropertiesFlag || invalidateSizeFlag || invalidateDisplayListFlag;
  }

  private function waitAFrame(event:Event):void {
    displayDispatcher.removeEventListener(Event.ENTER_FRAME, waitAFrame);
    displayDispatcher.addEventListener(Event.ENTER_FRAME, doPhasedInstantiationCallback);
    waitedAFrame = true;
  }

  private function attachListeners():void {
    if (!waitedAFrame) {
      displayDispatcher.addEventListener(Event.ENTER_FRAME, waitAFrame);
    }
    else {
      displayDispatcher.addEventListener(Event.ENTER_FRAME, doPhasedInstantiationCallback);
      displayDispatcher.addEventListener(Event.RENDER, doPhasedInstantiationCallback);
      displayDispatcher.stage.invalidate();
    }

    listenersAttached = true;
  }

  private function doPhasedInstantiationCallback(event:Event):void {
    if (UIComponentGlobals.callLaterSuspendCount > 0) {
      return;
    }

    removeListeners();
    tryDoPhasedInstantiation();
    currentObject = null;
  }

  private function removeListeners():void {
    displayDispatcher.removeEventListener(Event.ENTER_FRAME, doPhasedInstantiationCallback);
    displayDispatcher.removeEventListener(Event.RENDER, doPhasedInstantiationCallback);
  }

  private function tryDoPhasedInstantiation():void {
    try {
      doPhasedInstantiation();

      if (fteTextFieldClass != null) {
        fteTextFieldClass.staticRenderHandler(null);
      }
    }
    catch (e:Error) {
      uiErrorHandler.handleUiError(e, currentObject, currentObject == null ? null : "Can't do phased instantiation " + currentObject);
    }
  }

  private function doPhasedInstantiation():void {
    if (invalidatePropertiesFlag) {
      validateProperties();
    }

    if (invalidateSizeFlag) {
      validateSize();
    }

    if (invalidateDisplayListFlag) {
      validateDisplayList();
    }

    if (invalidatePropertiesFlag || invalidateSizeFlag || invalidateDisplayListFlag) {
      attachListeners();
    }
    else {
      listenersAttached = false;
      var obj:ILayoutManagerClient = ILayoutManagerClient(updateCompleteQueue.removeLargest());
      while (obj) {
        if (!obj.initialized && obj.processedDescriptors) {
          obj.initialized = true;
        }
        if (obj.hasEventListener(FlexEvent.UPDATE_COMPLETE)) {
          obj.dispatchEvent(new FlexEvent(FlexEvent.UPDATE_COMPLETE));
        }
        obj.updateCompletePendingFlag = false;
        obj = ILayoutManagerClient(updateCompleteQueue.removeLargest());
      }

      if (hasEventListener(FlexEvent.UPDATE_COMPLETE)) {
        dispatchEvent(new FlexEvent(FlexEvent.UPDATE_COMPLETE));
      }
    }
  }

  public function validateNow():void {
    var infiniteLoopGuard:int = 0;
    while (listenersAttached && infiniteLoopGuard++ < 100) {
      tryDoPhasedInstantiation();
    }
  }
}
}