angular.module('sample', [
  'ui.router'
])

    .config(
        ['$stateProvider', '$urlRouterProvider',
          function ($stateProvider,   $urlRouterProvider) {
            $stateProvider.state({
              name: 'one',
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
                      .state({
                        name: 'two.words',
                        views: {
                          '' : {
                            templateUrl: 'three.html'
                          }
                        }
                      });
          }]);

