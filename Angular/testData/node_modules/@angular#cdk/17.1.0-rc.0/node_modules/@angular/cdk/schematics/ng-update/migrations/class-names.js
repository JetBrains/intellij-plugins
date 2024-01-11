"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.ClassNamesMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const imports_1 = require("../typescript/imports");
const module_specifiers_1 = require("../typescript/module-specifiers");
const upgrade_data_1 = require("../upgrade-data");
/**
 * Migration that walks through every identifier that is part of Angular Material or thr CDK
 * and replaces the outdated name with the new one if specified in the upgrade data.
 */
// TODO: rework this rule to identify symbols using the import identifier resolver. This
// makes it more robust, less AST convoluted and is more TypeScript AST idiomatic. COMP-300.
class ClassNamesMigration extends migration_1.Migration {
    /** Change data that upgrades to the specified target version. */
    data = (0, upgrade_data_1.getVersionUpgradeData)(this, 'classNames');
    /**
     * List of identifier names that have been imported from `@angular/material` or `@angular/cdk`
     * in the current source file and therefore can be considered trusted.
     */
    trustedIdentifiers = new Set();
    /** List of namespaces that have been imported from `@angular/material` or `@angular/cdk`. */
    trustedNamespaces = new Set();
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (ts.isIdentifier(node)) {
            this._visitIdentifier(node);
        }
    }
    /** Method that is called for every identifier inside of the specified project. */
    _visitIdentifier(identifier) {
        // For identifiers that aren't listed in the className data, the whole check can be
        // skipped safely.
        if (!this.data.some(data => data.replace === identifier.text)) {
            return;
        }
        // For namespace imports that are referring to Angular Material or the CDK, we store the
        // namespace name in order to be able to safely find identifiers that don't belong to the
        // developer's application.
        if ((0, imports_1.isNamespaceImportNode)(identifier) && (0, module_specifiers_1.isMaterialImportDeclaration)(identifier)) {
            this.trustedNamespaces.add(identifier.text);
            return this._createFailureWithReplacement(identifier);
        }
        // For export declarations that are referring to Angular Material or the CDK, the identifier
        // can be immediately updated to the new name.
        if ((0, imports_1.isExportSpecifierNode)(identifier) && (0, module_specifiers_1.isMaterialExportDeclaration)(identifier)) {
            return this._createFailureWithReplacement(identifier);
        }
        // For import declarations that are referring to Angular Material or the CDK, the name of
        // the import identifiers. This allows us to identify identifiers that belong to Material and
        // the CDK, and we won't accidentally touch a developer's identifier.
        if ((0, imports_1.isImportSpecifierNode)(identifier) && (0, module_specifiers_1.isMaterialImportDeclaration)(identifier)) {
            this.trustedIdentifiers.add(identifier.text);
            return this._createFailureWithReplacement(identifier);
        }
        // In case the identifier is part of a property access expression, we need to verify that the
        // property access originates from a namespace that has been imported from Material or the CDK.
        if (ts.isPropertyAccessExpression(identifier.parent)) {
            const expression = identifier.parent.expression;
            if (ts.isIdentifier(expression) && this.trustedNamespaces.has(expression.text)) {
                return this._createFailureWithReplacement(identifier);
            }
        }
        else if (this.trustedIdentifiers.has(identifier.text)) {
            return this._createFailureWithReplacement(identifier);
        }
    }
    /** Creates a failure and replacement for the specified identifier. */
    _createFailureWithReplacement(identifier) {
        const classData = this.data.find(data => data.replace === identifier.text);
        const filePath = this.fileSystem.resolve(identifier.getSourceFile().fileName);
        this.fileSystem
            .edit(filePath)
            .remove(identifier.getStart(), identifier.getWidth())
            .insertRight(identifier.getStart(), classData.replaceWith);
    }
}
exports.ClassNamesMigration = ClassNamesMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiY2xhc3MtbmFtZXMuanMiLCJzb3VyY2VSb290IjoiIiwic291cmNlcyI6WyIuLi8uLi8uLi8uLi8uLi8uLi8uLi8uLi9zcmMvY2RrL3NjaGVtYXRpY3MvbmctdXBkYXRlL21pZ3JhdGlvbnMvY2xhc3MtbmFtZXMudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IjtBQUFBOzs7Ozs7R0FNRzs7O0FBRUgsaUNBQWlDO0FBQ2pDLDJEQUFzRDtBQUd0RCxtREFJK0I7QUFDL0IsdUVBR3lDO0FBQ3pDLGtEQUFtRTtBQUVuRTs7O0dBR0c7QUFDSCx3RkFBd0Y7QUFDeEYsNEZBQTRGO0FBQzVGLE1BQWEsbUJBQW9CLFNBQVEscUJBQXNCO0lBQzdELGlFQUFpRTtJQUNqRSxJQUFJLEdBQTJCLElBQUEsb0NBQXFCLEVBQUMsSUFBSSxFQUFFLFlBQVksQ0FBQyxDQUFDO0lBRXpFOzs7T0FHRztJQUNILGtCQUFrQixHQUFnQixJQUFJLEdBQUcsRUFBRSxDQUFDO0lBRTVDLDZGQUE2RjtJQUM3RixpQkFBaUIsR0FBZ0IsSUFBSSxHQUFHLEVBQUUsQ0FBQztJQUUzQywyREFBMkQ7SUFDM0QsT0FBTyxHQUFHLElBQUksQ0FBQyxJQUFJLENBQUMsTUFBTSxLQUFLLENBQUMsQ0FBQztJQUV4QixTQUFTLENBQUMsSUFBYTtRQUM5QixJQUFJLEVBQUUsQ0FBQyxZQUFZLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQztZQUMxQixJQUFJLENBQUMsZ0JBQWdCLENBQUMsSUFBSSxDQUFDLENBQUM7UUFDOUIsQ0FBQztJQUNILENBQUM7SUFFRCxrRkFBa0Y7SUFDMUUsZ0JBQWdCLENBQUMsVUFBeUI7UUFDaEQsbUZBQW1GO1FBQ25GLGtCQUFrQjtRQUNsQixJQUFJLENBQUMsSUFBSSxDQUFDLElBQUksQ0FBQyxJQUFJLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQyxJQUFJLENBQUMsT0FBTyxLQUFLLFVBQVUsQ0FBQyxJQUFJLENBQUMsRUFBRSxDQUFDO1lBQzlELE9BQU87UUFDVCxDQUFDO1FBRUQsd0ZBQXdGO1FBQ3hGLHlGQUF5RjtRQUN6RiwyQkFBMkI7UUFDM0IsSUFBSSxJQUFBLCtCQUFxQixFQUFDLFVBQVUsQ0FBQyxJQUFJLElBQUEsK0NBQTJCLEVBQUMsVUFBVSxDQUFDLEVBQUUsQ0FBQztZQUNqRixJQUFJLENBQUMsaUJBQWlCLENBQUMsR0FBRyxDQUFDLFVBQVUsQ0FBQyxJQUFJLENBQUMsQ0FBQztZQUU1QyxPQUFPLElBQUksQ0FBQyw2QkFBNkIsQ0FBQyxVQUFVLENBQUMsQ0FBQztRQUN4RCxDQUFDO1FBRUQsNEZBQTRGO1FBQzVGLDhDQUE4QztRQUM5QyxJQUFJLElBQUEsK0JBQXFCLEVBQUMsVUFBVSxDQUFDLElBQUksSUFBQSwrQ0FBMkIsRUFBQyxVQUFVLENBQUMsRUFBRSxDQUFDO1lBQ2pGLE9BQU8sSUFBSSxDQUFDLDZCQUE2QixDQUFDLFVBQVUsQ0FBQyxDQUFDO1FBQ3hELENBQUM7UUFFRCx5RkFBeUY7UUFDekYsNkZBQTZGO1FBQzdGLHFFQUFxRTtRQUNyRSxJQUFJLElBQUEsK0JBQXFCLEVBQUMsVUFBVSxDQUFDLElBQUksSUFBQSwrQ0FBMkIsRUFBQyxVQUFVLENBQUMsRUFBRSxDQUFDO1lBQ2pGLElBQUksQ0FBQyxrQkFBa0IsQ0FBQyxHQUFHLENBQUMsVUFBVSxDQUFDLElBQUksQ0FBQyxDQUFDO1lBRTdDLE9BQU8sSUFBSSxDQUFDLDZCQUE2QixDQUFDLFVBQVUsQ0FBQyxDQUFDO1FBQ3hELENBQUM7UUFFRCw2RkFBNkY7UUFDN0YsK0ZBQStGO1FBQy9GLElBQUksRUFBRSxDQUFDLDBCQUEwQixDQUFDLFVBQVUsQ0FBQyxNQUFNLENBQUMsRUFBRSxDQUFDO1lBQ3JELE1BQU0sVUFBVSxHQUFHLFVBQVUsQ0FBQyxNQUFNLENBQUMsVUFBVSxDQUFDO1lBRWhELElBQUksRUFBRSxDQUFDLFlBQVksQ0FBQyxVQUFVLENBQUMsSUFBSSxJQUFJLENBQUMsaUJBQWlCLENBQUMsR0FBRyxDQUFDLFVBQVUsQ0FBQyxJQUFJLENBQUMsRUFBRSxDQUFDO2dCQUMvRSxPQUFPLElBQUksQ0FBQyw2QkFBNkIsQ0FBQyxVQUFVLENBQUMsQ0FBQztZQUN4RCxDQUFDO1FBQ0gsQ0FBQzthQUFNLElBQUksSUFBSSxDQUFDLGtCQUFrQixDQUFDLEdBQUcsQ0FBQyxVQUFVLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQztZQUN4RCxPQUFPLElBQUksQ0FBQyw2QkFBNkIsQ0FBQyxVQUFVLENBQUMsQ0FBQztRQUN4RCxDQUFDO0lBQ0gsQ0FBQztJQUVELHNFQUFzRTtJQUM5RCw2QkFBNkIsQ0FBQyxVQUF5QjtRQUM3RCxNQUFNLFNBQVMsR0FBRyxJQUFJLENBQUMsSUFBSSxDQUFDLElBQUksQ0FBQyxJQUFJLENBQUMsRUFBRSxDQUFDLElBQUksQ0FBQyxPQUFPLEtBQUssVUFBVSxDQUFDLElBQUksQ0FBRSxDQUFDO1FBQzVFLE1BQU0sUUFBUSxHQUFHLElBQUksQ0FBQyxVQUFVLENBQUMsT0FBTyxDQUFDLFVBQVUsQ0FBQyxhQUFhLEVBQUUsQ0FBQyxRQUFRLENBQUMsQ0FBQztRQUU5RSxJQUFJLENBQUMsVUFBVTthQUNaLElBQUksQ0FBQyxRQUFRLENBQUM7YUFDZCxNQUFNLENBQUMsVUFBVSxDQUFDLFFBQVEsRUFBRSxFQUFFLFVBQVUsQ0FBQyxRQUFRLEVBQUUsQ0FBQzthQUNwRCxXQUFXLENBQUMsVUFBVSxDQUFDLFFBQVEsRUFBRSxFQUFFLFNBQVMsQ0FBQyxXQUFXLENBQUMsQ0FBQztJQUMvRCxDQUFDO0NBQ0Y7QUE3RUQsa0RBNkVDIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5cbmltcG9ydCAqIGFzIHRzIGZyb20gJ3R5cGVzY3JpcHQnO1xuaW1wb3J0IHtNaWdyYXRpb259IGZyb20gJy4uLy4uL3VwZGF0ZS10b29sL21pZ3JhdGlvbic7XG5cbmltcG9ydCB7Q2xhc3NOYW1lVXBncmFkZURhdGF9IGZyb20gJy4uL2RhdGEnO1xuaW1wb3J0IHtcbiAgaXNFeHBvcnRTcGVjaWZpZXJOb2RlLFxuICBpc0ltcG9ydFNwZWNpZmllck5vZGUsXG4gIGlzTmFtZXNwYWNlSW1wb3J0Tm9kZSxcbn0gZnJvbSAnLi4vdHlwZXNjcmlwdC9pbXBvcnRzJztcbmltcG9ydCB7XG4gIGlzTWF0ZXJpYWxFeHBvcnREZWNsYXJhdGlvbixcbiAgaXNNYXRlcmlhbEltcG9ydERlY2xhcmF0aW9uLFxufSBmcm9tICcuLi90eXBlc2NyaXB0L21vZHVsZS1zcGVjaWZpZXJzJztcbmltcG9ydCB7Z2V0VmVyc2lvblVwZ3JhZGVEYXRhLCBVcGdyYWRlRGF0YX0gZnJvbSAnLi4vdXBncmFkZS1kYXRhJztcblxuLyoqXG4gKiBNaWdyYXRpb24gdGhhdCB3YWxrcyB0aHJvdWdoIGV2ZXJ5IGlkZW50aWZpZXIgdGhhdCBpcyBwYXJ0IG9mIEFuZ3VsYXIgTWF0ZXJpYWwgb3IgdGhyIENES1xuICogYW5kIHJlcGxhY2VzIHRoZSBvdXRkYXRlZCBuYW1lIHdpdGggdGhlIG5ldyBvbmUgaWYgc3BlY2lmaWVkIGluIHRoZSB1cGdyYWRlIGRhdGEuXG4gKi9cbi8vIFRPRE86IHJld29yayB0aGlzIHJ1bGUgdG8gaWRlbnRpZnkgc3ltYm9scyB1c2luZyB0aGUgaW1wb3J0IGlkZW50aWZpZXIgcmVzb2x2ZXIuIFRoaXNcbi8vIG1ha2VzIGl0IG1vcmUgcm9idXN0LCBsZXNzIEFTVCBjb252b2x1dGVkIGFuZCBpcyBtb3JlIFR5cGVTY3JpcHQgQVNUIGlkaW9tYXRpYy4gQ09NUC0zMDAuXG5leHBvcnQgY2xhc3MgQ2xhc3NOYW1lc01pZ3JhdGlvbiBleHRlbmRzIE1pZ3JhdGlvbjxVcGdyYWRlRGF0YT4ge1xuICAvKiogQ2hhbmdlIGRhdGEgdGhhdCB1cGdyYWRlcyB0byB0aGUgc3BlY2lmaWVkIHRhcmdldCB2ZXJzaW9uLiAqL1xuICBkYXRhOiBDbGFzc05hbWVVcGdyYWRlRGF0YVtdID0gZ2V0VmVyc2lvblVwZ3JhZGVEYXRhKHRoaXMsICdjbGFzc05hbWVzJyk7XG5cbiAgLyoqXG4gICAqIExpc3Qgb2YgaWRlbnRpZmllciBuYW1lcyB0aGF0IGhhdmUgYmVlbiBpbXBvcnRlZCBmcm9tIGBAYW5ndWxhci9tYXRlcmlhbGAgb3IgYEBhbmd1bGFyL2Nka2BcbiAgICogaW4gdGhlIGN1cnJlbnQgc291cmNlIGZpbGUgYW5kIHRoZXJlZm9yZSBjYW4gYmUgY29uc2lkZXJlZCB0cnVzdGVkLlxuICAgKi9cbiAgdHJ1c3RlZElkZW50aWZpZXJzOiBTZXQ8c3RyaW5nPiA9IG5ldyBTZXQoKTtcblxuICAvKiogTGlzdCBvZiBuYW1lc3BhY2VzIHRoYXQgaGF2ZSBiZWVuIGltcG9ydGVkIGZyb20gYEBhbmd1bGFyL21hdGVyaWFsYCBvciBgQGFuZ3VsYXIvY2RrYC4gKi9cbiAgdHJ1c3RlZE5hbWVzcGFjZXM6IFNldDxzdHJpbmc+ID0gbmV3IFNldCgpO1xuXG4gIC8vIE9ubHkgZW5hYmxlIHRoZSBtaWdyYXRpb24gcnVsZSBpZiB0aGVyZSBpcyB1cGdyYWRlIGRhdGEuXG4gIGVuYWJsZWQgPSB0aGlzLmRhdGEubGVuZ3RoICE9PSAwO1xuXG4gIG92ZXJyaWRlIHZpc2l0Tm9kZShub2RlOiB0cy5Ob2RlKTogdm9pZCB7XG4gICAgaWYgKHRzLmlzSWRlbnRpZmllcihub2RlKSkge1xuICAgICAgdGhpcy5fdmlzaXRJZGVudGlmaWVyKG5vZGUpO1xuICAgIH1cbiAgfVxuXG4gIC8qKiBNZXRob2QgdGhhdCBpcyBjYWxsZWQgZm9yIGV2ZXJ5IGlkZW50aWZpZXIgaW5zaWRlIG9mIHRoZSBzcGVjaWZpZWQgcHJvamVjdC4gKi9cbiAgcHJpdmF0ZSBfdmlzaXRJZGVudGlmaWVyKGlkZW50aWZpZXI6IHRzLklkZW50aWZpZXIpIHtcbiAgICAvLyBGb3IgaWRlbnRpZmllcnMgdGhhdCBhcmVuJ3QgbGlzdGVkIGluIHRoZSBjbGFzc05hbWUgZGF0YSwgdGhlIHdob2xlIGNoZWNrIGNhbiBiZVxuICAgIC8vIHNraXBwZWQgc2FmZWx5LlxuICAgIGlmICghdGhpcy5kYXRhLnNvbWUoZGF0YSA9PiBkYXRhLnJlcGxhY2UgPT09IGlkZW50aWZpZXIudGV4dCkpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICAvLyBGb3IgbmFtZXNwYWNlIGltcG9ydHMgdGhhdCBhcmUgcmVmZXJyaW5nIHRvIEFuZ3VsYXIgTWF0ZXJpYWwgb3IgdGhlIENESywgd2Ugc3RvcmUgdGhlXG4gICAgLy8gbmFtZXNwYWNlIG5hbWUgaW4gb3JkZXIgdG8gYmUgYWJsZSB0byBzYWZlbHkgZmluZCBpZGVudGlmaWVycyB0aGF0IGRvbid0IGJlbG9uZyB0byB0aGVcbiAgICAvLyBkZXZlbG9wZXIncyBhcHBsaWNhdGlvbi5cbiAgICBpZiAoaXNOYW1lc3BhY2VJbXBvcnROb2RlKGlkZW50aWZpZXIpICYmIGlzTWF0ZXJpYWxJbXBvcnREZWNsYXJhdGlvbihpZGVudGlmaWVyKSkge1xuICAgICAgdGhpcy50cnVzdGVkTmFtZXNwYWNlcy5hZGQoaWRlbnRpZmllci50ZXh0KTtcblxuICAgICAgcmV0dXJuIHRoaXMuX2NyZWF0ZUZhaWx1cmVXaXRoUmVwbGFjZW1lbnQoaWRlbnRpZmllcik7XG4gICAgfVxuXG4gICAgLy8gRm9yIGV4cG9ydCBkZWNsYXJhdGlvbnMgdGhhdCBhcmUgcmVmZXJyaW5nIHRvIEFuZ3VsYXIgTWF0ZXJpYWwgb3IgdGhlIENESywgdGhlIGlkZW50aWZpZXJcbiAgICAvLyBjYW4gYmUgaW1tZWRpYXRlbHkgdXBkYXRlZCB0byB0aGUgbmV3IG5hbWUuXG4gICAgaWYgKGlzRXhwb3J0U3BlY2lmaWVyTm9kZShpZGVudGlmaWVyKSAmJiBpc01hdGVyaWFsRXhwb3J0RGVjbGFyYXRpb24oaWRlbnRpZmllcikpIHtcbiAgICAgIHJldHVybiB0aGlzLl9jcmVhdGVGYWlsdXJlV2l0aFJlcGxhY2VtZW50KGlkZW50aWZpZXIpO1xuICAgIH1cblxuICAgIC8vIEZvciBpbXBvcnQgZGVjbGFyYXRpb25zIHRoYXQgYXJlIHJlZmVycmluZyB0byBBbmd1bGFyIE1hdGVyaWFsIG9yIHRoZSBDREssIHRoZSBuYW1lIG9mXG4gICAgLy8gdGhlIGltcG9ydCBpZGVudGlmaWVycy4gVGhpcyBhbGxvd3MgdXMgdG8gaWRlbnRpZnkgaWRlbnRpZmllcnMgdGhhdCBiZWxvbmcgdG8gTWF0ZXJpYWwgYW5kXG4gICAgLy8gdGhlIENESywgYW5kIHdlIHdvbid0IGFjY2lkZW50YWxseSB0b3VjaCBhIGRldmVsb3BlcidzIGlkZW50aWZpZXIuXG4gICAgaWYgKGlzSW1wb3J0U3BlY2lmaWVyTm9kZShpZGVudGlmaWVyKSAmJiBpc01hdGVyaWFsSW1wb3J0RGVjbGFyYXRpb24oaWRlbnRpZmllcikpIHtcbiAgICAgIHRoaXMudHJ1c3RlZElkZW50aWZpZXJzLmFkZChpZGVudGlmaWVyLnRleHQpO1xuXG4gICAgICByZXR1cm4gdGhpcy5fY3JlYXRlRmFpbHVyZVdpdGhSZXBsYWNlbWVudChpZGVudGlmaWVyKTtcbiAgICB9XG5cbiAgICAvLyBJbiBjYXNlIHRoZSBpZGVudGlmaWVyIGlzIHBhcnQgb2YgYSBwcm9wZXJ0eSBhY2Nlc3MgZXhwcmVzc2lvbiwgd2UgbmVlZCB0byB2ZXJpZnkgdGhhdCB0aGVcbiAgICAvLyBwcm9wZXJ0eSBhY2Nlc3Mgb3JpZ2luYXRlcyBmcm9tIGEgbmFtZXNwYWNlIHRoYXQgaGFzIGJlZW4gaW1wb3J0ZWQgZnJvbSBNYXRlcmlhbCBvciB0aGUgQ0RLLlxuICAgIGlmICh0cy5pc1Byb3BlcnR5QWNjZXNzRXhwcmVzc2lvbihpZGVudGlmaWVyLnBhcmVudCkpIHtcbiAgICAgIGNvbnN0IGV4cHJlc3Npb24gPSBpZGVudGlmaWVyLnBhcmVudC5leHByZXNzaW9uO1xuXG4gICAgICBpZiAodHMuaXNJZGVudGlmaWVyKGV4cHJlc3Npb24pICYmIHRoaXMudHJ1c3RlZE5hbWVzcGFjZXMuaGFzKGV4cHJlc3Npb24udGV4dCkpIHtcbiAgICAgICAgcmV0dXJuIHRoaXMuX2NyZWF0ZUZhaWx1cmVXaXRoUmVwbGFjZW1lbnQoaWRlbnRpZmllcik7XG4gICAgICB9XG4gICAgfSBlbHNlIGlmICh0aGlzLnRydXN0ZWRJZGVudGlmaWVycy5oYXMoaWRlbnRpZmllci50ZXh0KSkge1xuICAgICAgcmV0dXJuIHRoaXMuX2NyZWF0ZUZhaWx1cmVXaXRoUmVwbGFjZW1lbnQoaWRlbnRpZmllcik7XG4gICAgfVxuICB9XG5cbiAgLyoqIENyZWF0ZXMgYSBmYWlsdXJlIGFuZCByZXBsYWNlbWVudCBmb3IgdGhlIHNwZWNpZmllZCBpZGVudGlmaWVyLiAqL1xuICBwcml2YXRlIF9jcmVhdGVGYWlsdXJlV2l0aFJlcGxhY2VtZW50KGlkZW50aWZpZXI6IHRzLklkZW50aWZpZXIpIHtcbiAgICBjb25zdCBjbGFzc0RhdGEgPSB0aGlzLmRhdGEuZmluZChkYXRhID0+IGRhdGEucmVwbGFjZSA9PT0gaWRlbnRpZmllci50ZXh0KSE7XG4gICAgY29uc3QgZmlsZVBhdGggPSB0aGlzLmZpbGVTeXN0ZW0ucmVzb2x2ZShpZGVudGlmaWVyLmdldFNvdXJjZUZpbGUoKS5maWxlTmFtZSk7XG5cbiAgICB0aGlzLmZpbGVTeXN0ZW1cbiAgICAgIC5lZGl0KGZpbGVQYXRoKVxuICAgICAgLnJlbW92ZShpZGVudGlmaWVyLmdldFN0YXJ0KCksIGlkZW50aWZpZXIuZ2V0V2lkdGgoKSlcbiAgICAgIC5pbnNlcnRSaWdodChpZGVudGlmaWVyLmdldFN0YXJ0KCksIGNsYXNzRGF0YS5yZXBsYWNlV2l0aCk7XG4gIH1cbn1cbiJdfQ==