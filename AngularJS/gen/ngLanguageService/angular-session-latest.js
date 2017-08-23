"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
exports.__esModule = true;
var ngutil_1 = require("./ngutil");
function createAngularSessionClass(ts_impl, sessionClass) {
    var AngularSessionLatest = (function (_super) {
        __extends(AngularSessionLatest, _super);
        function AngularSessionLatest() {
            return _super !== null && _super.apply(this, arguments) || this;
        }
        AngularSessionLatest.prototype.executeCommand = function (request) {
            var command = request.command;
            if (command == ngutil_1.IDEGetHtmlErrors) {
                request.command = ts_impl.server.CommandNames.SemanticDiagnosticsSync;
                return _super.prototype.executeCommand.call(this, request);
            }
            if (command == ngutil_1.IDENgCompletions) {
                request.command = ts_impl.server.CommandNames.Completions;
                return _super.prototype.executeCommand.call(this, request);
            }
            if (command == ngutil_1.IDEGetProjectHtmlErr) {
                request.command = ts_impl.server.CommandNames.GeterrForProject;
                return _super.prototype.executeCommand.call(this, request);
            }
            return _super.prototype.executeCommand.call(this, request);
        };
        return AngularSessionLatest;
    }(sessionClass));
    return AngularSessionLatest;
}
exports.createAngularSessionClass = createAngularSessionClass;
