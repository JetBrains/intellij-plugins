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
 * @name ng.directive:ngApp
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngBind
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
 * @name ng.directive:ngInit
 * @param {expression} ngInit {@link guide/expression Expression} to eval.
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngRepeat
 * @param {repeat_expression} ngRepeat The expression indicating how to enumerate a collection. These
 *   formats are currently supported:
 */
/**
 * @ngdoc directive
 * @name ngShow
 *
 * @element ANY
 * @param {expression} ngShow If the {@link guide/expression expression} is truthy
 *     then the element is shown or hidden respectively.
*/
/**
 * @ngdoc directive
 * @name select
 * @restrict E
 *
 * @param {string} ngModel Assignable angular expression to data-bind to.
 * @param {string=} name Property name of the form under which the control is published.
 * @param {string=} multiple Allows multiple options to be selected. The selected values will be
 *     bound to the model as an array.
 * @param {string=} required Sets `required` validation error key if the value is not entered.
 * @param {string=} ngRequired Adds required attribute and required validation constraint to
 * the element when the ngRequired expression evaluates to true. Use ngRequired instead of required
 * when you want to data-bind to the required attribute.
 * @param {string=} ngChange Angular expression to be executed when selected option(s) changes due to user
 *    interaction with the select element.
 * @param {string=} ngOptions sets the options that the select is populated with and defines what is
 * set on the model on selection. See {@link ngOptions `ngOptions`}.
 * @param {string=} ngAttrSize sets the size of the select element dynamically. Uses the
 * {@link guide/interpolation#-ngattr-for-binding-to-arbitrary-attributes ngAttr} directive.
 */
/**
 * @ngdoc directive
 * @name ng.directive:ngIf
 * @param {expression} ngIf The expression indicating how to enumerate a collection. These
 *   formats are currently supported:
 */
angular.directive({ngModel: ngModelDirective})
