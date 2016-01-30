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

import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceChange;

import java.util.List;

/**
 * The interface {@code AssistsConsumer} defines the behavior of objects that consume assists
 * {@link SourceChange}s.
 * 
 * @coverage dart.server
 */
public interface GetAssistsConsumer extends Consumer {
  /**
   * A set of {@link SourceChange}s that have been computed.
   * 
   * @param proposals an array of computed {@link SourceChange}s
   */
  public void computedSourceChanges(List<SourceChange> sourceChanges);

  /**
   * If a set of assists cannot be passed back, some {@link RequestError} is passed back instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
