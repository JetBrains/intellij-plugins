module.value('myValueFunc', function(name) {
    return "Hello " + name;
});

module.controller('myController', function(myValueFunc) {
    return myValueFunc('Matt');
});
