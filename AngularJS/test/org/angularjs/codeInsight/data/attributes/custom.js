angular.module('docsSimpleDirective', [])
    .controller('SupaController', function($scope) {
        $scope.customer = {
            name: 'Naomi',
            address: '1600 Amphitheatre'
        };
    })
    .directive('myCustomer', function() {
        return {
            template: 'Name: {{customer.name}} Address: {{customer.address}}'
        };
    })
    .directive('someTag', function() {
        return {
            restrict: 'E',
            transclude: true,
            scope: {
                'close': '&onClose'
            },
            templateUrl: 'my-dialog-close.html'
        };
    })
    .directive('someOtherTag', function() {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: 'my-dialog-close.html'
        };
    });