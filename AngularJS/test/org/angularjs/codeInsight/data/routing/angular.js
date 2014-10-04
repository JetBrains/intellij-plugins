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
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngInit
 */

/**
 * @ngdoc directive
 * @name ng.directive:ngRepeat
 */

 /**
 * @ngdoc directive
 * @name ng.directive:ngInclude
 * @param {string} ngInclude|src angular expression evaluating to URL. If the source is a string constant,
 *                 make sure you wrap it in **single** quotes, e.g. `src="'myPartialTemplate.html'"`.
 * @param {string=} onload Expression to evaluate when a new partial is loaded.
 *
 * @param {string=} autoscroll Whether `ngInclude` should call {@link ng.$anchorScroll
 *                  $anchorScroll} to scroll the viewport after the content is loaded.
 *
 *                  - If the attribute is not set, disable scrolling.
 *                  - If the attribute is set without value, enable scrolling.
 *                  - Otherwise enable scrolling only if the expression evaluates to truthy value.
 */
angular.directive({ngModel: ngModelDirective})
