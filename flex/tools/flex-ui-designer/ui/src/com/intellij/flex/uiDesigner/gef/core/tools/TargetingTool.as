package com.intellij.flex.uiDesigner.gef.core.tools {
import com.intellij.flex.uiDesigner.gef.core.EditPart;

public class TargetingTool extends Tool {
  private var target:EditPart;
  private var showingFeedback:Boolean;

  override public function deactivate():void {
    eraseTargetFeedback();
  }

  /**
   * Asks the current target editpart to erase target feedback using the target request. If target
   * feedback is not being shown, this method does nothing and returns. Otherwise, the target
   * feedback flag is reset to false, and the target editpart is asked to erase target feedback.
   * This methods should rarely be overridden.
   */
  protected function eraseTargetFeedback():void {
    if (showingFeedback) {
      showingFeedback = false;
      if (target != null) {
        var targetRequest:Request = getTargetRequest();
        targetRequest.setEraseFeedback(true);
        try {
          target.eraseTargetFeedback(targetRequest);
        }
        finally {
          targetRequest.setEraseFeedback(false);
        }
      }
    }
  }
}
}
