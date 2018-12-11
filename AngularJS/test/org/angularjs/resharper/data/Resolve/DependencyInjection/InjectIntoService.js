module.constant('defaultGreeting', 'Hi');
module.service('myService', function(defaultGreeting) {
    this.sayHello = function(name) {
        return defaultGreeting + ' ' + name;
    }
    this.sayGoodbye = function(name) {
        return 'Bye ' + name;
    }
});

module.controller('myController', function(myService) {
    myService.sayHello('Matt');
});
