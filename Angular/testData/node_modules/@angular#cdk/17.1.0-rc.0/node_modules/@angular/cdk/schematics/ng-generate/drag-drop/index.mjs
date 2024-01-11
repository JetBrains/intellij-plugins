"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
const schematics_1 = require("@angular-devkit/schematics");
const utils_1 = require("../../utils");
/** Scaffolds a new Angular component that uses the Drag and Drop module. */
function default_1(options) {
    return (0, schematics_1.chain)([
        (0, utils_1.buildComponent)({ ...options }, {
            template: './__path__/__name@dasherize@if-flat__/__name@dasherize__.component.html.template',
            stylesheet: './__path__/__name@dasherize@if-flat__/__name@dasherize__.component.__style__.template',
        }),
        options.skipImport ? (0, schematics_1.noop)() : addDragDropModulesToModule(options),
    ]);
}
exports.default = default_1;
/** Adds the required modules to the main module of the CLI project. */
function addDragDropModulesToModule(options) {
    return async (host) => {
        const isStandalone = await (0, utils_1.isStandaloneSchematic)(host, options);
        if (!isStandalone) {
            const modulePath = await (0, utils_1.findModuleFromOptions)(host, options);
            (0, utils_1.addModuleImportToModule)(host, modulePath, 'DragDropModule', '@angular/cdk/drag-drop');
        }
    };
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiaW5kZXguanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi8uLi8uLi8uLi8uLi9zcmMvY2RrL3NjaGVtYXRpY3MvbmctZ2VuZXJhdGUvZHJhZy1kcm9wL2luZGV4LnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7O0FBRUgsMkRBQW1FO0FBQ25FLHVDQUtxQjtBQUdyQiw0RUFBNEU7QUFDNUUsbUJBQXlCLE9BQWU7SUFDdEMsT0FBTyxJQUFBLGtCQUFLLEVBQUM7UUFDWCxJQUFBLHNCQUFjLEVBQ1osRUFBQyxHQUFHLE9BQU8sRUFBQyxFQUNaO1lBQ0UsUUFBUSxFQUNOLGtGQUFrRjtZQUNwRixVQUFVLEVBQ1IsdUZBQXVGO1NBQzFGLENBQ0Y7UUFDRCxPQUFPLENBQUMsVUFBVSxDQUFDLENBQUMsQ0FBQyxJQUFBLGlCQUFJLEdBQUUsQ0FBQyxDQUFDLENBQUMsMEJBQTBCLENBQUMsT0FBTyxDQUFDO0tBQ2xFLENBQUMsQ0FBQztBQUNMLENBQUM7QUFiRCw0QkFhQztBQUVELHVFQUF1RTtBQUN2RSxTQUFTLDBCQUEwQixDQUFDLE9BQWU7SUFDakQsT0FBTyxLQUFLLEVBQUUsSUFBVSxFQUFFLEVBQUU7UUFDMUIsTUFBTSxZQUFZLEdBQUcsTUFBTSxJQUFBLDZCQUFxQixFQUFDLElBQUksRUFBRSxPQUFPLENBQUMsQ0FBQztRQUVoRSxJQUFJLENBQUMsWUFBWSxFQUFFLENBQUM7WUFDbEIsTUFBTSxVQUFVLEdBQUcsTUFBTSxJQUFBLDZCQUFxQixFQUFDLElBQUksRUFBRSxPQUFPLENBQUMsQ0FBQztZQUM5RCxJQUFBLCtCQUF1QixFQUFDLElBQUksRUFBRSxVQUFXLEVBQUUsZ0JBQWdCLEVBQUUsd0JBQXdCLENBQUMsQ0FBQztRQUN6RixDQUFDO0lBQ0gsQ0FBQyxDQUFDO0FBQ0osQ0FBQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5pbXBvcnQge2NoYWluLCBub29wLCBSdWxlLCBUcmVlfSBmcm9tICdAYW5ndWxhci1kZXZraXQvc2NoZW1hdGljcyc7XG5pbXBvcnQge1xuICBhZGRNb2R1bGVJbXBvcnRUb01vZHVsZSxcbiAgYnVpbGRDb21wb25lbnQsXG4gIGZpbmRNb2R1bGVGcm9tT3B0aW9ucyxcbiAgaXNTdGFuZGFsb25lU2NoZW1hdGljLFxufSBmcm9tICcuLi8uLi91dGlscyc7XG5pbXBvcnQge1NjaGVtYX0gZnJvbSAnLi9zY2hlbWEnO1xuXG4vKiogU2NhZmZvbGRzIGEgbmV3IEFuZ3VsYXIgY29tcG9uZW50IHRoYXQgdXNlcyB0aGUgRHJhZyBhbmQgRHJvcCBtb2R1bGUuICovXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbiAob3B0aW9uczogU2NoZW1hKTogUnVsZSB7XG4gIHJldHVybiBjaGFpbihbXG4gICAgYnVpbGRDb21wb25lbnQoXG4gICAgICB7Li4ub3B0aW9uc30sXG4gICAgICB7XG4gICAgICAgIHRlbXBsYXRlOlxuICAgICAgICAgICcuL19fcGF0aF9fL19fbmFtZUBkYXNoZXJpemVAaWYtZmxhdF9fL19fbmFtZUBkYXNoZXJpemVfXy5jb21wb25lbnQuaHRtbC50ZW1wbGF0ZScsXG4gICAgICAgIHN0eWxlc2hlZXQ6XG4gICAgICAgICAgJy4vX19wYXRoX18vX19uYW1lQGRhc2hlcml6ZUBpZi1mbGF0X18vX19uYW1lQGRhc2hlcml6ZV9fLmNvbXBvbmVudC5fX3N0eWxlX18udGVtcGxhdGUnLFxuICAgICAgfSxcbiAgICApLFxuICAgIG9wdGlvbnMuc2tpcEltcG9ydCA/IG5vb3AoKSA6IGFkZERyYWdEcm9wTW9kdWxlc1RvTW9kdWxlKG9wdGlvbnMpLFxuICBdKTtcbn1cblxuLyoqIEFkZHMgdGhlIHJlcXVpcmVkIG1vZHVsZXMgdG8gdGhlIG1haW4gbW9kdWxlIG9mIHRoZSBDTEkgcHJvamVjdC4gKi9cbmZ1bmN0aW9uIGFkZERyYWdEcm9wTW9kdWxlc1RvTW9kdWxlKG9wdGlvbnM6IFNjaGVtYSkge1xuICByZXR1cm4gYXN5bmMgKGhvc3Q6IFRyZWUpID0+IHtcbiAgICBjb25zdCBpc1N0YW5kYWxvbmUgPSBhd2FpdCBpc1N0YW5kYWxvbmVTY2hlbWF0aWMoaG9zdCwgb3B0aW9ucyk7XG5cbiAgICBpZiAoIWlzU3RhbmRhbG9uZSkge1xuICAgICAgY29uc3QgbW9kdWxlUGF0aCA9IGF3YWl0IGZpbmRNb2R1bGVGcm9tT3B0aW9ucyhob3N0LCBvcHRpb25zKTtcbiAgICAgIGFkZE1vZHVsZUltcG9ydFRvTW9kdWxlKGhvc3QsIG1vZHVsZVBhdGghLCAnRHJhZ0Ryb3BNb2R1bGUnLCAnQGFuZ3VsYXIvY2RrL2RyYWctZHJvcCcpO1xuICAgIH1cbiAgfTtcbn1cbiJdfQ==