package com.intellij.flex.uiDesigner.gef.core.tools {
import com.intellij.flex.uiDesigner.gef.core.*;

[Abstract]
public class Tool {
  /**
   * The final state for a tool to be in. Once a tool reaches this state, it will not change states
   * until it is activated() again.
   */
  protected static const STATE_NONE:int = 0;
  /**
   * The first state that a tool is in. The tool will generally be in this state immediately
   * following {@link #activate()}.
   */
  protected static const STATE_INIT:int = 1;
  /**
   * The state indicating that one or more buttons is pressed, but the user has not moved past the
   * drag threshold. Many tools will do nothing during this state but wait until
   * {@link #STATE_DRAG_IN_PROGRESS} is entered.
   */
  protected static const STATE_DRAG:int = 2;

  private var active:Boolean;
  protected var state:int;

  private var operationSet:Vector.<EditPart>;

  //private var defaultCursor:Cursor;
  //private var disabledCursor:Cursor;

  /**
   * Called when this tool becomes the active tool for the {@link EditDomain}. Implementors can
   * perform any necessary initialization here.
   */
  public function activate():void {
    resetState();
    state = STATE_INIT;
    active = true;
  }

  /**
   * Called when another Tool becomes the active tool for the {@link EditDomain}. Implementors can
   * perform state clean-up or to free resources.
   */
  public function deactivate():void {
    active = false;
    command = null;
    operationSet = null;
  }

  private var _command:Command;
  protected function set command(value:Command):void {
    _command = value;
    //refreshCursor();
  }

  private var _domain:EditDomain;
  public function set domain(value:EditDomain):void {
    _domain = value;
  }

  protected function resetState():void {
    //m_currentScreenX = 0;
    //m_currentScreenY = 0;
    //m_stateMask = 0;
    //m_button = 0;
    ////
    //m_startScreenX = 0;
    //m_startScreenY = 0;
    ////
    //m_canPastThreshold = false;
  }
}
}
