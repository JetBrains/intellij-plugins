"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.findStylesheetFiles = void 0;
const core_1 = require("@angular-devkit/core");
/** Regular expression that matches stylesheet paths */
const STYLESHEET_REGEX = /.*\.(css|scss)$/;
/**
 * Finds stylesheets in the given directory from within the specified tree.
 * @param tree Devkit tree where stylesheet files can be found in.
 * @param startDirectory Optional start directory where stylesheets should be searched in.
 *   This can be useful if only stylesheets within a given folder are relevant (to avoid
 *   unnecessary iterations).
 */
function findStylesheetFiles(tree, startDirectory = '/') {
    const result = [];
    const visitDir = (dirPath) => {
        const { subfiles, subdirs } = tree.getDir(dirPath);
        subfiles.forEach(fileName => {
            if (STYLESHEET_REGEX.test(fileName)) {
                result.push((0, core_1.join)(dirPath, fileName));
            }
        });
        // Visit directories within the current directory to find other stylesheets.
        subdirs.forEach(fragment => {
            // Do not visit directories or files inside node modules or `dist/` folders.
            if (fragment !== 'node_modules' && fragment !== 'dist') {
                visitDir((0, core_1.join)(dirPath, fragment));
            }
        });
    };
    visitDir(startDirectory);
    return result;
}
exports.findStylesheetFiles = findStylesheetFiles;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZmluZC1zdHlsZXNoZWV0cy5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy9uZy11cGRhdGUvZmluZC1zdHlsZXNoZWV0cy50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiO0FBQUE7Ozs7OztHQU1HOzs7QUFFSCwrQ0FBZ0Q7QUFHaEQsdURBQXVEO0FBQ3ZELE1BQU0sZ0JBQWdCLEdBQUcsaUJBQWlCLENBQUM7QUFFM0M7Ozs7OztHQU1HO0FBQ0gsU0FBZ0IsbUJBQW1CLENBQUMsSUFBVSxFQUFFLGlCQUF5QixHQUFHO0lBQzFFLE1BQU0sTUFBTSxHQUFhLEVBQUUsQ0FBQztJQUM1QixNQUFNLFFBQVEsR0FBRyxDQUFDLE9BQWEsRUFBRSxFQUFFO1FBQ2pDLE1BQU0sRUFBQyxRQUFRLEVBQUUsT0FBTyxFQUFDLEdBQUcsSUFBSSxDQUFDLE1BQU0sQ0FBQyxPQUFPLENBQUMsQ0FBQztRQUVqRCxRQUFRLENBQUMsT0FBTyxDQUFDLFFBQVEsQ0FBQyxFQUFFO1lBQzFCLElBQUksZ0JBQWdCLENBQUMsSUFBSSxDQUFDLFFBQVEsQ0FBQyxFQUFFLENBQUM7Z0JBQ3BDLE1BQU0sQ0FBQyxJQUFJLENBQUMsSUFBQSxXQUFJLEVBQUMsT0FBTyxFQUFFLFFBQVEsQ0FBQyxDQUFDLENBQUM7WUFDdkMsQ0FBQztRQUNILENBQUMsQ0FBQyxDQUFDO1FBRUgsNEVBQTRFO1FBQzVFLE9BQU8sQ0FBQyxPQUFPLENBQUMsUUFBUSxDQUFDLEVBQUU7WUFDekIsNEVBQTRFO1lBQzVFLElBQUksUUFBUSxLQUFLLGNBQWMsSUFBSSxRQUFRLEtBQUssTUFBTSxFQUFFLENBQUM7Z0JBQ3ZELFFBQVEsQ0FBQyxJQUFBLFdBQUksRUFBQyxPQUFPLEVBQUUsUUFBUSxDQUFDLENBQUMsQ0FBQztZQUNwQyxDQUFDO1FBQ0gsQ0FBQyxDQUFDLENBQUM7SUFDTCxDQUFDLENBQUM7SUFDRixRQUFRLENBQUMsY0FBc0IsQ0FBQyxDQUFDO0lBQ2pDLE9BQU8sTUFBTSxDQUFDO0FBQ2hCLENBQUM7QUFyQkQsa0RBcUJDIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5cbmltcG9ydCB7am9pbiwgUGF0aH0gZnJvbSAnQGFuZ3VsYXItZGV2a2l0L2NvcmUnO1xuaW1wb3J0IHtUcmVlfSBmcm9tICdAYW5ndWxhci1kZXZraXQvc2NoZW1hdGljcyc7XG5cbi8qKiBSZWd1bGFyIGV4cHJlc3Npb24gdGhhdCBtYXRjaGVzIHN0eWxlc2hlZXQgcGF0aHMgKi9cbmNvbnN0IFNUWUxFU0hFRVRfUkVHRVggPSAvLipcXC4oY3NzfHNjc3MpJC87XG5cbi8qKlxuICogRmluZHMgc3R5bGVzaGVldHMgaW4gdGhlIGdpdmVuIGRpcmVjdG9yeSBmcm9tIHdpdGhpbiB0aGUgc3BlY2lmaWVkIHRyZWUuXG4gKiBAcGFyYW0gdHJlZSBEZXZraXQgdHJlZSB3aGVyZSBzdHlsZXNoZWV0IGZpbGVzIGNhbiBiZSBmb3VuZCBpbi5cbiAqIEBwYXJhbSBzdGFydERpcmVjdG9yeSBPcHRpb25hbCBzdGFydCBkaXJlY3Rvcnkgd2hlcmUgc3R5bGVzaGVldHMgc2hvdWxkIGJlIHNlYXJjaGVkIGluLlxuICogICBUaGlzIGNhbiBiZSB1c2VmdWwgaWYgb25seSBzdHlsZXNoZWV0cyB3aXRoaW4gYSBnaXZlbiBmb2xkZXIgYXJlIHJlbGV2YW50ICh0byBhdm9pZFxuICogICB1bm5lY2Vzc2FyeSBpdGVyYXRpb25zKS5cbiAqL1xuZXhwb3J0IGZ1bmN0aW9uIGZpbmRTdHlsZXNoZWV0RmlsZXModHJlZTogVHJlZSwgc3RhcnREaXJlY3Rvcnk6IHN0cmluZyA9ICcvJyk6IHN0cmluZ1tdIHtcbiAgY29uc3QgcmVzdWx0OiBzdHJpbmdbXSA9IFtdO1xuICBjb25zdCB2aXNpdERpciA9IChkaXJQYXRoOiBQYXRoKSA9PiB7XG4gICAgY29uc3Qge3N1YmZpbGVzLCBzdWJkaXJzfSA9IHRyZWUuZ2V0RGlyKGRpclBhdGgpO1xuXG4gICAgc3ViZmlsZXMuZm9yRWFjaChmaWxlTmFtZSA9PiB7XG4gICAgICBpZiAoU1RZTEVTSEVFVF9SRUdFWC50ZXN0KGZpbGVOYW1lKSkge1xuICAgICAgICByZXN1bHQucHVzaChqb2luKGRpclBhdGgsIGZpbGVOYW1lKSk7XG4gICAgICB9XG4gICAgfSk7XG5cbiAgICAvLyBWaXNpdCBkaXJlY3RvcmllcyB3aXRoaW4gdGhlIGN1cnJlbnQgZGlyZWN0b3J5IHRvIGZpbmQgb3RoZXIgc3R5bGVzaGVldHMuXG4gICAgc3ViZGlycy5mb3JFYWNoKGZyYWdtZW50ID0+IHtcbiAgICAgIC8vIERvIG5vdCB2aXNpdCBkaXJlY3RvcmllcyBvciBmaWxlcyBpbnNpZGUgbm9kZSBtb2R1bGVzIG9yIGBkaXN0L2AgZm9sZGVycy5cbiAgICAgIGlmIChmcmFnbWVudCAhPT0gJ25vZGVfbW9kdWxlcycgJiYgZnJhZ21lbnQgIT09ICdkaXN0Jykge1xuICAgICAgICB2aXNpdERpcihqb2luKGRpclBhdGgsIGZyYWdtZW50KSk7XG4gICAgICB9XG4gICAgfSk7XG4gIH07XG4gIHZpc2l0RGlyKHN0YXJ0RGlyZWN0b3J5IGFzIFBhdGgpO1xuICByZXR1cm4gcmVzdWx0O1xufVxuIl19