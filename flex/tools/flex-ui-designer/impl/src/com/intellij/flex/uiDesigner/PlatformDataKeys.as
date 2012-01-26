package com.intellij.flex.uiDesigner {
public final class PlatformDataKeys {
  public static var PROJECT:ProjectDataKey;
  public static var DOCUMENT:DocumentDataKey;
  public static var COMPONENT:ComponentDataKey;

  internal static function burnInHellAdobe():void {
    PROJECT = new ProjectDataKey();
    DOCUMENT = new DocumentDataKey();
    COMPONENT = new ComponentDataKey();
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

final class ComponentDataKey extends DataKey {
  public function getData(dataContext:DataContext):Object {
    return dataContext.getData(this);
  }
}