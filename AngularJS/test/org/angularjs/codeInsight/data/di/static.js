angular.module('desktop-shared').factory('DomUtils', function ()
{
  'use strict';

  /**
   * @name DomUtils
   * @desc Provides a number of utilities relating to manipulating DOM
   * @namespace
   */
  var DomUtils = {};


  /**
   * Create SVG element template
   *
   * @function
   * @static
   * @param {String} iconName
   * @param {String} [className=''] optional classname to apply to svg element
   * @param {String} [svgWidth] optional svgWidth to apply to svg element
   * @return {String} SVG template
   */
  DomUtils.createSVGElementTemplate = function(iconName, className, svgWidth)
  {
    if (iconName.indexOf('icon-') !== 0)
    {
      iconName = 'icon-' + iconName;
    }

    if (!svgWidth)
    {
      svgWidth = 16;
    }

    className = className || '';
    return '<svg class="iconSvg ' + iconName + ' ' + className + '" viewBox="0 0 ' + svgWidth + ' 16" preserveAspectRatio="xMidYMid meet"><use xlink:href="images/SVGIcons.svg#' + iconName + '"></use></svg>';
  };

  return DomUtils;
}).directive('cmcTable',
    function ($compile, $timeout, DomUtils)
    {
      'use strict';

      var directive = {};
      directive.restrict = 'E';
      directive.replace = true;
      directive.link = function (scope)
      {
          for (var i = 0; i < 10; i++)
          {
            var column = {};
            column.className = "test";
            column.sortable = true;
            column.headerTooltipText = 'test';
            column.headerTextHidden = false;
            column.headerText = 'test';

            var columnScope = scope.$new();
            columnScope.column = column;

            var columnElementHtml = '<div class="' + column.className + '"';

            if (column.sortable)
            {
              columnElementHtml += ' ng-click="sort(column)"';
            }

            if (column.headerTooltipText)
            {
              columnElementHtml += ' title="' + column.headerTooltipText + '"';
            }

            columnElementHtml += '><span>';

            if (!column.headerTextHidden)
            {
              columnElementHtml += column.headerText;
            }

            columnElementHtml += '</span>';

            if (column.sortable)
            {
              columnElementHtml += DomUtils.create<caret>SVGElementTemplate('icon-sort') + DomUtils.createSVGElementTemplate('icon-sort-up') +
              DomUtils.createSVGElementTemplate('icon-sort-down');
            }

            columnElementHtml += '</div>';

            console.log(columnElementHtml);
          }
        };

      return directive;
    });

