module.constant('defaultSalutation', 'hello');

module.provider('greeter', function(defaultSalutation) {
    var salutation = defaultSalutation;
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

module.controller('myController', function(greeter) {
    greeter.greet();
});
