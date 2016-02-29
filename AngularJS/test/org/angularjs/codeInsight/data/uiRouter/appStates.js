angular.module('sample', [
      'ui.router'
    ])

    .config(
        ['$stateProvider', '$urlRouterProvider',
          function ($stateProvider,   $urlRouterProvider) {
            $stateProvider.state('one', {
              views: {
                '' : {
                  templateUrl: 'one.html'
                },
                'menuTip': {}
              })
                .state('two', {
              views: {
                '' : {
                  templateUrl: 'two.html'
                }
              }
                  .state('two.words', {
                    views: {
                      '' : {
                        templateUrl: 'three.html'
                      }
                    }
            });
          }]);
