module.factory('myFactory', function() {
    return {
        'sayHello': function(name) {
            return 'Hi ' + name;
        }
    }
});

module.controller('myController', function(myFactory) {
    myFactory.sayHello('Matt');
});
