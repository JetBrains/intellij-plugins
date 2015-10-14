package com.intellij.flex.uiDesigner.gef.core {
import com.intellij.flex.uiDesigner.gef.core.policies.EditPolicy;

[Abstract]
public class EditPart {
  /**
   * Used to indicate no selection.
   */
  public static const SELECTED_NONE:int = 0;
  /**
   * Used to indicate non-primary selection.
   */
  public static const SELECTED:int = 1;
  /**
   * Used to indicate primary selection, or "Anchor" selection. Primary selection is defined as the
   * last object selected.
   */
  public static const SELECTED_PRIMARY:int = 2;

  private var children:Vector.<EditPart>;
  private var isActive:Boolean;
  private const policies:Vector.<EditPolicy> = new Vector.<EditPolicy>();

  public function activate():void {
    isActive = true;
    activateEditPolicies();
    for each (var editPart:EditPart in children) {
      editPart.activate();
    }
  }

  private function activateEditPolicies():void {
    for each (var editPolicy:EditPolicy in policies) {
      editPolicy.activate();
    }
  }
}
}