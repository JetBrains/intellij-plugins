module.value('myValue', 'hello world');

module.controller('myController', function(myValue) {
    return myValue;
});
