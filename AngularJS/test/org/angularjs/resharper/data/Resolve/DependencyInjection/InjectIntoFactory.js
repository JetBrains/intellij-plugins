module.constant('defaultGreeting', 'Hi');

module.factory('myFactory', function(defaultGreeting) {
    return {
        'sayHello': function(name) {
            return defaultGreeting + ' ' + name;
        }
    }
});

module.controller('myController', function(myFactory) {
    myFactory.sayHello('Matt');
});
