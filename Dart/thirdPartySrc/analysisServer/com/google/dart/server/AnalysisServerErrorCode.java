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

package com.google.dart.server;

/**
 * The enumeration {@code AnalysisServerErrorCode} defines problems that are reported to
 * {@link AnalysisServerListener}.
 * 
 * @coverage dart.server
 */
public enum AnalysisServerErrorCode {
  DISCONNECTED("The connection to the server has been lost"),
  EXCEPTION("An exception happened %s"),
  INVALID_CONTEXT_ID("Cannot find a context with the id '%s'"),
  INVALID_REFACTORING_ID("Cannot find a refactoring with the id '%s'");

  /**
   * The template used to create the message to be displayed for this error.
   */
  private final String message;

  /**
   * Initialize a newly created error code to have the given message.
   * 
   * @param message the message template used to create the message to be displayed for the error
   */
  private AnalysisServerErrorCode(String message) {
    this.message = message;
  }

  /**
   * Return the template used to create the message to be displayed for this error. The message
   * should indicate what is wrong and why it is wrong.
   * 
   * @return the template used to create the message to be displayed for this error
   */
  public String getMessage() {
    return message;
  }
}
