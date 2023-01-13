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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
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
@SuppressWarnings({"WeakerAccess", "unused"})
public class VmService extends VmServiceBase {

  public static final String DEBUG_STREAM_ID = "Debug";

  public static final String EXTENSION_STREAM_ID = "Extension";

  public static final String GC_STREAM_ID = "GC";

  public static final String HEAPSNAPSHOT_STREAM_ID = "HeapSnapshot";

  public static final String ISOLATE_STREAM_ID = "Isolate";

  public static final String LOGGING_STREAM_ID = "Logging";

  public static final String SERVICE_STREAM_ID = "Service";

  public static final String STDERR_STREAM_ID = "Stderr";

  public static final String STDOUT_STREAM_ID = "Stdout";

  public static final String TIMELINE_STREAM_ID = "Timeline";

  public static final String VM_STREAM_ID = "VM";

  /**
   * The major version number of the protocol supported by this client.
   */
  public static final int versionMajor = 3;

  /**
   * The minor version number of the protocol supported by this client.
   */
  public static final int versionMinor = 27;

  /**
   * The [addBreakpoint] RPC is used to add a breakpoint at a specific line of some script.
   */
  public void addBreakpoint(String isolateId, String scriptId, int line, BreakpointConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("scriptId", scriptId);
    params.addProperty("line", line);
    request("addBreakpoint", params, consumer);
  }

  /**
   * The [addBreakpoint] RPC is used to add a breakpoint at a specific line of some script.
   * @param column This parameter is optional and may be null.
   */
  public void addBreakpoint(String isolateId, String scriptId, int line, Integer column, BreakpointConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("scriptId", scriptId);
    params.addProperty("line", line);
    if (column != null) params.addProperty("column", column);
    request("addBreakpoint", params, consumer);
  }

  /**
   * The [addBreakpointAtEntry] RPC is used to add a breakpoint at the entrypoint of some function.
   */
  public void addBreakpointAtEntry(String isolateId, String functionId, BreakpointConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("functionId", functionId);
    request("addBreakpointAtEntry", params, consumer);
  }

  /**
   * The [addBreakpoint] RPC is used to add a breakpoint at a specific line of some script. This
   * RPC is useful when a script has not yet been assigned an id, for example, if a script is in a
   * deferred library which has not yet been loaded.
   */
  public void addBreakpointWithScriptUri(String isolateId, String scriptUri, int line, BreakpointConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("scriptUri", scriptUri);
    params.addProperty("line", line);
    request("addBreakpointWithScriptUri", params, consumer);
  }

  /**
   * The [addBreakpoint] RPC is used to add a breakpoint at a specific line of some script. This
   * RPC is useful when a script has not yet been assigned an id, for example, if a script is in a
   * deferred library which has not yet been loaded.
   * @param column This parameter is optional and may be null.
   */
  public void addBreakpointWithScriptUri(String isolateId, String scriptUri, int line, Integer column, BreakpointConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("scriptUri", scriptUri);
    params.addProperty("line", line);
    if (column != null) params.addProperty("column", column);
    request("addBreakpointWithScriptUri", params, consumer);
  }

  /**
   * Clears all CPU profiling samples.
   */
  public void clearCpuSamples(String isolateId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("clearCpuSamples", params, consumer);
  }

  /**
   * Clears all VM timeline events.
   */
  public void clearVMTimeline(SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("clearVMTimeline", params, consumer);
  }

  /**
   * The [evaluate] RPC is used to evaluate an expression in the context of some target.
   */
  public void evaluate(String isolateId, String targetId, String expression, EvaluateConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("expression", expression);
    request("evaluate", params, consumer);
  }

  /**
   * The [evaluate] RPC is used to evaluate an expression in the context of some target.
   * @param scope This parameter is optional and may be null.
   * @param disableBreakpoints This parameter is optional and may be null.
   */
  public void evaluate(String isolateId, String targetId, String expression, Map<String, String> scope, Boolean disableBreakpoints, EvaluateConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("expression", expression);
    if (scope != null) params.add("scope", convertMapToJsonObject(scope));
    if (disableBreakpoints != null) params.addProperty("disableBreakpoints", disableBreakpoints);
    request("evaluate", params, consumer);
  }

  /**
   * The [evaluateInFrame] RPC is used to evaluate an expression in the context of a particular
   * stack frame. [frameIndex] is the index of the desired Frame, with an index of [0] indicating
   * the top (most recent) frame.
   */
  public void evaluateInFrame(String isolateId, int frameIndex, String expression, EvaluateInFrameConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("frameIndex", frameIndex);
    params.addProperty("expression", expression);
    request("evaluateInFrame", params, consumer);
  }

  /**
   * The [evaluateInFrame] RPC is used to evaluate an expression in the context of a particular
   * stack frame. [frameIndex] is the index of the desired Frame, with an index of [0] indicating
   * the top (most recent) frame.
   * @param scope This parameter is optional and may be null.
   * @param disableBreakpoints This parameter is optional and may be null.
   */
  public void evaluateInFrame(String isolateId, int frameIndex, String expression, Map<String, String> scope, Boolean disableBreakpoints, EvaluateInFrameConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("frameIndex", frameIndex);
    params.addProperty("expression", expression);
    if (scope != null) params.add("scope", convertMapToJsonObject(scope));
    if (disableBreakpoints != null) params.addProperty("disableBreakpoints", disableBreakpoints);
    request("evaluateInFrame", params, consumer);
  }

  /**
   * The [getAllocationProfile] RPC is used to retrieve allocation information for a given isolate.
   */
  public void getAllocationProfile(String isolateId, AllocationProfileConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getAllocationProfile", params, consumer);
  }

  /**
   * The [getAllocationProfile] RPC is used to retrieve allocation information for a given isolate.
   * @param reset This parameter is optional and may be null.
   * @param gc This parameter is optional and may be null.
   */
  public void getAllocationProfile(String isolateId, Boolean reset, Boolean gc, AllocationProfileConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    if (reset != null) params.addProperty("reset", reset);
    if (gc != null) params.addProperty("gc", gc);
    request("getAllocationProfile", params, consumer);
  }

  /**
   * The [getCpuSamples] RPC is used to retrieve samples collected by the CPU profiler. Only
   * samples collected in the time range <code>[timeOriginMicros, timeOriginMicros +
   * timeExtentMicros]</code>[timeOriginMicros, timeOriginMicros + timeExtentMicros] will be
   * reported.
   */
  public void getCpuSamples(String isolateId, int timeOriginMicros, int timeExtentMicros, CpuSamplesConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("timeOriginMicros", timeOriginMicros);
    params.addProperty("timeExtentMicros", timeExtentMicros);
    request("getCpuSamples", params, consumer);
  }

  /**
   * The [getFlagList] RPC returns a list of all command line flags in the VM along with their
   * current values.
   */
  public void getFlagList(FlagListConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("getFlagList", params, consumer);
  }

  /**
   * Returns a set of inbound references to the object specified by [targetId]. Up to [limit]
   * references will be returned.
   */
  public void getInboundReferences(String isolateId, String targetId, int limit, GetInboundReferencesConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("limit", limit);
    request("getInboundReferences", params, consumer);
  }

  /**
   * The [getInstances] RPC is used to retrieve a set of instances which are of a specific class.
   * This does not include instances of subclasses of the given class.
   */
  public void getInstances(String isolateId, String objectId, int limit, InstanceSetConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("objectId", objectId);
    params.addProperty("limit", limit);
    request("getInstances", params, consumer);
  }

  /**
   * The [getIsolate] RPC is used to lookup an [Isolate] object by its [id].
   */
  public void getIsolate(String isolateId, GetIsolateConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getIsolate", params, consumer);
  }

  /**
   * The [getMemoryUsage] RPC is used to lookup an isolate's memory usage statistics by its [id].
   */
  public void getMemoryUsage(String isolateId, GetMemoryUsageConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getMemoryUsage", params, consumer);
  }

  /**
   * The [getObject] RPC is used to lookup an [object] from some isolate by its [id].
   */
  public void getObject(String isolateId, String objectId, GetObjectConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("objectId", objectId);
    request("getObject", params, consumer);
  }

  /**
   * The [getObject] RPC is used to lookup an [object] from some isolate by its [id].
   * @param offset This parameter is optional and may be null.
   * @param count This parameter is optional and may be null.
   */
  public void getObject(String isolateId, String objectId, Integer offset, Integer count, GetObjectConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("objectId", objectId);
    if (offset != null) params.addProperty("offset", offset);
    if (count != null) params.addProperty("count", count);
    request("getObject", params, consumer);
  }

  /**
   * The [getRetainingPath] RPC is used to lookup a path from an object specified by [targetId] to
   * a GC root (i.e., the object which is preventing this object from being garbage collected).
   */
  public void getRetainingPath(String isolateId, String targetId, int limit, RetainingPathConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("limit", limit);
    request("getRetainingPath", params, consumer);
  }

  /**
   * The [getScripts] RPC is used to retrieve a [ScriptList] containing all scripts for an isolate
   * based on the isolate's [isolateId].
   */
  public void getScripts(String isolateId, ScriptListConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getScripts", params, consumer);
  }

  /**
   * The [getSourceReport] RPC is used to generate a set of reports tied to source locations in an
   * isolate.
   */
  public void getSourceReport(String isolateId, List<SourceReportKind> reports, SourceReportConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.add("reports", convertIterableToJsonArray(reports));
    request("getSourceReport", params, consumer);
  }

  /**
   * The [getSourceReport] RPC is used to generate a set of reports tied to source locations in an
   * isolate.
   * @param scriptId This parameter is optional and may be null.
   * @param tokenPos This parameter is optional and may be null.
   * @param endTokenPos This parameter is optional and may be null.
   * @param forceCompile This parameter is optional and may be null.
   */
  public void getSourceReport(String isolateId, List<SourceReportKind> reports, String scriptId, Integer tokenPos, Integer endTokenPos, Boolean forceCompile, SourceReportConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.add("reports", convertIterableToJsonArray(reports));
    if (scriptId != null) params.addProperty("scriptId", scriptId);
    if (tokenPos != null) params.addProperty("tokenPos", tokenPos);
    if (endTokenPos != null) params.addProperty("endTokenPos", endTokenPos);
    if (forceCompile != null) params.addProperty("forceCompile", forceCompile);
    request("getSourceReport", params, consumer);
  }

  /**
   * The [getStack] RPC is used to retrieve the current execution stack and message queue for an
   * isolate. The isolate does not need to be paused.
   */
  public void getStack(String isolateId, StackConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("getStack", params, consumer);
  }

  /**
   * The [getVM] RPC returns global information about a Dart virtual machine.
   */
  public void getVM(VMConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("getVM", params, consumer);
  }

  /**
   * The [getVMTimeline] RPC is used to retrieve an object which contains VM timeline events.
   * @param timeOriginMicros This parameter is optional and may be null.
   * @param timeExtentMicros This parameter is optional and may be null.
   */
  public void getVMTimeline(Integer timeOriginMicros, Integer timeExtentMicros, TimelineConsumer consumer) {
    final JsonObject params = new JsonObject();
    if (timeOriginMicros != null) params.addProperty("timeOriginMicros", timeOriginMicros);
    if (timeExtentMicros != null) params.addProperty("timeExtentMicros", timeExtentMicros);
    request("getVMTimeline", params, consumer);
  }

  /**
   * The [getVMTimeline] RPC is used to retrieve an object which contains VM timeline events.
   */
  public void getVMTimeline(TimelineConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("getVMTimeline", params, consumer);
  }

  /**
   * The [getVMTimelineFlags] RPC returns information about the current VM timeline configuration.
   */
  public void getVMTimelineFlags(TimelineFlagsConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("getVMTimelineFlags", params, consumer);
  }

  /**
   * The [getVMTimelineMicros] RPC returns the current time stamp from the clock used by the
   * timeline, similar to <code>Timeline.now</code>Timeline.now in
   * <code>dart:developer</code>dart:developer and
   * <code>Dart_TimelineGetMicros</code>Dart_TimelineGetMicros in the VM embedding API.
   */
  public void getVMTimelineMicros(TimestampConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("getVMTimelineMicros", params, consumer);
  }

  /**
   * The [getVersion] RPC is used to determine what version of the Service Protocol is served by a
   * VM.
   */
  public void getVersion(VersionConsumer consumer) {
    final JsonObject params = new JsonObject();
    request("getVersion", params, consumer);
  }

  /**
   * The [invoke] RPC is used to perform regular method invocation on some receiver, as if by
   * dart:mirror's ObjectMirror.invoke. Note this does not provide a way to perform getter, setter
   * or constructor invocation.
   * @param disableBreakpoints This parameter is optional and may be null.
   */
  public void invoke(String isolateId, String targetId, String selector, List<String> argumentIds, Boolean disableBreakpoints, InvokeConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("selector", selector);
    params.add("argumentIds", convertIterableToJsonArray(argumentIds));
    if (disableBreakpoints != null) params.addProperty("disableBreakpoints", disableBreakpoints);
    request("invoke", params, consumer);
  }

  /**
   * The [invoke] RPC is used to perform regular method invocation on some receiver, as if by
   * dart:mirror's ObjectMirror.invoke. Note this does not provide a way to perform getter, setter
   * or constructor invocation.
   */
  public void invoke(String isolateId, String targetId, String selector, List<String> argumentIds, InvokeConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("targetId", targetId);
    params.addProperty("selector", selector);
    params.add("argumentIds", convertIterableToJsonArray(argumentIds));
    request("invoke", params, consumer);
  }

  /**
   * The [kill] RPC is used to kill an isolate as if by dart:isolate's
   * <code>Isolate.kill(IMMEDIATE)</code>Isolate.kill(IMMEDIATE).
   */
  public void kill(String isolateId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("kill", params, consumer);
  }

  /**
   * The [pause] RPC is used to interrupt a running isolate. The RPC enqueues the interrupt request
   * and potentially returns before the isolate is paused.
   */
  public void pause(String isolateId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("pause", params, consumer);
  }

  /**
   * Registers a service that can be invoked by other VM service clients, where
   * <code>service</code>service is the name of the service to advertise and
   * <code>alias</code>alias is an alternative name for the registered service.
   */
  public void registerService(String service, String alias, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("service", service);
    params.addProperty("alias", alias);
    request("registerService", params, consumer);
  }

  /**
   * The [reloadSources] RPC is used to perform a hot reload of an Isolate's sources.
   * @param force This parameter is optional and may be null.
   * @param pause This parameter is optional and may be null.
   * @param rootLibUri This parameter is optional and may be null.
   * @param packagesUri This parameter is optional and may be null.
   */
  public void reloadSources(String isolateId, Boolean force, Boolean pause, String rootLibUri, String packagesUri, ReloadReportConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    if (force != null) params.addProperty("force", force);
    if (pause != null) params.addProperty("pause", pause);
    if (rootLibUri != null) params.addProperty("rootLibUri", rootLibUri);
    if (packagesUri != null) params.addProperty("packagesUri", packagesUri);
    request("reloadSources", params, consumer);
  }

  /**
   * The [reloadSources] RPC is used to perform a hot reload of an Isolate's sources.
   */
  public void reloadSources(String isolateId, ReloadReportConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("reloadSources", params, consumer);
  }

  /**
   * The [removeBreakpoint] RPC is used to remove a breakpoint by its [id].
   */
  public void removeBreakpoint(String isolateId, String breakpointId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("breakpointId", breakpointId);
    request("removeBreakpoint", params, consumer);
  }

  /**
   * Requests a dump of the Dart heap of the given isolate.
   */
  public void requestHeapSnapshot(String isolateId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("requestHeapSnapshot", params, consumer);
  }

  /**
   * The [resume] RPC is used to resume execution of a paused isolate.
   * @param step A [StepOption] indicates which form of stepping is requested in a resume RPC. This
   * parameter is optional and may be null.
   * @param frameIndex This parameter is optional and may be null.
   */
  public void resume(String isolateId, StepOption step, Integer frameIndex, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    if (step != null) params.addProperty("step", step.name());
    if (frameIndex != null) params.addProperty("frameIndex", frameIndex);
    request("resume", params, consumer);
  }

  /**
   * The [resume] RPC is used to resume execution of a paused isolate.
   */
  public void resume(String isolateId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    request("resume", params, consumer);
  }

  /**
   * The [setExceptionPauseMode] RPC is used to control if an isolate pauses when an exception is
   * thrown.
   *
   * @param mode An [ExceptionPauseMode] indicates how the isolate pauses when an exception is thrown.
   * @deprecated Use {@link #setIsolatePauseMode(String, ExceptionPauseMode, SuccessConsumer)}
   */
  public void setExceptionPauseMode(String isolateId, ExceptionPauseMode mode, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("mode", mode.name());
    request("setExceptionPauseMode", params, consumer);
  }

  /**
   * The [setIsolatePauseMode] RPC is used to control if or when an isolate will pause due to a change in execution state.
   */
  public void setIsolatePauseMode(String isolateId, ExceptionPauseMode exceptionPauseMode, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("exceptionPauseMode", exceptionPauseMode.name());
    request("setIsolatePauseMode", params, consumer);
  }

  /**
   * The [setFlag] RPC is used to set a VM flag at runtime. Returns an error if the named flag does
   * not exist, the flag may not be set at runtime, or the value is of the wrong type for the flag.
   */
  public void setFlag(String name, String value, SetFlagConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("name", name);
    params.addProperty("value", value);
    request("setFlag", params, consumer);
  }

  /**
   * The [setLibraryDebuggable] RPC is used to enable or disable whether breakpoints and stepping
   * work for a given library.
   */
  public void setLibraryDebuggable(String isolateId, String libraryId, boolean isDebuggable, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("libraryId", libraryId);
    params.addProperty("isDebuggable", isDebuggable);
    request("setLibraryDebuggable", params, consumer);
  }

  /**
   * The [setName] RPC is used to change the debugging name for an isolate.
   */
  public void setName(String isolateId, String name, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("isolateId", isolateId);
    params.addProperty("name", name);
    request("setName", params, consumer);
  }

  /**
   * The [setVMName] RPC is used to change the debugging name for the vm.
   */
  public void setVMName(String name, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("name", name);
    request("setVMName", params, consumer);
  }

  /**
   * The [setVMTimelineFlags] RPC is used to set which timeline streams are enabled.
   */
  public void setVMTimelineFlags(List<String> recordedStreams, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.add("recordedStreams", convertIterableToJsonArray(recordedStreams));
    request("setVMTimelineFlags", params, consumer);
  }

  /**
   * The [streamCancel] RPC cancels a stream subscription in the VM.
   */
  public void streamCancel(String streamId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("streamId", streamId);
    request("streamCancel", params, consumer);
  }

  /**
   * The [streamListen] RPC subscribes to a stream in the VM. Once subscribed, the client will
   * begin receiving events from the stream.
   */
  public void streamListen(String streamId, SuccessConsumer consumer) {
    final JsonObject params = new JsonObject();
    params.addProperty("streamId", streamId);
    request("streamListen", params, consumer);
  }

  private JsonArray convertIterableToJsonArray(Iterable list) {
    JsonArray arr = new JsonArray();
    for (Object element : list) {
      arr.add(new JsonPrimitive(element.toString()));
    }
    return arr;
  }

  private JsonObject convertMapToJsonObject(Map<String, String> map) {
    JsonObject obj = new JsonObject();
    for (String key : map.keySet()) {
      obj.addProperty(key, map.get(key));
    }
    return obj;
  }

  @Override
  void forwardResponse(Consumer consumer, String responseType, JsonObject json) {
    if (consumer instanceof AllocationProfileConsumer) {
      if (responseType.equals("AllocationProfile")) {
        ((AllocationProfileConsumer) consumer).received(new AllocationProfile(json));
        return;
      }
    }
    if (consumer instanceof BreakpointConsumer) {
      if (responseType.equals("Breakpoint")) {
        ((BreakpointConsumer) consumer).received(new Breakpoint(json));
        return;
      }
    }
    if (consumer instanceof CpuSamplesConsumer) {
      if (responseType.equals("CpuSamples")) {
        ((CpuSamplesConsumer) consumer).received(new CpuSamples(json));
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
      if (responseType.equals("Sentinel")) {
        ((EvaluateInFrameConsumer) consumer).received(new Sentinel(json));
        return;
      }
    }
    if (consumer instanceof FlagListConsumer) {
      if (responseType.equals("FlagList")) {
        ((FlagListConsumer) consumer).received(new FlagList(json));
        return;
      }
    }
    if (consumer instanceof GetInboundReferencesConsumer) {
      if (responseType.equals("InboundReferences")) {
        ((GetInboundReferencesConsumer) consumer).received(new InboundReferences(json));
        return;
      }
      if (responseType.equals("Sentinel")) {
        ((GetInboundReferencesConsumer) consumer).received(new Sentinel(json));
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
    if (consumer instanceof GetMemoryUsageConsumer) {
      if (responseType.equals("MemoryUsage")) {
        ((GetMemoryUsageConsumer) consumer).received(new MemoryUsage(json));
        return;
      }
      if (responseType.equals("Sentinel")) {
        ((GetMemoryUsageConsumer) consumer).received(new Sentinel(json));
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
      if (responseType.equals("Function")) {
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
      if (responseType.equals("Object")) {
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
    if (consumer instanceof InstanceSetConsumer) {
      if (responseType.equals("InstanceSet")) {
        ((InstanceSetConsumer) consumer).received(new InstanceSet(json));
        return;
      }
    }
    if (consumer instanceof InvokeConsumer) {
      if (responseType.equals("@Error")) {
        ((InvokeConsumer) consumer).received(new ErrorRef(json));
        return;
      }
      if (responseType.equals("@Instance")) {
        ((InvokeConsumer) consumer).received(new InstanceRef(json));
        return;
      }
      if (responseType.equals("@Null")) {
        ((InvokeConsumer) consumer).received(new NullRef(json));
        return;
      }
      if (responseType.equals("Sentinel")) {
        ((InvokeConsumer) consumer).received(new Sentinel(json));
        return;
      }
    }
    if (consumer instanceof ReloadReportConsumer) {
      if (responseType.equals("ReloadReport")) {
        ((ReloadReportConsumer) consumer).received(new ReloadReport(json));
        return;
      }
    }
    if (consumer instanceof RetainingPathConsumer) {
      if (responseType.equals("RetainingPath")) {
        ((RetainingPathConsumer) consumer).received(new RetainingPath(json));
        return;
      }
    }
    if (consumer instanceof ScriptListConsumer) {
      if (responseType.equals("ScriptList")) {
        ((ScriptListConsumer) consumer).received(new ScriptList(json));
        return;
      }
    }
    if (consumer instanceof SetFlagConsumer) {
      if (responseType.equals("Error")) {
        ((SetFlagConsumer) consumer).received(new ErrorObj(json));
        return;
      }
      if (responseType.equals("Success")) {
        ((SetFlagConsumer) consumer).received(new Success(json));
        return;
      }
    }
    if (consumer instanceof SourceReportConsumer) {
      if (responseType.equals("SourceReport")) {
        ((SourceReportConsumer) consumer).received(new SourceReport(json));
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
    if (consumer instanceof TimelineConsumer) {
      if (responseType.equals("Timeline")) {
        ((TimelineConsumer) consumer).received(new Timeline(json));
        return;
      }
    }
    if (consumer instanceof TimelineFlagsConsumer) {
      if (responseType.equals("TimelineFlags")) {
        ((TimelineFlagsConsumer) consumer).received(new TimelineFlags(json));
        return;
      }
    }
    if (consumer instanceof TimestampConsumer) {
      if (responseType.equals("Timestamp")) {
        ((TimestampConsumer) consumer).received(new Timestamp(json));
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
    if (consumer instanceof ServiceExtensionConsumer) {
      ((ServiceExtensionConsumer) consumer).received(json);
      return;
    }
    logUnknownResponse(consumer, json);
  }
}
