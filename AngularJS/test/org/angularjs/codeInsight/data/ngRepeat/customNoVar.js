var app = angular.module("app", []);

app.controller("AppCtrl", function ($http) {
  this.people = [
    {firstName: "John", lastName: "Lindquist"},
    {firstName: "Bill", lastName: "Russel"},
    {firstName: "Frank", lastName: "Oz"},
    {firstName: "Oprah", lastName: "Winfrey"}
  ]
})