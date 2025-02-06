angular.module('sample', [
      'ui.router'
    ])

    .config(
        ['$stateProvider', '$urlRouterProvider',
          function ($stateProvider,   $urlRouterProvider) {
            $stateProvider.state('one', {
              views: {
                'menuTip' :
              }})
                .state('two', {
              views: {
                '' : {
                  templateUrl: 'two.html'
                }
              }
            });
          }]);
