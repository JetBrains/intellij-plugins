"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.SymbolRemovalMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const upgrade_data_1 = require("../upgrade-data");
/** Migration that flags imports for symbols that have been removed. */
class SymbolRemovalMigration extends migration_1.Migration {
    /** Change data that upgrades to the specified target version. */
    data = (0, upgrade_data_1.getVersionUpgradeData)(this, 'symbolRemoval');
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (!ts.isImportDeclaration(node) || !ts.isStringLiteral(node.moduleSpecifier)) {
            return;
        }
        const namedBindings = node.importClause && node.importClause.namedBindings;
        if (!namedBindings || !ts.isNamedImports(namedBindings)) {
            return;
        }
        const moduleNameMatches = this.data.filter(entry => node.moduleSpecifier.text === entry.module);
        if (!moduleNameMatches.length) {
            return;
        }
        namedBindings.elements.forEach(element => {
            const elementName = element.propertyName?.text || element.name.text;
            moduleNameMatches.forEach(match => {
                if (match.name === elementName) {
                    this.createFailureAtNode(element, match.message);
                }
            });
        });
    }
}
exports.SymbolRemovalMigration = SymbolRemovalMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoic3ltYm9sLXJlbW92YWwuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi8uLi8uLi8uLi8uLi9zcmMvY2RrL3NjaGVtYXRpY3MvbmctdXBkYXRlL21pZ3JhdGlvbnMvc3ltYm9sLXJlbW92YWwudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IjtBQUFBOzs7Ozs7R0FNRzs7O0FBRUgsaUNBQWlDO0FBQ2pDLDJEQUFzRDtBQUV0RCxrREFBbUU7QUFFbkUsdUVBQXVFO0FBQ3ZFLE1BQWEsc0JBQXVCLFNBQVEscUJBQXNCO0lBQ2hFLGlFQUFpRTtJQUNqRSxJQUFJLEdBQStCLElBQUEsb0NBQXFCLEVBQUMsSUFBSSxFQUFFLGVBQWUsQ0FBQyxDQUFDO0lBRWhGLDJEQUEyRDtJQUMzRCxPQUFPLEdBQUcsSUFBSSxDQUFDLElBQUksQ0FBQyxNQUFNLEtBQUssQ0FBQyxDQUFDO0lBRXhCLFNBQVMsQ0FBQyxJQUFhO1FBQzlCLElBQUksQ0FBQyxFQUFFLENBQUMsbUJBQW1CLENBQUMsSUFBSSxDQUFDLElBQUksQ0FBQyxFQUFFLENBQUMsZUFBZSxDQUFDLElBQUksQ0FBQyxlQUFlLENBQUMsRUFBRSxDQUFDO1lBQy9FLE9BQU87UUFDVCxDQUFDO1FBRUQsTUFBTSxhQUFhLEdBQUcsSUFBSSxDQUFDLFlBQVksSUFBSSxJQUFJLENBQUMsWUFBWSxDQUFDLGFBQWEsQ0FBQztRQUUzRSxJQUFJLENBQUMsYUFBYSxJQUFJLENBQUMsRUFBRSxDQUFDLGNBQWMsQ0FBQyxhQUFhLENBQUMsRUFBRSxDQUFDO1lBQ3hELE9BQU87UUFDVCxDQUFDO1FBRUQsTUFBTSxpQkFBaUIsR0FBRyxJQUFJLENBQUMsSUFBSSxDQUFDLE1BQU0sQ0FDeEMsS0FBSyxDQUFDLEVBQUUsQ0FBRSxJQUFJLENBQUMsZUFBb0MsQ0FBQyxJQUFJLEtBQUssS0FBSyxDQUFDLE1BQU0sQ0FDMUUsQ0FBQztRQUVGLElBQUksQ0FBQyxpQkFBaUIsQ0FBQyxNQUFNLEVBQUUsQ0FBQztZQUM5QixPQUFPO1FBQ1QsQ0FBQztRQUVELGFBQWEsQ0FBQyxRQUFRLENBQUMsT0FBTyxDQUFDLE9BQU8sQ0FBQyxFQUFFO1lBQ3ZDLE1BQU0sV0FBVyxHQUFHLE9BQU8sQ0FBQyxZQUFZLEVBQUUsSUFBSSxJQUFJLE9BQU8sQ0FBQyxJQUFJLENBQUMsSUFBSSxDQUFDO1lBRXBFLGlCQUFpQixDQUFDLE9BQU8sQ0FBQyxLQUFLLENBQUMsRUFBRTtnQkFDaEMsSUFBSSxLQUFLLENBQUMsSUFBSSxLQUFLLFdBQVcsRUFBRSxDQUFDO29CQUMvQixJQUFJLENBQUMsbUJBQW1CLENBQUMsT0FBTyxFQUFFLEtBQUssQ0FBQyxPQUFPLENBQUMsQ0FBQztnQkFDbkQsQ0FBQztZQUNILENBQUMsQ0FBQyxDQUFDO1FBQ0wsQ0FBQyxDQUFDLENBQUM7SUFDTCxDQUFDO0NBQ0Y7QUFwQ0Qsd0RBb0NDIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5cbmltcG9ydCAqIGFzIHRzIGZyb20gJ3R5cGVzY3JpcHQnO1xuaW1wb3J0IHtNaWdyYXRpb259IGZyb20gJy4uLy4uL3VwZGF0ZS10b29sL21pZ3JhdGlvbic7XG5pbXBvcnQge1N5bWJvbFJlbW92YWxVcGdyYWRlRGF0YX0gZnJvbSAnLi4vZGF0YSc7XG5pbXBvcnQge2dldFZlcnNpb25VcGdyYWRlRGF0YSwgVXBncmFkZURhdGF9IGZyb20gJy4uL3VwZ3JhZGUtZGF0YSc7XG5cbi8qKiBNaWdyYXRpb24gdGhhdCBmbGFncyBpbXBvcnRzIGZvciBzeW1ib2xzIHRoYXQgaGF2ZSBiZWVuIHJlbW92ZWQuICovXG5leHBvcnQgY2xhc3MgU3ltYm9sUmVtb3ZhbE1pZ3JhdGlvbiBleHRlbmRzIE1pZ3JhdGlvbjxVcGdyYWRlRGF0YT4ge1xuICAvKiogQ2hhbmdlIGRhdGEgdGhhdCB1cGdyYWRlcyB0byB0aGUgc3BlY2lmaWVkIHRhcmdldCB2ZXJzaW9uLiAqL1xuICBkYXRhOiBTeW1ib2xSZW1vdmFsVXBncmFkZURhdGFbXSA9IGdldFZlcnNpb25VcGdyYWRlRGF0YSh0aGlzLCAnc3ltYm9sUmVtb3ZhbCcpO1xuXG4gIC8vIE9ubHkgZW5hYmxlIHRoZSBtaWdyYXRpb24gcnVsZSBpZiB0aGVyZSBpcyB1cGdyYWRlIGRhdGEuXG4gIGVuYWJsZWQgPSB0aGlzLmRhdGEubGVuZ3RoICE9PSAwO1xuXG4gIG92ZXJyaWRlIHZpc2l0Tm9kZShub2RlOiB0cy5Ob2RlKTogdm9pZCB7XG4gICAgaWYgKCF0cy5pc0ltcG9ydERlY2xhcmF0aW9uKG5vZGUpIHx8ICF0cy5pc1N0cmluZ0xpdGVyYWwobm9kZS5tb2R1bGVTcGVjaWZpZXIpKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY29uc3QgbmFtZWRCaW5kaW5ncyA9IG5vZGUuaW1wb3J0Q2xhdXNlICYmIG5vZGUuaW1wb3J0Q2xhdXNlLm5hbWVkQmluZGluZ3M7XG5cbiAgICBpZiAoIW5hbWVkQmluZGluZ3MgfHwgIXRzLmlzTmFtZWRJbXBvcnRzKG5hbWVkQmluZGluZ3MpKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY29uc3QgbW9kdWxlTmFtZU1hdGNoZXMgPSB0aGlzLmRhdGEuZmlsdGVyKFxuICAgICAgZW50cnkgPT4gKG5vZGUubW9kdWxlU3BlY2lmaWVyIGFzIHRzLlN0cmluZ0xpdGVyYWwpLnRleHQgPT09IGVudHJ5Lm1vZHVsZSxcbiAgICApO1xuXG4gICAgaWYgKCFtb2R1bGVOYW1lTWF0Y2hlcy5sZW5ndGgpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBuYW1lZEJpbmRpbmdzLmVsZW1lbnRzLmZvckVhY2goZWxlbWVudCA9PiB7XG4gICAgICBjb25zdCBlbGVtZW50TmFtZSA9IGVsZW1lbnQucHJvcGVydHlOYW1lPy50ZXh0IHx8IGVsZW1lbnQubmFtZS50ZXh0O1xuXG4gICAgICBtb2R1bGVOYW1lTWF0Y2hlcy5mb3JFYWNoKG1hdGNoID0+IHtcbiAgICAgICAgaWYgKG1hdGNoLm5hbWUgPT09IGVsZW1lbnROYW1lKSB7XG4gICAgICAgICAgdGhpcy5jcmVhdGVGYWlsdXJlQXROb2RlKGVsZW1lbnQsIG1hdGNoLm1lc3NhZ2UpO1xuICAgICAgICB9XG4gICAgICB9KTtcbiAgICB9KTtcbiAgfVxufVxuIl19