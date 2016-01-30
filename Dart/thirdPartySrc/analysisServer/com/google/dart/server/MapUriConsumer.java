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

/**
 * The interface {@code MapUriConsumer} defines the behavior of objects that consume map uri
 * responses.
 * 
 * @coverage dart.server
 */
public interface MapUriConsumer extends Consumer {
  /**
   * A file or uri to which the path was mapped.
   * 
   * @param file the file to which the URI was mapped. This field is omitted if the uri field was
   *          not given in the request
   * @param uri the URI to which the file path was mapped. This field is omitted if the file field
   *          was not given in the request
   */
  public void computedFileOrUri(String file, String uri);

  /**
   * If a file or uri was not mapped and cannot be passed back, some {@link RequestError} is passed
   * back instead.
   * 
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
