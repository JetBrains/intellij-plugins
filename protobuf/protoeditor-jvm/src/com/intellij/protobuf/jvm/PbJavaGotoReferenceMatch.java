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
package com.intellij.protobuf.jvm;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.protobuf.lang.psi.PbDefinition;
import com.intellij.protobuf.lang.psi.PbFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Check if a resolved java reference refers to elements generated from protos, and gathers
 * contextual information like the containing class.
 */
public class PbJavaGotoReferenceMatch {

  // All protobuf generated code inherits from classes/interfaces in this package.
  private static final String PROTO2_PACKAGE = "com.google.protobuf";
  private static final String PROTO2_PACKAGE_WITH_DOT = PROTO2_PACKAGE + ".";
  private static final String PROTO1_PACKAGE = "com.google.io.protocol";
  private static final String PROTO1_PACKAGE_WITH_DOT = PROTO1_PACKAGE + ".";

  /**
   * Check if the given reference refers to something generated from protos.
   *
   * @param resolvedReference the resolved element from a java reference
   * @return If matches, return contextual information needed to resolve the reference to the
   *     original .proto element. Returns null if not a match.
   */
  @Nullable
  public static PbJavaGotoDeclarationContext isFromProto(PsiElement resolvedReference) {
    PsiNamedElement resolved = ObjectUtils.tryCast(resolvedReference, PsiNamedElement.class);
    if (resolved == null) {
      return null;
    }
    // We only search for the Class in Class.X<caret>YZ, and check if Class is a generated class.
    // This means it won't handle things like Class.nanoRepeatedField.len<caret>gth.
    // Eventually we should handle messageInstance<caret>Variable, etc. as well.
    PsiClass classContext = PsiTreeUtil.getContextOfType(resolved, PsiClass.class, false);
    if (classContext == null || !isPbGeneratedDefinition(classContext)) {
      return null;
    }
    PsiClass fileClass = getFileClass(classContext);
    // Bail if we can't figure out the qualified name. This could be because the class is
    // a type parameter, etc.
    if (fileClass.getQualifiedName() == null) {
      return null;
    }
    return new PbJavaGotoDeclarationContext(resolved, classContext, fileClass);
  }

  /**
   * Get the generated outermost class. It could represent the {@link
   * PbFile}, or, if java_multiple_files is set,
   * then it could just be the outermost {@link
   * PbDefinition}.
   */
  @NotNull
  private static PsiClass getFileClass(@NotNull PsiClass definitionClass) {
    PsiClass containingClass = definitionClass;
    while (true) {
      PsiClass next = containingClass.getContainingClass();
      if (next == null) {
        return containingClass;
      }
      containingClass = next;
    }
  }

  /**
   * Returns true if the class extends/implements a known protobuf base class, and so it is
   * pertaining to a definition (could be a builder too).
   */
  private static boolean isPbGeneratedDefinition(PsiClass psiClass) {
    if (psiClass.getContainingClass() != null
        && isPbGeneratedDefinition(psiClass.getContainingClass())) {
      return true;
    }
    // We could use PsiClass#getSupers and cover both, but that eagerly resolves all the classes.
    Set<PsiClass> visited = new HashSet<>();
    return derivesFromProto(psiClass.getExtendsListTypes(), visited)
        || derivesFromProto(psiClass.getImplementsListTypes(), visited);
  }

  private static boolean derivesFromProto(PsiClassType[] superTypes, Set<PsiClass> visited) {
    return Arrays.stream(superTypes).anyMatch(classType -> isProtoClass(classType, visited));
  }

  private static boolean isProtoClass(PsiClassType superType, Set<PsiClass> visited) {
    PsiClass resolved = superType.resolve();
    if (resolved == null) {
      return false;
    }
    if (!visited.add(resolved)) {
      return false;
    }
    String qualifiedName = resolved.getQualifiedName();
    if (qualifiedName == null) {
      return false;
    }
    // We could be stricter, and check for specific interfaces like "GeneratedMessage",
    // or "GeneratedMessageV3". For now, just check that it's rooted in the protobuf packages
    if (qualifiedName.startsWith(PROTO2_PACKAGE_WITH_DOT)
        || qualifiedName.startsWith(PROTO1_PACKAGE_WITH_DOT)) {
      return true;
    }
    // There are experimental options to let folks control what a message extends or implements,
    // so we have to check further parents.
    return derivesFromProto(resolved.getExtendsListTypes(), visited)
        || derivesFromProto(resolved.getImplementsListTypes(), visited);
  }
}
