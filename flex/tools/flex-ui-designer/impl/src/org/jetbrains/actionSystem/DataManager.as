package org.jetbrains.actionSystem {
import flash.display.DisplayObject;
import flash.errors.IllegalOperationError;

import org.flyti.plexus.PlexusManager;

[Abstract]
public class DataManager {
  public static function get instance():DataManager {
    return DataManager(PlexusManager.instance.container.lookup(DataManager));
  }

  public function getDataContext(component:DisplayObject):DataContext {
    throw new IllegalOperationError();
  }

  //public function registerDataContext(component:DisplayObject, dataContext:DataContext):void {
  //  throw new IllegalOperationError();
  //}
  //
  //public function unregisterDataContext(component:DisplayObject):void {
  //  throw new IllegalOperationError();
  //}
}
}