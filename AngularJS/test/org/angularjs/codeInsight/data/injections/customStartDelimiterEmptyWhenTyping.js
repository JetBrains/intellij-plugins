var app = angular.module("MyTutorialApp", [], function ($interpolateProvider) {
  $interpolateProvider.startSymbol('');
  $interpolateProvider.endSymbol(']]');
});