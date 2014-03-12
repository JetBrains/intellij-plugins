/*
 * Copyright 2012 Dart project authors.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This class represents a VM variable.
 */
public class VmVariable extends VmRef implements Comparable<VmVariable> {

  static class LazyValue {
    private VmConnection connection;

    private VmValue listValue;
    private int index;

    public LazyValue(VmConnection connection, VmValue listValue, int index) {
      this.connection = connection;
      this.listValue = listValue;
      this.index = index;
    }

    public VmValue evaluate(VmIsolate isolate) {
      final VmValue[] result = new VmValue[1];

      final CountDownLatch latch = new CountDownLatch(1);

      try {
        connection.getListElements(
          isolate,
          listValue.getObjectId(),
          index,
          new VmCallback<VmValue>() {
            @Override
            public void handleResult(VmResult<VmValue> r) {
              result[0] = r.getResult();

              latch.countDown();
            }
          }
        );
      }
      catch (IOException e) {
        latch.countDown();
      }

      try {
        latch.await();
      }
      catch (InterruptedException e) {

      }

      return result[0];
    }
  }

  static VmVariable createArrayEntry(VmConnection connection, VmValue listValue, int index) {
    VmVariable var = new VmVariable(listValue.getIsolate());

    var.name = "[" + Integer.toString(index) + "]";
    // var.value is lazyily populated
    var.lazyValue = new LazyValue(connection, listValue, index);

    return var;
  }

  static List<VmVariable> createFrom(VmIsolate isolate, JSONArray arr, boolean isLocal)
    throws JSONException {
    if (arr == null) {
      return null;
    }

    List<VmVariable> variables = new ArrayList<VmVariable>();

    for (int i = 0; i < arr.length(); i++) {
      variables.add(createFrom(isolate, arr.getJSONObject(i), isLocal));
    }

    if (!isLocal) {
      Collections.sort(variables);
    }

    return variables;
  }

  static VmVariable createFrom(VmIsolate isolate, JSONObject obj, boolean isLocal) {
    // {"name":"server","value":{"objectId":4,"kind":"object","text":"Instance of '_HttpServer@14117cc4'"}}
    VmVariable var = new VmVariable(isolate);

    var.name = obj.optString("name");
    var.value = VmValue.createFrom(isolate, obj.optJSONObject("value"));
    var.isLocal = isLocal;

    return var;
  }

  static VmVariable createFromException(VmValue exception) {
    VmVariable variable = new VmVariable(exception.getIsolate());

    variable.name = "exception";
    variable.value = exception;
    variable.isException = true;

    return variable;
  }

  private VmValue value;

  private String name;

  private boolean isException;

  private boolean isLocal;

  private LazyValue lazyValue;

  private VmVariable(VmIsolate isolate) {
    super(isolate);
  }

  @Override
  public int compareTo(VmVariable other) {
    return getName().compareTo(other.getName());
  }

  public boolean getIsException() {
    return isException;
  }

  public String getName() {
    return name;
  }

  public VmValue getValue() {
    if (lazyValue != null) {
      value = lazyValue.evaluate(getIsolate());
      lazyValue = null;
    }

    return value;
  }

  public boolean isLocal() {
    return isLocal;
  }

  public boolean isThisObject() {
    return "this".equals(name);
  }

  @Override
  public String toString() {
    return "[" + getName() + "]";
  }
}
