package org.jetbrains.actionSystem {
public class DataKey {
  function DataKey(name:String) {
    _name = name;
  }
  
  private var _name:String;
  public function get name():String {
    return _name;
  }

  public function getData(dataContext:DataContext):* {
    return dataContext.getData(_name);
  }
}
}
