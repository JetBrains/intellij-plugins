var applicationModuleName = 'messageFormatExample';
export var registerModule = function(moduleName) {
  // Create angular module
  angular.module(moduleName, []);

  // Add the module to the AngularJS configuration file
  angular.module(applicationModuleName).requires.push(moduleName);
};

