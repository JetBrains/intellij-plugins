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
__exportStar(require("./data/index"), exports);
__exportStar(require("./devkit-migration"), exports);
__exportStar(require("./devkit-migration-rule"), exports);
__exportStar(require("./devkit-file-system"), exports);
__exportStar(require("./html-parsing/angular"), exports);
__exportStar(require("./html-parsing/elements"), exports);
__exportStar(require("./typescript/base-types"), exports);
__exportStar(require("./typescript/imports"), exports);
__exportStar(require("./typescript/literal"), exports);
__exportStar(require("./typescript/module-specifiers"), exports);
__exportStar(require("./find-stylesheets"), exports);
__exportStar(require("./upgrade-data"), exports);
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicHVibGljLWFwaS5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy9uZy11cGRhdGUvcHVibGljLWFwaS50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiO0FBQUE7Ozs7OztHQU1HOzs7Ozs7Ozs7Ozs7Ozs7O0FBRUgsK0NBQTZCO0FBQzdCLHFEQUFtQztBQUNuQywwREFBd0M7QUFDeEMsdURBQXFDO0FBQ3JDLHlEQUF1QztBQUN2QywwREFBd0M7QUFDeEMsMERBQXdDO0FBQ3hDLHVEQUFxQztBQUNyQyx1REFBcUM7QUFDckMsaUVBQStDO0FBQy9DLHFEQUFtQztBQUNuQyxpREFBK0IiLCJzb3VyY2VzQ29udGVudCI6WyIvKipcbiAqIEBsaWNlbnNlXG4gKiBDb3B5cmlnaHQgR29vZ2xlIExMQyBBbGwgUmlnaHRzIFJlc2VydmVkLlxuICpcbiAqIFVzZSBvZiB0aGlzIHNvdXJjZSBjb2RlIGlzIGdvdmVybmVkIGJ5IGFuIE1JVC1zdHlsZSBsaWNlbnNlIHRoYXQgY2FuIGJlXG4gKiBmb3VuZCBpbiB0aGUgTElDRU5TRSBmaWxlIGF0IGh0dHBzOi8vYW5ndWxhci5pby9saWNlbnNlXG4gKi9cblxuZXhwb3J0ICogZnJvbSAnLi9kYXRhL2luZGV4JztcbmV4cG9ydCAqIGZyb20gJy4vZGV2a2l0LW1pZ3JhdGlvbic7XG5leHBvcnQgKiBmcm9tICcuL2RldmtpdC1taWdyYXRpb24tcnVsZSc7XG5leHBvcnQgKiBmcm9tICcuL2RldmtpdC1maWxlLXN5c3RlbSc7XG5leHBvcnQgKiBmcm9tICcuL2h0bWwtcGFyc2luZy9hbmd1bGFyJztcbmV4cG9ydCAqIGZyb20gJy4vaHRtbC1wYXJzaW5nL2VsZW1lbnRzJztcbmV4cG9ydCAqIGZyb20gJy4vdHlwZXNjcmlwdC9iYXNlLXR5cGVzJztcbmV4cG9ydCAqIGZyb20gJy4vdHlwZXNjcmlwdC9pbXBvcnRzJztcbmV4cG9ydCAqIGZyb20gJy4vdHlwZXNjcmlwdC9saXRlcmFsJztcbmV4cG9ydCAqIGZyb20gJy4vdHlwZXNjcmlwdC9tb2R1bGUtc3BlY2lmaWVycyc7XG5leHBvcnQgKiBmcm9tICcuL2ZpbmQtc3R5bGVzaGVldHMnO1xuZXhwb3J0ICogZnJvbSAnLi91cGdyYWRlLWRhdGEnO1xuIl19