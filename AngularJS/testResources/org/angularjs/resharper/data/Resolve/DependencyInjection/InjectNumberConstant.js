module.constant('myConstant', 42);

module.controller('myController', function(myConstant) {
    return myConstant * 2;
});
