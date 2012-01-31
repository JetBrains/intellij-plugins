package com.intellij.flex.uiDesigner.gef.core {
import com.intellij.flex.uiDesigner.gef.core.tools.SelectionTool;
import com.intellij.flex.uiDesigner.gef.core.tools.Tool;

public class EditDomain {
  public function EditDomain(activeTool:Tool) {
    _activeTool = new SelectionTool();
  }

  private var _activeTool:Tool;
  public function get activeTool():Tool {
    return _activeTool;
  }

  public function set activeTool(value:Tool):void {
    _activeTool = value;
    if (activeTool != null) {
      activeTool.deactivate();
    }

    _activeTool = activeTool;

    if (activeTool != null) {
      activeTool.domain = this;
      activeTool.activate();
      // notify listeners
      //for (IActiveToolListener listener : m_eventTable.getListeners(IActiveToolListener.class)) {
      //  listener.toolActivated(m_activeTool);
      //}
      // handle auto reload tool and update cursor
      //if (currentViewer != null) {
      //  activeTool.viewer = currentViewer;
        //activeTool.refreshCursor();
        //activeTool.mouseMove(m_currentMouseEvent, m_currentViewer);
      //}
    }
  }
}
}
