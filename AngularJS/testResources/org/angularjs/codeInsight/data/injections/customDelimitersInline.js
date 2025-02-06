var app = angular.module("MyTutorialApp", [], function ($interpolateProvider) {
  $interpolateProvider.startSymbol('[[').endSymbol(']]');
});