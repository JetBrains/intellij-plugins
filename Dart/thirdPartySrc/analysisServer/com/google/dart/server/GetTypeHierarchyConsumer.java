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
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;

import java.util.List;

/**
 * The interface {@code TypeHierarchyConsumer} defines the behavior of objects that consume type
 * hierarchy.
 * 
 * @coverage dart.server
 */
public interface GetTypeHierarchyConsumer extends Consumer {
  /**
   * Type hierarchy has been computed.
   * 
   * @param hierarchyItems A list of the types in the requested hierarchy. The first element of the
   *          list is the item representing the type for which the hierarchy was requested. The
   *          index of other elements of the list is unspecified, but correspond to the integers
   *          used to reference supertype and subtype items within the items. This field will be
   *          absent if the code at the given file and offset does not represent a type, or if the
   *          file has not been sufficiently analyzed to allow a type hierarchy to be produced.
   */
  public void computedHierarchy(List<TypeHierarchyItem> hierarchyItems);

  /**
   * If hierarchy items cannot be passed back, some {@link RequestError} is passed back instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
