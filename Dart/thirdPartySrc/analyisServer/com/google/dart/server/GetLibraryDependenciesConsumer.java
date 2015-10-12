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
 * The interface {@code GetLibraryDependenciesConsumer} defines the behavior of objects that consume
 * library dependency responses.
 * 
 * @coverage dart.server
 */
public interface GetLibraryDependenciesConsumer extends Consumer {

  /**
   * A list of computed dependent library paths.
   * 
   * @param libraries an array of computed library paths
   * @param packageMap a map of context source roots to maps of package names to lists of associated
   *          source directories
   */
  public void computedDependencies(String[] libraries,
      Map<String, Map<String, List<String>>> packageMap);

  /**
   * If a result cannot be passed back, some {@link RequestError} is passed back instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
