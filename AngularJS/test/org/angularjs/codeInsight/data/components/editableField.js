// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

angular.module('heroApp').controller(
    'EditableFieldController', function($scope, $element, $attrs) {
      var ctrl = this;
      ctrl.editMode = false;

      ctrl.handleModeChange = function () {
        if (ctrl.editMode) {
          ctrl.onUpdate({value: ctrl.fieldValue});
          ctrl.fieldValueCopy = ctrl.fieldValue;
        }
        ctrl.editMode = !ctrl.editMode;
      };

      ctrl.reset = function () {
        ctrl.fieldValue = ctrl.fieldValueCopy;
      };

      ctrl.$onInit = function () {
        // Make a copy of the initial value to be able to reset it later
        ctrl.fieldValueCopy = ctrl.fieldValue;

        // Set a default fieldType
        if (!ctrl.fieldType) {
          ctrl.fieldType = 'text';
        }
      };
    });

angular.module('heroApp').component('editableField', {
  templateUrl: 'editableField.html',
  controller: 'EditableFieldController',
  bindings: {
    fieldValue: '<',
    fieldType: '@?',
    onUpdate: '&'
  }
});

