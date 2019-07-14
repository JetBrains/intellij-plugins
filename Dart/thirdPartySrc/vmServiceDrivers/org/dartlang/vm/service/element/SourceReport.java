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
import com.google.gson.JsonObject;

/**
 * The {@link SourceReport} class represents a set of reports tied to source locations in an
 * isolate.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class SourceReport extends Response {

  public SourceReport(JsonObject json) {
    super(json);
  }

  /**
   * A list of ranges in the program source.  These ranges correspond to ranges of executable code
   * in the user's program (functions, methods, constructors, etc.)
   *
   * Note that ranges may nest in other ranges, in the case of nested functions.
   *
   * Note that ranges may be duplicated, in the case of mixins.
   */
  public ElementList<SourceReportRange> getRanges() {
    return new ElementList<SourceReportRange>(json.get("ranges").getAsJsonArray()) {
      @Override
      protected SourceReportRange basicGet(JsonArray array, int index) {
        return new SourceReportRange(array.get(index).getAsJsonObject());
      }
    };
  }

  /**
   * A list of scripts, referenced by index in the report's ranges.
   */
  public ElementList<ScriptRef> getScripts() {
    return new ElementList<ScriptRef>(json.get("scripts").getAsJsonArray()) {
      @Override
      protected ScriptRef basicGet(JsonArray array, int index) {
        return new ScriptRef(array.get(index).getAsJsonObject());
      }
    };
  }
}
