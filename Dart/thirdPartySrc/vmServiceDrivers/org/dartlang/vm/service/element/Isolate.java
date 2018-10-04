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
package org.dartlang.vm.service.element;

// This is a generated file.

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;

/**
 * An {@link Isolate} object provides information about one isolate in the VM.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class Isolate extends Response {

  public Isolate(JsonObject json) {
    super(json);
  }

  /**
   * A list of all breakpoints for this isolate.
   */
  public ElementList<Breakpoint> getBreakpoints() {
    return new ElementList<Breakpoint>(json.get("breakpoints").getAsJsonArray()) {
      @Override
      protected Breakpoint basicGet(JsonArray array, int index) {
        return new Breakpoint(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The error that is causing this isolate to exit, if applicable.
   *
   * Can return <code>null</code>.
   */
  public ErrorObj getError() {
    return json.get("error") == null ? null : new ErrorObj((JsonObject) json.get("error"));
  }

  /**
   * The current pause on exception mode for this isolate.
   */
  public ExceptionPauseMode getExceptionPauseMode() {
    JsonElement value = json.get("exceptionPauseMode");
    try {
      return value == null ? ExceptionPauseMode.Unknown : ExceptionPauseMode.valueOf(value.getAsString());
    } catch (IllegalArgumentException e) {
      return ExceptionPauseMode.Unknown;
    }
  }

  /**
   * The list of service extension RPCs that are registered for this isolate, if any.
   *
   * Can return <code>null</code>.
   */
  public List<String> getExtensionRPCs() {
    return json.get("extensionRPCs") == null ? null : getListString("extensionRPCs");
  }

  /**
   * The id which is passed to the getIsolate RPC to reload this isolate.
   */
  public String getId() {
    return json.get("id").getAsString();
  }

  /**
   * A list of all libraries for this isolate.
   *
   * Guaranteed to be initialized when the IsolateRunnable event fires.
   */
  public ElementList<LibraryRef> getLibraries() {
    return new ElementList<LibraryRef>(json.get("libraries").getAsJsonArray()) {
      @Override
      protected LibraryRef basicGet(JsonArray array, int index) {
        return new LibraryRef(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * The number of live ports for this isolate.
   */
  public int getLivePorts() {
    return json.get("livePorts") == null ? -1 : json.get("livePorts").getAsInt();
  }

  /**
   * A name identifying this isolate. Not guaranteed to be unique.
   */
  public String getName() {
    return json.get("name").getAsString();
  }

  /**
   * A numeric id for this isolate, represented as a string. Unique.
   */
  public String getNumber() {
    return json.get("number").getAsString();
  }

  /**
   * The last pause event delivered to the isolate. If the isolate is running, this will be a
   * resume event.
   */
  public Event getPauseEvent() {
    return new Event((JsonObject) json.get("pauseEvent"));
  }

  /**
   * Will this isolate pause when exiting?
   */
  public boolean getPauseOnExit() {
    return json.get("pauseOnExit").getAsBoolean();
  }

  /**
   * The root library for this isolate.
   *
   * Guaranteed to be initialized when the IsolateRunnable event fires.
   *
   * Can return <code>null</code>.
   */
  public LibraryRef getRootLib() {
    return json.get("rootLib") == null ? null : new LibraryRef((JsonObject) json.get("rootLib"));
  }

  /**
   * Is the isolate in a runnable state?
   */
  public boolean getRunnable() {
    return json.get("runnable").getAsBoolean();
  }

  /**
   * The time that the VM started in milliseconds since the epoch.
   *
   * Suitable to pass to DateTime.fromMillisecondsSinceEpoch.
   */
  public int getStartTime() {
    return json.get("startTime") == null ? -1 : json.get("startTime").getAsInt();
  }
}
