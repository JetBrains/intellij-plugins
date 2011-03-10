package com.intellij.lang.javascript.flex.debug;

import com.intellij.xdebugger.Obsolescent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Maxim.Mossienko
 *         Date: Jun 20, 2008
 *         Time: 7:42:22 PM
 */
class CompositeDebuggerCommand extends DebuggerCommand {
  private final DebuggerCommand[] myCommands;
  private int myCurrentIndex;
  private FlexDebugProcess myFlexDebugProcess;
  private final Obsolescent myObsolescent;
  private volatile boolean myBecomeObsolete;
  private volatile boolean mySucceeded;

  public CompositeDebuggerCommand(final @NotNull DebuggerCommand... commands) {
    this(null, commands);
  }
  
  public CompositeDebuggerCommand(@Nullable Obsolescent obsolescent, final @NotNull DebuggerCommand... commands) {
    super("hz");

    myCommands = commands;
    assert commands.length > 0;
    myObsolescent = obsolescent;
  }

  @Override
  public void post(final FlexDebugProcess flexDebugProcess) throws IOException {
    myFlexDebugProcess = flexDebugProcess;
    if (myObsolescent != null && myObsolescent.isObsolete() && !myBecomeObsolete) {
      dispatchObsolete();
      return;
    }
    myCommands[myCurrentIndex].post(flexDebugProcess);
  }

  private void dispatchObsolete() {
    if (!myBecomeObsolete) {
      obsolete();
    }
  }

  protected void obsolete() {
    myBecomeObsolete = true;
  }

  @Override
  public CommandOutputProcessingType getOutputProcessingMode() {
    return myCommands[myCurrentIndex].getOutputProcessingMode();
  }

  @Override
  public VMState getStartVMState() {
    return myCommands[myCurrentIndex].getStartVMState();
  }

  @Override
  public VMState getEndVMState() {
    return myCommands[myCurrentIndex].getEndVMState();
  }

  @Override
  CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
    if (myBecomeObsolete) {
      return CommandOutputProcessingMode.DONE;
    }
    final CommandOutputProcessingMode b = myCommands[myCurrentIndex].onTextAvailable(s);

    if (b == CommandOutputProcessingMode.DONE) {
      if (myObsolescent != null && myObsolescent.isObsolete()) {
        dispatchObsolete();
        return CommandOutputProcessingMode.DONE;
      }
      ++myCurrentIndex;
      if (myCurrentIndex != myCommands.length) {
        myFlexDebugProcess.insertCommand(this);
      } else {
        succeeded();
      }
    }
    return b;
  }

  protected void succeeded() {
    mySucceeded = true;
  }

  @Override
  public String read(FlexDebugProcess flexDebugProcess) throws IOException {
    if (myBecomeObsolete) {
      return "*obsolete*";
    }
    return myCommands[myCurrentIndex].read(flexDebugProcess);
  }
}
