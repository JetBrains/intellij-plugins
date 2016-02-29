System.register(["angular2/core"], function(exports_1) {
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
  var core_1;
  var HoverListDirective;
  return {
    setters:[
      function (core_1_1) {
        core_1 = core_1_1;
      }],
    execute: function() {
      HoverListDirective = (function () {
        function HoverListDirective(el, renderer) {
          this.testing = new core_1.EventEmitter();
          this.testOne = "HELLO";
        }
        HoverListDirective.prototype.hello = function () {
          console.log("Hello");
        };
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
                                          __metadata('design:paramtypes', [core_1.ElementRef, core_1.Renderer])
                                        ], HoverListDirective);
        return HoverListDirective;
      }());
      exports_1("HoverListDirective", HoverListDirective);
    }
  }
});