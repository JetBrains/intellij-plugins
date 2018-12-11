module.factory('myFactory', function() {
    return {
        'sayHello': function(name) {
            return 'Hi ' + name;
        }
    }
});

module.directive('myDirective', function(myFactory) {
    function link(scope, element, attrs) {
        scope.greeting = myFactory.sayHello('Matt');
    }
    return {
        link: link
    };
});
