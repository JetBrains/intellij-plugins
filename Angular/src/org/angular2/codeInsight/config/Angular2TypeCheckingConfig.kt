/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
package org.angular2.codeInsight.config

data class Angular2TypeCheckingConfig (
  /**
   * Whether to check the left-hand side type of binding operations.
   *
   * For example, if this is `false` then the expression `[input]="expr"` will have `expr` type-
   * checked, but not the assignment of the resulting type to the `input` property of whichever
   * directive or component is receiving the binding. If set to `true`, both sides of the assignment
   * are checked.
   *
   * This flag only affects bindings to components/directives. Bindings to the DOM are checked if
   * `checkTypeOfDomBindings` is set.
   */
  val checkTypeOfInputBindings: Boolean,

  /**
   * Whether to honor the access modifiers on input bindings for the component/directive.
   *
   * If a template binding attempts to assign to an input that is private/protected/readonly,
   * this will produce errors when enabled but will not when disabled.
   */
  val honorAccessModifiersForInputBindings: Boolean,

  /**
   * Whether to use strict null types for input bindings for directives.
   *
   * If this is `true`, applications that are compiled with TypeScript's `strictNullChecks` enabled
   * will produce type errors for bindings which can evaluate to `undefined` or `null` where the
   * inputs's type does not include `undefined` or `null` in its type. If set to `false`, all
   * binding expressions are wrapped in a non-null assertion operator to effectively disable strict
   * null checks. This may be particularly useful when the directive is from a library that is not
   * compiled with `strictNullChecks` enabled.
   *
   * If `checkTypeOfInputBindings` is set to `false`, this flag has no effect.
   */
  val strictNullInputBindings: Boolean,

  /**
   * Whether to check text attributes that happen to be consumed by a directive or component.
   *
   * For example, in a template containing `<input matInput disabled>` the `disabled` attribute ends
   * up being consumed as an input with type `Boolean` by the `matInput` directive. At runtime, the
   * input will be set to the attribute's string value, which is an empty string for attributes
   * without a value, so with this flag set to `true`, an error would be reported. If set to
   * `false`, text attributes will never report an error.
   *
   * Note that if `checkTypeOfInputBindings` is set to `false`, this flag has no effect.
   */
  val checkTypeOfAttributes: Boolean,

  /**
   * Whether to check the left-hand side type of binding operations to DOM properties.
   *
   * As `checkTypeOfBindings`, but only applies to bindings to DOM properties.
   *
   * This does not affect the use of the `DomSchemaChecker` to validate the template against the DOM
   * schema. Rather, this flag is an experimental, not yet complete feature which uses the
   * lib.dom.d.ts DOM typings in TypeScript to validate that DOM bindings are of the correct type
   * for assignability to the underlying DOM element properties.
   */
  val checkTypeOfDomBindings: Boolean,

  /**
   * Whether to infer the type of the `$event` variable in event bindings for directive outputs or
   * animation events.
   *
   * If this is `true`, the type of `$event` will be inferred based on the generic type of
   * `EventEmitter`/`Subject` of the output. If set to `false`, the `$event` variable will be of
   * type `any`.
   */
  val checkTypeOfOutputEvents: Boolean,

  /**
   * Whether to infer the type of the `$event` variable in event bindings for animations.
   *
   * If this is `true`, the type of `$event` will be `AnimationEvent` from `@angular/animations`.
   * If set to `false`, the `$event` variable will be of type `any`.
   */
  val checkTypeOfAnimationEvents: Boolean,

  /**
   * Whether to infer the type of the `$event` variable in event bindings to DOM events.
   *
   * If this is `true`, the type of `$event` will be inferred based on TypeScript's
   * `HTMLElementEventMap`, with a fallback to the native `Event` type. If set to `false`, the
   * `$event` variable will be of type `any`.
   */
  val checkTypeOfDomEvents: Boolean,

  /**
   * Whether to infer the type of local references to DOM elements.
   *
   * If this is `true`, the type of a `#ref` variable on a DOM node in the template will be
   * determined by the type of `document.createElement` for the given DOM node type. If set to
   * `false`, the type of `ref` for DOM nodes will be `any`.
   */
  val checkTypeOfDomReferences: Boolean,


  /**
   * Whether to infer the type of local references.
   *
   * If this is `true`, the type of a `#ref` variable that points to a directive or `TemplateRef` in
   * the template will be inferred correctly. If set to `false`, the type of `ref` for will be
   * `any`.
   */
  val checkTypeOfNonDomReferences: Boolean,

  /**
   * Whether to adjust the output of the TCB to ensure compatibility with the `TemplateTypeChecker`.
   *
   * The statements generated in the TCB are optimized for performance and producing diagnostics.
   * These optimizations can result in generating a TCB that does not have all the information
   * needed by the `TemplateTypeChecker` for retrieving `Symbol`s. For example, as an optimization,
   * the TCB will not generate variable declaration statements for directives that have no
   * references, inputs, or outputs. However, the `TemplateTypeChecker` always needs these
   * statements to be present in order to provide `ts.Symbol`s and `ts.Type`s for the directives.
   *
   * When set to `false`, enables TCB optimizations for template diagnostics.
   * When set to `true`, ensures all information required by `TemplateTypeChecker` to
   * retrieve symbols for template nodes is available in the TCB.
   */
  val enableTemplateTypeChecker: Boolean,

  /**
   * Whether to include type information from pipes in the type-checking operation.
   *
   * If this is `true`, then the pipe's type signature for `transform()` will be used to check the
   * usage of the pipe. If this is `false`, then the result of applying a pipe will be `any`, and
   * the types of the pipe's value and arguments will not be matched against the `transform()`
   * method.
   */
  val checkTypeOfPipes: Boolean,

  /**
   * Whether to narrow the types of template contexts.
   */
  val applyTemplateContextGuards: Boolean,

  /**
   * Whether to use a strict type for null-safe navigation operations.
   *
   * If this is `false`, then the return type of `a?.b` or `a?()` will be `any`. If set to `true`,
   * then the return type of `a?.b` for example will be the same as the type of the ternary
   * expression `a != null ? a.b : a`.
   */
  val strictSafeNavigationTypes: Boolean,

  /**
   * Whether to descend into template bodies and check any bindings there.
   */
  val checkTemplateBodies: Boolean,

  /**
   * Whether to always apply DOM schema checks in template bodies, independently of the
   * `checkTemplateBodies` setting.
   */
  val alwaysCheckSchemaInTemplateBodies: Boolean,

  /**
   * Whether to check if control flow syntax will prevent a node from being projected.
   */
  val controlFlowPreventingContentProjection: ControlFlowPreventingContentProjectionKind,

  /**
   * Whether to use any generic types of the context component.
   *
   * If this is `true`, then if the context component has generic types, those will be mirrored in
   * the template type-checking context. If `false`, any generic type parameters of the context
   * component will be set to `any` during type-checking.
   */
  val useContextGenericType: Boolean,

  /**
   * Whether or not to infer types for object and array literals in the template.
   *
   * If this is `true`, then the type of an object or an array literal in the template will be the
   * same type that TypeScript would infer if the literal appeared in code. If `false`, then such
   * literals are cast to `any` when declared.
   */
  val strictLiteralTypes: Boolean,

  /**
   * Whether to use inline type constructors.
   *
   * If this is `true`, create inline type constructors when required. For example, if a type
   * constructor's parameters has private types, it cannot be created normally, so we inline it in
   * the directives definition file.
   *
   * If false, do not create inline type constructors. Fall back to using `any` type for
   * constructors that normally require inlining.
   *
   * This option requires the environment to support inlining. If the environment does not support
   * inlining, this must be set to `false`.
   */
  val useInlineTypeConstructors: Boolean,

  /**
   * Whether or not to produce diagnostic suggestions in cases where the compiler could have
   * inferred a better type for a construct, but was prevented from doing so by the current type
   * checking configuration.
   *
   * For example, if the compiler could have used a template context guard to infer a better type
   * for a structural directive's context and `let-` variables, but the user is in
   * `fullTemplateTypeCheck` mode and such guards are therefore disabled.
   *
   * This mode is useful for clients like the Language Service which want to inform users of
   * opportunities to improve their own developer experience.
   */
  val suggestionsForSuboptimalTypeInference: Boolean,

  /**
   * Whether the type of two-way bindings should be widened to allow `WritableSignal`.
   */
  val allowSignalsInTwoWayBindings: Boolean,

  /**
   * Whether to descend into the bodies of control flow blocks (`@if`, `@switch` and `@for`).
   */
  val checkControlFlowBodies: Boolean,
) {
  enum class ControlFlowPreventingContentProjectionKind {
    Error,
    Warning,
    Suppress
  }
}