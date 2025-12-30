package org.angular2.lang.expr.service.tcb

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptSingleType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.psi.PsiElement

internal class TypeParameterEmitter(private val typeParameters: Array<TypeScriptTypeParameter>) {

  /**
   * Determines whether the type parameters can be emitted. If this returns true, then a call to
   * `emit` is known to succeed. Vice versa, if false is returned then `emit` should not be
   * called, as it would fail.
   */
  fun canEmit(canEmitReference: (ref: TypeScriptSingleType) -> Boolean): Boolean {
    if (this.typeParameters.isEmpty()) {
      return true
    }

    return this.typeParameters.all { typeParam ->
      this.canEmitType(typeParam.typeConstraint, canEmitReference) &&
      this.canEmitType(typeParam.default, canEmitReference)
    }
  }

  private fun canEmitType(type: TypeScriptType?, canEmitReference: (ref: TypeScriptSingleType) -> Boolean): Boolean {
    if (type == null) {
      return true
    }

    return internalCanEmitType(type) { typeReference ->
      val reference = this.resolveTypeReference(typeReference)
      if (reference == null)
        false
      else if (reference is TypeScriptSingleType)
        canEmitReference(reference)
      else
        true
    }
  }

  /**
   * Emits the type parameters using the provided emitter function for `Reference`s.
   */
  //fun emit(emitReference: (ref: TypeScriptSingleType) -> ts.Expression): ts.TypeParameterDeclaration[]? {
  //  if (this.typeParameters == undefined) {
  //    return undefined;
  //  }
  //
  //  val emitter = new TypeEmitter(type => this.translateTypeReference(type, emitReference));
  //
  //  return this.typeParameters.map(typeParam => {
  //    val constraint =
  //    typeParam.constraint !== undefined ? emitter.emitType(typeParam.constraint) : undefined;
  //    val defaultType =
  //    typeParam.default !== undefined ? emitter.emitType(typeParam.default) : undefined;
  //
  //    return ts.factory.updateTypeParameterDeclaration(
  //      typeParam, typeParam.modifiers, typeParam.name, constraint, defaultType);
  //  });
  //}

  private fun resolveTypeReference(type: TypeScriptSingleType): JSType? {
    return type.jsType
    //val target = ts.isIdentifier(type.typeName) ? type.typeName : type.typeName.right
    //val declaration = this.reflector.getDeclarationOfIdentifier(target)
    //
    //// If no declaration could be resolved or does not have a `ts.Declaration`, the type cannot be
    //// resolved.
    //if (declaration == null || declaration.node == null) {
    //  return null
    //}
    //
    //// If the declaration corresponds with a local type parameter, the type reference can be used
    //// as is.
    //if (this.isLocalTypeParameter(declaration.node)) {
    //  return type
    //}
    //
    //var owningModule: OwningModule? = null
    //if (typeof declaration.viaModule == 'string') {
    //  owningModule = {
    //    specifier: declaration.viaModule,
    //    resolutionContext: type.getSourceFile().fileName,
    //  }
    //}
    //
    //return new Reference(
    //  declaration.node, declaration.viaModule == AmbientImport ? AmbientImport : owningModule)
  }

  //private fun translateTypeReference(type: ts.TypeReferenceNode, emitReference: (ref: Reference) => ts.TypeNode | null): ts.TypeReferenceNode? {
  //  val reference = this.resolveTypeReference(type);
  //  if (!(reference instanceof Reference)) {
  //    return reference;
  //  }
  //
  //  val typeNode = emitReference(reference);
  //  if (typeNode == null) {
  //    return null;
  //  }
  //
  //  if (!ts.isTypeReferenceNode(typeNode)) {
  //    throw new Error(
  //      `Expected TypeReferenceNode for emitted reference, got ${ts.SyntaxKind[typeNode.kind]}.`);
  //  }
  //  return typeNode;
  //}

  private fun isLocalTypeParameter(decl: TypeScriptTypeParameter): Boolean {
    // Checking for local type parameters only occurs during resolution of type parameters, so it is
    // guaranteed that type parameters are present.
    return this.typeParameters.any { param -> param == decl }
  }
}

private fun internalCanEmitType(type: TypeScriptType, canEmit: (type: TypeScriptSingleType) -> Boolean): Boolean {
  // To determine whether a type can be emitted, we have to recursively look through all type nodes.
  // If an unsupported type node is found at any position within the type, then the `INELIGIBLE`
  // constant is returned to stop the recursive walk as the type as a whole cannot be emitted in
  // that case. Otherwise, the result of visiting all child nodes determines the result. If no
  // ineligible type reference node is found then the walk returns `undefined`, indicating that
  // no type node was visited that could not be emitted.
  fun visitNode(node: PsiElement): Boolean {
    // `import('module')` type nodes are not supported, as it may require rewriting the module
    // specifier which is currently not done.
    //if (ts.isImportTypeNode(node)) {
    //  return false;
    //}

    // Emitting a type reference node in a different context requires that an import for the type
    // can be created. If a type reference node cannot be emitted, `INELIGIBLE` is returned to stop
    // the walk.
    if (node is TypeScriptSingleType) {
      if (!canEmit(node)) {
        return false
      }
      return node.typeArguments.all { visitNode(it) }
    }
    else {
      return node.children.all { visitNode(it) }
    }
  }

  return visitNode(type)
}
