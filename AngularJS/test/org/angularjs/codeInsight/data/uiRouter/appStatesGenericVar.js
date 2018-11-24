var homeState = {
  parent: 'app',
  name: 'home',
  url: '/home',
  template: template,
  views: {
    "test" : {}
  }
};
let loginState;
if (true) {
  loginState = {
    parent: 'app',
    name: 'login',
    url: '/login',
    template: template,
    controller: controller,
    controllerAs: 'vm',
    resolve: { returnTo: returnTo }
  };
}
angular.module('sample', [
  'ui.router'
]).config(['$stateProvider', $stateProvider => {
            [homeState, loginState].forEach(state => $stateProvider.state(state));
}]);
