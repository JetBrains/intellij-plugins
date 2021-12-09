/**
 * @ngdoc directive
 * @name ng.directive:ngModel
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngChange
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngRepeat
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngApp
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngBind
 * @element ANY
 * @param {expression} ngBind {@link guide/expression Expression} to evaluate.
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngController
 * @param {expression} ngController Name of a globally accessible constructor function or an
 *     {@link guide/expression expression} that on the current scope evaluates to a
 *     constructor function. The controller instance can be published into a scope property
 *     by specifying `as propertyName`.
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngInclude
 */

/**
 * @ngdoc event
 * @name ng.directive:ngInclude#$includeContentRequested
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngSrc
 * @element IMG
 */

/**
 * @ngdoc directive
 * @name ng.directive:form
 * @restrict E
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngDisabled
 * @element input
 */

/**
 * @ngdoc directive
 * @name ngOptions
 * @restrict A
 *
 * @param {string} ngModel Assignable AngularJS expression to data-bind to.
 * @param {comprehension_expression} ngOptions in one of the following forms:
 * @param {string=} name Property name of the form under which the control is published.
 * @param {string=} required The control is considered valid only if value is entered.
 * @param {string=} ngRequired Adds `required` attribute and `required` validation constraint to
 *    the element when the ngRequired expression evaluates to true. Use `ngRequired` instead of
 *    `required` when you want to data-bind to the `required` attribute.
 * @param {string=} ngAttrSize sets the size of the select element dynamically. Uses the
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngCopy
 * @element window, input, select, textarea, a
 */
angular.directive({ngModel: ngModelDirective})
