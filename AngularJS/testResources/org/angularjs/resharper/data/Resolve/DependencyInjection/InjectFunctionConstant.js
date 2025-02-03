module.constant('myConstantFunc', function(name) {
    return "Hello " + name;
});

module.controller('myController', function(myConstantFunc) {
    return myConstantFunc('Matt');
});
