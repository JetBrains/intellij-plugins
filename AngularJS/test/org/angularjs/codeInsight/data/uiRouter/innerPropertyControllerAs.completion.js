angular.module('sample', [
      'ui.router'
    ])

    .config(
        ['$stateProvider', '$urlRouterProvider',
          function ($stateProvider,   $urlRouterProvider) {
            $stateProvider.state('one', {
              views: {
                '' : {
                  templateUrl: 'one.html',
                  controller: function(){},
                  controllerAs: 'testMe'
                },
                'menuTip': {}
              }}).state('two', {
              views: {
                '' : {
                  templateUrl: 'two.html',
                  controller: '<caret>'
                }
              }
            });
          }]);
