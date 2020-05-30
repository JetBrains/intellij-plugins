// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

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
