import {homeState} from './homeState';
import {loginState} from './loginState';

angular.module('sample', [
  'ui.router'
]).config(['$stateProvider', $stateProvider => {
                                             [homeState, loginState].forEach(state => $stateProvider.state(state));
}]);

