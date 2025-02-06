var app = angular.module("app", ['ngRoute']);

app.controller('AppCtrl', function ($http) {
  var app = this;

  app.people = [
    {firstName: "John", lastName: "Lindquist"},
    {firstName: "Bill", lastName: "Russel"},
    {firstName: "Frank", lastName: "Oz"},
    {firstName: "Oprah", lastName: "Winfrey"}
  ]
})

app.controller("OtherCtrl", function ($http) {

})

app.config(['$routeProvider',
  function ($routeProvider) {
    $routeProvider.
      when('/phones', {
        templateUrl: 'partials/phone-list.html',
        controller: 'AppCtrl'
      }).
      when('/phones/:phoneId', {
        templateUrl: 'partials/phone-details.html',
        controller: 'AppCtrl'
      }).
      when('/template/:template', {
        templateUrl: 'templateId.htm',
        controller: function(){},
        controllerAs: 'localCtl'
      }).
      when('/template/:template222', {
        templateUrl: 'templateId.htm',
        controllerAs: 'localCtl2'
      }).
      when('/template/:template333', {
        templateUrl: 'templateId.htm',
        controller: 'localCtl'
      }).
      when('/template/:template444', {
        templateUrl: 'templateId.htm',
        controller: 'localCtl2'
      }).
      otherwise({
        redirectTo: '/phones'
      });
  }]);