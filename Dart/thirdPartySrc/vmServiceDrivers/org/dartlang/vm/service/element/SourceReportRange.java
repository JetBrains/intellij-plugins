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

import com.google.gson.JsonObject;
import java.util.List;

/**
 * The {@link SourceReportRange} class represents a range of executable code (function, method,
 * constructor, etc) in the running program. It is part of a SourceReport.
 */
@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public class SourceReportRange extends Element {

  public SourceReportRange(JsonObject json) {
    super(json);
  }

  /**
   * Has this range been compiled by the Dart VM?
   */
  public boolean getCompiled() {
    return json.get("compiled").getAsBoolean();
  }

  /**
   * Code coverage information for this range.  Provided only when the Coverage report has been
   * requested and the range has been compiled.
   *
   * Can return <code>null</code>.
   */
  public SourceReportCoverage getCoverage() {
    return json.get("coverage") == null ? null : new SourceReportCoverage((JsonObject) json.get("coverage"));
  }

  /**
   * The token position at which this range ends.  Inclusive.
   */
  public int getEndPos() {
    return json.get("endPos") == null ? -1 : json.get("endPos").getAsInt();
  }

  /**
   * The error while attempting to compile this range, if this report was generated with
   * forceCompile=true.
   *
   * Can return <code>null</code>.
   */
  public ErrorRef getError() {
    return json.get("error") == null ? null : new ErrorRef((JsonObject) json.get("error"));
  }

  /**
   * Possible breakpoint information for this range, represented as a sorted list of token
   * positions.  Provided only when the when the PossibleBreakpoint report has been requested and
   * the range has been compiled.
   *
   * Can return <code>null</code>.
   */
  public List<Integer> getPossibleBreakpoints() {
    return getListInt("possibleBreakpoints");
  }

  /**
   * An index into the script table of the SourceReport, indicating which script contains this
   * range of code.
   */
  public int getScriptIndex() {
    return json.get("scriptIndex") == null ? -1 : json.get("scriptIndex").getAsInt();
  }

  /**
   * The token position at which this range begins.
   */
  public int getStartPos() {
    return json.get("startPos") == null ? -1 : json.get("startPos").getAsInt();
  }
}
