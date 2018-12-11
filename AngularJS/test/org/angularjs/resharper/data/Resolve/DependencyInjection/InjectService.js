module.service('myService', function() {
    this.sayHello = function(name) {
        return 'Hi ' + name;
    }
    this.sayGoodbye = function(name) {
        return 'Bye ' + name;
    }
});

module.controller('myController', function(myService) {
    myService.sayHello('Matt');
});
