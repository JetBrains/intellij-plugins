package com.intellij.flex.uiDesigner {
public final class PlatformDataKeys {
  ProjectDataKey;

  public static var PROJECT:ProjectDataKey;
  public static var DOCUMENT:DocumentDataKey = null;
  public static var ELEMENT:ElementDataKey = null;

  public static function burnInHellAdobe():void {
    PROJECT = new ProjectDataKey();
  }
}

PlatformDataKeys.burnInHellAdobe();
}

import com.intellij.flex.uiDesigner.Document;
import com.intellij.flex.uiDesigner.Project;

import org.jetbrains.actionSystem.DataContext;
import org.jetbrains.actionSystem.DataKey;

class ProjectDataKey extends DataKey {
  public function getData(dataContext:DataContext):Project {
    return Project(dataContext.getData(this));
  }
}

final class DocumentDataKey extends DataKey {
  public function getData(dataContext:DataContext):Document {
    return Document(dataContext.getData(this));
  }
}

final class ElementDataKey extends DataKey {
  public function getData(dataContext:DataContext):Object {
    return dataContext.getData(this);
  }
}