// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
module.value('myValue', 42);

module.factory('myFactory', function() {
    return {
        'sayHi': function(name) {
            return 'Hi ' + name;
        }
    }
});

module.service('myService', function() {
    this.sayHello = function(name) {
        return 'Hi ' + name;
    }
    this.sayGoodbye = function(name) {
        return 'Bye ' + name;
    }
});

module.controller('myController', [ 'myValue', 'myFactory', 'myService', function(v, f, s) {
    var p = v * 2;
    f.sayHi('Matt');
    s.sayHello('Matt');
});
