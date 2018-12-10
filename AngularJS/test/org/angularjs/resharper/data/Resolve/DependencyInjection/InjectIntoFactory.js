// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
module.constant('defaultGreeting', 'Hi');

module.factory('myFactory', function(defaultGreeting) {
    return {
        'sayHello': function(name) {
            return defaultGreeting + ' ' + name;
        }
    }
});

module.controller('myController', function(myFactory) {
    myFactory.sayHello('Matt');
});
