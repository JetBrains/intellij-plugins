/*
 * Copyright (c) 2012, the Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;

import com.intellij.openapi.application.ApplicationManager;
import com.jetbrains.lang.dart.ide.runner.server.google.VmListener.PausedReason;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess.LOG;

/**
 * A low level interface to the Dart VM debugger protocol.
 */
public class VmConnection {

  public static enum BreakOnExceptionsType {
    all,
    none,
    unhandled
  }

  //public static interface BreakpointResolvedCallback {
  //  public void handleResolved(VmBreakpoint bp);
  //}

  static interface Callback {
    public void handleResult(JSONObject result) throws JSONException;
  }

  private static final String EVENT_ISOLATE = "isolate";
  private static final String EVENT_PAUSED = "paused";
  private static final String EVENT_BREAKPOINTRESOLVED = "breakpointResolved";

  private static Charset UTF8 = Charset.forName("UTF-8");

  private List<VmListener> listeners = new ArrayList<VmListener>();

  private String host;
  private int port;

  private Map<Integer, Callback> callbackMap = new HashMap<Integer, Callback>();

  private int nextCommandId = 1;

  private Socket socket;
  private OutputStream out;

  private List<VmBreakpoint> breakpoints = Collections.synchronizedList(new ArrayList<VmBreakpoint>());

  private Map<String, String> sourceCache = new HashMap<String, String>();

  private Map<String, VmLineNumberTable> lineNumberTableCache = new HashMap<String, VmLineNumberTable>();

  private Map<Integer, VmIsolate> isolateMap = new HashMap<Integer, VmIsolate>();

  private VmLocation currentLocation;
  private boolean isStepping;
  private String stepCommand;

  public VmConnection(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void addListener(VmListener listener) {
    listeners.add(listener);
  }

  public void callToString(final VmValue object, final VmCallback<VmValue> callback)
      throws IOException {
    evaluateObject(object.getIsolate(), object, "toString()", callback);
  }

  public void close() throws IOException {
    if (socket != null) {
      socket.close();
      socket = null;
    }
  }

  /**
   * Connect to the VM debug server.
   *
   * @throws IOException
   */
  public void connect() throws IOException {
    socket = new Socket(host, port);

    out = socket.getOutputStream();
    final InputStream in = socket.getInputStream();

    // Start a reader thread.
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        for (VmListener listener : listeners) {
          listener.connectionOpened(VmConnection.this);
        }

        try {
          processVmEvents(in);
        }
        catch (EOFException e) {

        }
        catch (SocketException se) {
          // ignore java.net.SocketException: Connection reset
          final String reset = "Connection reset";

          if (!(se.getMessage() != null && se.getMessage().contains(reset))) {
            LOG.warn(se);
          }
        }
        catch (IOException e) {
          LOG.warn(e);
        }
        finally {
          socket = null;
        }

        for (VmListener listener : listeners) {
          listener.connectionClosed(VmConnection.this);
        }

        handleTerminated();
      }
    });
  }

  /**
   * Enable stepping for all libraries (except for certain core ones).
   *
   * @throws IOException
   */
  public void enableAllSteppingSync(final VmIsolate isolate) throws IOException {
    final CountDownLatch latch = new CountDownLatch(1);

    getLibraries(isolate, new VmCallback<List<VmLibraryRef>>() {
      @Override
      public void handleResult(VmResult<List<VmLibraryRef>> result) {
        try {
          if (!result.isError()) {
            for (VmLibraryRef ref : result.getResult()) {
              if (!ref.isInternal() && !ref.isAsync()) {
                try {
                  setLibraryProperties(isolate, ref.getId(), true);
                } catch (IOException e) {

                }
              }
            }
          }
        } finally {
          latch.countDown();
        }
      }
    });

    try {
      latch.await();
    }
    catch (InterruptedException e) {

    }
  }

  public void evaluateLibrary(final VmIsolate isolate, VmLibrary vmLibrary, String expression,
                              final VmCallback<VmValue> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "evaluateExpr");
      request.put(
        "params",
        new JSONObject().put("libraryId", vmLibrary.getLibraryId()).put("expression", expression));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmValue> evalResult = convertEvaluateObjectResult(isolate, result);

          callback.handleResult(evalResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void evaluateObject(final VmIsolate isolate, VmClass vmClass, String expression,
                             final VmCallback<VmValue> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "evaluateExpr");
      request.put(
        "params",
        new JSONObject().put("classId", vmClass.getClassId()).put("expression", expression));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmValue> evalResult = convertEvaluateObjectResult(isolate, result);

          callback.handleResult(evalResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void evaluateObject(final VmIsolate isolate, VmValue value, String expression,
                             final VmCallback<VmValue> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "evaluateExpr");
      request.put(
        "params",
        new JSONObject().put("objectId", value.getObjectId()).put("expression", expression));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmValue> evalResult = convertEvaluateObjectResult(isolate, result);

          callback.handleResult(evalResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void evaluateOnCallFrame(final VmIsolate isolate, VmCallFrame callFrame,
                                  String expression, final VmCallback<VmValue> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "evaluateExpr");
      request.put(
        "params",
        new JSONObject().put("frameId", callFrame.getFrameId()).put("expression", expression));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmValue> evalResult = convertEvaluateObjectResult(isolate, result);

          callback.handleResult(evalResult);
        }
      });
    } catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public VmClass getClassInfoSync(VmIsolate isolate, int classId) {
    if (!isolate.hasClassInfo(classId)) {
      populateClassInfo(isolate, classId);
    }

    return isolate.getClassInfo(classId);
  }

  public VmClass getClassInfoSync(VmObject obj) {
    if (obj.getClassId() == -1) {
      return null;
    }

    return getClassInfoSync(obj.getIsolate(), obj.getClassId());
  }

  public String getClassNameSync(VmObject obj) {
    VmClass vmClass = getClassInfoSync(obj);

    if (vmClass == null) {
      return "";
    }
    else {
      VmIsolate isolate = obj.getIsolate();

      return isolate.getClassName(obj.getClassId());
    }
  }

  public void getClassProperties(final VmIsolate isolate, final int classId,
                                 final VmCallback<VmClass> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getClassProperties");
      request.put("params", new JSONObject().put("classId", classId));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmClass> vmClassResult = convertGetClassPropertiesResult(
            isolate,
            classId,
            result);

          callback.handleResult(vmClassResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void getGlobalVariables(final VmIsolate isolate, final int libraryId,
                                 final VmCallback<List<VmVariable>> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getGlobalVariables");
      request.put("params", new JSONObject().put("libraryId", libraryId));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<List<VmVariable>> retValue = convertGetGlobalVariablesResult(isolate, result);

          callback.handleResult(retValue);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void getIsolateIds(final VmCallback<List<Integer>> callback) throws IOException {
    sendSimpleCommand("getIsolateIds", -1, new Callback() {
      @Override
      public void handleResult(JSONObject result) throws JSONException {
        callback.handleResult(convertGetIsolateIdsResult(result));
      }
    });
  }

  public void getLibraries(VmIsolate isolate, final VmCallback<List<VmLibraryRef>> callback)
    throws IOException {
    if (isolate.isPaused()) {
      sendSimpleCommand("getLibraries", isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          callback.handleResult(convertGetLibrariesResult(result));
        }
      });
    }
    else {
      VmResult<List<VmLibraryRef>> result = new VmResult<List<VmLibraryRef>>();
      result.setResult(new ArrayList<VmLibraryRef>());
      callback.handleResult(result);
    }
  }

  public void getLibraryProperties(final VmIsolate isolate, final int libraryId,
                                   final VmCallback<VmLibrary> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getLibraryProperties");
      request.put("params", new JSONObject().put("libraryId", libraryId));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmLibrary> retValue = convertGetLibraryPropertiesResult(
            isolate,
            libraryId,
            result);

          callback.handleResult(retValue);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public VmLibrary getLibraryPropertiesSync(VmIsolate isolate, int libraryId) {
    if (!isolate.hasLibraryInfo(libraryId)) {
      populateLibraryInfo(isolate, libraryId);
    }

    return isolate.getLibraryInfo(libraryId);
  }

  public VmLibrary getLibraryPropertiesSync(VmObject obj) {
    VmClass vmClass = getClassInfoSync(obj);

    if (vmClass == null) {
      return null;
    }

    return getLibraryPropertiesSync(obj.getIsolate(), vmClass.getLibraryId());
  }

  public int getLineNumberFromLocation(VmIsolate isolate, VmLocation location) {
    String cacheKey = location.getLibraryId() + ":" + location.getUrl();

    if (!lineNumberTableCache.containsKey(cacheKey)) {
      final CountDownLatch latch = new CountDownLatch(1);
      final VmLineNumberTable[] result = new VmLineNumberTable[1];

      try {
        getLineNumberTable(
          isolate,
          location.getLibraryId(),
          location.getUrl(),
          new VmCallback<VmLineNumberTable>() {
            @Override
            public void handleResult(VmResult<VmLineNumberTable> r) {
              result[0] = r.getResult();

              latch.countDown();
            }
          }
        );
      }
      catch (IOException ex) {
        latch.countDown();
      }

      try {
        latch.await();
      }
      catch (InterruptedException e) {

      }

      lineNumberTableCache.put(cacheKey, result[0]);
    }

    VmLineNumberTable lineNumberTable = lineNumberTableCache.get(cacheKey);

    if (lineNumberTable == null) {
      return 0;
    }
    else {
      return lineNumberTable.getLineForLocation(location);
    }
  }

  public void getLineNumberTable(final VmIsolate isolate, final int libraryId,
                                 final String eclipseUrl, final VmCallback<VmLineNumberTable> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    final String vmUrl = VmUtils.eclipseUrlToVm(eclipseUrl);

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getLineNumberTable");
      request.put("params", new JSONObject().put("libraryId", libraryId).put("url", vmUrl));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmLineNumberTable> vmObjectResult = convertGetLineNumberTableResult(
            isolate,
            libraryId,
            eclipseUrl,
            result);

          callback.handleResult(vmObjectResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void getListElements(final VmIsolate isolate, int listObjectId, int index,
                              final VmCallback<VmValue> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getListElements");
      request.put("params", new JSONObject().put("objectId", listObjectId).put("index", index));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmValue> vmObjectResult = convertGetListElementsResult(isolate, result);

          callback.handleResult(vmObjectResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void getObjectProperties(final VmIsolate isolate, final int objectId,
                                  final VmCallback<VmObject> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getObjectProperties");
      request.put("params", new JSONObject().put("objectId", objectId));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          VmResult<VmObject> vmObjectResult = convertGetObjectPropertiesResult(
            isolate,
            objectId,
            result);

          callback.handleResult(vmObjectResult);
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  /**
   * This synchronous, potentially long-running call returns the cached source for the given
   * libraryId and source url.
   *
   * @param isolate
   * @param libraryId
   * @param url
   * @return
   */
  public String getScriptSource(VmIsolate isolate, final int libraryId, String url) {
    final String cacheKey = libraryId + ":" + url;

    if (!sourceCache.containsKey(cacheKey)) {
      final CountDownLatch latch = new CountDownLatch(1);

      try {
        getScriptSourceAsync(isolate, libraryId, url, new VmCallback<String>() {
          @Override
          public void handleResult(VmResult<String> result) {
            if (result.isError()) {
              sourceCache.put(cacheKey, null);
            }
            else {
              sourceCache.put(cacheKey, result.getResult());
            }

            latch.countDown();
          }
        });
      }
      catch (IOException e) {
        sourceCache.put(cacheKey, null);
        latch.countDown();
      }

      try {
        latch.await();
      }
      catch (InterruptedException e) {

      }
    }

    return sourceCache.get(cacheKey);
  }

  public void getScriptSourceAsync(VmIsolate isolate, int libraryId, String url,
                                   final VmCallback<String> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getScriptSource");
      request.put("params", new JSONObject().put("libraryId", libraryId).put("url", url));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          callback.handleResult(convertGetScriptSourceResult(result));
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void getScriptURLs(VmIsolate isolate, int libraryId,
                            final VmCallback<List<String>> callback) throws IOException {
    if (callback == null) {
      throw new IllegalArgumentException("a callback is required");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "getScriptURLs");
      request.put("params", new JSONObject().put("libraryId", libraryId));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject result) throws JSONException {
          callback.handleResult(convertGetScriptURLsResult(result));
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void getStackTrace(final VmIsolate isolate, final VmCallback<List<VmCallFrame>> callback)
    throws IOException {
    sendSimpleCommand("getStackTrace", isolate.getId(), new Callback() {
      @Override
      public void handleResult(JSONObject result) throws JSONException {
        callback.handleResult(convertGetStackTraceResult(isolate, result));
      }
    });
  }

  /**
   * Send an interrupt command to the given isolate.
   *
   * @param isolate
   * @throws IOException
   */
  public void interrupt(VmIsolate isolate) throws IOException {
    sendSimpleCommand("interrupt", isolate.getId());
  }

  /**
   * If the given isolate is running, send it a pause ('interrupt') command. Return an object that
   * can be used to undo the operation. If the interrupt was already paused, no interrupt command
   * will be sent and the returned VmInterruptResult object will represent a no-op.
   * <p/>
   * Ex:
   * <p/>
   * <pre>
   *   VmInterruptResult interruptResult = connection.interruptConditionally(isolate);
   *   ...do work to the now paused isolate...
   *   interruptResult.resume();
   * </pre>
   *
   * @param isolate
   * @return
   * @throws IOException
   */
  public VmInterruptResult interruptConditionally(VmIsolate isolate) throws IOException {
    if (!isolate.isPaused()) {
      interrupt(isolate);

      isolate.setTemporarilyInterrupted(true);
      isolate.setPaused(true);

      return VmInterruptResult.createResumeResult(this, isolate);
    }
    else {
      return VmInterruptResult.createNoopResult(this);
    }
  }

  /**
   * @return whether the connection is still open
   */
  public boolean isConnected() {
    return socket != null;
  }

  public void removeBreakpoint(VmIsolate isolate, final VmBreakpoint breakpoint) throws IOException {
    try {
      JSONObject request = new JSONObject();

      request.put("command", "removeBreakpoint");
      request.put("params", new JSONObject().put("breakpointId", breakpoint.getBreakpointId()));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject object) throws JSONException {
          // Update the list of breakpoints based on the result code.
          VmResult<?> result = VmResult.createFrom(object);

          if (!result.isError()) {
            breakpoints.remove(breakpoint);
          }
        }
      });

      breakpoints.remove(breakpoint);
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  public void removeListener(VmListener listener) {
    listeners.remove(listener);
  }

  public void resume(VmIsolate isolate) throws IOException {
    sendSimpleCommand("resume", isolate.getId(), resumeOnSuccess(isolate));
  }

  /**
   * Set a breakpoint in the given file and line.
   *
   * @param isolate
   * @param url
   * @param line
   * @param callback
   * @throws IOException
   */
  public void setBreakpoint(final VmIsolate isolate, final String url, final int line,
                            final VmCallback<VmBreakpoint> callback) throws IOException {
    if (!isolate.isPaused()) {
      throw new IOException("attempt to set breakpoint on a running isolate");
    }

    try {
      JSONObject request = new JSONObject();

      request.put("command", "setBreakpoint");
      request.put(
        "params",
        new JSONObject().put("url", VmUtils.eclipseUrlToVm(url)).put("line", line));

      sendRequest(request, isolate.getId(), new Callback() {
        @Override
        public void handleResult(JSONObject object) throws JSONException {
          VmResult<VmBreakpoint> result = new VmResult<VmBreakpoint>();

          if (!object.has("error")) {
            int breakpointId = JsonUtils.getInt(object.getJSONObject("result"), "breakpointId");

            VmBreakpoint breakpoint = new VmBreakpoint(isolate, null, breakpointId);

            breakpoints.add(breakpoint);

            result.setResult(breakpoint);
          }
          else {
            result.setError(object.getString("error"));
          }

          if (callback != null) {
            callback.handleResult(result);
          }
        }
      });
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }

    try {
      // TODO(devoncarew): workaround for bug https://code.google.com/p/dart/issues/detail?id=9705
      // We need to give the VM time to process all the events before we start sending more.
      // There's some race condition going on in the VM's queue.
      Thread.sleep(10);
    }
    catch (InterruptedException e) {

    }
  }

  /**
   * Set the given library's properties; currently this enables / disables stepping into the
   * library.
   *
   * @param libraryId
   * @param debuggingEnabled
   * @throws IOException
   */
  public void setLibraryProperties(VmIsolate isolate, int libraryId, boolean debuggingEnabled)
    throws IOException {
    try {
      JSONObject request = new JSONObject();

      request.put("command", "setLibraryProperties");
      request.put(
        "params",
        new JSONObject().put("libraryId", libraryId).put(
          "debuggingEnabled",
          Boolean.toString(debuggingEnabled))
      );

      sendRequest(request, isolate.getId(), null);
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  /**
   * Set the VM to pause on exceptions.
   *
   * @param isolate
   * @param kind
   * @throws IOException
   */
  public void setPauseOnException(VmIsolate isolate, BreakOnExceptionsType kind) throws IOException {
    setPauseOnException(isolate, kind, null);
  }

  /**
   * Set the VM to pause on exceptions.
   *
   * @param isolate
   * @param kind
   * @param callback
   * @throws IOException
   */
  public void setPauseOnException(VmIsolate isolate, BreakOnExceptionsType kind,
                                  final VmCallback<Boolean> callback) throws IOException {
    try {
      JSONObject request = new JSONObject();

      request.put("command", "setPauseOnException");
      request.put("params", new JSONObject().put("exceptions", kind.toString()));

      if (callback == null) {
        sendRequest(request, isolate.getId(), null);
      }
      else {
        sendRequest(request, isolate.getId(), new Callback() {
          @Override
          public void handleResult(JSONObject result) throws JSONException {
            VmResult<Boolean> callbackResult = VmResult.createFrom(result);
            callbackResult.setResult(true);
            callback.handleResult(callbackResult);
          }
        });
      }
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  /**
   * Set the VM to pause on exceptions.
   *
   * @param isolate
   * @param kind
   * @throws IOException
   */
  public void setPauseOnExceptionSync(VmIsolate isolate, BreakOnExceptionsType kind)
    throws IOException {
    final CountDownLatch latch = new CountDownLatch(1);

    try {
      setPauseOnException(isolate, kind, new VmCallback<Boolean>() {
        @Override
        public void handleResult(VmResult<Boolean> result) {
          latch.countDown();
        }
      });
    }
    catch (IOException ioe) {
      latch.countDown();
    }
    catch (Throwable t) {
      latch.countDown();
      throw new RuntimeException(t);
    }

    try {
      latch.await();
    }
    catch (InterruptedException e) {

    }
  }

  public void stepInto(VmIsolate isolate) throws IOException {
    isStepping = true;
    stepCommand = "stepInto";

    sendSimpleCommand(stepCommand, isolate.getId(), resumeOnSuccess(isolate));
  }

  public void stepOut(VmIsolate isolate) throws IOException {
    sendSimpleCommand("stepOut", isolate.getId(), resumeOnSuccess(isolate));
  }

  public void stepOver(VmIsolate isolate) throws IOException {
    isStepping = true;
    stepCommand = "stepOver";

    sendSimpleCommand(stepCommand, isolate.getId(), resumeOnSuccess(isolate));
  }

  public synchronized void handleTerminated() {
    // Clean up the callbackMap on termination.
    List<Callback> callbacks = new ArrayList<VmConnection.Callback>(callbackMap.values());

    for (Callback callback : callbacks) {
      try {
        callback.handleResult(VmResult.createJsonErrorResult("connection termination"));
      }
      catch (JSONException e) {

      }
    }

    callbackMap.clear();
  }

  protected void processJson(final JSONObject result) {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        try {
          if (LOG.isDebugEnabled()) {
            LOG.debug("<== (" + Thread.currentThread().getName() + ")" + result);
          }

          if (result.has("id")) {
            processResponse(result);
          }
          else {
            processNotification(result);
          }
        }
        catch (IOException exception) {
          LOG.info(exception);
        }
        catch (JSONException exception) {
          LOG.info(exception);
        }
        //catch (Throwable exception) {
        //  LOG.info(exception);
        //}
      }
    });
  }

  protected void sendSimpleCommand(String command, int isolateId) throws IOException {
    sendSimpleCommand(command, isolateId, null);
  }

  protected void sendSimpleCommand(String command, int isolateId, Callback callback)
    throws IOException {
    try {
      JSONObject request = new JSONObject();

      request.put("command", command);

      sendRequest(request, isolateId, callback);
    }
    catch (JSONException exception) {
      throw new IOException(exception);
    }
  }

  void sendRequest(JSONObject request, int isolateId, Callback callback) throws IOException {
    int id = 0;

    try {
      if (!isConnected()) {
        if (callback != null) {
          callback.handleResult(VmResult.createJsonErrorResult("connection termination"));
        }

        return;
      }

      if (!request.has("params")) {
        request.put("params", new JSONObject());
      }

      JSONObject params = request.getJSONObject("params");

      if (!params.has("isolateId") && isolateId != -1) {
        params.put("isolateId", isolateId);
      }
    }
    catch (JSONException jse) {
      throw new IOException(jse);
    }

    synchronized (this) {
      id = nextCommandId++;

      try {
        request.put("id", id);
      }
      catch (JSONException ex) {
        throw new IOException(ex);
      }

      if (callback != null) {
        callbackMap.put(id, callback);
      }
    }

    try {
      send(request.toString());
    }
    catch (IOException ex) {
      if (callback != null) {
        synchronized (this) {
          callbackMap.remove(id);
        }
      }

      throw ex;
    }
  }

  private VmResult<VmValue> convertEvaluateObjectResult(VmIsolate isolate, JSONObject object)
    throws JSONException {
    VmResult<VmValue> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmValue.createFrom(isolate, object.getJSONObject("result")));
    }

    return result;
  }

  private VmResult<VmClass> convertGetClassPropertiesResult(VmIsolate isolate, int classId,
                                                            JSONObject object) throws JSONException {
    VmResult<VmClass> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmClass.createFrom(isolate, object.getJSONObject("result")));
      result.getResult().setClassId(classId);
    }

    return result;
  }

  private VmResult<List<VmVariable>> convertGetGlobalVariablesResult(VmIsolate isolate,
                                                                     JSONObject object) throws JSONException {
    VmResult<List<VmVariable>> result = VmResult.createFrom(object);

    if (object.has("result")) {
      JSONObject jsonResult = object.getJSONObject("result");

      result.setResult(VmVariable.createFrom(isolate, jsonResult.optJSONArray("globals"), false));
    }

    return result;
  }

  private VmResult<List<Integer>> convertGetIsolateIdsResult(JSONObject object)
    throws JSONException {
    VmResult<List<Integer>> result = VmResult.createFrom(object);

    if (object.has("result")) {
      JSONArray arr = object.getJSONObject("result").optJSONArray("isolateIds");

      List<Integer> isolateIds = new ArrayList<Integer>();

      for (int i = 0; i < arr.length(); i++) {
        isolateIds.add(new Integer(arr.getInt(i)));
      }

      result.setResult(isolateIds);
    }

    return result;
  }

  private VmResult<List<VmLibraryRef>> convertGetLibrariesResult(JSONObject object)
    throws JSONException {
    VmResult<List<VmLibraryRef>> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmLibraryRef.createFrom(object.getJSONObject("result").optJSONArray(
        "libraries")));
    }

    return result;
  }

  private VmResult<VmLibrary> convertGetLibraryPropertiesResult(VmIsolate isolate, int libraryId,
                                                                JSONObject object) throws JSONException {
    VmResult<VmLibrary> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmLibrary.createFrom(isolate, libraryId, object.getJSONObject("result")));
    }

    return result;
  }

  private VmResult<VmLineNumberTable> convertGetLineNumberTableResult(VmIsolate isolate,
                                                                      int libraryId, String url, JSONObject object) throws JSONException {
    VmResult<VmLineNumberTable> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmLineNumberTable.createFrom(
        isolate,
        libraryId,
        url,
        object.getJSONObject("result")));
    }

    return result;
  }

  private VmResult<VmValue> convertGetListElementsResult(VmIsolate isolate, JSONObject object)
    throws JSONException {
    VmResult<VmValue> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmValue.createFrom(isolate, object.getJSONObject("result")));
    }

    return result;
  }

  private VmResult<VmObject> convertGetObjectPropertiesResult(VmIsolate isolate, int objectId,
                                                              JSONObject object) throws JSONException {
    VmResult<VmObject> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(VmObject.createFrom(isolate, object.getJSONObject("result")));
      result.getResult().setObjectId(objectId);
    }

    return result;
  }

  private VmResult<String> convertGetScriptSourceResult(JSONObject object) throws JSONException {
    VmResult<String> result = VmResult.createFrom(object);

    if (object.has("result")) {
      result.setResult(object.getJSONObject("result").getString("text"));
    }

    return result;
  }

  private VmResult<List<String>> convertGetScriptURLsResult(JSONObject object) throws JSONException {
    VmResult<List<String>> result = VmResult.createFrom(object);

    if (object.has("result")) {
      List<String> libUrls = new ArrayList<String>();

      JSONArray arr = object.getJSONObject("result").getJSONArray("urls");

      for (int i = 0; i < arr.length(); i++) {
        libUrls.add(VmUtils.vmUrlToEclipse(arr.getString(i)));
      }

      result.setResult(libUrls);
    }

    return result;
  }

  private VmResult<List<VmCallFrame>> convertGetStackTraceResult(VmIsolate isolate,
                                                                 JSONObject object) throws JSONException {
    VmResult<List<VmCallFrame>> result = VmResult.createFrom(object);

    if (object.has("result")) {
      List<VmCallFrame> frames = VmCallFrame.createFrom(
        isolate,
        object.getJSONObject("result").getJSONArray("callFrames"));

      result.setResult(frames);
    }

    return result;
  }

  private VmIsolate getCreateIsolate(int isolateId) {
    if (isolateId == -1) {
      return null;
    }

    if (isolateMap.get(isolateId) == null) {
      isolateMap.put(isolateId, new VmIsolate(isolateId));
    }

    return isolateMap.get(isolateId);
  }

  private void handleBreakpointResolved(VmIsolate isolate, int breakpointId, VmLocation location) {
    VmBreakpoint breakpoint = null;

    synchronized (breakpoints) {
      for (VmBreakpoint bp : breakpoints) {
        if (bp.getBreakpointId() == breakpointId) {
          breakpoint = bp;

          break;
        }
      }
    }

    if (breakpoint == null) {
      breakpoint = new VmBreakpoint(isolate, location, breakpointId);

      breakpoints.add(breakpoint);
    }
    else {
      breakpoint.updateLocation(location);
    }

    for (VmListener listener : listeners) {
      listener.breakpointResolved(isolate, breakpoint);
    }
  }

  private void notifyDebuggerResumed(VmIsolate isolate) {
    isolate.clearClassInfoMap();

    for (VmListener listener : listeners) {
      listener.debuggerResumed(isolate);
    }
  }

  private void populateClassInfo(final VmIsolate isolate, final int classId) {
    final CountDownLatch latch = new CountDownLatch(1);

    try {
      getClassProperties(isolate, classId, new VmCallback<VmClass>() {
        @Override
        public void handleResult(VmResult<VmClass> result) {
          if (!result.isError()) {
            isolate.setClassInfo(classId, result.getResult());
          }

          latch.countDown();
        }
      });
    }
    catch (IOException e1) {
      latch.countDown();
    }

    try {
      latch.await();
    }
    catch (InterruptedException e) {

    }
  }

  private void populateLibraryInfo(final VmIsolate isolate, final int libraryId) {
    final CountDownLatch latch = new CountDownLatch(1);

    try {
      getLibraryProperties(isolate, libraryId, new VmCallback<VmLibrary>() {
        @Override
        public void handleResult(VmResult<VmLibrary> result) {
          if (!result.isError()) {
            isolate.setLibraryInfo(libraryId, result.getResult());
          }

          latch.countDown();
        }
      });
    }
    catch (IOException e1) {
      latch.countDown();
    }

    try {
      latch.await();
    }
    catch (InterruptedException e) {

    }
  }

  private void processNotification(JSONObject result) throws JSONException, IOException {
    if (result.has("event")) {
      String eventName = result.getString("event");
      JSONObject params = result.optJSONObject("params");

      if (eventName.equals(EVENT_PAUSED)) {
        int isolateId = params.optInt("isolateId", -1);
        String reason = params.optString("reason", null);
        VmIsolate isolate = getCreateIsolate(isolateId);
        VmValue exception = VmValue.createFrom(isolate, params.optJSONObject("exception"));
        VmLocation location = VmLocation.createFrom(isolate, params.optJSONObject("location"));

        isolate.setPaused(true);

        if (!"interrupted".equals(reason) || !isolate.isTemporarilyInterrupted()) {
          sendDelayedDebuggerPaused(PausedReason.parse(reason), isolate, location, exception);
        }
      }
      else if (eventName.equals(EVENT_BREAKPOINTRESOLVED)) {
        // { "event": "breakpointResolved", "params": {"breakpointId": 2, "url": "file:///Users/devoncarew/tools/eclipse_37/eclipse/samples/time/time_server.dart", "line": 19 }}

        int breakpointId = params.optInt("breakpointId");
        int isolateId = params.optInt("isolateId");
        VmIsolate isolate = getCreateIsolate(isolateId);
        VmLocation location = VmLocation.createFrom(isolate, params.getJSONObject("location"));

        handleBreakpointResolved(isolate, breakpointId, location);
      }
      else if (eventName.equals(EVENT_ISOLATE)) {
        // { "event": "isolate", "params": { "reason": "created", "id": 7114 }}]
        // { "event": "isolate", "params": { "reason": "shutdown", "id": 7114 }}]

        String reason = params.optString("reason", null);
        int isolateId = params.optInt("id", -1);

        final VmIsolate isolate = getCreateIsolate(isolateId);

        if ("created".equals(reason)) {
          for (VmListener listener : listeners) {
            listener.isolateCreated(isolate);
          }
        }
        else if ("shutdown".equals(reason)) {
          for (VmListener listener : listeners) {
            listener.isolateShutdown(isolate);
          }

          isolate.setPaused(false);

          isolateMap.remove(isolate.getId());
        }
      }
      else {
        LOG.info("no handler for notification: " + eventName);
      }
    }
    else {
      LOG.info("event not understood: " + result);
    }
  }

  private void processResponse(JSONObject result) throws JSONException {
    // Process a command response.
    int id = result.getInt("id");

    Callback callback;

    synchronized (this) {
      callback = callbackMap.remove(id);
    }

    if (callback != null) {
      callback.handleResult(result);
    }
    else if (result.has("error")) {
      // If we get an error back, and nobody was listening for the result, then log it.
      VmResult<?> vmResult = VmResult.createFrom(result);

      LOG.info("Error from command id " + id + ": " + vmResult.getError());
    }
  }

  private void processVmEvents(InputStream in) throws IOException {
    Reader reader = new InputStreamReader(in, UTF8);

    JSONObject obj = readJson(reader);

    while (obj != null) {
      processJson(obj);

      obj = readJson(reader);
    }
  }

  private JSONObject readJson(Reader in) throws IOException {
    StringBuilder builder = new StringBuilder();

    boolean inQuote = false;
    boolean ignoreLast = false;
    int curlyCount = 0;

    int c = in.read();

    while (true) {
      if (c == -1) {
        throw new EOFException();
      }

      builder.append((char)c);

      if (!ignoreLast) {
        if (c == '"') {
          inQuote = !inQuote;
        }
      }

      if (inQuote && c == '\\') {
        ignoreLast = true;
      }
      else {
        ignoreLast = false;
      }

      if (!inQuote) {
        if (c == '{') {
          curlyCount++;
        }
        else if (c == '}') {
          curlyCount--;

          if (curlyCount == 0) {
            try {
              String str = builder.toString();

              // TODO(devoncarew): we know this is occurring for exception text.
              // Possibly from toString() invocations?
              if (str.indexOf('\n') != -1) {
                LOG.error("bad json from vm: " + str);

                str = str.replace("\n", "\\n");
              }

              return new JSONObject(str);
            }
            catch (JSONException e) {
              throw new IOException(e);
            }
          }
        }
      }

      c = in.read();
    }
  }

  private Callback resumeOnSuccess(final VmIsolate isolate) {
    return new Callback() {
      @Override
      public void handleResult(JSONObject result) throws JSONException {
        VmResult<String> response = VmResult.createFrom(result);
        isolate.setTemporarilyInterrupted(false);

        if (!response.isError()) {
          isolate.setPaused(false);

          notifyDebuggerResumed(isolate);
        }
      }
    };
  }

  /**
   * Return whether the given vm locations represent the same source line.
   */
  private boolean sameSourceLine(VmLocation location1, VmLocation location2) {
    if (location1 == null || location2 == null) {
      return false;
    }

    int line1 = getLineNumberFromLocation(location1.getIsolate(), location1);
    int line2 = getLineNumberFromLocation(location2.getIsolate(), location2);

    if (line1 <= 0 || line2 <= 0) {
      return false;
    }

    return line1 == line2;
  }

  private void send(String str) throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("==> (" + Thread.currentThread().getName() + ")" + str);
    }

    byte[] bytes = str.getBytes(UTF8);

    out.write(bytes);
    out.flush();
  }

  private void sendDelayedDebuggerPaused(final PausedReason reason, final VmIsolate isolate,
                                         final VmLocation location, final VmValue exception) throws JSONException, IOException {
    // If we're stepping, check here to see if we should continue stepping.
    if (reason == PausedReason.breakpoint && isStepping && sameSourceLine(currentLocation, location)) {
      sendSimpleCommand(stepCommand, isolate.getId());
    }
    else {
      try {
        getStackTrace(isolate, new VmCallback<List<VmCallFrame>>() {
          @Override
          public void handleResult(VmResult<List<VmCallFrame>> result) {
            try {
              if (result.isError()) {
                LOG.info(result.getError());
              }
              else {
                List<VmCallFrame> frames = result.getResult();

                for (VmListener listener : listeners) {
                  listener.debuggerPaused(reason, isolate, frames, exception, isStepping);
                }
              }
            }
            finally {
              currentLocation = location;
              isStepping = false;
            }
          }
        });
      }
      catch (IOException e) {
        throw new JSONException(e);
      }
    }
  }
}
