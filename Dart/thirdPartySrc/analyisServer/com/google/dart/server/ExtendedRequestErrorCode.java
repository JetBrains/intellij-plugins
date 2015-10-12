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

import org.dartlang.analysis.server.protocol.RequestErrorCode;

/**
 * An enumeration of additional types of errors that can occur in the execution of the server, that
 * are not defined by the analysis server specification.
 * 
 * @coverage dart.server
 */
public class ExtendedRequestErrorCode extends RequestErrorCode {

  /**
   * Some response from the server was not in JSON format, or did not have expected required
   * parameters as specified by the Analysis Server specification.
   */
  public static final String INVALID_SERVER_RESPONSE = "INVALID_SERVER_RESPONSE";
}
