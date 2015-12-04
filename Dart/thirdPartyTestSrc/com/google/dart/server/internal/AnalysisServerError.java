/*
 * Copyright (c) 2014, the Dart project authors.
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
package com.google.dart.server.internal;

import com.google.dart.server.AnalysisServerListener;

/**
 * A wrapper test class to hold onto the fields passed back from
 * {@link AnalysisServerListener#serverError(boolean, String, String)}.
 */
public class AnalysisServerError {
  /**
   * The error code associated with the error.
   */
  private final boolean isFatal;

  /**
   * The error message.
   */
  private final String message;

  /**
   * The stack trace.
   */
  private final String stackTrace;

  public AnalysisServerError(boolean isFatal, String message, String stackTrace) {
    this.isFatal = isFatal;
    this.message = message;
    this.stackTrace = stackTrace;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof AnalysisServerError) {
      AnalysisServerError other = (AnalysisServerError) object;
      return isFatal == other.isFatal && message.equals(other.message)
          && stackTrace.equals(other.stackTrace);
    }
    return false;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @return the stackTrace
   */
  public String getStackTrace() {
    return stackTrace;
  }

  /**
   * @return isFatal
   */
  public boolean isFatal() {
    return isFatal;
  }
}
