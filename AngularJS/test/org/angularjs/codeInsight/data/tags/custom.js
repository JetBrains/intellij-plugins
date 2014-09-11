angular.module('docsSimpleDirective', [])
    .controller('SupaController', function ($scope) {
                  $scope.customer = {
                    name: 'Naomi',
                    address: '1600 Amphitheatre'
                  };
                })
    .directive('myCustomer', function () {
                 return {
                   template: 'Name: {{customer.name}} Address: {{customer.address}}',
                   restrict: 'E'
                 };
               })
    .directive('hisCustomer', function () {
                 return {
                   template: 'Name: {{customer.name}} Address: {{customer.address}}',
                   restrict: 'E',
                   scope: {
                     name: "",
                     homeAddress: ""
                   }
                 };
               })
    .directive('herCustomer', ["$bar", function ($bar) {
                 return {
                   template: 'Name: {{customer.name}} Address: {{customer.address}}',
                   restrict: 'E'
                 };
               }]);