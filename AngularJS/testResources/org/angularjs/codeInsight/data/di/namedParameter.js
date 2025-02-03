angular
    .module("acme")
    .factory('myService', function () {
               return {
                 /**
                  * Fobbalize
                  * @param num the number of feebles
                  */
                 foo: function (num) {
                 },
                 bar: function (text) {
                 }
               }
             })
    .controller('myDiController', ['myS<caret>', function (myService) {
                }]);