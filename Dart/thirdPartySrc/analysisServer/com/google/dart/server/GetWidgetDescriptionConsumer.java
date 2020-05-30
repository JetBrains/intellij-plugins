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

import org.dartlang.analysis.server.protocol.FlutterWidgetProperty;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * The interface {@code GetWidgetDescriptionConsumer} defines the behavior of objects that consume a getWidgetDescription request.
 *
 * @coverage dart.server
 */
public interface GetWidgetDescriptionConsumer extends Consumer {
  /**
   * @param properties The list of properties of the widget. Some of the properties might be
   *                   read only, when their editor is not set. This might be
   *                   because they have type that we don't know how to edit, or for
   *                   compound properties that work as containers for sub-properties.
   */
  public void computedGetWidgetDescription(List<FlutterWidgetProperty> properties);

  /**
   * If a transitive closure cannot be passed back, some {@link RequestError} is passed back
   * instead.
   *
   * @param requestError the reason why a result was not passed back
   */
  public void onError(RequestError requestError);
}
