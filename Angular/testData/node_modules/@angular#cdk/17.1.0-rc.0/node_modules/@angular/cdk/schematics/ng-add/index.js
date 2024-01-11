"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
const tasks_1 = require("@angular-devkit/schematics/tasks");
const package_config_1 = require("./package-config");
/**
 * Schematic factory entry-point for the `ng-add` schematic. The ng-add schematic will be
 * automatically executed if developers run `ng add @angular/cdk`.
 *
 * By default, the CLI already installs the package that has been specified with `ng add`.
 * We just store the version in the `package.json` in case the package manager didn't. Also
 * this ensures that there will be no error that says that the CDK does not support `ng add`.
 */
function default_1() {
    return (host, context) => {
        // The CLI inserts `@angular/cdk` into the `package.json` before this schematic runs. This
        // means that we do not need to insert the CDK into `package.json` files again. In some cases
        // though, it could happen that this schematic runs outside of the CLI `ng add` command, or
        // the CDK is only listed as a dev dependency. If that is the case, we insert a version based
        // on the current build version (substituted version placeholder).
        if ((0, package_config_1.getPackageVersionFromPackageJson)(host, '@angular/cdk') === null) {
            // In order to align the CDK version with other Angular dependencies that are setup by
            // `@schematics/angular`, we use tilde instead of caret. This is default for Angular
            // dependencies in new CLI projects.
            (0, package_config_1.addPackageToPackageJson)(host, '@angular/cdk', `~17.1.0-rc.0`);
            // Add a task to run the package manager. This is necessary because we updated the
            // workspace "package.json" file and we want lock files to reflect the new version range.
            context.addTask(new tasks_1.NodePackageInstallTask());
        }
    };
}
exports.default = default_1;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiaW5kZXguanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi8uLi8uLi8uLi9zcmMvY2RrL3NjaGVtYXRpY3MvbmctYWRkL2luZGV4LnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7O0FBR0gsNERBQXdFO0FBQ3hFLHFEQUEyRjtBQUUzRjs7Ozs7OztHQU9HO0FBQ0g7SUFDRSxPQUFPLENBQUMsSUFBVSxFQUFFLE9BQXlCLEVBQUUsRUFBRTtRQUMvQywwRkFBMEY7UUFDMUYsNkZBQTZGO1FBQzdGLDJGQUEyRjtRQUMzRiw2RkFBNkY7UUFDN0Ysa0VBQWtFO1FBQ2xFLElBQUksSUFBQSxpREFBZ0MsRUFBQyxJQUFJLEVBQUUsY0FBYyxDQUFDLEtBQUssSUFBSSxFQUFFLENBQUM7WUFDcEUsc0ZBQXNGO1lBQ3RGLG9GQUFvRjtZQUNwRixvQ0FBb0M7WUFDcEMsSUFBQSx3Q0FBdUIsRUFBQyxJQUFJLEVBQUUsY0FBYyxFQUFFLG9CQUFvQixDQUFDLENBQUM7WUFFcEUsa0ZBQWtGO1lBQ2xGLHlGQUF5RjtZQUN6RixPQUFPLENBQUMsT0FBTyxDQUFDLElBQUksOEJBQXNCLEVBQUUsQ0FBQyxDQUFDO1FBQ2hELENBQUM7SUFDSCxDQUFDLENBQUM7QUFDSixDQUFDO0FBbEJELDRCQWtCQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5pbXBvcnQge1J1bGUsIFNjaGVtYXRpY0NvbnRleHQsIFRyZWV9IGZyb20gJ0Bhbmd1bGFyLWRldmtpdC9zY2hlbWF0aWNzJztcbmltcG9ydCB7Tm9kZVBhY2thZ2VJbnN0YWxsVGFza30gZnJvbSAnQGFuZ3VsYXItZGV2a2l0L3NjaGVtYXRpY3MvdGFza3MnO1xuaW1wb3J0IHthZGRQYWNrYWdlVG9QYWNrYWdlSnNvbiwgZ2V0UGFja2FnZVZlcnNpb25Gcm9tUGFja2FnZUpzb259IGZyb20gJy4vcGFja2FnZS1jb25maWcnO1xuXG4vKipcbiAqIFNjaGVtYXRpYyBmYWN0b3J5IGVudHJ5LXBvaW50IGZvciB0aGUgYG5nLWFkZGAgc2NoZW1hdGljLiBUaGUgbmctYWRkIHNjaGVtYXRpYyB3aWxsIGJlXG4gKiBhdXRvbWF0aWNhbGx5IGV4ZWN1dGVkIGlmIGRldmVsb3BlcnMgcnVuIGBuZyBhZGQgQGFuZ3VsYXIvY2RrYC5cbiAqXG4gKiBCeSBkZWZhdWx0LCB0aGUgQ0xJIGFscmVhZHkgaW5zdGFsbHMgdGhlIHBhY2thZ2UgdGhhdCBoYXMgYmVlbiBzcGVjaWZpZWQgd2l0aCBgbmcgYWRkYC5cbiAqIFdlIGp1c3Qgc3RvcmUgdGhlIHZlcnNpb24gaW4gdGhlIGBwYWNrYWdlLmpzb25gIGluIGNhc2UgdGhlIHBhY2thZ2UgbWFuYWdlciBkaWRuJ3QuIEFsc29cbiAqIHRoaXMgZW5zdXJlcyB0aGF0IHRoZXJlIHdpbGwgYmUgbm8gZXJyb3IgdGhhdCBzYXlzIHRoYXQgdGhlIENESyBkb2VzIG5vdCBzdXBwb3J0IGBuZyBhZGRgLlxuICovXG5leHBvcnQgZGVmYXVsdCBmdW5jdGlvbiAoKTogUnVsZSB7XG4gIHJldHVybiAoaG9zdDogVHJlZSwgY29udGV4dDogU2NoZW1hdGljQ29udGV4dCkgPT4ge1xuICAgIC8vIFRoZSBDTEkgaW5zZXJ0cyBgQGFuZ3VsYXIvY2RrYCBpbnRvIHRoZSBgcGFja2FnZS5qc29uYCBiZWZvcmUgdGhpcyBzY2hlbWF0aWMgcnVucy4gVGhpc1xuICAgIC8vIG1lYW5zIHRoYXQgd2UgZG8gbm90IG5lZWQgdG8gaW5zZXJ0IHRoZSBDREsgaW50byBgcGFja2FnZS5qc29uYCBmaWxlcyBhZ2Fpbi4gSW4gc29tZSBjYXNlc1xuICAgIC8vIHRob3VnaCwgaXQgY291bGQgaGFwcGVuIHRoYXQgdGhpcyBzY2hlbWF0aWMgcnVucyBvdXRzaWRlIG9mIHRoZSBDTEkgYG5nIGFkZGAgY29tbWFuZCwgb3JcbiAgICAvLyB0aGUgQ0RLIGlzIG9ubHkgbGlzdGVkIGFzIGEgZGV2IGRlcGVuZGVuY3kuIElmIHRoYXQgaXMgdGhlIGNhc2UsIHdlIGluc2VydCBhIHZlcnNpb24gYmFzZWRcbiAgICAvLyBvbiB0aGUgY3VycmVudCBidWlsZCB2ZXJzaW9uIChzdWJzdGl0dXRlZCB2ZXJzaW9uIHBsYWNlaG9sZGVyKS5cbiAgICBpZiAoZ2V0UGFja2FnZVZlcnNpb25Gcm9tUGFja2FnZUpzb24oaG9zdCwgJ0Bhbmd1bGFyL2NkaycpID09PSBudWxsKSB7XG4gICAgICAvLyBJbiBvcmRlciB0byBhbGlnbiB0aGUgQ0RLIHZlcnNpb24gd2l0aCBvdGhlciBBbmd1bGFyIGRlcGVuZGVuY2llcyB0aGF0IGFyZSBzZXR1cCBieVxuICAgICAgLy8gYEBzY2hlbWF0aWNzL2FuZ3VsYXJgLCB3ZSB1c2UgdGlsZGUgaW5zdGVhZCBvZiBjYXJldC4gVGhpcyBpcyBkZWZhdWx0IGZvciBBbmd1bGFyXG4gICAgICAvLyBkZXBlbmRlbmNpZXMgaW4gbmV3IENMSSBwcm9qZWN0cy5cbiAgICAgIGFkZFBhY2thZ2VUb1BhY2thZ2VKc29uKGhvc3QsICdAYW5ndWxhci9jZGsnLCBgfjAuMC4wLVBMQUNFSE9MREVSYCk7XG5cbiAgICAgIC8vIEFkZCBhIHRhc2sgdG8gcnVuIHRoZSBwYWNrYWdlIG1hbmFnZXIuIFRoaXMgaXMgbmVjZXNzYXJ5IGJlY2F1c2Ugd2UgdXBkYXRlZCB0aGVcbiAgICAgIC8vIHdvcmtzcGFjZSBcInBhY2thZ2UuanNvblwiIGZpbGUgYW5kIHdlIHdhbnQgbG9jayBmaWxlcyB0byByZWZsZWN0IHRoZSBuZXcgdmVyc2lvbiByYW5nZS5cbiAgICAgIGNvbnRleHQuYWRkVGFzayhuZXcgTm9kZVBhY2thZ2VJbnN0YWxsVGFzaygpKTtcbiAgICB9XG4gIH07XG59XG4iXX0=