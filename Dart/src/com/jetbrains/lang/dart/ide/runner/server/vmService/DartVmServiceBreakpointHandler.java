package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.openapi.util.Pair;
import com.intellij.util.SmartList;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.lang.dart.ide.runner.DartLineBreakpointType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.dartlang.vm.service.element.Breakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.intellij.icons.AllIcons.Debugger.Db_invalid_breakpoint;
import static com.intellij.icons.AllIcons.Debugger.Db_verified_breakpoint;

public class DartVmServiceBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>> {

  private final DartVmServiceDebugProcess myDebugProcess;

  private final Set<XLineBreakpoint<XBreakpointProperties>> myXBreakpoints = new THashSet<XLineBreakpoint<XBreakpointProperties>>();
  private final Map<String, XLineBreakpoint<XBreakpointProperties>> myVmBreakpointIdToXBreakpointMap =
    new THashMap<String, XLineBreakpoint<XBreakpointProperties>>();
  private final Map<XLineBreakpoint<XBreakpointProperties>, Collection<Pair<String, String>>> myXBreakpointToIsolateAndVmBreakpointIdsMap =
    new THashMap<XLineBreakpoint<XBreakpointProperties>, Collection<Pair<String, String>>>();
  private final Map<String, List<String>> myIsolateTemporaryBreakpoints = new THashMap<String, List<String>>();

  protected DartVmServiceBreakpointHandler(@NotNull final DartVmServiceDebugProcess debugProcess) {
    super(DartLineBreakpointType.class);
    myDebugProcess = debugProcess;
  }

  @Override
  public void registerBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    myXBreakpoints.add(xBreakpoint);

    final VmServiceWrapper vmServiceWrapper = myDebugProcess.getVmServiceWrapper();
    if (vmServiceWrapper != null) {
      vmServiceWrapper.addBreakpointForIsolates(xBreakpoint, myDebugProcess.getIsolateInfos());
    }
  }

  @Override
  public void unregisterBreakpoint(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint, boolean temporary) {
    myXBreakpoints.remove(xBreakpoint);

    final Collection<Pair<String, String>> isolateAndVmBreakpointIds = myXBreakpointToIsolateAndVmBreakpointIdsMap.remove(xBreakpoint);
    if (isolateAndVmBreakpointIds != null) {
      for (Pair<String, String> isolateAndVmBreakpointId : isolateAndVmBreakpointIds) {
        final String isolateId = isolateAndVmBreakpointId.first;
        final String vmBreakpointId = isolateAndVmBreakpointId.second;
        if (myDebugProcess.isIsolateAlive(isolateId)) {
          myDebugProcess.getVmServiceWrapper().removeBreakpoint(isolateId, vmBreakpointId);
        }
      }
    }
  }

  public Set<XLineBreakpoint<XBreakpointProperties>> getXBreakpoints() {
    return myXBreakpoints;
  }

  public void vmBreakpointAdded(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint,
                                @NotNull final String isolateId,
                                @NotNull final Breakpoint vmBreakpoint) {
    myVmBreakpointIdToXBreakpointMap.put(vmBreakpoint.getId(), xBreakpoint);

    Collection<Pair<String, String>> isolateAndVmBreakpointIds = myXBreakpointToIsolateAndVmBreakpointIdsMap.get(xBreakpoint);
    if (isolateAndVmBreakpointIds == null) {
      isolateAndVmBreakpointIds = new SmartList<Pair<String, String>>();
      myXBreakpointToIsolateAndVmBreakpointIdsMap.put(xBreakpoint, isolateAndVmBreakpointIds);
    }
    isolateAndVmBreakpointIds.add(Pair.create(isolateId, vmBreakpoint.getId()));

    if (vmBreakpoint.getResolved()) {
      breakpointResolved(vmBreakpoint);
    }
  }

  public void temporaryBreakpointAdded(String isolateId, Breakpoint breakpoint) {
    List<String> breakpoints = myIsolateTemporaryBreakpoints.get(isolateId);
    if (breakpoints == null) {
      breakpoints = new ArrayList<String>();
      myIsolateTemporaryBreakpoints.put(isolateId, breakpoints);
    }
    breakpoints.add(breakpoint.getId());
  }

  public void removeTemporaryBreakpoints(String isolateId) {
    List<String> breakpoints = myIsolateTemporaryBreakpoints.get(isolateId);
    if (breakpoints != null) {
      for (String breakpointId : breakpoints) {
        myDebugProcess.getVmServiceWrapper().removeBreakpoint(isolateId, breakpointId);
      }
      breakpoints.clear();
    }
  }

  public void breakpointResolved(@NotNull final Breakpoint vmBreakpoint) {
    final XLineBreakpoint<XBreakpointProperties> xBreakpoint = myVmBreakpointIdToXBreakpointMap.get(vmBreakpoint.getId());

    // This can be null when the breakpoint has been set by another debugger client.
    if (xBreakpoint != null) {
      myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_verified_breakpoint, null);
    }
  }

  public void breakpointFailed(@NotNull final XLineBreakpoint<XBreakpointProperties> xBreakpoint) {
    // can this xBreakpoint be resolved for other isolate?
    myDebugProcess.getSession().updateBreakpointPresentation(xBreakpoint, Db_invalid_breakpoint, null);
  }

  public XLineBreakpoint<XBreakpointProperties> getXBreakpoint(@NotNull final Breakpoint vmBreakpoint) {
    return myVmBreakpointIdToXBreakpointMap.get(vmBreakpoint.getId());
  }
}
