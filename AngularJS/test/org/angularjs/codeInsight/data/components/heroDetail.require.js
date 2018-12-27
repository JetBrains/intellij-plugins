// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

function HeroDetailController() {
  var ctrl = this;

  ctrl.delete = function () {
    ctrl.onDelete({hero: ctrl.hero});
  };

  ctrl.update = function (prop, value) {
    ctrl.onUpdate({hero: ctrl.hero, prop: prop, value: value});
  };
}

var heroDetail = {
  template: require("./heroDetail.html"),
  controller: HeroDetailController,
  controllerAs: 'vm',
  bindings: {
    hero: '<',
    onDelete: '&',
    onUpdate: '&'
  }
}

angular.module('heroApp').component('heroDetail', heroDetail);

