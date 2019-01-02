angular.module('youtrack.newFeaturesPopup')
  .directive('createTitlesId', function() {
    return {
      restrict: 'A',
      scope: {
        ids: '=createTitlesId',
        anotherProp: "@"
      },
      link: function($scope, $element) {
        $scope.ids = $scope.ids || [];

        $element.find('.new-features-popup__title').each(function(index, node) {
          var title = node.textContent.trim();
          var id = title.toLowerCase().split(' ').join('-');
          node.setAttribute('id', id);

          $scope.ids.push({
            text: title,
            id: id
          });
        });
      }
    };
  })
    .directive('test-create-titles-ids', function () {
      return {
        templateUrl: './create-titles-ids-injected.html',
        scope: {
        },
        bindToController: {
          navData: "="
        }
      }
    });
