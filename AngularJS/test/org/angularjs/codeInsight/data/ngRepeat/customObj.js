var app = angular.module("app", []);

app.controller("AppCtrl", function ($http) {
  var app = this;

  app.people = {
    JL: {
      firstName: "John", lastName: "Lindquist"
    },
    BR: {
      firstName: "Bill", lastName: "Russel"
    },
    FO: {
      firstName: "Frank", lastName: "Oz"
    },
    OW: {
      firstName: "Oprah", lastName: "Winfrey"
    }
  }
})