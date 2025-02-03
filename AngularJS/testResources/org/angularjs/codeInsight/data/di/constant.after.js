angular
    .module("acme")
    .factory('myService', {
                 foo: function (num) {
                 },
                 bar: function (text) {
                 }
             })
    .run('myDiController', function (myService) {
                  myService.bar();
                });