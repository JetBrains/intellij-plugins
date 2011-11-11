package org.jetbrains {
import flash.errors.IllegalOperationError;

public final class EntityLists {
  public static function add(list:Object, entity:Identifiable):void {
    var id:int = entity.id;
    const size:int = list.length;
    if (id < size) {
      if (list[id] != null) {
        throw new IllegalOperationError("Cannot add " + entity + " to " + list + " because id " + id + " is not free");
      }
    }
    else if (id > size) {
      throw new IllegalOperationError("Cannot add " + entity + " to " + list + " because id " + id + " is greater than list size (" + size + ")");
    }

    list[id] = entity;
  }
}
}