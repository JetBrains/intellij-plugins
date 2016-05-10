"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
      var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
      if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
      else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
      return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
var __metadata = (this && this.__metadata) || function (k, v) {
      if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
var core_1 = require('@angular/core');
var HoverListDirective = (function () {
  function HoverListDirective(template, renderer) {
    this.testing = new core_1.EventEmitter();
    this.testOne = "HELLO";
  }
  __decorate([
               core_1.Output(),
               __metadata('design:type', core_1.EventEmitter)
             ], HoverListDirective.prototype, "testing", void 0);
  __decorate([
               core_1.Input(),
               __metadata('design:type', String)
             ], HoverListDirective.prototype, "testOne", void 0);
  __decorate([
               core_1.Input(),
               __metadata('design:type', String)
             ], HoverListDirective.prototype, "testTwo", void 0);
  HoverListDirective = __decorate([
                                    core_1.Directive({
                                                       selector: '[myHoverList]',
                                                       host: {
                                                         '(mouseenter)': 'hello()'
                                                       }
                                                     }),
                                    __metadata('design:paramtypes', [core_1.TemplateRef, core_1.Renderer])
                                  ], HoverListDirective);
  return HoverListDirective;
}());
exports.HoverListDirective = HoverListDirective;
//# sourceMappingURL=hover-list-directive.component.js.map