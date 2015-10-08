/*
 * Copyright (c) 2015, the Dart project authors.
 *
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dartlang.vm.service;

// This is a generated file.

import com.google.gson.JsonObject;
import org.dartlang.vm.service.consumer.*;
import org.dartlang.vm.service.element.*;

/**
 * {@link VmService} allows control of and access to information in a running
 * Dart VM instance.
 * <br/>
 * Launch the Dart VM with the arguments:
 * <pre>
 * --pause_isolates_on_start
 * --observe
 * --enable-vm-service=some-port
 * </pre>
 * where <strong>some-port</strong> is a port number of your choice
 * which this client will use to communicate with the Dart VM.
 * See https://www.dartlang.org/tools/dart-vm/ for more details.
 * Once the VM is running, instantiate a new {@link VmService}
 * to connect to that VM via {@link VmService#connect(String)}
 * or {@link VmService#localConnect(int)}.
 * <br/>
 * {@link VmService} is not thread safe and should only be accessed from
 * a single thread. In addition, a given VM should only be accessed from
 * a single instance of {@link VmService}.
 * <br/>
 * Calls to {@link VmService} should not be nested.
 * More specifically, you should not make any calls to {@link VmService}
 * from within any {@link Consumer} method.
 */
public class VmService extends VmServiceBase {

  public static final String DEBUG_STREAM_ID = "Debug";

  public static final String GC_STREAM_ID = "GC";

  public static final String ISOLATE_STREAM_ID = "Isolate";

  public static final String STDERR_STREAM_ID = "Stderr";

  public static final String STDOUT_STREAM_ID = "Stdout";

  /**
   * The major version number of the protocol supported by this client.
   */
  public static final int versionMajor = 3;

  /**
   * The minor version number of the protocol supported by this client.
   */
  public static final int versionMinor = 0;

  /**
   * The [addBreakpoint] RPC is used to add a breakpoint at a specific line of some script.
   */
  public void addBreakpoint(String isolateId, String scriptId, int line, int column, BreakpointConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("scriptId", scriptId);
    params.addProperty("line", line);
    params.addProperty("column", column);
    request("addBreakpoint", params, consumer);
  }

  /**
   * The [addBreakpointAtEntry] RPC is used to add a breakpoint at the entrypoint of some function.
   */
  public void addBreakpointAtEntry(String isolateId, String functionId, BreakpointConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("functionId", functionId);
    request("addBreakpointAtEntry", params, consumer);
  }

  /**
   * The [addBreakpoint] RPC is used to add a breakpoint at a specific line of some script. This
   * RPC is useful when a script has not yet been assigned an id, for example, if a script is in a
   * deferred library which has not yet been loaded.
   */
  public void addBreakpointWithScriptUri(String isolateId, String scriptUri, int line, int column, BreakpointConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("scriptUri", scriptUri);
    params.addProperty("line", line);
    params.addProperty("column", column);
    request("addBreakpointWithScriptUri", params, consumer);
  }

  /**
   * The [evaluate] RPC is used to evaluate an expression in the context of some target.
   */
  public void evaluate(String isolateId, String targetId, String expression, EvaluateConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("expression", expression);
    request("evaluate", params, consumer);
  }

  /**
   * The [evaluateInFrame] RPC is used to evaluate an expression in the context of a particular
   * stack frame. [frameIndex] is the index of the desired Frame, with an index of [0] indicating
   * the top (most recent) frame.
   */
  public void evaluateInFrame(String isolateId, int frameIndex, String expression, EvaluateInFrameConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("frameIndex", frameIndex);
    params.addProperty("expression", expression);
    request("evaluateInFrame", params, consumer);
  }

  /**
   * The _getFlagList RPC returns a list of all command line flags in the VM along with their
   * current values.
   */
  public void getFlagList(FlagListConsumer consumer) {
    JsonObject params = new JsonObject();
    request("getFlagList", params, consumer);
  }

  /**
   * The [getIsolate] RPC is used to lookup an [Isolate] object by its [id].
   */
  public void getIsolate(String isolateId, GetIsolateConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getIsolate", params, consumer);
  }

  /**
   * The [getObject] RPC is used to lookup an [object] from some isolate by its [id].
   */
  public void getObject(String isolateId, String objectId, GetObjectConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("objectId", objectId);
    request("getObject", params, consumer);
  }

  /**
   * The [getStack] RPC is used to retrieve the current execution stack and message queue for an
   * isolate. The isolate does not need to be paused.
   */
  public void getStack(String isolateId, StackConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getStack", params, consumer);
  }

  /**
   * The [getVM] RPC returns global information about a Dart virtual machine.
   */
  public void getVM(VMConsumer consumer) {
    JsonObject params = new JsonObject();
    request("getVM", params, consumer);
  }

  /**
   * The [getVersion] RPC is used to determine what version of the Service Protocol is served by a
   * VM.
   */
  public void getVersion(VersionConsumer consumer) {
    JsonObject params = new JsonObject();
    request("getVersion", params, consumer);
  }

  /**
   * The [pause] RPC is used to interrupt a running isolate. The RPC enqueues the interrupt request
   * and potentially returns before the isolate is paused.
   */
  public void pause(String isolateId, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("pause", params, consumer);
  }

  /**
   * The [removeBreakpoint] RPC is used to remove a breakpoint by its [id].
   */
  public void removeBreakpoint(String isolateId, String breakpointId, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("breakpointId", breakpointId);
    request("removeBreakpoint", params, consumer);
  }

  /**
   * The [resume] RPC is used to resume execution of a paused isolate.
   * 
   * @param step A [StepOption] indicates which form of stepping is requested in a resume RPC. This
   * parameter is optional and may be null.
   */
  public void resume(String isolateId, StepOption step, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    if (step != null) params.addProperty("step", step.name());
    request("resume", params, consumer);
  }

  /**
   * The [setLibraryDebuggable] RPC is used to enable or disable whether breakpoints and stepping
   * work for a given library.
   */
  public void setLibraryDebuggable(String isolateId, String libraryId, boolean isDebuggable, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("libraryId", libraryId);
    params.addProperty("isDebuggable", isDebuggable);
    request("setLibraryDebuggable", params, consumer);
  }

  /**
   * The [setName] RPC is used to change the debugging name for an isolate.
   */
  public void setName(String isolateId, String name, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("name", name);
    request("setName", params, consumer);
  }

  /**
   * The [streamCancel] RPC cancels a stream subscription in the VM.
   */
  public void streamCancel(String streamId, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("streamId", streamId);
    request("streamCancel", params, consumer);
  }

  /**
   * The [streamListen] RPC subscribes to a stream in the VM. Once subscribed, the client will
   * begin receiving events from the stream.
   */
  public void streamListen(String streamId, SuccessConsumer consumer) {
    JsonObject params = new JsonObject();
    params.addProperty("streamId", streamId);
    request("streamListen", params, consumer);
  }

  @Override
  void forwardResponse(Consumer consumer, String responseType, JsonObject json) {
    if (consumer instanceof BreakpointConsumer) {
      if (responseType.equals("Breakpoint")) {
        ((BreakpointConsumer) consumer).received(new Breakpoint(json));
        return;
      }
    }
    if (consumer instanceof EvaluateConsumer) {
      if (responseType.equals("@Error")) {
        ((EvaluateConsumer) consumer).received(new ErrorRef(json));
        return;
      }
      if (responseType.equals("@Instance")) {
        ((EvaluateConsumer) consumer).received(new InstanceRef(json));
        return;
      }
      if (responseType.equals("@Null")) {
        ((EvaluateConsumer) consumer).received(new NullRef(json));
        return;
      }
      if (responseType.equals("Sentinel")) {
        ((EvaluateConsumer) consumer).received(new Sentinel(json));
        return;
      }
    }
    if (consumer instanceof EvaluateInFrameConsumer) {
      if (responseType.equals("@Error")) {
        ((EvaluateInFrameConsumer) consumer).received(new ErrorRef(json));
        return;
      }
      if (responseType.equals("@Instance")) {
        ((EvaluateInFrameConsumer) consumer).received(new InstanceRef(json));
        return;
      }
      if (responseType.equals("@Null")) {
        ((EvaluateInFrameConsumer) consumer).received(new NullRef(json));
        return;
      }
    }
    if (consumer instanceof FlagListConsumer) {
      if (responseType.equals("FlagList")) {
        ((FlagListConsumer) consumer).received(new FlagList(json));
        return;
      }
    }
    if (consumer instanceof GetIsolateConsumer) {
      if (responseType.equals("Isolate")) {
        ((GetIsolateConsumer) consumer).received(new Isolate(json));
        return;
      }
      if (responseType.equals("Sentinel")) {
        ((GetIsolateConsumer) consumer).received(new Sentinel(json));
        return;
      }
    }
    if (consumer instanceof GetObjectConsumer) {
      if (responseType.equals("Breakpoint")) {
        ((GetObjectConsumer) consumer).received(new Breakpoint(json));
        return;
      }
      if (responseType.equals("Class")) {
        ((GetObjectConsumer) consumer).received(new ClassObj(json));
        return;
      }
      if (responseType.equals("Context")) {
        ((GetObjectConsumer) consumer).received(new Context(json));
        return;
      }
      if (responseType.equals("Error")) {
        ((GetObjectConsumer) consumer).received(new ErrorObj(json));
        return;
      }
      if (responseType.equals("Field")) {
        ((GetObjectConsumer) consumer).received(new Field(json));
        return;
      }
      if (responseType.equals("Func")) {
        ((GetObjectConsumer) consumer).received(new Func(json));
        return;
      }
      if (responseType.equals("Instance")) {
        ((GetObjectConsumer) consumer).received(new Instance(json));
        return;
      }
      if (responseType.equals("Library")) {
        ((GetObjectConsumer) consumer).received(new Library(json));
        return;
      }
      if (responseType.equals("Null")) {
        ((GetObjectConsumer) consumer).received(new Null(json));
        return;
      }
      if (responseType.equals("Obj")) {
        ((GetObjectConsumer) consumer).received(new Obj(json));
        return;
      }
      if (responseType.equals("Script")) {
        ((GetObjectConsumer) consumer).received(new Script(json));
        return;
      }
      if (responseType.equals("Sentinel")) {
        ((GetObjectConsumer) consumer).received(new Sentinel(json));
        return;
      }
      if (responseType.equals("TypeArguments")) {
        ((GetObjectConsumer) consumer).received(new TypeArguments(json));
        return;
      }
    }
    if (consumer instanceof StackConsumer) {
      if (responseType.equals("Stack")) {
        ((StackConsumer) consumer).received(new Stack(json));
        return;
      }
    }
    if (consumer instanceof SuccessConsumer) {
      if (responseType.equals("Success")) {
        ((SuccessConsumer) consumer).received(new Success(json));
        return;
      }
    }
    if (consumer instanceof VMConsumer) {
      if (responseType.equals("VM")) {
        ((VMConsumer) consumer).received(new VM(json));
        return;
      }
    }
    if (consumer instanceof VersionConsumer) {
      if (responseType.equals("Version")) {
        ((VersionConsumer) consumer).received(new Version(json));
        return;
      }
    }
    logUnknownResponse(consumer, json);
  }
}
