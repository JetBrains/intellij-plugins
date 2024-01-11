"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.CssSelectorsMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const literal_1 = require("../typescript/literal");
const upgrade_data_1 = require("../upgrade-data");
/**
 * Migration that walks through every string literal, template and stylesheet in
 * order to migrate outdated CSS selectors to the new selector.
 */
class CssSelectorsMigration extends migration_1.Migration {
    /** Change data that upgrades to the specified target version. */
    data = (0, upgrade_data_1.getVersionUpgradeData)(this, 'cssSelectors');
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (ts.isStringLiteralLike(node)) {
            this._visitStringLiteralLike(node);
        }
    }
    visitTemplate(template) {
        this.data.forEach(data => {
            if (data.replaceIn && !data.replaceIn.html) {
                return;
            }
            (0, literal_1.findAllSubstringIndices)(template.content, data.replace)
                .map(offset => template.start + offset)
                .forEach(start => this._replaceSelector(template.filePath, start, data));
        });
    }
    visitStylesheet(stylesheet) {
        this.data.forEach(data => {
            if (data.replaceIn && !data.replaceIn.stylesheet) {
                return;
            }
            (0, literal_1.findAllSubstringIndices)(stylesheet.content, data.replace)
                .map(offset => stylesheet.start + offset)
                .forEach(start => this._replaceSelector(stylesheet.filePath, start, data));
        });
    }
    _visitStringLiteralLike(node) {
        if (node.parent && node.parent.kind !== ts.SyntaxKind.CallExpression) {
            return;
        }
        const textContent = node.getText();
        const filePath = this.fileSystem.resolve(node.getSourceFile().fileName);
        this.data.forEach(data => {
            if (data.replaceIn && !data.replaceIn.tsStringLiterals) {
                return;
            }
            (0, literal_1.findAllSubstringIndices)(textContent, data.replace)
                .map(offset => node.getStart() + offset)
                .forEach(start => this._replaceSelector(filePath, start, data));
        });
    }
    _replaceSelector(filePath, start, data) {
        this.fileSystem
            .edit(filePath)
            .remove(start, data.replace.length)
            .insertRight(start, data.replaceWith);
    }
}
exports.CssSelectorsMigration = CssSelectorsMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiY3NzLXNlbGVjdG9ycy5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy9uZy11cGRhdGUvbWlncmF0aW9ucy9jc3Mtc2VsZWN0b3JzLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7OztBQUVILGlDQUFpQztBQUdqQywyREFBc0Q7QUFFdEQsbURBQThEO0FBQzlELGtEQUFtRTtBQUVuRTs7O0dBR0c7QUFDSCxNQUFhLHFCQUFzQixTQUFRLHFCQUFzQjtJQUMvRCxpRUFBaUU7SUFDakUsSUFBSSxHQUE2QixJQUFBLG9DQUFxQixFQUFDLElBQUksRUFBRSxjQUFjLENBQUMsQ0FBQztJQUU3RSwyREFBMkQ7SUFDM0QsT0FBTyxHQUFHLElBQUksQ0FBQyxJQUFJLENBQUMsTUFBTSxLQUFLLENBQUMsQ0FBQztJQUV4QixTQUFTLENBQUMsSUFBYTtRQUM5QixJQUFJLEVBQUUsQ0FBQyxtQkFBbUIsQ0FBQyxJQUFJLENBQUMsRUFBRSxDQUFDO1lBQ2pDLElBQUksQ0FBQyx1QkFBdUIsQ0FBQyxJQUFJLENBQUMsQ0FBQztRQUNyQyxDQUFDO0lBQ0gsQ0FBQztJQUVRLGFBQWEsQ0FBQyxRQUEwQjtRQUMvQyxJQUFJLENBQUMsSUFBSSxDQUFDLE9BQU8sQ0FBQyxJQUFJLENBQUMsRUFBRTtZQUN2QixJQUFJLElBQUksQ0FBQyxTQUFTLElBQUksQ0FBQyxJQUFJLENBQUMsU0FBUyxDQUFDLElBQUksRUFBRSxDQUFDO2dCQUMzQyxPQUFPO1lBQ1QsQ0FBQztZQUVELElBQUEsaUNBQXVCLEVBQUMsUUFBUSxDQUFDLE9BQU8sRUFBRSxJQUFJLENBQUMsT0FBTyxDQUFDO2lCQUNwRCxHQUFHLENBQUMsTUFBTSxDQUFDLEVBQUUsQ0FBQyxRQUFRLENBQUMsS0FBSyxHQUFHLE1BQU0sQ0FBQztpQkFDdEMsT0FBTyxDQUFDLEtBQUssQ0FBQyxFQUFFLENBQUMsSUFBSSxDQUFDLGdCQUFnQixDQUFDLFFBQVEsQ0FBQyxRQUFRLEVBQUUsS0FBSyxFQUFFLElBQUksQ0FBQyxDQUFDLENBQUM7UUFDN0UsQ0FBQyxDQUFDLENBQUM7SUFDTCxDQUFDO0lBRVEsZUFBZSxDQUFDLFVBQTRCO1FBQ25ELElBQUksQ0FBQyxJQUFJLENBQUMsT0FBTyxDQUFDLElBQUksQ0FBQyxFQUFFO1lBQ3ZCLElBQUksSUFBSSxDQUFDLFNBQVMsSUFBSSxDQUFDLElBQUksQ0FBQyxTQUFTLENBQUMsVUFBVSxFQUFFLENBQUM7Z0JBQ2pELE9BQU87WUFDVCxDQUFDO1lBRUQsSUFBQSxpQ0FBdUIsRUFBQyxVQUFVLENBQUMsT0FBTyxFQUFFLElBQUksQ0FBQyxPQUFPLENBQUM7aUJBQ3RELEdBQUcsQ0FBQyxNQUFNLENBQUMsRUFBRSxDQUFDLFVBQVUsQ0FBQyxLQUFLLEdBQUcsTUFBTSxDQUFDO2lCQUN4QyxPQUFPLENBQUMsS0FBSyxDQUFDLEVBQUUsQ0FBQyxJQUFJLENBQUMsZ0JBQWdCLENBQUMsVUFBVSxDQUFDLFFBQVEsRUFBRSxLQUFLLEVBQUUsSUFBSSxDQUFDLENBQUMsQ0FBQztRQUMvRSxDQUFDLENBQUMsQ0FBQztJQUNMLENBQUM7SUFFTyx1QkFBdUIsQ0FBQyxJQUEwQjtRQUN4RCxJQUFJLElBQUksQ0FBQyxNQUFNLElBQUksSUFBSSxDQUFDLE1BQU0sQ0FBQyxJQUFJLEtBQUssRUFBRSxDQUFDLFVBQVUsQ0FBQyxjQUFjLEVBQUUsQ0FBQztZQUNyRSxPQUFPO1FBQ1QsQ0FBQztRQUVELE1BQU0sV0FBVyxHQUFHLElBQUksQ0FBQyxPQUFPLEVBQUUsQ0FBQztRQUNuQyxNQUFNLFFBQVEsR0FBRyxJQUFJLENBQUMsVUFBVSxDQUFDLE9BQU8sQ0FBQyxJQUFJLENBQUMsYUFBYSxFQUFFLENBQUMsUUFBUSxDQUFDLENBQUM7UUFFeEUsSUFBSSxDQUFDLElBQUksQ0FBQyxPQUFPLENBQUMsSUFBSSxDQUFDLEVBQUU7WUFDdkIsSUFBSSxJQUFJLENBQUMsU0FBUyxJQUFJLENBQUMsSUFBSSxDQUFDLFNBQVMsQ0FBQyxnQkFBZ0IsRUFBRSxDQUFDO2dCQUN2RCxPQUFPO1lBQ1QsQ0FBQztZQUVELElBQUEsaUNBQXVCLEVBQUMsV0FBVyxFQUFFLElBQUksQ0FBQyxPQUFPLENBQUM7aUJBQy9DLEdBQUcsQ0FBQyxNQUFNLENBQUMsRUFBRSxDQUFDLElBQUksQ0FBQyxRQUFRLEVBQUUsR0FBRyxNQUFNLENBQUM7aUJBQ3ZDLE9BQU8sQ0FBQyxLQUFLLENBQUMsRUFBRSxDQUFDLElBQUksQ0FBQyxnQkFBZ0IsQ0FBQyxRQUFRLEVBQUUsS0FBSyxFQUFFLElBQUksQ0FBQyxDQUFDLENBQUM7UUFDcEUsQ0FBQyxDQUFDLENBQUM7SUFDTCxDQUFDO0lBRU8sZ0JBQWdCLENBQUMsUUFBdUIsRUFBRSxLQUFhLEVBQUUsSUFBNEI7UUFDM0YsSUFBSSxDQUFDLFVBQVU7YUFDWixJQUFJLENBQUMsUUFBUSxDQUFDO2FBQ2QsTUFBTSxDQUFDLEtBQUssRUFBRSxJQUFJLENBQUMsT0FBTyxDQUFDLE1BQU0sQ0FBQzthQUNsQyxXQUFXLENBQUMsS0FBSyxFQUFFLElBQUksQ0FBQyxXQUFXLENBQUMsQ0FBQztJQUMxQyxDQUFDO0NBQ0Y7QUE5REQsc0RBOERDIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5cbmltcG9ydCAqIGFzIHRzIGZyb20gJ3R5cGVzY3JpcHQnO1xuaW1wb3J0IHtXb3Jrc3BhY2VQYXRofSBmcm9tICcuLi8uLi91cGRhdGUtdG9vbC9maWxlLXN5c3RlbSc7XG5pbXBvcnQge1Jlc29sdmVkUmVzb3VyY2V9IGZyb20gJy4uLy4uL3VwZGF0ZS10b29sL2NvbXBvbmVudC1yZXNvdXJjZS1jb2xsZWN0b3InO1xuaW1wb3J0IHtNaWdyYXRpb259IGZyb20gJy4uLy4uL3VwZGF0ZS10b29sL21pZ3JhdGlvbic7XG5pbXBvcnQge0Nzc1NlbGVjdG9yVXBncmFkZURhdGF9IGZyb20gJy4uL2RhdGEvY3NzLXNlbGVjdG9ycyc7XG5pbXBvcnQge2ZpbmRBbGxTdWJzdHJpbmdJbmRpY2VzfSBmcm9tICcuLi90eXBlc2NyaXB0L2xpdGVyYWwnO1xuaW1wb3J0IHtnZXRWZXJzaW9uVXBncmFkZURhdGEsIFVwZ3JhZGVEYXRhfSBmcm9tICcuLi91cGdyYWRlLWRhdGEnO1xuXG4vKipcbiAqIE1pZ3JhdGlvbiB0aGF0IHdhbGtzIHRocm91Z2ggZXZlcnkgc3RyaW5nIGxpdGVyYWwsIHRlbXBsYXRlIGFuZCBzdHlsZXNoZWV0IGluXG4gKiBvcmRlciB0byBtaWdyYXRlIG91dGRhdGVkIENTUyBzZWxlY3RvcnMgdG8gdGhlIG5ldyBzZWxlY3Rvci5cbiAqL1xuZXhwb3J0IGNsYXNzIENzc1NlbGVjdG9yc01pZ3JhdGlvbiBleHRlbmRzIE1pZ3JhdGlvbjxVcGdyYWRlRGF0YT4ge1xuICAvKiogQ2hhbmdlIGRhdGEgdGhhdCB1cGdyYWRlcyB0byB0aGUgc3BlY2lmaWVkIHRhcmdldCB2ZXJzaW9uLiAqL1xuICBkYXRhOiBDc3NTZWxlY3RvclVwZ3JhZGVEYXRhW10gPSBnZXRWZXJzaW9uVXBncmFkZURhdGEodGhpcywgJ2Nzc1NlbGVjdG9ycycpO1xuXG4gIC8vIE9ubHkgZW5hYmxlIHRoZSBtaWdyYXRpb24gcnVsZSBpZiB0aGVyZSBpcyB1cGdyYWRlIGRhdGEuXG4gIGVuYWJsZWQgPSB0aGlzLmRhdGEubGVuZ3RoICE9PSAwO1xuXG4gIG92ZXJyaWRlIHZpc2l0Tm9kZShub2RlOiB0cy5Ob2RlKTogdm9pZCB7XG4gICAgaWYgKHRzLmlzU3RyaW5nTGl0ZXJhbExpa2Uobm9kZSkpIHtcbiAgICAgIHRoaXMuX3Zpc2l0U3RyaW5nTGl0ZXJhbExpa2Uobm9kZSk7XG4gICAgfVxuICB9XG5cbiAgb3ZlcnJpZGUgdmlzaXRUZW1wbGF0ZSh0ZW1wbGF0ZTogUmVzb2x2ZWRSZXNvdXJjZSk6IHZvaWQge1xuICAgIHRoaXMuZGF0YS5mb3JFYWNoKGRhdGEgPT4ge1xuICAgICAgaWYgKGRhdGEucmVwbGFjZUluICYmICFkYXRhLnJlcGxhY2VJbi5odG1sKSB7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cblxuICAgICAgZmluZEFsbFN1YnN0cmluZ0luZGljZXModGVtcGxhdGUuY29udGVudCwgZGF0YS5yZXBsYWNlKVxuICAgICAgICAubWFwKG9mZnNldCA9PiB0ZW1wbGF0ZS5zdGFydCArIG9mZnNldClcbiAgICAgICAgLmZvckVhY2goc3RhcnQgPT4gdGhpcy5fcmVwbGFjZVNlbGVjdG9yKHRlbXBsYXRlLmZpbGVQYXRoLCBzdGFydCwgZGF0YSkpO1xuICAgIH0pO1xuICB9XG5cbiAgb3ZlcnJpZGUgdmlzaXRTdHlsZXNoZWV0KHN0eWxlc2hlZXQ6IFJlc29sdmVkUmVzb3VyY2UpOiB2b2lkIHtcbiAgICB0aGlzLmRhdGEuZm9yRWFjaChkYXRhID0+IHtcbiAgICAgIGlmIChkYXRhLnJlcGxhY2VJbiAmJiAhZGF0YS5yZXBsYWNlSW4uc3R5bGVzaGVldCkge1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIGZpbmRBbGxTdWJzdHJpbmdJbmRpY2VzKHN0eWxlc2hlZXQuY29udGVudCwgZGF0YS5yZXBsYWNlKVxuICAgICAgICAubWFwKG9mZnNldCA9PiBzdHlsZXNoZWV0LnN0YXJ0ICsgb2Zmc2V0KVxuICAgICAgICAuZm9yRWFjaChzdGFydCA9PiB0aGlzLl9yZXBsYWNlU2VsZWN0b3Ioc3R5bGVzaGVldC5maWxlUGF0aCwgc3RhcnQsIGRhdGEpKTtcbiAgICB9KTtcbiAgfVxuXG4gIHByaXZhdGUgX3Zpc2l0U3RyaW5nTGl0ZXJhbExpa2Uobm9kZTogdHMuU3RyaW5nTGl0ZXJhbExpa2UpIHtcbiAgICBpZiAobm9kZS5wYXJlbnQgJiYgbm9kZS5wYXJlbnQua2luZCAhPT0gdHMuU3ludGF4S2luZC5DYWxsRXhwcmVzc2lvbikge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNvbnN0IHRleHRDb250ZW50ID0gbm9kZS5nZXRUZXh0KCk7XG4gICAgY29uc3QgZmlsZVBhdGggPSB0aGlzLmZpbGVTeXN0ZW0ucmVzb2x2ZShub2RlLmdldFNvdXJjZUZpbGUoKS5maWxlTmFtZSk7XG5cbiAgICB0aGlzLmRhdGEuZm9yRWFjaChkYXRhID0+IHtcbiAgICAgIGlmIChkYXRhLnJlcGxhY2VJbiAmJiAhZGF0YS5yZXBsYWNlSW4udHNTdHJpbmdMaXRlcmFscykge1xuICAgICAgICByZXR1cm47XG4gICAgICB9XG5cbiAgICAgIGZpbmRBbGxTdWJzdHJpbmdJbmRpY2VzKHRleHRDb250ZW50LCBkYXRhLnJlcGxhY2UpXG4gICAgICAgIC5tYXAob2Zmc2V0ID0+IG5vZGUuZ2V0U3RhcnQoKSArIG9mZnNldClcbiAgICAgICAgLmZvckVhY2goc3RhcnQgPT4gdGhpcy5fcmVwbGFjZVNlbGVjdG9yKGZpbGVQYXRoLCBzdGFydCwgZGF0YSkpO1xuICAgIH0pO1xuICB9XG5cbiAgcHJpdmF0ZSBfcmVwbGFjZVNlbGVjdG9yKGZpbGVQYXRoOiBXb3Jrc3BhY2VQYXRoLCBzdGFydDogbnVtYmVyLCBkYXRhOiBDc3NTZWxlY3RvclVwZ3JhZGVEYXRhKSB7XG4gICAgdGhpcy5maWxlU3lzdGVtXG4gICAgICAuZWRpdChmaWxlUGF0aClcbiAgICAgIC5yZW1vdmUoc3RhcnQsIGRhdGEucmVwbGFjZS5sZW5ndGgpXG4gICAgICAuaW5zZXJ0UmlnaHQoc3RhcnQsIGRhdGEucmVwbGFjZVdpdGgpO1xuICB9XG59XG4iXX0=