/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.structure;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;

/**
 * Element description for structure tree.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsTreeDescriptionLocation extends ElementDescriptionLocation {

  public static final StrutsTreeDescriptionLocation INSTANCE = new StrutsTreeDescriptionLocation();

  private static final ElementDescriptionProvider PROVIDER = new StrutsTreeDescriptionProvider();

  @Override
  public ElementDescriptionProvider getDefaultProvider() {
    return PROVIDER;
  }

}
