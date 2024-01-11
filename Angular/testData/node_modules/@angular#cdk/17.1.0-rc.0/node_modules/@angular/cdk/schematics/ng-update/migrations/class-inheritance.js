"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.ClassInheritanceMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const base_types_1 = require("../typescript/base-types");
const upgrade_data_1 = require("../upgrade-data");
/**
 * Migration that identifies class declarations that extend CDK or Material classes
 * which had a public property change.
 */
class ClassInheritanceMigration extends migration_1.Migration {
    /**
     * Map of classes that have been updated. Each class name maps to the according property
     * change data.
     */
    propertyNames = new Map();
    // Only enable the migration rule if there is upgrade data.
    enabled = this.propertyNames.size !== 0;
    init() {
        (0, upgrade_data_1.getVersionUpgradeData)(this, 'propertyNames')
            .filter(data => data.limitedTo && data.limitedTo.classes)
            .forEach(data => data.limitedTo.classes.forEach(name => this.propertyNames.set(name, data)));
    }
    visitNode(node) {
        if (ts.isClassDeclaration(node)) {
            this._visitClassDeclaration(node);
        }
    }
    _visitClassDeclaration(node) {
        const baseTypes = (0, base_types_1.determineBaseTypes)(node);
        const className = node.name ? node.name.text : '{unknown-name}';
        if (!baseTypes) {
            return;
        }
        baseTypes.forEach(typeName => {
            const data = this.propertyNames.get(typeName);
            if (data) {
                this.createFailureAtNode(node, `Found class "${className}" which extends class ` +
                    `"${typeName}". Please note that the base class property ` +
                    `"${data.replace}" has changed to "${data.replaceWith}". ` +
                    `You may need to update your class as well.`);
            }
        });
    }
}
exports.ClassInheritanceMigration = ClassInheritanceMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiY2xhc3MtaW5oZXJpdGFuY2UuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi8uLi8uLi8uLi8uLi9zcmMvY2RrL3NjaGVtYXRpY3MvbmctdXBkYXRlL21pZ3JhdGlvbnMvY2xhc3MtaW5oZXJpdGFuY2UudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IjtBQUFBOzs7Ozs7R0FNRzs7O0FBRUgsaUNBQWlDO0FBQ2pDLDJEQUFzRDtBQUV0RCx5REFBNEQ7QUFDNUQsa0RBQW1FO0FBRW5FOzs7R0FHRztBQUNILE1BQWEseUJBQTBCLFNBQVEscUJBQXNCO0lBQ25FOzs7T0FHRztJQUNILGFBQWEsR0FBRyxJQUFJLEdBQUcsRUFBbUMsQ0FBQztJQUUzRCwyREFBMkQ7SUFDM0QsT0FBTyxHQUFHLElBQUksQ0FBQyxhQUFhLENBQUMsSUFBSSxLQUFLLENBQUMsQ0FBQztJQUUvQixJQUFJO1FBQ1gsSUFBQSxvQ0FBcUIsRUFBQyxJQUFJLEVBQUUsZUFBZSxDQUFDO2FBQ3pDLE1BQU0sQ0FBQyxJQUFJLENBQUMsRUFBRSxDQUFDLElBQUksQ0FBQyxTQUFTLElBQUksSUFBSSxDQUFDLFNBQVMsQ0FBQyxPQUFPLENBQUM7YUFDeEQsT0FBTyxDQUFDLElBQUksQ0FBQyxFQUFFLENBQUMsSUFBSSxDQUFDLFNBQVMsQ0FBQyxPQUFPLENBQUMsT0FBTyxDQUFDLElBQUksQ0FBQyxFQUFFLENBQUMsSUFBSSxDQUFDLGFBQWEsQ0FBQyxHQUFHLENBQUMsSUFBSSxFQUFFLElBQUksQ0FBQyxDQUFDLENBQUMsQ0FBQztJQUNqRyxDQUFDO0lBRVEsU0FBUyxDQUFDLElBQWE7UUFDOUIsSUFBSSxFQUFFLENBQUMsa0JBQWtCLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQztZQUNoQyxJQUFJLENBQUMsc0JBQXNCLENBQUMsSUFBSSxDQUFDLENBQUM7UUFDcEMsQ0FBQztJQUNILENBQUM7SUFFTyxzQkFBc0IsQ0FBQyxJQUF5QjtRQUN0RCxNQUFNLFNBQVMsR0FBRyxJQUFBLCtCQUFrQixFQUFDLElBQUksQ0FBQyxDQUFDO1FBQzNDLE1BQU0sU0FBUyxHQUFHLElBQUksQ0FBQyxJQUFJLENBQUMsQ0FBQyxDQUFDLElBQUksQ0FBQyxJQUFJLENBQUMsSUFBSSxDQUFDLENBQUMsQ0FBQyxnQkFBZ0IsQ0FBQztRQUVoRSxJQUFJLENBQUMsU0FBUyxFQUFFLENBQUM7WUFDZixPQUFPO1FBQ1QsQ0FBQztRQUVELFNBQVMsQ0FBQyxPQUFPLENBQUMsUUFBUSxDQUFDLEVBQUU7WUFDM0IsTUFBTSxJQUFJLEdBQUcsSUFBSSxDQUFDLGFBQWEsQ0FBQyxHQUFHLENBQUMsUUFBUSxDQUFDLENBQUM7WUFFOUMsSUFBSSxJQUFJLEVBQUUsQ0FBQztnQkFDVCxJQUFJLENBQUMsbUJBQW1CLENBQ3RCLElBQUksRUFDSixnQkFBZ0IsU0FBUyx3QkFBd0I7b0JBQy9DLElBQUksUUFBUSw4Q0FBOEM7b0JBQzFELElBQUksSUFBSSxDQUFDLE9BQU8scUJBQXFCLElBQUksQ0FBQyxXQUFXLEtBQUs7b0JBQzFELDRDQUE0QyxDQUMvQyxDQUFDO1lBQ0osQ0FBQztRQUNILENBQUMsQ0FBQyxDQUFDO0lBQ0wsQ0FBQztDQUNGO0FBNUNELDhEQTRDQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5pbXBvcnQgKiBhcyB0cyBmcm9tICd0eXBlc2NyaXB0JztcbmltcG9ydCB7TWlncmF0aW9ufSBmcm9tICcuLi8uLi91cGRhdGUtdG9vbC9taWdyYXRpb24nO1xuaW1wb3J0IHtQcm9wZXJ0eU5hbWVVcGdyYWRlRGF0YX0gZnJvbSAnLi4vZGF0YS9wcm9wZXJ0eS1uYW1lcyc7XG5pbXBvcnQge2RldGVybWluZUJhc2VUeXBlc30gZnJvbSAnLi4vdHlwZXNjcmlwdC9iYXNlLXR5cGVzJztcbmltcG9ydCB7Z2V0VmVyc2lvblVwZ3JhZGVEYXRhLCBVcGdyYWRlRGF0YX0gZnJvbSAnLi4vdXBncmFkZS1kYXRhJztcblxuLyoqXG4gKiBNaWdyYXRpb24gdGhhdCBpZGVudGlmaWVzIGNsYXNzIGRlY2xhcmF0aW9ucyB0aGF0IGV4dGVuZCBDREsgb3IgTWF0ZXJpYWwgY2xhc3Nlc1xuICogd2hpY2ggaGFkIGEgcHVibGljIHByb3BlcnR5IGNoYW5nZS5cbiAqL1xuZXhwb3J0IGNsYXNzIENsYXNzSW5oZXJpdGFuY2VNaWdyYXRpb24gZXh0ZW5kcyBNaWdyYXRpb248VXBncmFkZURhdGE+IHtcbiAgLyoqXG4gICAqIE1hcCBvZiBjbGFzc2VzIHRoYXQgaGF2ZSBiZWVuIHVwZGF0ZWQuIEVhY2ggY2xhc3MgbmFtZSBtYXBzIHRvIHRoZSBhY2NvcmRpbmcgcHJvcGVydHlcbiAgICogY2hhbmdlIGRhdGEuXG4gICAqL1xuICBwcm9wZXJ0eU5hbWVzID0gbmV3IE1hcDxzdHJpbmcsIFByb3BlcnR5TmFtZVVwZ3JhZGVEYXRhPigpO1xuXG4gIC8vIE9ubHkgZW5hYmxlIHRoZSBtaWdyYXRpb24gcnVsZSBpZiB0aGVyZSBpcyB1cGdyYWRlIGRhdGEuXG4gIGVuYWJsZWQgPSB0aGlzLnByb3BlcnR5TmFtZXMuc2l6ZSAhPT0gMDtcblxuICBvdmVycmlkZSBpbml0KCk6IHZvaWQge1xuICAgIGdldFZlcnNpb25VcGdyYWRlRGF0YSh0aGlzLCAncHJvcGVydHlOYW1lcycpXG4gICAgICAuZmlsdGVyKGRhdGEgPT4gZGF0YS5saW1pdGVkVG8gJiYgZGF0YS5saW1pdGVkVG8uY2xhc3NlcylcbiAgICAgIC5mb3JFYWNoKGRhdGEgPT4gZGF0YS5saW1pdGVkVG8uY2xhc3Nlcy5mb3JFYWNoKG5hbWUgPT4gdGhpcy5wcm9wZXJ0eU5hbWVzLnNldChuYW1lLCBkYXRhKSkpO1xuICB9XG5cbiAgb3ZlcnJpZGUgdmlzaXROb2RlKG5vZGU6IHRzLk5vZGUpOiB2b2lkIHtcbiAgICBpZiAodHMuaXNDbGFzc0RlY2xhcmF0aW9uKG5vZGUpKSB7XG4gICAgICB0aGlzLl92aXNpdENsYXNzRGVjbGFyYXRpb24obm9kZSk7XG4gICAgfVxuICB9XG5cbiAgcHJpdmF0ZSBfdmlzaXRDbGFzc0RlY2xhcmF0aW9uKG5vZGU6IHRzLkNsYXNzRGVjbGFyYXRpb24pIHtcbiAgICBjb25zdCBiYXNlVHlwZXMgPSBkZXRlcm1pbmVCYXNlVHlwZXMobm9kZSk7XG4gICAgY29uc3QgY2xhc3NOYW1lID0gbm9kZS5uYW1lID8gbm9kZS5uYW1lLnRleHQgOiAne3Vua25vd24tbmFtZX0nO1xuXG4gICAgaWYgKCFiYXNlVHlwZXMpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBiYXNlVHlwZXMuZm9yRWFjaCh0eXBlTmFtZSA9PiB7XG4gICAgICBjb25zdCBkYXRhID0gdGhpcy5wcm9wZXJ0eU5hbWVzLmdldCh0eXBlTmFtZSk7XG5cbiAgICAgIGlmIChkYXRhKSB7XG4gICAgICAgIHRoaXMuY3JlYXRlRmFpbHVyZUF0Tm9kZShcbiAgICAgICAgICBub2RlLFxuICAgICAgICAgIGBGb3VuZCBjbGFzcyBcIiR7Y2xhc3NOYW1lfVwiIHdoaWNoIGV4dGVuZHMgY2xhc3MgYCArXG4gICAgICAgICAgICBgXCIke3R5cGVOYW1lfVwiLiBQbGVhc2Ugbm90ZSB0aGF0IHRoZSBiYXNlIGNsYXNzIHByb3BlcnR5IGAgK1xuICAgICAgICAgICAgYFwiJHtkYXRhLnJlcGxhY2V9XCIgaGFzIGNoYW5nZWQgdG8gXCIke2RhdGEucmVwbGFjZVdpdGh9XCIuIGAgK1xuICAgICAgICAgICAgYFlvdSBtYXkgbmVlZCB0byB1cGRhdGUgeW91ciBjbGFzcyBhcyB3ZWxsLmAsXG4gICAgICAgICk7XG4gICAgICB9XG4gICAgfSk7XG4gIH1cbn1cbiJdfQ==