/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom;

import com.intellij.util.xml.*;

import java.util.Collection;

/**
 * Exclude the current element from completion/resolving variants. The element must have a {@link NameValue} annotation.
 *
 * @author Yann C&eacute;bron
 */
public abstract class FilterCurrentElementInVariantsResolvingConverter<T extends DomElement>
    extends ResolvingConverter<T> {

  protected Collection<? extends T> filterVariants(final ConvertContext context,
                                                   final Collection<? extends T> allVariants) {

    //noinspection unchecked
    final T currentElement = (T) DomUtil.getDomElement(context.getTag());
    assert currentElement != null : "currentElement was null for " + context.getTag();
    final GenericDomValue currentNameElement = currentElement.getGenericInfo().getNameDomElement(currentElement);
    if (currentNameElement == null) {
      return allVariants; // skip due to XML errors
    }

    final String currentName = currentNameElement.getStringValue();
    if (currentName == null) {
      return allVariants; // skip due to XML errors
    }

    final T currentElementInVariants = DomUtil.findByName(allVariants, currentName);
    if (currentElementInVariants != null) {
      //noinspection SuspiciousMethodCalls
      allVariants.remove(currentElementInVariants);
    }

    return allVariants;
  }

}