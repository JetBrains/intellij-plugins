/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.communicator.core.transport;

import org.jdom.Element;

/**
 * These are chunks of information Transports exchange with.
 *
 * @author Kir
 */
public interface XmlMessage {
  String WHEN_ATTR = "when";

  String getTagName();
  String getTagNamespace();
  boolean needsResponse();

  /** The implementation should fill message data under element
   * @param element element created according to {@link #getTagName()} and {@link #getTagNamespace()};
   * can be used for providing additional message data
   */
  void fillRequest(Element element);

  /** Should be called with data - response to the current request if {@link #needsResponse()} returns true */
  void processResponse(Element responseElement);
}
