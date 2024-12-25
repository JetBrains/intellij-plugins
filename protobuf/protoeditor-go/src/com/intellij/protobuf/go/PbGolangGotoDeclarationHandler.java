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
package com.intellij.protobuf.go;

import com.goide.GoLanguage;
import com.goide.GoTypes;
import com.goide.psi.*;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.protobuf.shared.gencode.ProtoFromSourceComments;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Handles goto declaration from golang generated code to .proto files. */
public class PbGolangGotoDeclarationHandler implements GotoDeclarationHandler {

  @Override
  public PsiElement @Nullable [] getGotoDeclarationTargets(
      @Nullable PsiElement sourceElement, int offset, Editor editor) {
    if (sourceElement == null || DumbService.isDumb(sourceElement.getProject())) return PsiElement.EMPTY_ARRAY;
    return Optional.of(sourceElement)
        .filter(e -> e.getLanguage().is(GoLanguage.INSTANCE))
        .filter(LeafPsiElement.class::isInstance)
        .map(LeafPsiElement.class::cast)
        .filter(e -> e.getElementType().equals(GoTypes.IDENTIFIER))
        .map(e -> PsiTreeUtil.getParentOfType(e, GoReferenceExpressionBase.class))
        .map(GoReferenceExpressionBase::getReference)
        .map(PsiReference::resolve)
        .map(PbGolangGotoDeclarationHandler::convertToProtoSymbols)
        .filter(symbols -> !symbols.isEmpty())
        .map(symbols -> symbols.toArray(new PbSymbol[0]))
        .orElse(null);
  }

  /** Reference: https://developers.google.com/protocol-buffers/docs/reference/go-generated */
  private static Collection<PbSymbol> convertToProtoSymbols(PsiElement element) {
    PbFile pbFile = getPbFile(element);
    if (pbFile == null) {
      return ImmutableList.of();
    }
    QualifiedName convertedName = null;
    if (element instanceof GoTypeSpec) {
      convertedName = convertTypeSpec((GoTypeSpec) element);
    } else if (element instanceof GoMethodDeclaration) {
      convertedName = convertToProtoFieldOrMethodName((GoMethodDeclaration) element);
    } else if (element instanceof GoMethodSpec) {
      convertedName = convertToProtoServiceMethodName((GoMethodSpec) element);
    } else if (element instanceof GoFieldDefinition) {
      convertedName = convertToProtoFieldName((GoFieldDefinition) element);
    } else if (element instanceof GoConstDefinition) {
      convertedName = convertToProtoEnumValueName((GoConstDefinition) element);
    } else if (element instanceof GoFunctionDeclaration) {
      convertedName = convertToProtoServiceName((GoFunctionDeclaration) element);
    }
    if (convertedName == null) {
      return ImmutableList.of();
    }
    QualifiedName protoPackage = pbFile.getPackageQualifiedName();
    return pbFile.getLocalQualifiedSymbolMap().get(protoPackage.append(convertedName));
  }

  /**
   * Converts to oneof field, message or service name.
   *
   * <p>If the type has exactly one method attached, and that method is isSomething, then it's
   * probably a oneof field, otherwise delegate to {@link
   * #convertToProtoMessageOrServiceName(GoTypeSpec)}.
   */
  private static QualifiedName convertTypeSpec(GoTypeSpec type) {
    List<GoNamedSignatureOwner> methods = type.getAllMethods();
    if (methods.size() == 1) {
      String methodName = methods.get(0).getName();
      if (methodName != null && methodName.startsWith("is")) {
        QualifiedName qualifiedFieldName = underscoreToQualifiedName(type.getName());
        if (qualifiedFieldName == null || qualifiedFieldName.getLastComponent() == null) {
          return null;
        }
        String fieldName = qualifiedFieldName.getLastComponent();
        fieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
        return qualifiedFieldName.removeLastComponent().append(fieldName);
      }
    }
    return convertToProtoMessageOrServiceName(type);
  }

  /**
   * Converts to either message or service name.
   *
   * <p>FooBarClient or FooBarServer converts to service FooBar, everything else converts to
   * message.
   */
  private static @Nullable QualifiedName convertToProtoMessageOrServiceName(GoTypeSpec type) {
    QualifiedName messageOrServiceName = underscoreToQualifiedName(type.getName());
    if (messageOrServiceName == null) {
      return null;
    }
    QualifiedName serviceName = convertToServiceName(messageOrServiceName);
    return serviceName != null ? serviceName : messageOrServiceName;
  }

  private static @Nullable QualifiedName underscoreToQualifiedName(@Nullable String name) {
    return Optional.ofNullable(name)
        .map(n -> n.replace('_', '.'))
        .map(QualifiedName::fromDottedString)
        .orElse(null);
  }

  private static @Nullable QualifiedName convertToServiceName(QualifiedName serviceQualifiedName) {
    String serviceName =
        Optional.of(serviceQualifiedName)
            .filter(qn -> qn.getComponentCount() == 1)
            .map(QualifiedName::getLastComponent)
            .orElse(null);
    if (serviceName == null) {
      return null;
    }
    for (String suffix : ImmutableList.of("Client", "Server", "ClientInterface")) {
      if (serviceName.endsWith(suffix)) {
        serviceName = serviceName.substring(0, serviceName.lastIndexOf(suffix));
        return QualifiedName.fromComponents(serviceName);
      }
    }
    return null;
  }

  /**
   * Converts to either message field or service method name.
   *
   * <p>GetFooBar converts to field foo_bar, everything else converts to method.
   */
  private static @Nullable QualifiedName convertToProtoFieldOrMethodName(GoMethodDeclaration method) {
    QualifiedName messageOrServiceName =
        Optional.of(method)
            .map(GoMethodDeclaration::getReceiverType)
            .filter(GoPointerType.class::isInstance)
            .map(GoPointerType.class::cast)
            .map(GoPointerType::getType)
            .map(GoType::contextlessResolve)
            .filter(GoTypeSpec.class::isInstance)
            .map(GoTypeSpec.class::cast)
            .map(PbGolangGotoDeclarationHandler::convertToProtoMessageOrServiceName)
            .orElse(null);
    String methodName = method.getName();
    if (messageOrServiceName == null || methodName == null) {
      return null;
    }
    String fieldPrefix = "Get";
    if (methodName.startsWith(fieldPrefix)) {
      String goFieldName = methodName.substring(fieldPrefix.length());
      String pbFieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, goFieldName);
      return messageOrServiceName.append(pbFieldName);
    }
    return messageOrServiceName.append(methodName);
  }

  /**
   * Interface method (which is spec instead of declaration) converts to service method in proto.
   */
  private static @Nullable QualifiedName convertToProtoServiceMethodName(GoMethodSpec method) {
    QualifiedName serviceName =
        Optional.ofNullable(PsiTreeUtil.getParentOfType(method, GoTypeSpec.class))
            .map(PbGolangGotoDeclarationHandler::convertToProtoMessageOrServiceName)
            .orElse(null);
    String methodName = method.getName();
    if (methodName == null || serviceName == null) {
      return null;
    }
    return serviceName.append(methodName);
  }

  private static @Nullable QualifiedName convertToProtoFieldName(GoFieldDefinition field) {
    QualifiedName messageName =
        Optional.ofNullable(PsiTreeUtil.getParentOfType(field, GoTypeSpec.class))
            .map(PbGolangGotoDeclarationHandler::convertToProtoMessageOrServiceName)
            .orElse(null);
    String goFieldName = field.getName();
    if (messageName == null || goFieldName == null) {
      return null;
    }
    String pbFieldName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, goFieldName);
    return messageName.append(pbFieldName);
  }

  private static @Nullable QualifiedName convertToProtoEnumValueName(GoConstDefinition definition) {
    QualifiedName enumTypeName =
        Optional.ofNullable(PsiTreeUtil.getParentOfType(definition, GoConstSpec.class))
            .map(GoConstSpec::getType)
            .map(GoType::contextlessResolve)
            .filter(GoTypeSpec.class::isInstance)
            .map(GoTypeSpec.class::cast)
            .map(PbGolangGotoDeclarationHandler::convertToProtoMessageOrServiceName)
            .orElse(null);
    String valueName = definition.getName();
    if (enumTypeName == null || valueName == null) {
      return null;
    }
    String prefix;
    if (enumTypeName.getComponentCount() == 1) {
      // If there's only one component, then the enum is top level
      // the go enum value will be prefixed by the enum type,
      // but the proto value will be directly under the proto package.
      prefix = enumTypeName.getLastComponent() + '_';
      enumTypeName = enumTypeName.removeLastComponent();
    } else if (enumTypeName.getComponentCount() > 1) {
      // If there's more than one component then this is an enum nested under a message
      // the go enum value will be prefixed by the containing enum type (with parent message),
      // but the proto value will be under the containing message type (*without* the enum type).
      enumTypeName = enumTypeName.removeLastComponent();
      prefix = enumTypeName.join("_") + '_';
    } else {
      // Shouldn't happen.
      return null;
    }
    if (valueName.startsWith(prefix)) {
      return enumTypeName.append(valueName.substring(prefix.length()));
    }
    return null;
  }

  /** NewFooClient is a standalone function to produce a client for the Foo service. */
  private static @Nullable QualifiedName convertToProtoServiceName(GoFunctionDeclaration function) {
    String functionName = function.getName();
    if (functionName == null) {
      return null;
    }
    String prefix = "New";
    String suffix = "Client";
    if (functionName.startsWith(prefix) && functionName.endsWith(suffix)) {
      String serviceName =
          functionName.substring(prefix.length(), functionName.lastIndexOf(suffix));
      return QualifiedName.fromComponents(serviceName);
    }
    return null;
  }

  private static @Nullable PbFile getPbFile(PsiElement element) {
    return Optional.of(element)
        .map(PsiElement::getContainingFile)
        .filter(GoFile.class::isInstance)
        .map(GoFile.class::cast)
        .map(PbGolangGotoDeclarationHandler::getPbFile)
        .orElse(null);
  }

  private static @Nullable PbFile getPbFile(GoFile goFile) {
    VirtualFile resolvedFile = goFile.getVirtualFile();
    if (resolvedFile == null || !resolvedFile.getName().endsWith(".pb.go")) {
      return null;
    }
    PsiFile resolvedPsiFile = goFile.getManager().findFile(resolvedFile);
    if (resolvedPsiFile == null) {
      return null;
    }
    return ProtoFromSourceComments.findProtoOfGeneratedCode("//", resolvedPsiFile);
  }
}
