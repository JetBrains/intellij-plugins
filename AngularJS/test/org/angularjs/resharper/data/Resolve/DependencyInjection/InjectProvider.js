// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
module.provider('greeter', function() {
    var salutation = 'hello';
    this.setSalutation = function(s) {
        salutation = s;
    }

    function Greeter(a) {
        this.greet = function() {
            return salutation + ' ' + s;
        }
    }

    this.$get = function(a) {
        return new Greeter(a);
    }
});

module.config(function(greeterProvider) {
    greeterProvider.setSalutation('hi');
});

module.controller('myController', function(greeter) {
    greeter.greet();
});
