module.constant('myConstant', 'hello world');

module.controller('myController', function(myConstant) {
    return myConstant;
});
