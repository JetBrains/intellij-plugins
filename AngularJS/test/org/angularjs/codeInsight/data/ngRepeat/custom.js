var app = angular.module("app", []);

app.controller("AppCtrl", function ($http) {
  var app = this;

  app.people = [
    {firstName: "John", lastName: "Lindquist"},
    {firstName: "Bill", lastName: "Russel"},
    {firstName: "Frank", lastName: "Oz"},
    {firstName: "Oprah", lastName: "Winfrey"}
  ]
})