/*
 * Copyright (c) 2019, the Dart project authors.
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

import org.dartlang.analysis.server.protocol.ParameterInfo;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * The interface {@code GetSignatureConsumer} defines the behavior of objects that consume
 * a get signature request.
 *
 * @coverage dart.server
 */
public interface GetSignatureConsumer extends Consumer {
  /**
   * @param name           The name of the function being invoked at the given offset.
   * @param parameterInfos A list of information about each of the parameters of the function being invoked.
   * @param dartdoc        The dartdoc associated with the function being invoked. Other than the removal of the comment delimiters, including
   *                       leading asterisks in the case of a block comment, the dartdoc is unprocessed markdown. This data is omitted if there
   *                       is no referenced element, or if the element has no dartdoc.
   */
  public void computedSignature(String name, List<ParameterInfo> parameterInfos, String dartdoc);

  /**
   * If a transitive closure cannot be passed back, some {@link RequestError} is passed back
   * instead.
   *
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
