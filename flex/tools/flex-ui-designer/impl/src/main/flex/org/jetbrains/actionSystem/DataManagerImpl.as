package org.jetbrains.actionSystem {
import flash.display.DisplayObject;
import flash.utils.Dictionary;

// currently, only stage, it is enough for us
public class DataManagerImpl extends DataManager {
  private const map:Dictionary = new Dictionary(true);

  override public function getDataContext(component:DisplayObject):DataContext {
    return map[component.stage];
  }

  override public function registerDataContext(component:DisplayObject, dataContext:DataContext):void {
    map[component.stage] = dataContext;
  }

  override public function unregisterDataContext(component:DisplayObject):void {
    map[component.stage] = null;
  }
}
}
