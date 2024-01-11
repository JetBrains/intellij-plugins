"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.ConstructorSignatureMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const version_changes_1 = require("../../update-tool/version-changes");
/**
 * List of diagnostic codes that refer to pre-emit diagnostics which indicate invalid
 * new expression or super call signatures. See the list of diagnostics here:
 *
 * https://github.com/Microsoft/TypeScript/blob/master/src/compiler/diagnosticMessages.json
 */
const signatureErrorDiagnostics = [
    // Type not assignable error diagnostic.
    2345,
    // Constructor argument length invalid diagnostics
    2554, 2555, 2556, 2557,
];
/**
 * Migration that visits every TypeScript new expression or super call and checks if
 * the parameter type signature is invalid and needs to be updated manually.
 */
class ConstructorSignatureMigration extends migration_1.Migration {
    // Note that the data for this rule is not distinguished based on the target version because
    // we don't keep track of the new signature and don't want to update incrementally.
    // See: https://github.com/angular/components/pull/12970#issuecomment-418337566
    data = (0, version_changes_1.getAllChanges)(this.upgradeData.constructorChecks);
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (ts.isSourceFile(node)) {
            this._visitSourceFile(node);
        }
    }
    /**
     * Method that will be called for each source file of the upgrade project. In order to
     * properly determine invalid constructor signatures, we take advantage of the pre-emit
     * diagnostics from TypeScript.
     *
     * By using the diagnostics, the migration can handle type assignability. Not using
     * diagnostics would mean that we need to use simple type equality checking which is
     * too strict. See related issue: https://github.com/Microsoft/TypeScript/issues/9879
     */
    _visitSourceFile(sourceFile) {
        // List of classes of which the constructor signature has changed.
        const diagnostics = ts
            .getPreEmitDiagnostics(this.program, sourceFile)
            .filter(diagnostic => signatureErrorDiagnostics.includes(diagnostic.code))
            .filter(diagnostic => diagnostic.start !== undefined);
        for (const diagnostic of diagnostics) {
            const node = findConstructorNode(diagnostic, sourceFile);
            if (!node) {
                continue;
            }
            const classType = this.typeChecker.getTypeAtLocation(node.expression);
            const className = classType.symbol && classType.symbol.name;
            const isNewExpression = ts.isNewExpression(node);
            // Determine the class names of the actual construct signatures because we cannot assume that
            // the diagnostic refers to a constructor of the actual expression. In case the constructor
            // is inherited, we need to detect that the owner-class of the constructor is added to the
            // constructor checks upgrade data. e.g. `class CustomCalendar extends MatCalendar {}`.
            const signatureClassNames = classType
                .getConstructSignatures()
                .map(signature => getClassDeclarationOfSignature(signature))
                .map(declaration => (declaration && declaration.name ? declaration.name.text : null))
                .filter(Boolean);
            // Besides checking the signature class names, we need to check the actual class name because
            // there can be classes without an explicit constructor.
            if (!this.data.includes(className) &&
                !signatureClassNames.some(name => this.data.includes(name))) {
                continue;
            }
            const classSignatures = classType
                .getConstructSignatures()
                .map(signature => getParameterTypesFromSignature(signature, this.typeChecker));
            const expressionName = isNewExpression ? `new ${className}` : 'super';
            const signatures = classSignatures
                .map(signature => signature.map(t => (t === null ? 'any' : this.typeChecker.typeToString(t))))
                .map(signature => `${expressionName}(${signature.join(', ')})`)
                .join(' or ');
            this.createFailureAtNode(node, `Found "${className}" constructed with ` +
                `an invalid signature. Please manually update the ${expressionName} expression to ` +
                `match the new signature${classSignatures.length > 1 ? 's' : ''}: ${signatures}`);
        }
    }
}
exports.ConstructorSignatureMigration = ConstructorSignatureMigration;
/** Resolves the type for each parameter in the specified signature. */
function getParameterTypesFromSignature(signature, typeChecker) {
    return signature
        .getParameters()
        .map(param => param.declarations ? typeChecker.getTypeAtLocation(param.declarations[0]) : null);
}
/**
 * Walks through each node of a source file in order to find a new-expression node or super-call
 * expression node that is captured by the specified diagnostic.
 */
function findConstructorNode(diagnostic, sourceFile) {
    let resolvedNode = null;
    const _visitNode = (node) => {
        // Check whether the current node contains the diagnostic. If the node contains the diagnostic,
        // walk deeper in order to find all constructor expression nodes.
        if (node.getStart() <= diagnostic.start && node.getEnd() >= diagnostic.start) {
            if (ts.isNewExpression(node) ||
                (ts.isCallExpression(node) && node.expression.kind === ts.SyntaxKind.SuperKeyword)) {
                resolvedNode = node;
            }
            ts.forEachChild(node, _visitNode);
        }
    };
    ts.forEachChild(sourceFile, _visitNode);
    return resolvedNode;
}
/** Determines the class declaration of the specified construct signature. */
function getClassDeclarationOfSignature(signature) {
    let node = signature.getDeclaration();
    // Handle signatures which don't have an actual declaration. This happens if a class
    // does not have an explicitly written constructor.
    if (!node) {
        return null;
    }
    while (!ts.isSourceFile((node = node.parent))) {
        if (ts.isClassDeclaration(node)) {
            return node;
        }
    }
    return null;
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiY29uc3RydWN0b3Itc2lnbmF0dXJlLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vLi4vLi4vLi4vLi4vLi4vc3JjL2Nkay9zY2hlbWF0aWNzL25nLXVwZGF0ZS9taWdyYXRpb25zL2NvbnN0cnVjdG9yLXNpZ25hdHVyZS50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiO0FBQUE7Ozs7OztHQU1HOzs7QUFFSCxpQ0FBaUM7QUFDakMsMkRBQXNEO0FBQ3RELHVFQUFnRTtBQUdoRTs7Ozs7R0FLRztBQUNILE1BQU0seUJBQXlCLEdBQUc7SUFDaEMsd0NBQXdDO0lBQ3hDLElBQUk7SUFDSixrREFBa0Q7SUFDbEQsSUFBSSxFQUFFLElBQUksRUFBRSxJQUFJLEVBQUUsSUFBSTtDQUN2QixDQUFDO0FBRUY7OztHQUdHO0FBQ0gsTUFBYSw2QkFBOEIsU0FBUSxxQkFBc0I7SUFDdkUsNEZBQTRGO0lBQzVGLG1GQUFtRjtJQUNuRiwrRUFBK0U7SUFDL0UsSUFBSSxHQUFHLElBQUEsK0JBQWEsRUFBQyxJQUFJLENBQUMsV0FBVyxDQUFDLGlCQUFpQixDQUFDLENBQUM7SUFFekQsMkRBQTJEO0lBQzNELE9BQU8sR0FBRyxJQUFJLENBQUMsSUFBSSxDQUFDLE1BQU0sS0FBSyxDQUFDLENBQUM7SUFFeEIsU0FBUyxDQUFDLElBQWE7UUFDOUIsSUFBSSxFQUFFLENBQUMsWUFBWSxDQUFDLElBQUksQ0FBQyxFQUFFLENBQUM7WUFDMUIsSUFBSSxDQUFDLGdCQUFnQixDQUFDLElBQUksQ0FBQyxDQUFDO1FBQzlCLENBQUM7SUFDSCxDQUFDO0lBRUQ7Ozs7Ozs7O09BUUc7SUFDSyxnQkFBZ0IsQ0FBQyxVQUF5QjtRQUNoRCxrRUFBa0U7UUFDbEUsTUFBTSxXQUFXLEdBQUcsRUFBRTthQUNuQixxQkFBcUIsQ0FBQyxJQUFJLENBQUMsT0FBTyxFQUFFLFVBQVUsQ0FBQzthQUMvQyxNQUFNLENBQUMsVUFBVSxDQUFDLEVBQUUsQ0FBQyx5QkFBeUIsQ0FBQyxR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R0FBWSxTQUFTLENBQUMsY0FBYyxFQUFFLENBQUM7SUFDL0Msb0ZBQW9GO0lBQ3BGLG1EQUFtRDtJQUNuRCxJQUFJLENBQUMsSUFBSSxFQUFFLENBQUM7UUFDVixPQUFPLElBQUksQ0FBQztJQUNkLENBQUM7SUFDRCxPQUFPLENBQUMsRUFBRSxDQUFDLFlBQVksQ0FBQyxDQUFDLElBQUksR0FBRyxJQUFJLENBQUMsTUFBTSxDQUFDLENBQUMsRUFBRSxDQUFDO1FBQzlDLElBQUksRUFBRSxDQUFDLGtCQUFrQixDQUFDLElBQUksQ0FBQyxFQUFFLENBQUM7WUFDaEMsT0FBTyxJQUFJLENBQUM7UUFDZCxDQUFDO0lBQ0gsQ0FBQztJQUNELE9BQU8sSUFBSSxDQUFDO0FBQ2QsQ0FBQyIsInNvdXJjZXNDb250ZW50IjpbIi8qKlxuICogQGxpY2Vuc2VcbiAqIENvcHlyaWdodCBHb29nbGUgTExDIEFsbCBSaWdodHMgUmVzZXJ2ZWQuXG4gKlxuICogVXNlIG9mIHRoaXMgc291cmNlIGNvZGUgaXMgZ292ZXJuZWQgYnkgYW4gTUlULXN0eWxlIGxpY2Vuc2UgdGhhdCBjYW4gYmVcbiAqIGZvdW5kIGluIHRoZSBMSUNFTlNFIGZpbGUgYXQgaHR0cHM6Ly9hbmd1bGFyLmlvL2xpY2Vuc2VcbiAqL1xuXG5pbXBvcnQgKiBhcyB0cyBmcm9tICd0eXBlc2NyaXB0JztcbmltcG9ydCB7TWlncmF0aW9ufSBmcm9tICcuLi8uLi91cGRhdGUtdG9vbC9taWdyYXRpb24nO1xuaW1wb3J0IHtnZXRBbGxDaGFuZ2VzfSBmcm9tICcuLi8uLi91cGRhdGUtdG9vbC92ZXJzaW9uLWNoYW5nZXMnO1xuaW1wb3J0IHtVcGdyYWRlRGF0YX0gZnJvbSAnLi4vdXBncmFkZS1kYXRhJztcblxuLyoqXG4gKiBMaXN0IG9mIGRpYWdub3N0aWMgY29kZXMgdGhhdCByZWZlciB0byBwcmUtZW1pdCBkaWFnbm9zdGljcyB3aGljaCBpbmRpY2F0ZSBpbnZhbGlkXG4gKiBuZXcgZXhwcmVzc2lvbiBvciBzdXBlciBjYWxsIHNpZ25hdHVyZXMuIFNlZSB0aGUgbGlzdCBvZiBkaWFnbm9zdGljcyBoZXJlOlxuICpcbiAqIGh0dHBzOi8vZ2l0aHViLmNvbS9NaWNyb3NvZnQvVHlwZVNjcmlwdC9ibG9iL21hc3Rlci9zcmMvY29tcGlsZXIvZGlhZ25vc3RpY01lc3NhZ2VzLmpzb25cbiAqL1xuY29uc3Qgc2lnbmF0dXJlRXJyb3JEaWFnbm9zdGljcyA9IFtcbiAgLy8gVHlwZSBub3QgYXNzaWduYWJsZSBlcnJvciBkaWFnbm9zdGljLlxuICAyMzQ1LFxuICAvLyBDb25zdHJ1Y3RvciBhcmd1bWVudCBsZW5ndGggaW52YWxpZCBkaWFnbm9zdGljc1xuICAyNTU0LCAyNTU1LCAyNTU2LCAyNTU3LFxuXTtcblxuLyoqXG4gKiBNaWdyYXRpb24gdGhhdCB2aXNpdHMgZXZlcnkgVHlwZVNjcmlwdCBuZXcgZXhwcmVzc2lvbiBvciBzdXBlciBjYWxsIGFuZCBjaGVja3MgaWZcbiAqIHRoZSBwYXJhbWV0ZXIgdHlwZSBzaWduYXR1cmUgaXMgaW52YWxpZCBhbmQgbmVlZHMgdG8gYmUgdXBkYXRlZCBtYW51YWxseS5cbiAqL1xuZXhwb3J0IGNsYXNzIENvbnN0cnVjdG9yU2lnbmF0dXJlTWlncmF0aW9uIGV4dGVuZHMgTWlncmF0aW9uPFVwZ3JhZGVEYXRhPiB7XG4gIC8vIE5vdGUgdGhhdCB0aGUgZGF0YSBmb3IgdGhpcyBydWxlIGlzIG5vdCBkaXN0aW5ndWlzaGVkIGJhc2VkIG9uIHRoZSB0YXJnZXQgdmVyc2lvbiBiZWNhdXNlXG4gIC8vIHdlIGRvbid0IGtlZXAgdHJhY2sgb2YgdGhlIG5ldyBzaWduYXR1cmUgYW5kIGRvbid0IHdhbnQgdG8gdXBkYXRlIGluY3JlbWVudGFsbHkuXG4gIC8vIFNlZTogaHR0cHM6Ly9naXRodWIuY29tL2FuZ3VsYXIvY29tcG9uZW50cy9wdWxsLzEyOTcwI2lzc3VlY29tbWVudC00MTgzMzc1NjZcbiAgZGF0YSA9IGdldEFsbENoYW5nZXModGhpcy51cGdyYWRlRGF0YS5jb25zdHJ1Y3RvckNoZWNrcyk7XG5cbiAgLy8gT25seSBlbmFibGUgdGhlIG1pZ3JhdGlvbiBydWxlIGlmIHRoZXJlIGlzIHVwZ3JhZGUgZGF0YS5cbiAgZW5hYmxlZCA9IHRoaXMuZGF0YS5sZW5ndGggIT09IDA7XG5cbiAgb3ZlcnJpZGUgdmlzaXROb2RlKG5vZGU6IHRzLk5vZGUpOiB2b2lkIHtcbiAgICBpZiAodHMuaXNTb3VyY2VGaWxlKG5vZGUpKSB7XG4gICAgICB0aGlzLl92aXNpdFNvdXJjZUZpbGUobm9kZSk7XG4gICAgfVxuICB9XG5cbiAgLyoqXG4gICAqIE1ldGhvZCB0aGF0IHdpbGwgYmUgY2FsbGVkIGZvciBlYWNoIHNvdXJjZSBmaWxlIG9mIHRoZSB1cGdyYWRlIHByb2plY3QuIEluIG9yZGVyIHRvXG4gICAqIHByb3Blcmx5IGRldGVybWluZSBpbnZhbGlkIGNvbnN0cnVjdG9yIHNpZ25hdHVyZXMsIHdlIHRha2UgYWR2YW50YWdlIG9mIHRoZSBwcmUtZW1pdFxuICAgKiBkaWFnbm9zdGljcyBmcm9tIFR5cGVTY3JpcHQuXG4gICAqXG4gICAqIEJ5IHVzaW5nIHRoZSBkaWFnbm9zdGljcywgdGhlIG1pZ3JhdGlvbiBjYW4gaGFuZGxlIHR5cGUgYXNzaWduYWJpbGl0eS4gTm90IHVzaW5nXG4gICAqIGRpYWdub3N0aWNzIHdvdWxkIG1lYW4gdGhhdCB3ZSBuZWVkIHRvIHVzZSBzaW1wbGUgdHlwZSBlcXVhbGl0eSBjaGVja2luZyB3aGljaCBpc1xuICAgKiB0b28gc3RyaWN0LiBTZWUgcmVsYXRlZCBpc3N1ZTogaHR0cHM6Ly9naXRodWIuY29tL01pY3Jvc29mdC9UeXBlU2NyaXB0L2lzc3Vlcy85ODc5XG4gICAqL1xuICBwcml2YXRlIF92aXNpdFNvdXJjZUZpbGUoc291cmNlRmlsZTogdHMuU291cmNlRmlsZSkge1xuICAgIC8vIExpc3Qgb2YgY2xhc3NlcyBvZiB3aGljaCB0aGUgY29uc3RydWN0b3Igc2lnbmF0dXJlIGhhcyBjaGFuZ2VkLlxuICAgIGNvbnN0IGRpYWdub3N0aWNzID0gdHNcbiAgICAgIC5nZXRQcmVFbWl0RGlhZ25vc3RpY3ModGhpcy5wcm9ncmFtLCBzb3VyY2VGaWxlKVxuICAgICAgLmZpbHRlcihkaWFnbm9zdGljID0+IHNpZ25hdHVyZUVycm9yRGlhZ25vc3RpY3MuaW5jbHVkZXMoZGlhZ25vc3RpYy5jb2RlKSlcbiAgICAgIC5maWx0ZXIoZGlhZ25vc3RpYyA9PiBkaWFnbm9zdGljLnN0YXJ0ICE9PSB1bmRlZmluZWQpO1xuXG4gICAgZm9yIChjb25zdCBkaWFnbm9zdGljIG9mIGRpYWdub3N0aWNzKSB7XG4gICAgICBjb25zdCBub2RlID0gZmluZENvbnN0cnVjdG9yTm9kZShkaWFnbm9zdGljLCBzb3VyY2VGaWxlKTtcblxuICAgICAgaWYgKCFub2RlKSB7XG4gICAgICAgIGNvbnRpbnVlO1xuICAgICAgfVxuXG4gICAgICBjb25zdCBjbGFzc1R5cGUgPSB0aGlzLnR5cGVDaGVja2VyLmdldFR5cGVBdExvY2F0aW9uKG5vZGUuZXhwcmVzc2lvbik7XG4gICAgICBjb25zdCBjbGFzc05hbWUgPSBjbGFzc1R5cGUuc3ltYm9sICYmIGNsYXNzVHlwZS5zeW1ib2wubmFtZTtcbiAgICAgIGNvbnN0IGlzTmV3RXhwcmVzc2lvbiA9IHRzLmlzTmV3RXhwcmVzc2lvbihub2RlKTtcblxuICAgICAgLy8gRGV0ZXJtaW5lIHRoZSBjbGFzcyBuYW1lcyBvZiB0aGUgYWN0dWFsIGNvbnN0cnVjdCBzaWduYXR1cmVzIGJlY2F1c2Ugd2UgY2Fubm90IGFzc3VtZSB0aGF0XG4gICAgICAvLyB0aGUgZGlhZ25vc3RpYyByZWZlcnMgdG8gYSBjb25zdHJ1Y3RvciBvZiB0aGUgYWN0dWFsIGV4cHJlc3Npb24uIEluIGNhc2UgdGhlIGNvbnN0cnVjdG9yXG4gICAgICAvLyBpcyBpbmhlcml0ZWQsIHdlIG5lZWQgdG8gZGV0ZWN0IHRoYXQgdGhlIG93bmVyLWNsYXNzIG9mIHRoZSBjb25zdHJ1Y3RvciBpcyBhZGRlZCB0byB0aGVcbiAgICAgIC8vIGNvbnN0cnVjdG9yIGNoZWNrcyB1cGdyYWRlIGRhdGEuIGUuZy4gYGNsYXNzIEN1c3RvbUNhbGVuZGFyIGV4dGVuZHMgTWF0Q2FsZW5kYXIge31gLlxuICAgICAgY29uc3Qgc2lnbmF0dXJlQ2xhc3NOYW1lcyA9IGNsYXNzVHlwZVxuICAgICAgICAuZ2V0Q29uc3RydWN0U2lnbmF0dXJlcygpXG4gICAgICAgIC5tYXAoc2lnbmF0dXJlID0+IGdldENsYXNzRGVjbGFyYXRpb25PZlNpZ25hdHVyZShzaWduYXR1cmUpKVxuICAgICAgICAubWFwKGRlY2xhcmF0aW9uID0+IChkZWNsYXJhdGlvbiAmJiBkZWNsYXJhdGlvbi5uYW1lID8gZGVjbGFyYXRpb24ubmFtZS50ZXh0IDogbnVsbCkpXG4gICAgICAgIC5maWx0ZXIoQm9vbGVhbik7XG5cbiAgICAgIC8vIEJlc2lkZXMgY2hlY2tpbmcgdGhlIHNpZ25hdHVyZSBjbGFzcyBuYW1lcywgd2UgbmVlZCB0byBjaGVjayB0aGUgYWN0dWFsIGNsYXNzIG5hbWUgYmVjYXVzZVxuICAgICAgLy8gdGhlcmUgY2FuIGJlIGNsYXNzZXMgd2l0aG91dCBhbiBleHBsaWNpdCBjb25zdHJ1Y3Rvci5cbiAgICAgIGlmIChcbiAgICAgICAgIXRoaXMuZGF0YS5pbmNsdWRlcyhjbGFzc05hbWUpICYmXG4gICAgICAgICFzaWduYXR1cmVDbGFzc05hbWVzLnNvbWUobmFtZSA9PiB0aGlzLmRhdGEuaW5jbHVkZXMobmFtZSEpKVxuICAgICAgKSB7XG4gICAgICAgIGNvbnRpbnVlO1xuICAgICAgfVxuXG4gICAgICBjb25zdCBjbGFzc1NpZ25hdHVyZXMgPSBjbGFzc1R5cGVcbiAgICAgICAgLmdldENvbnN0cnVjdFNpZ25hdHVyZXMoKVxuICAgICAgICAubWFwKHNpZ25hdHVyZSA9PiBnZXRQYXJhbWV0ZXJUeXBlc0Zyb21TaWduYXR1cmUoc2lnbmF0dXJlLCB0aGlzLnR5cGVDaGVja2VyKSk7XG5cbiAgICAgIGNvbnN0IGV4cHJlc3Npb25OYW1lID0gaXNOZXdFeHByZXNzaW9uID8gYG5ldyAke2NsYXNzTmFtZX1gIDogJ3N1cGVyJztcbiAgICAgIGNvbnN0IHNpZ25hdHVyZXMgPSBjbGFzc1NpZ25hdHVyZXNcbiAgICAgICAgLm1hcChzaWduYXR1cmUgPT5cbiAgICAgICAgICBzaWduYXR1cmUubWFwKHQgPT4gKHQgPT09IG51bGwgPyAnYW55JyA6IHRoaXMudHlwZUNoZWNrZXIudHlwZVRvU3RyaW5nKHQpKSksXG4gICAgICAgIClcbiAgICAgICAgLm1hcChzaWduYXR1cmUgPT4gYCR7ZXhwcmVzc2lvbk5hbWV9KCR7c2lnbmF0dXJlLmpvaW4oJywgJyl9KWApXG4gICAgICAgIC5qb2luKCcgb3IgJyk7XG5cbiAgICAgIHRoaXMuY3JlYXRlRmFpbHVyZUF0Tm9kZShcbiAgICAgICAgbm9kZSxcbiAgICAgICAgYEZvdW5kIFwiJHtjbGFzc05hbWV9XCIgY29uc3RydWN0ZWQgd2l0aCBgICtcbiAgICAgICAgICBgYW4gaW52YWxpZCBzaWduYXR1cmUuIFBsZWFzZSBtYW51YWxseSB1cGRhdGUgdGhlICR7ZXhwcmVzc2lvbk5hbWV9IGV4cHJlc3Npb24gdG8gYCArXG4gICAgICAgICAgYG1hdGNoIHRoZSBuZXcgc2lnbmF0dXJlJHtjbGFzc1NpZ25hdHVyZXMubGVuZ3RoID4gMSA/ICdzJyA6ICcnfTogJHtzaWduYXR1cmVzfWAsXG4gICAgICApO1xuICAgIH1cbiAgfVxufVxuXG4vKiogUmVzb2x2ZXMgdGhlIHR5cGUgZm9yIGVhY2ggcGFyYW1ldGVyIGluIHRoZSBzcGVjaWZpZWQgc2lnbmF0dXJlLiAqL1xuZnVuY3Rpb24gZ2V0UGFyYW1ldGVyVHlwZXNGcm9tU2lnbmF0dXJlKFxuICBzaWduYXR1cmU6IHRzLlNpZ25hdHVyZSxcbiAgdHlwZUNoZWNrZXI6IHRzLlR5cGVDaGVja2VyLFxuKTogKHRzLlR5cGUgfCBudWxsKVtdIHtcbiAgcmV0dXJuIHNpZ25hdHVyZVxuICAgIC5nZXRQYXJhbWV0ZXJzKClcbiAgICAubWFwKHBhcmFtID0+XG4gICAgICBwYXJhbS5kZWNsYXJhdGlvbnMgPyB0eXBlQ2hlY2tlci5nZXRUeXBlQXRMb2NhdGlvbihwYXJhbS5kZWNsYXJhdGlvbnNbMF0pIDogbnVsbCxcbiAgICApO1xufVxuXG4vKipcbiAqIFdhbGtzIHRocm91Z2ggZWFjaCBub2RlIG9mIGEgc291cmNlIGZpbGUgaW4gb3JkZXIgdG8gZmluZCBhIG5ldy1leHByZXNzaW9uIG5vZGUgb3Igc3VwZXItY2FsbFxuICogZXhwcmVzc2lvbiBub2RlIHRoYXQgaXMgY2FwdHVyZWQgYnkgdGhlIHNwZWNpZmllZCBkaWFnbm9zdGljLlxuICovXG5mdW5jdGlvbiBmaW5kQ29uc3RydWN0b3JOb2RlKFxuICBkaWFnbm9zdGljOiB0cy5EaWFnbm9zdGljLFxuICBzb3VyY2VGaWxlOiB0cy5Tb3VyY2VGaWxlLFxuKTogdHMuQ2FsbEV4cHJlc3Npb24gfCB0cy5OZXdFeHByZXNzaW9uIHwgbnVsbCB7XG4gIGxldCByZXNvbHZlZE5vZGU6IHRzLk5vZGUgfCBudWxsID0gbnVsbDtcblxuICBjb25zdCBfdmlzaXROb2RlID0gKG5vZGU6IHRzLk5vZGUpID0+IHtcbiAgICAvLyBDaGVjayB3aGV0aGVyIHRoZSBjdXJyZW50IG5vZGUgY29udGFpbnMgdGhlIGRpYWdub3N0aWMuIElmIHRoZSBub2RlIGNvbnRhaW5zIHRoZSBkaWFnbm9zdGljLFxuICAgIC8vIHdhbGsgZGVlcGVyIGluIG9yZGVyIHRvIGZpbmQgYWxsIGNvbnN0cnVjdG9yIGV4cHJlc3Npb24gbm9kZXMuXG4gICAgaWYgKG5vZGUuZ2V0U3RhcnQoKSA8PSBkaWFnbm9zdGljLnN0YXJ0ISAmJiBub2RlLmdldEVuZCgpID49IGRpYWdub3N0aWMuc3RhcnQhKSB7XG4gICAgICBpZiAoXG4gICAgICAgIHRzLmlzTmV3RXhwcmVzc2lvbihub2RlKSB8fFxuICAgICAgICAodHMuaXNDYWxsRXhwcmVzc2lvbihub2RlKSAmJiBub2RlLmV4cHJlc3Npb24ua2luZCA9PT0gdHMuU3ludGF4S2luZC5TdXBlcktleXdvcmQpXG4gICAgICApIHtcbiAgICAgICAgcmVzb2x2ZWROb2RlID0gbm9kZTtcbiAgICAgIH1cblxuICAgICAgdHMuZm9yRWFjaENoaWxkKG5vZGUsIF92aXNpdE5vZGUpO1xuICAgIH1cbiAgfTtcblxuICB0cy5mb3JFYWNoQ2hpbGQoc291cmNlRmlsZSwgX3Zpc2l0Tm9kZSk7XG5cbiAgcmV0dXJuIHJlc29sdmVkTm9kZTtcbn1cblxuLyoqIERldGVybWluZXMgdGhlIGNsYXNzIGRlY2xhcmF0aW9uIG9mIHRoZSBzcGVjaWZpZWQgY29uc3RydWN0IHNpZ25hdHVyZS4gKi9cbmZ1bmN0aW9uIGdldENsYXNzRGVjbGFyYXRpb25PZlNpZ25hdHVyZShzaWduYXR1cmU6IHRzLlNpZ25hdHVyZSk6IHRzLkNsYXNzRGVjbGFyYXRpb24gfCBudWxsIHtcbiAgbGV0IG5vZGU6IHRzLk5vZGUgPSBzaWduYXR1cmUuZ2V0RGVjbGFyYXRpb24oKTtcbiAgLy8gSGFuZGxlIHNpZ25hdHVyZXMgd2hpY2ggZG9uJ3QgaGF2ZSBhbiBhY3R1YWwgZGVjbGFyYXRpb24uIFRoaXMgaGFwcGVucyBpZiBhIGNsYXNzXG4gIC8vIGRvZXMgbm90IGhhdmUgYW4gZXhwbGljaXRseSB3cml0dGVuIGNvbnN0cnVjdG9yLlxuICBpZiAoIW5vZGUpIHtcbiAgICByZXR1cm4gbnVsbDtcbiAgfVxuICB3aGlsZSAoIXRzLmlzU291cmNlRmlsZSgobm9kZSA9IG5vZGUucGFyZW50KSkpIHtcbiAgICBpZiAodHMuaXNDbGFzc0RlY2xhcmF0aW9uKG5vZGUpKSB7XG4gICAgICByZXR1cm4gbm9kZTtcbiAgICB9XG4gIH1cbiAgcmV0dXJuIG51bGw7XG59XG4iXX0=