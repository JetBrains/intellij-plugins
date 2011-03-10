package com.intellij.flex.uiDesigner {
import com.intellij.flex.uiDesigner.css.LocalStyleHolder;
import com.intellij.flex.uiDesigner.css.StyleManagerEx;

public final class Module {
  public function Module(id:int, project:Project, librarySets:Vector.<LibrarySet>, localStyleHolders:Vector.<LocalStyleHolder>) {
    _localStyleHolders = localStyleHolders;
    _project = project;
    _context = new ModuleContextImpl(librarySets);

    _id = id;
  }

  private var _id:int;
  public function get id():int {
    return _id;
  }

  private var _project:Project;
  public function get project():Project {
    return _project;
  }
  
  
  private var _context:ModuleContextImpl;
  public function get context():ModuleContextEx {
    return _context;
  }

  public function get librarySets():Vector.<LibrarySet> {
    return _context.librarySets;
  }
  
  public function getClass(fqn:String):Class {
    return _context.getClass(fqn);
  }

  private var _styleManager:StyleManagerEx;
  public function get styleManager():StyleManagerEx {
    return _styleManager == null ? _context.styleManager : _styleManager;
  }
  public function set styleManager(styleManager:StyleManagerEx):void {
    _styleManager = styleManager;
  }
  
  public function get hasOwnStyleManager():Boolean {
    return _styleManager != null;
  }

  private var _localStyleHolders:Vector.<LocalStyleHolder>;
  public function get localStyleHolders():Vector.<LocalStyleHolder> {
    return _localStyleHolders;
  }
}
}