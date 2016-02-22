angular.module('sample', [
      'ui.router'
    ])

    .config(
        ['$stateProvider', '$urlRouterProvider',
          function ($stateProvider,   $urlRouterProvider) {
            $stateProvider.state('one', {
              views: {
                '<caret>' : {
                  templateUrl: 'one.html'
                }
              }})
                .state('two', {
              views: {
                '' : {
                  templateUrl: 'two.html'
                }
              }
            });
          }]);
