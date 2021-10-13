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

import org.dartlang.analysis.server.protocol.KytheEntry;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * The interface {@code GetKytheEntriesConsumer} defines the behavior of objects that consume a get Kythe entries request.
 *
 * @coverage dart.server
 */
public interface GetKytheEntriesConsumer extends Consumer {
  /**
   * @param entries The list of {@link KytheEntry} objects for the queried file.
   * @param files   The set of files paths that were required, but not in the file system, to give a complete and accurate Kythe graph for
   *                the file. This could be due to a referenced file that does not exist or generated files not being generated or passed
   *                before the call to "getKytheEntries".
   */
  void computedKytheEntries(List<KytheEntry> entries, List<String> files);

  /**
   * If a transitive closure cannot be passed back, some {@link RequestError} is passed back
   * instead.
   *
   * @param requestError the reason why a result was not passed back
   */
  void onError(RequestError requestError);
}
