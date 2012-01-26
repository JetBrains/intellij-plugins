package org.jetbrains.actionSystem {
import cocoa.plaf.Skin;

import flash.display.DisplayObject;

// currently, only stage, it is enough for us
public class DataManagerImpl extends DataManager {
  override public function getDataContext(component:DisplayObject):DataContext {
    var dataContext:DataContext;
    var p:DisplayObject = component;
    while (p != null) {
      if (p is DataContextProvider) {
        if ((dataContext = DataContextProvider(p).dataContext) != null) {
          return dataContext;
        }
      }
      else if (p is Skin && Skin(p).component is DataContextProvider) {
        return DataContextProvider(Skin(p).component).dataContext;
      }

      p = p.parent;
    }

    throw new Error("dataContext not found");
  }

  //override public function registerDataContext(component:DisplayObject, dataContext:DataContext):void {
  //  map[component.stage] = dataContext;
  //}

  //override public function unregisterDataContext(component:DisplayObject):void {
  //  map[component.stage] = null;
  //}
}
}
