"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.DevkitFileSystem = void 0;
const core_1 = require("@angular-devkit/core");
const file_system_1 = require("../update-tool/file-system");
const path = require("path");
/**
 * File system that leverages the virtual tree from the CLI devkit. This file
 * system is commonly used by `ng update` migrations that run as part of the
 * Angular CLI.
 */
class DevkitFileSystem extends file_system_1.FileSystem {
    _tree;
    _updateRecorderCache = new Map();
    constructor(_tree) {
        super();
        this._tree = _tree;
    }
    resolve(...segments) {
        // Note: We use `posix.resolve` as the devkit paths are using posix separators.
        return (0, core_1.normalize)(path.posix.resolve('/', ...segments.map(core_1.normalize)));
    }
    edit(filePath) {
        if (this._updateRecorderCache.has(filePath)) {
            return this._updateRecorderCache.get(filePath);
        }
        const recorder = this._tree.beginUpdate(filePath);
        this._updateRecorderCache.set(filePath, recorder);
        return recorder;
    }
    commitEdits() {
        this._updateRecorderCache.forEach(r => this._tree.commitUpdate(r));
        this._updateRecorderCache.clear();
    }
    fileExists(filePath) {
        return this._tree.exists(filePath);
    }
    directoryExists(dirPath) {
        // The devkit tree does not expose an API for checking whether a given
        // directory exists. It throws a specific error though if a directory
        // is being read as a file. We use that to check if a directory exists.
        try {
            this._tree.get(dirPath);
        }
        catch (e) {
            // Note: We do not use an `instanceof` check here. It could happen that
            // the devkit version used by the CLI is different than the one we end up
            // loading. This can happen depending on how Yarn/NPM hoists the NPM
            // packages / whether there are multiple versions installed. Typescript
            // throws a compilation error if the type isn't specified and we can't
            // check the type, so we have to cast the error output to any.
            if (e.constructor.name === 'PathIsDirectoryException') {
                return true;
            }
        }
        return false;
    }
    overwrite(filePath, content) {
        this._tree.overwrite(filePath, content);
    }
    create(filePath, content) {
        this._tree.create(filePath, content);
    }
    delete(filePath) {
        this._tree.delete(filePath);
    }
    read(filePath) {
        const buffer = this._tree.read(filePath);
        return buffer !== null ? buffer.toString() : null;
    }
    readDirectory(dirPath) {
        const { subdirs: directories, subfiles: files } = this._tree.getDir(dirPath);
        return { directories, files };
    }
}
exports.DevkitFileSystem = DevkitFileSystem;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiZGV2a2l0LWZpbGUtc3lzdGVtLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vLi4vLi4vLi4vLi4vc3JjL2Nkay9zY2hlbWF0aWNzL25nLXVwZGF0ZS9kZXZraXQtZmlsZS1zeXN0ZW0udHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IjtBQUFBOzs7Ozs7R0FNRzs7O0FBRUgsK0NBQXFEO0FBRXJELDREQUFzRTtBQUN0RSw2QkFBNkI7QUFFN0I7Ozs7R0FJRztBQUNILE1BQWEsZ0JBQWlCLFNBQVEsd0JBQVU7SUFHMUI7SUFGWixvQkFBb0IsR0FBRyxJQUFJLEdBQUcsRUFBMEIsQ0FBQztJQUVqRSxZQUFvQixLQUFXO1FBQzdCLEtBQUssRUFBRSxDQUFDO1FBRFUsVUFBSyxHQUFMLEtBQUssQ0FBTTtJQUUvQixDQUFDO0lBRUQsT0FBTyxDQUFDLEdBQUcsUUFBa0I7UUFDM0IsK0VBQStFO1FBQy9FLE9BQU8sSUFBQSxnQkFBUyxFQUFDLElBQUksQ0FBQyxLQUFLLENBQUMsT0FBTyxDQUFDLEdBQUcsRUFBRSxHQUFHLFFBQVEsQ0FBQyxHQUFHLENBQUMsZ0JBQVMsQ0FBQyxDQUFDLENBQUMsQ0FBQztJQUN4RSxDQUFDO0lBRUQsSUFBSSxDQUFDLFFBQWM7UUFDakIsSUFBSSxJQUFJLENBQUMsb0JBQW9CLENBQUMsR0FBRyxDQUFDLFFBQVEsQ0FBQyxFQUFFLENBQUM7WUFDNUMsT0FBTyxJQUFJLENBQUMsb0JBQW9CLENBQUMsR0FBRyxDQUFDLFFBQVEsQ0FBRSxDQUFDO1FBQ2xELENBQUM7UUFDRCxNQUFNLFFBQVEsR0FBRyxJQUFJLENBQUMsS0FBSyxDQUFDLFdBQVcsQ0FBQyxRQUFRLENBQUMsQ0FBQztRQUNsRCxJQUFJLENBQUMsb0JBQW9CLENBQUMsR0FBRyxDQUFDLFFBQVEsRUFBRSxRQUFRLENBQUMsQ0FBQztRQUNsRCxPQUFPLFFBQVEsQ0FBQztJQUNsQixDQUFDO0lBRUQsV0FBVztRQUNULElBQUksQ0FBQyxvQkFBb0IsQ0FBQyxPQUFPLENBQUMsQ0FBQyxDQUFDLEVBQUUsQ0FBQyxJQUFJLENBQUMsS0FBSyxDQUFDLFlBQVksQ0FBQyxDQUFDLENBQUMsQ0FBQyxDQUFDO1FBQ25FLElBQUksQ0FBQyxvQkFBb0IsQ0FBQyxLQUFLLEVBQUUsQ0FBQztJQUNwQyxDQUFDO0lBRUQsVUFBVSxDQUFDLFFBQWM7UUFDdkIsT0FBTyxJQUFJLENBQUMsS0FBSyxDQUFDLE1BQU0sQ0FBQyxRQUFRLENBQUMsQ0FBQztJQUNyQyxDQUFDO0lBRUQsZUFBZSxDQUFDLE9BQWE7UUFDM0Isc0VBQXNFO1FBQ3RFLHFFQUFxRTtRQUNyRSx1RUFBdUU7UUFDdkUsSUFBSSxDQUFDO1lBQ0gsSUFBSSxDQUFDLEtBQUssQ0FBQyxHQUFHLENBQUMsT0FBTyxDQUFDLENBQUM7UUFDMUIsQ0FBQztRQUFDLE9BQU8sQ0FBQyxFQUFFLENBQUM7WUFDWCx1RUFBdUU7WUFDdkUseUVBQXlFO1lBQ3pFLG9FQUFvRTtZQUNwRSx1RUFBdUU7WUFDdkUsc0VBQXNFO1lBQ3RFLDhEQUE4RDtZQUM5RCxJQUFLLENBQVMsQ0FBQyxXQUFXLENBQUMsSUFBSSxLQUFLLDBCQUEwQixFQUFFLENBQUM7Z0JBQy9ELE9BQU8sSUFBSSxDQUFDO1lBQ2QsQ0FBQztRQUNILENBQUM7UUFDRCxPQUFPLEtBQUssQ0FBQztJQUNmLENBQUM7SUFFRCxTQUFTLENBQUMsUUFBYyxFQUFFLE9BQWU7UUFDdkMsSUFBSSxDQUFDLEtBQUssQ0FBQyxTQUFTLENBQUMsUUFBUSxFQUFFLE9BQU8sQ0FBQyxDQUFDO0lBQzFDLENBQUM7SUFFRCxNQUFNLENBQUMsUUFBYyxFQUFFLE9BQWU7UUFDcEMsSUFBSSxDQUFDLEtBQUssQ0FBQyxNQUFNLENBQUMsUUFBUSxFQUFFLE9BQU8sQ0FBQyxDQUFDO0lBQ3ZDLENBQUM7SUFFRCxNQUFNLENBQUMsUUFBYztRQUNuQixJQUFJLENBQUMsS0FBSyxDQUFDLE1BQU0sQ0FBQyxRQUFRLENBQUMsQ0FBQztJQUM5QixDQUFDO0lBRUQsSUFBSSxDQUFDLFFBQWM7UUFDakIsTUFBTSxNQUFNLEdBQUcsSUFBSSxDQUFDLEtBQUssQ0FBQyxJQUFJLENBQUMsUUFBUSxDQUFDLENBQUM7UUFDekMsT0FBTyxNQUFNLEtBQUssSUFBSSxDQUFDLENBQUMsQ0FBQyxNQUFNLENBQUMsUUFBUSxFQUFFLENBQUMsQ0FBQyxDQUFDLElBQUksQ0FBQztJQUNwRCxDQUFDO0lBRUQsYUFBYSxDQUFDLE9BQWE7UUFDekIsTUFBTSxFQUFDLE9BQU8sRUFBRSxXQUFXLEVBQUUsUUFBUSxFQUFFLEtBQUssRUFBQyxHQUFHLElBQUksQ0FBQyxLQUFLLENBQUMsTUFBTSxDQUFDLE9BQU8sQ0FBQyxDQUFDO1FBQzNFLE9BQU8sRUFBQyxXQUFXLEVBQUUsS0FBSyxFQUFDLENBQUM7SUFDOUIsQ0FBQztDQUNGO0FBdkVELDRDQXVFQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5pbXBvcnQge25vcm1hbGl6ZSwgUGF0aH0gZnJvbSAnQGFuZ3VsYXItZGV2a2l0L2NvcmUnO1xuaW1wb3J0IHtUcmVlLCBVcGRhdGVSZWNvcmRlcn0gZnJvbSAnQGFuZ3VsYXItZGV2a2l0L3NjaGVtYXRpY3MnO1xuaW1wb3J0IHtEaXJlY3RvcnlFbnRyeSwgRmlsZVN5c3RlbX0gZnJvbSAnLi4vdXBkYXRlLXRvb2wvZmlsZS1zeXN0ZW0nO1xuaW1wb3J0ICogYXMgcGF0aCBmcm9tICdwYXRoJztcblxuLyoqXG4gKiBGaWxlIHN5c3RlbSB0aGF0IGxldmVyYWdlcyB0aGUgdmlydHVhbCB0cmVlIGZyb20gdGhlIENMSSBkZXZraXQuIFRoaXMgZmlsZVxuICogc3lzdGVtIGlzIGNvbW1vbmx5IHVzZWQgYnkgYG5nIHVwZGF0ZWAgbWlncmF0aW9ucyB0aGF0IHJ1biBhcyBwYXJ0IG9mIHRoZVxuICogQW5ndWxhciBDTEkuXG4gKi9cbmV4cG9ydCBjbGFzcyBEZXZraXRGaWxlU3lzdGVtIGV4dGVuZHMgRmlsZVN5c3RlbSB7XG4gIHByaXZhdGUgX3VwZGF0ZVJlY29yZGVyQ2FjaGUgPSBuZXcgTWFwPHN0cmluZywgVXBkYXRlUmVjb3JkZXI+KCk7XG5cbiAgY29uc3RydWN0b3IocHJpdmF0ZSBfdHJlZTogVHJlZSkge1xuICAgIHN1cGVyKCk7XG4gIH1cblxuICByZXNvbHZlKC4uLnNlZ21lbnRzOiBzdHJpbmdbXSk6IFBhdGgge1xuICAgIC8vIE5vdGU6IFdlIHVzZSBgcG9zaXgucmVzb2x2ZWAgYXMgdGhlIGRldmtpdCBwYXRocyBhcmUgdXNpbmcgcG9zaXggc2VwYXJhdG9ycy5cbiAgICByZXR1cm4gbm9ybWFsaXplKHBhdGgucG9zaXgucmVzb2x2ZSgnLycsIC4uLnNlZ21lbnRzLm1hcChub3JtYWxpemUpKSk7XG4gIH1cblxuICBlZGl0KGZpbGVQYXRoOiBQYXRoKSB7XG4gICAgaWYgKHRoaXMuX3VwZGF0ZVJlY29yZGVyQ2FjaGUuaGFzKGZpbGVQYXRoKSkge1xuICAgICAgcmV0dXJuIHRoaXMuX3VwZGF0ZVJlY29yZGVyQ2FjaGUuZ2V0KGZpbGVQYXRoKSE7XG4gICAgfVxuICAgIGNvbnN0IHJlY29yZGVyID0gdGhpcy5fdHJlZS5iZWdpblVwZGF0ZShmaWxlUGF0aCk7XG4gICAgdGhpcy5fdXBkYXRlUmVjb3JkZXJDYWNoZS5zZXQoZmlsZVBhdGgsIHJlY29yZGVyKTtcbiAgICByZXR1cm4gcmVjb3JkZXI7XG4gIH1cblxuICBjb21taXRFZGl0cygpIHtcbiAgICB0aGlzLl91cGRhdGVSZWNvcmRlckNhY2hlLmZvckVhY2gociA9PiB0aGlzLl90cmVlLmNvbW1pdFVwZGF0ZShyKSk7XG4gICAgdGhpcy5fdXBkYXRlUmVjb3JkZXJDYWNoZS5jbGVhcigpO1xuICB9XG5cbiAgZmlsZUV4aXN0cyhmaWxlUGF0aDogUGF0aCkge1xuICAgIHJldHVybiB0aGlzLl90cmVlLmV4aXN0cyhmaWxlUGF0aCk7XG4gIH1cblxuICBkaXJlY3RvcnlFeGlzdHMoZGlyUGF0aDogUGF0aCkge1xuICAgIC8vIFRoZSBkZXZraXQgdHJlZSBkb2VzIG5vdCBleHBvc2UgYW4gQVBJIGZvciBjaGVja2luZyB3aGV0aGVyIGEgZ2l2ZW5cbiAgICAvLyBkaXJlY3RvcnkgZXhpc3RzLiBJdCB0aHJvd3MgYSBzcGVjaWZpYyBlcnJvciB0aG91Z2ggaWYgYSBkaXJlY3RvcnlcbiAgICAvLyBpcyBiZWluZyByZWFkIGFzIGEgZmlsZS4gV2UgdXNlIHRoYXQgdG8gY2hlY2sgaWYgYSBkaXJlY3RvcnkgZXhpc3RzLlxuICAgIHRyeSB7XG4gICAgICB0aGlzLl90cmVlLmdldChkaXJQYXRoKTtcbiAgICB9IGNhdGNoIChlKSB7XG4gICAgICAvLyBOb3RlOiBXZSBkbyBub3QgdXNlIGFuIGBpbnN0YW5jZW9mYCBjaGVjayBoZXJlLiBJdCBjb3VsZCBoYXBwZW4gdGhhdFxuICAgICAgLy8gdGhlIGRldmtpdCB2ZXJzaW9uIHVzZWQgYnkgdGhlIENMSSBpcyBkaWZmZXJlbnQgdGhhbiB0aGUgb25lIHdlIGVuZCB1cFxuICAgICAgLy8gbG9hZGluZy4gVGhpcyBjYW4gaGFwcGVuIGRlcGVuZGluZyBvbiBob3cgWWFybi9OUE0gaG9pc3RzIHRoZSBOUE1cbiAgICAgIC8vIHBhY2thZ2VzIC8gd2hldGhlciB0aGVyZSBhcmUgbXVsdGlwbGUgdmVyc2lvbnMgaW5zdGFsbGVkLiBUeXBlc2NyaXB0XG4gICAgICAvLyB0aHJvd3MgYSBjb21waWxhdGlvbiBlcnJvciBpZiB0aGUgdHlwZSBpc24ndCBzcGVjaWZpZWQgYW5kIHdlIGNhbid0XG4gICAgICAvLyBjaGVjayB0aGUgdHlwZSwgc28gd2UgaGF2ZSB0byBjYXN0IHRoZSBlcnJvciBvdXRwdXQgdG8gYW55LlxuICAgICAgaWYgKChlIGFzIGFueSkuY29uc3RydWN0b3IubmFtZSA9PT0gJ1BhdGhJc0RpcmVjdG9yeUV4Y2VwdGlvbicpIHtcbiAgICAgICAgcmV0dXJuIHRydWU7XG4gICAgICB9XG4gICAgfVxuICAgIHJldHVybiBmYWxzZTtcbiAgfVxuXG4gIG92ZXJ3cml0ZShmaWxlUGF0aDogUGF0aCwgY29udGVudDogc3RyaW5nKSB7XG4gICAgdGhpcy5fdHJlZS5vdmVyd3JpdGUoZmlsZVBhdGgsIGNvbnRlbnQpO1xuICB9XG5cbiAgY3JlYXRlKGZpbGVQYXRoOiBQYXRoLCBjb250ZW50OiBzdHJpbmcpIHtcbiAgICB0aGlzLl90cmVlLmNyZWF0ZShmaWxlUGF0aCwgY29udGVudCk7XG4gIH1cblxuICBkZWxldGUoZmlsZVBhdGg6IFBhdGgpIHtcbiAgICB0aGlzLl90cmVlLmRlbGV0ZShmaWxlUGF0aCk7XG4gIH1cblxuICByZWFkKGZpbGVQYXRoOiBQYXRoKSB7XG4gICAgY29uc3QgYnVmZmVyID0gdGhpcy5fdHJlZS5yZWFkKGZpbGVQYXRoKTtcbiAgICByZXR1cm4gYnVmZmVyICE9PSBudWxsID8gYnVmZmVyLnRvU3RyaW5nKCkgOiBudWxsO1xuICB9XG5cbiAgcmVhZERpcmVjdG9yeShkaXJQYXRoOiBQYXRoKTogRGlyZWN0b3J5RW50cnkge1xuICAgIGNvbnN0IHtzdWJkaXJzOiBkaXJlY3Rvcmllcywgc3ViZmlsZXM6IGZpbGVzfSA9IHRoaXMuX3RyZWUuZ2V0RGlyKGRpclBhdGgpO1xuICAgIHJldHVybiB7ZGlyZWN0b3JpZXMsIGZpbGVzfTtcbiAgfVxufVxuIl19