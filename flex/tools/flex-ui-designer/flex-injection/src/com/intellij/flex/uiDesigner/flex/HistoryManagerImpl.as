package com.intellij.flex.uiDesigner.flex {
import mx.managers.IHistoryManager;
import mx.managers.IHistoryManagerClient;

public class HistoryManagerImpl implements IHistoryManager {
  private static var instance:HistoryManagerImpl;

  public static function getInstance():IHistoryManager {
    if (!instance) {
      instance = new HistoryManagerImpl();
    }

    return instance;
  }

  public function HistoryManagerImpl() {
  }

  public function register(obj:IHistoryManagerClient):void {
  }

  public function unregister(obj:IHistoryManagerClient):void {
  }

  public function save():void {
  }
}
}
