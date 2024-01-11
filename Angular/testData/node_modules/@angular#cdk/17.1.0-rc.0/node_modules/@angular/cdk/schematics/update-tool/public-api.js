"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __exportStar = (this && this.__exportStar) || function(m, exports) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
};
Object.defineProperty(exports, "__esModule", { value: true });
__exportStar(require("./component-resource-collector"), exports);
__exportStar(require("./file-system"), exports);
__exportStar(require("./index"), exports);
__exportStar(require("./migration"), exports);
__exportStar(require("./target-version"), exports);
__exportStar(require("./utils/decorators"), exports);
__exportStar(require("./utils/imports"), exports);
__exportStar(require("./utils/property-name"), exports);
__exportStar(require("./version-changes"), exports);
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicHVibGljLWFwaS5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy91cGRhdGUtdG9vbC9wdWJsaWMtYXBpLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7Ozs7Ozs7Ozs7Ozs7Ozs7QUFFSCxpRUFBK0M7QUFDL0MsZ0RBQThCO0FBQzlCLDBDQUF3QjtBQUN4Qiw4Q0FBNEI7QUFDNUIsbURBQWlDO0FBQ2pDLHFEQUFtQztBQUNuQyxrREFBZ0M7QUFDaEMsd0RBQXNDO0FBQ3RDLG9EQUFrQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5leHBvcnQgKiBmcm9tICcuL2NvbXBvbmVudC1yZXNvdXJjZS1jb2xsZWN0b3InO1xuZXhwb3J0ICogZnJvbSAnLi9maWxlLXN5c3RlbSc7XG5leHBvcnQgKiBmcm9tICcuL2luZGV4JztcbmV4cG9ydCAqIGZyb20gJy4vbWlncmF0aW9uJztcbmV4cG9ydCAqIGZyb20gJy4vdGFyZ2V0LXZlcnNpb24nO1xuZXhwb3J0ICogZnJvbSAnLi91dGlscy9kZWNvcmF0b3JzJztcbmV4cG9ydCAqIGZyb20gJy4vdXRpbHMvaW1wb3J0cyc7XG5leHBvcnQgKiBmcm9tICcuL3V0aWxzL3Byb3BlcnR5LW5hbWUnO1xuZXhwb3J0ICogZnJvbSAnLi92ZXJzaW9uLWNoYW5nZXMnO1xuIl19