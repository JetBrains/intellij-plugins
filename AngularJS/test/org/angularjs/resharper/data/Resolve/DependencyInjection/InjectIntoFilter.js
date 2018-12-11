module.value('greet', function(name) {
    return 'Hello ' + name;
});

module.filter('myFilter', function(greet) {
    return function(text) {
        return text && greet(text) || text;
    }
});
