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
package com.google.dart.server;

import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;
import java.util.Map;

/**
 * The interface {@code GetReachableSourcesConsumer} defines the behavior of objects that consume
 * the transitive closure of reachable sources for a given file.
 * 
 * @coverage dart.server
 */
public interface GetReachableSourcesConsumer extends Consumer {
  /**
   * @param sources a mapping from source URIs to directly reachable source URIs. For example, a
   *          file "foo.dart" that imports "bar.dart" would have the corresponding mapping {
   *          "file:///foo.dart" : ["file:///bar.dart"] }. If "bar.dart" has further imports (or
   *          exports) there will be a mapping from the URI "file:///bar.dart" to them. To check if
   *          a specific URI is reachable from a given file, clients can check for its presence in
   *          the resulting key set.
   */
  public void computedReachableSources(Map<String, List<String>> sources);

  /**
   * If a transitive closure cannot be passed back, some {@link RequestError} is passed back
   * instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
