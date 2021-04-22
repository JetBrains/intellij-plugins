/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.template;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.protobuf.lang.PbLanguage;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.NotNull;

/** Defines a Live Template context for protobuf files types. */
class PbLanguageContext extends TemplateContextType {

  PbLanguageContext() {
    super("PROTO", "Protocol Buffers");
  }

  @Override
  public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
    return PbLanguage.INSTANCE.is(PsiUtilCore.getLanguageAtOffset(
        templateActionContext.getFile(), templateActionContext.getStartOffset()));
  }

  /** Base context that returns true when the closest parent block is of the given type. */
  abstract static class BlockBodyContext extends TemplateContextType {

    private final Class<? extends PbBlockBody> bodyClass;

    BlockBodyContext(String id, String presentableName, Class<? extends PbBlockBody> bodyClass) {
      super(id, presentableName, PbLanguageContext.class);
      this.bodyClass = bodyClass;
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
      PsiElement element = PsiUtilCore.getElementAtOffset(
          templateActionContext.getFile(), templateActionContext.getStartOffset());
      return bodyClass.isInstance(
          PsiTreeUtil.getParentOfType(element, PbBlockBody.class, /* strict */ false));
    }
  }

  /** {@link TemplateContextType} implementation that matches within an extend body. */
  static class ExtendBody extends BlockBodyContext {
    ExtendBody() {
      super("PROTO_EXTEND", "Extend", PbExtendBody.class);
    }
  }

  /** {@link TemplateContextType} implementation that matches within an enum. */
  static class EnumBody extends BlockBodyContext {
    EnumBody() {
      super("PROTO_ENUM", "Enum", PbEnumBody.class);
    }
  }

  /** {@link TemplateContextType} implementation that matches within a message. */
  static class MessageBody extends BlockBodyContext {
    MessageBody() {
      super("PROTO_MESSAGE", "Message", PbMessageBody.class);
    }
  }

  /** {@link TemplateContextType} implementation that matches within a oneof. */
  static class OneofBody extends BlockBodyContext {
    OneofBody() {
      super("PROTO_ONEOF", "Oneof", PbOneofBody.class);
    }
  }

  /** {@link TemplateContextType} implementation that matches within a service. */
  static class ServiceBody extends BlockBodyContext {
    ServiceBody() {
      super("PROTO_SERVICE", "Service", PbServiceBody.class);
    }
  }
}
