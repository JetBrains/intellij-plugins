"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.PropertyNamesMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const upgrade_data_1 = require("../upgrade-data");
/**
 * Migration that walks through every property access expression and updates
 * accessed properties that have been updated to a new name.
 */
class PropertyNamesMigration extends migration_1.Migration {
    /** Change data that upgrades to the specified target version. */
    data = (0, upgrade_data_1.getVersionUpgradeData)(this, 'propertyNames');
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (ts.isPropertyAccessExpression(node)) {
            this._visitPropertyAccessExpression(node);
        }
    }
    _visitPropertyAccessExpression(node) {
        const hostType = this.typeChecker.getTypeAtLocation(node.expression);
        const typeNames = [];
        if (hostType) {
            if (hostType.isIntersection()) {
                hostType.types.forEach(type => {
                    if (type.symbol) {
                        typeNames.push(type.symbol.getName());
                    }
                });
            }
            else if (hostType.symbol) {
                typeNames.push(hostType.symbol.getName());
            }
        }
        this.data.forEach(data => {
            if (node.name.text !== data.replace) {
                return;
            }
            if (!data.limitedTo || typeNames.some(type => data.limitedTo.classes.includes(type))) {
                this.fileSystem
                    .edit(this.fileSystem.resolve(node.getSourceFile().fileName))
                    .remove(node.name.getStart(), node.name.getWidth())
                    .insertRight(node.name.getStart(), data.replaceWith);
            }
        });
    }
}
exports.PropertyNamesMigration = PropertyNamesMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoicHJvcGVydHktbmFtZXMuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi8uLi8uLi8uLi8uLi9zcmMvY2RrL3NjaGVtYXRpY3MvbmctdXBkYXRlL21pZ3JhdGlvbnMvcHJvcGVydHktbmFtZXMudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IjtBQUFBOzs7Ozs7R0FNRzs7O0FBRUgsaUNBQWlDO0FBQ2pDLDJEQUFzRDtBQUd0RCxrREFBbUU7QUFFbkU7OztHQUdHO0FBQ0gsTUFBYSxzQkFBdUIsU0FBUSxxQkFBc0I7SUFDaEUsaUVBQWlFO0lBQ2pFLElBQUksR0FBOEIsSUFBQSxvQ0FBcUIsRUFBQyxJQUFJLEVBQUUsZUFBZSxDQUFDLENBQUM7SUFFL0UsMkRBQTJEO0lBQzNELE9BQU8sR0FBRyxJQUFJLENBQUMsSUFBSSxDQUFDLE1BQU0sS0FBSyxDQUFDLENBQUM7SUFFeEIsU0FBUyxDQUFDLElBQWE7UUFDOUIsSUFBSSxFQUFFLENBQUMsMEJBQTBCLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQztZQUN4QyxJQUFJLENBQUMsOEJBQThCLENBQUMsSUFBSSxDQUFDLENBQUM7UUFDNUMsQ0FBQztJQUNILENBQUM7SUFFTyw4QkFBOEIsQ0FBQyxJQUFpQztRQUN0RSxNQUFNLFFBQVEsR0FBRyxJQUFJLENBQUMsV0FBVyxDQUFDLGlCQUFpQixDQUFDLElBQUksQ0FBQyxVQUFVLENBQUMsQ0FBQztRQUNyRSxNQUFNLFNBQVMsR0FBYSxFQUFFLENBQUM7UUFFL0IsSUFBSSxRQUFRLEVBQUUsQ0FBQztZQUNiLElBQUksUUFBUSxDQUFDLGNBQWMsRUFBRSxFQUFFLENBQUM7Z0JBQzlCLFFBQVEsQ0FBQyxLQUFLLENBQUMsT0FBTyxDQUFDLElBQUksQ0FBQyxFQUFFO29CQUM1QixJQUFJLElBQUksQ0FBQyxNQUFNLEVBQUUsQ0FBQzt3QkFDaEIsU0FBUyxDQUFDLElBQUksQ0FBQyxJQUFJLENBQUMsTUFBTSxDQUFDLE9BQU8sRUFBRSxDQUFDLENBQUM7b0JBQ3hDLENBQUM7Z0JBQ0gsQ0FBQyxDQUFDLENBQUM7WUFDTCxDQUFDO2lCQUFNLElBQUksUUFBUSxDQUFDLE1BQU0sRUFBRSxDQUFDO2dCQUMzQixTQUFTLENBQUMsSUFBSSxDQUFDLFFBQVEsQ0FBQyxNQUFNLENBQUMsT0FBTyxFQUFFLENBQUMsQ0FBQztZQUM1QyxDQUFDO1FBQ0gsQ0FBQztRQUVELElBQUksQ0FBQyxJQUFJLENBQUMsT0FBTyxDQUFDLElBQUksQ0FBQyxFQUFFO1lBQ3ZCLElBQUksSUFBSSxDQUFDLElBQUksQ0FBQyxJQUFJLEtBQUssSUFBSSxDQUFDLE9BQU8sRUFBRSxDQUFDO2dCQUNwQyxPQUFPO1lBQ1QsQ0FBQztZQUVELElBQUksQ0FBQyxJQUFJLENBQUMsU0FBUyxJQUFJLFNBQVMsQ0FBQyxJQUFJLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQyxJQUFJLENBQUMsU0FBUyxDQUFDLE9BQU8sQ0FBQyxRQUFRLENBQUMsSUFBSSxDQUFDLENBQUMsRUFBRSxDQUFDO2dCQUNyRixJQUFJLENBQUMsVUFBVTtxQkFDWixJQUFJLENBQUMsSUFBSSxDQUFDLFVBQVUsQ0FBQyxPQUFPLENBQUMsSUFBSSxDQUFDLGFBQWEsRUFBRSxDQUFDLFFBQVEsQ0FBQyxDQUFDO3FCQUM1RCxNQUFNLENBQUMsSUFBSSxDQUFDLElBQUksQ0FBQyxRQUFRLEVBQUUsRUFBRSxJQUFJLENBQUMsSUFBSSxDQUFDLFFBQVEsRUFBRSxDQUFDO3FCQUNsRCxXQUFXLENBQUMsSUFBSSxDQUFDLElBQUksQ0FBQyxRQUFRLEVBQUUsRUFBRSxJQUFJLENBQUMsV0FBVyxDQUFDLENBQUM7WUFDekQsQ0FBQztRQUNILENBQUMsQ0FBQyxDQUFDO0lBQ0wsQ0FBQztDQUNGO0FBMUNELHdEQTBDQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5pbXBvcnQgKiBhcyB0cyBmcm9tICd0eXBlc2NyaXB0JztcbmltcG9ydCB7TWlncmF0aW9ufSBmcm9tICcuLi8uLi91cGRhdGUtdG9vbC9taWdyYXRpb24nO1xuXG5pbXBvcnQge1Byb3BlcnR5TmFtZVVwZ3JhZGVEYXRhfSBmcm9tICcuLi9kYXRhJztcbmltcG9ydCB7Z2V0VmVyc2lvblVwZ3JhZGVEYXRhLCBVcGdyYWRlRGF0YX0gZnJvbSAnLi4vdXBncmFkZS1kYXRhJztcblxuLyoqXG4gKiBNaWdyYXRpb24gdGhhdCB3YWxrcyB0aHJvdWdoIGV2ZXJ5IHByb3BlcnR5IGFjY2VzcyBleHByZXNzaW9uIGFuZCB1cGRhdGVzXG4gKiBhY2Nlc3NlZCBwcm9wZXJ0aWVzIHRoYXQgaGF2ZSBiZWVuIHVwZGF0ZWQgdG8gYSBuZXcgbmFtZS5cbiAqL1xuZXhwb3J0IGNsYXNzIFByb3BlcnR5TmFtZXNNaWdyYXRpb24gZXh0ZW5kcyBNaWdyYXRpb248VXBncmFkZURhdGE+IHtcbiAgLyoqIENoYW5nZSBkYXRhIHRoYXQgdXBncmFkZXMgdG8gdGhlIHNwZWNpZmllZCB0YXJnZXQgdmVyc2lvbi4gKi9cbiAgZGF0YTogUHJvcGVydHlOYW1lVXBncmFkZURhdGFbXSA9IGdldFZlcnNpb25VcGdyYWRlRGF0YSh0aGlzLCAncHJvcGVydHlOYW1lcycpO1xuXG4gIC8vIE9ubHkgZW5hYmxlIHRoZSBtaWdyYXRpb24gcnVsZSBpZiB0aGVyZSBpcyB1cGdyYWRlIGRhdGEuXG4gIGVuYWJsZWQgPSB0aGlzLmRhdGEubGVuZ3RoICE9PSAwO1xuXG4gIG92ZXJyaWRlIHZpc2l0Tm9kZShub2RlOiB0cy5Ob2RlKTogdm9pZCB7XG4gICAgaWYgKHRzLmlzUHJvcGVydHlBY2Nlc3NFeHByZXNzaW9uKG5vZGUpKSB7XG4gICAgICB0aGlzLl92aXNpdFByb3BlcnR5QWNjZXNzRXhwcmVzc2lvbihub2RlKTtcbiAgICB9XG4gIH1cblxuICBwcml2YXRlIF92aXNpdFByb3BlcnR5QWNjZXNzRXhwcmVzc2lvbihub2RlOiB0cy5Qcm9wZXJ0eUFjY2Vzc0V4cHJlc3Npb24pIHtcbiAgICBjb25zdCBob3N0VHlwZSA9IHRoaXMudHlwZUNoZWNrZXIuZ2V0VHlwZUF0TG9jYXRpb24obm9kZS5leHByZXNzaW9uKTtcbiAgICBjb25zdCB0eXBlTmFtZXM6IHN0cmluZ1tdID0gW107XG5cbiAgICBpZiAoaG9zdFR5cGUpIHtcbiAgICAgIGlmIChob3N0VHlwZS5pc0ludGVyc2VjdGlvbigpKSB7XG4gICAgICAgIGhvc3RUeXBlLnR5cGVzLmZvckVhY2godHlwZSA9PiB7XG4gICAgICAgICAgaWYgKHR5cGUuc3ltYm9sKSB7XG4gICAgICAgICAgICB0eXBlTmFtZXMucHVzaCh0eXBlLnN5bWJvbC5nZXROYW1lKCkpO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9IGVsc2UgaWYgKGhvc3RUeXBlLnN5bWJvbCkge1xuICAgICAgICB0eXBlTmFtZXMucHVzaChob3N0VHlwZS5zeW1ib2wuZ2V0TmFtZSgpKTtcbiAgICAgIH1cbiAgICB9XG5cbiAgICB0aGlzLmRhdGEuZm9yRWFjaChkYXRhID0+IHtcbiAgICAgIGlmIChub2RlLm5hbWUudGV4dCAhPT0gZGF0YS5yZXBsYWNlKSB7XG4gICAgICAgIHJldHVybjtcbiAgICAgIH1cblxuICAgICAgaWYgKCFkYXRhLmxpbWl0ZWRUbyB8fCB0eXBlTmFtZXMuc29tZSh0eXBlID0+IGRhdGEubGltaXRlZFRvLmNsYXNzZXMuaW5jbHVkZXModHlwZSkpKSB7XG4gICAgICAgIHRoaXMuZmlsZVN5c3RlbVxuICAgICAgICAgIC5lZGl0KHRoaXMuZmlsZVN5c3RlbS5yZXNvbHZlKG5vZGUuZ2V0U291cmNlRmlsZSgpLmZpbGVOYW1lKSlcbiAgICAgICAgICAucmVtb3ZlKG5vZGUubmFtZS5nZXRTdGFydCgpLCBub2RlLm5hbWUuZ2V0V2lkdGgoKSlcbiAgICAgICAgICAuaW5zZXJ0UmlnaHQobm9kZS5uYW1lLmdldFN0YXJ0KCksIGRhdGEucmVwbGFjZVdpdGgpO1xuICAgICAgfVxuICAgIH0pO1xuICB9XG59XG4iXX0=