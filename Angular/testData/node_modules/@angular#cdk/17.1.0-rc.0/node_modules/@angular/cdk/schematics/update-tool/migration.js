"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.Migration = void 0;
const ts = require("typescript");
class Migration {
    program;
    typeChecker;
    targetVersion;
    context;
    upgradeData;
    fileSystem;
    logger;
    /** List of migration failures that need to be reported. */
    failures = [];
    constructor(
    /** TypeScript program for the migration. */
    program, 
    /** TypeChecker instance for the analysis program. */
    typeChecker, 
    /**
     * Version for which the migration rule should run. Null if the migration
     * is invoked manually.
     */
    targetVersion, 
    /** Context data for the migration. */
    context, 
    /** Upgrade data passed to the migration. */
    upgradeData, 
    /** File system that can be used for modifying files. */
    fileSystem, 
    /** Logger that can be used to print messages as part of the migration. */
    logger) {
        this.program = program;
        this.typeChecker = typeChecker;
        this.targetVersion = targetVersion;
        this.context = context;
        this.upgradeData = upgradeData;
        this.fileSystem = fileSystem;
        this.logger = logger;
    }
    /** Method can be used to perform global analysis of the program. */
    init() { }
    /**
     * Method that will be called once all nodes, templates and stylesheets
     * have been visited.
     */
    postAnalysis() { }
    /**
     * Method that will be called for each node in a given source file. Unlike tslint, this
     * function will only retrieve TypeScript nodes that need to be casted manually. This
     * allows us to only walk the program source files once per program and not per
     * migration rule (significant performance boost).
     */
    visitNode(node) { }
    /** Method that will be called for each Angular template in the program. */
    visitTemplate(template) { }
    /** Method that will be called for each stylesheet in the program. */
    visitStylesheet(stylesheet) { }
    /** Creates a failure with a specified message at the given node location. */
    createFailureAtNode(node, message) {
        const sourceFile = node.getSourceFile();
        this.failures.push({
            filePath: this.fileSystem.resolve(sourceFile.fileName),
            position: ts.getLineAndCharacterOfPosition(sourceFile, node.getStart()),
            message: message,
        });
    }
}
exports.Migration = Migration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoibWlncmF0aW9uLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vLi4vLi4vLi4vLi4vc3JjL2Nkay9zY2hlbWF0aWNzL3VwZGF0ZS10b29sL21pZ3JhdGlvbi50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiO0FBQUE7Ozs7OztHQU1HOzs7QUFFSCxpQ0FBaUM7QUF1QmpDLE1BQXNCLFNBQVM7SUFTcEI7SUFFQTtJQUtBO0lBRUE7SUFFQTtJQUVBO0lBRUE7SUF2QlQsMkRBQTJEO0lBQzNELFFBQVEsR0FBdUIsRUFBRSxDQUFDO0lBS2xDO0lBQ0UsNENBQTRDO0lBQ3JDLE9BQW1CO0lBQzFCLHFEQUFxRDtJQUM5QyxXQUEyQjtJQUNsQzs7O09BR0c7SUFDSSxhQUFtQztJQUMxQyxzQ0FBc0M7SUFDL0IsT0FBZ0I7SUFDdkIsNENBQTRDO0lBQ3JDLFdBQWlCO0lBQ3hCLHdEQUF3RDtJQUNqRCxVQUFzQjtJQUM3QiwwRUFBMEU7SUFDbkUsTUFBb0I7UUFmcEIsWUFBTyxHQUFQLE9BQU8sQ0FBWTtRQUVuQixnQkFBVyxHQUFYLFdBQVcsQ0FBZ0I7UUFLM0Isa0JBQWEsR0FBYixhQUFhLENBQXNCO1FBRW5DLFlBQU8sR0FBUCxPQUFPLENBQVM7UUFFaEIsZ0JBQVcsR0FBWCxXQUFXLENBQU07UUFFakIsZUFBVSxHQUFWLFVBQVUsQ0FBWTtRQUV0QixXQUFNLEdBQU4sTUFBTSxDQUFjO0lBQzFCLENBQUM7SUFFSixvRUFBb0U7SUFDcEUsSUFBSSxLQUFVLENBQUM7SUFFZjs7O09BR0c7SUFDSCxZQUFZLEtBQVUsQ0FBQztJQUV2Qjs7Ozs7T0FLRztJQUNILFNBQVMsQ0FBQyxJQUFhLElBQVMsQ0FBQztJQUVqQywyRUFBMkU7SUFDM0UsYUFBYSxDQUFDLFFBQTBCLElBQVMsQ0FBQztJQUVsRCxxRUFBcUU7SUFDckUsZUFBZSxDQUFDLFVBQTRCLElBQVMsQ0FBQztJQUV0RCw2RUFBNkU7SUFDbkUsbUJBQW1CLENBQUMsSUFBYSxFQUFFLE9BQWU7UUFDMUQsTUFBTSxVQUFVLEdBQUcsSUFBSSxDQUFDLGFBQWEsRUFBRSxDQUFDO1FBQ3hDLElBQUksQ0FBQyxRQUFRLENBQUMsSUFBSSxDQUFDO1lBQ2pCLFFBQVEsRUFBRSxJQUFJLENBQUMsVUFBVSxDQUFDLE9BQU8sQ0FBQyxVQUFVLENBQUMsUUFBUSxDQUFDO1lBQ3RELFFBQVEsRUFBRSxFQUFFLENBQUMsNkJBQTZCLENBQUMsVUFBVSxFQUFFLElBQUksQ0FBQyxRQUFRLEVBQUUsQ0FBQztZQUN2RSxPQUFPLEVBQUUsT0FBTztTQUNqQixDQUFDLENBQUM7SUFDTCxDQUFDO0NBQ0Y7QUEzREQsOEJBMkRDIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5cbmltcG9ydCAqIGFzIHRzIGZyb20gJ3R5cGVzY3JpcHQnO1xuaW1wb3J0IHtSZXNvbHZlZFJlc291cmNlfSBmcm9tICcuL2NvbXBvbmVudC1yZXNvdXJjZS1jb2xsZWN0b3InO1xuaW1wb3J0IHtGaWxlU3lzdGVtLCBXb3Jrc3BhY2VQYXRofSBmcm9tICcuL2ZpbGUtc3lzdGVtJztcbmltcG9ydCB7VXBkYXRlTG9nZ2VyfSBmcm9tICcuL2xvZ2dlcic7XG5pbXBvcnQge1RhcmdldFZlcnNpb259IGZyb20gJy4vdGFyZ2V0LXZlcnNpb24nO1xuaW1wb3J0IHtMaW5lQW5kQ2hhcmFjdGVyfSBmcm9tICcuL3V0aWxzL2xpbmUtbWFwcGluZ3MnO1xuXG5leHBvcnQgaW50ZXJmYWNlIE1pZ3JhdGlvbkZhaWx1cmUge1xuICBmaWxlUGF0aDogV29ya3NwYWNlUGF0aDtcbiAgbWVzc2FnZTogc3RyaW5nO1xuICBwb3NpdGlvbj86IExpbmVBbmRDaGFyYWN0ZXI7XG59XG5cbmV4cG9ydCB0eXBlIFBvc3RNaWdyYXRpb25BY3Rpb24gPSB2b2lkIHwge1xuICAvKiogV2hldGhlciB0aGUgcGFja2FnZSBtYW5hZ2VyIHNob3VsZCBydW4gdXBvbiBtaWdyYXRpb24gY29tcGxldGlvbi4gKi9cbiAgcnVuUGFja2FnZU1hbmFnZXI6IGJvb2xlYW47XG59O1xuXG4vKiogQ3JlYXRlcyBhIGNvbnN0cnVjdG9yIHR5cGUgZm9yIHRoZSBzcGVjaWZpZWQgdHlwZS4gKi9cbmV4cG9ydCB0eXBlIENvbnN0cnVjdG9yPFQ+ID0gbmV3ICguLi5hcmdzOiBhbnlbXSkgPT4gVDtcbi8qKiBHZXRzIGEgY29uc3RydWN0b3IgdHlwZSBmb3IgdGhlIHBhc3NlZCBtaWdyYXRpb24gZGF0YS4gKi9cbmV4cG9ydCB0eXBlIE1pZ3JhdGlvbkN0b3I8RGF0YSwgQ29udGV4dCA9IGFueT4gPSBDb25zdHJ1Y3RvcjxNaWdyYXRpb248RGF0YSwgQ29udGV4dD4+O1xuXG5leHBvcnQgYWJzdHJhY3QgY2xhc3MgTWlncmF0aW9uPERhdGEsIENvbnRleHQgPSBhbnk+IHtcbiAgLyoqIExpc3Qgb2YgbWlncmF0aW9uIGZhaWx1cmVzIHRoYXQgbmVlZCB0byBiZSByZXBvcnRlZC4gKi9cbiAgZmFpbHVyZXM6IE1pZ3JhdGlvbkZhaWx1cmVbXSA9IFtdO1xuXG4gIC8qKiBXaGV0aGVyIHRoZSBtaWdyYXRpb24gaXMgZW5hYmxlZCBvciBub3QuICovXG4gIGFic3RyYWN0IGVuYWJsZWQ6IGJvb2xlYW47XG5cbiAgY29uc3RydWN0b3IoXG4gICAgLyoqIFR5cGVTY3JpcHQgcHJvZ3JhbSBmb3IgdGhlIG1pZ3JhdGlvbi4gKi9cbiAgICBwdWJsaWMgcHJvZ3JhbTogdHMuUHJvZ3JhbSxcbiAgICAvKiogVHlwZUNoZWNrZXIgaW5zdGFuY2UgZm9yIHRoZSBhbmFseXNpcyBwcm9ncmFtLiAqL1xuICAgIHB1YmxpYyB0eXBlQ2hlY2tlcjogdHMuVHlwZUNoZWNrZXIsXG4gICAgLyoqXG4gICAgICogVmVyc2lvbiBmb3Igd2hpY2ggdGhlIG1pZ3JhdGlvbiBydWxlIHNob3VsZCBydW4uIE51bGwgaWYgdGhlIG1pZ3JhdGlvblxuICAgICAqIGlzIGludm9rZWQgbWFudWFsbHkuXG4gICAgICovXG4gICAgcHVibGljIHRhcmdldFZlcnNpb246IFRhcmdldFZlcnNpb24gfCBudWxsLFxuICAgIC8qKiBDb250ZXh0IGRhdGEgZm9yIHRoZSBtaWdyYXRpb24uICovXG4gICAgcHVibGljIGNvbnRleHQ6IENvbnRleHQsXG4gICAgLyoqIFVwZ3JhZGUgZGF0YSBwYXNzZWQgdG8gdGhlIG1pZ3JhdGlvbi4gKi9cbiAgICBwdWJsaWMgdXBncmFkZURhdGE6IERhdGEsXG4gICAgLyoqIEZpbGUgc3lzdGVtIHRoYXQgY2FuIGJlIHVzZWQgZm9yIG1vZGlmeWluZyBmaWxlcy4gKi9cbiAgICBwdWJsaWMgZmlsZVN5c3RlbTogRmlsZVN5c3RlbSxcbiAgICAvKiogTG9nZ2VyIHRoYXQgY2FuIGJlIHVzZWQgdG8gcHJpbnQgbWVzc2FnZXMgYXMgcGFydCBvZiB0aGUgbWlncmF0aW9uLiAqL1xuICAgIHB1YmxpYyBsb2dnZXI6IFVwZGF0ZUxvZ2dlcixcbiAgKSB7fVxuXG4gIC8qKiBNZXRob2QgY2FuIGJlIHVzZWQgdG8gcGVyZm9ybSBnbG9iYWwgYW5hbHlzaXMgb2YgdGhlIHByb2dyYW0uICovXG4gIGluaXQoKTogdm9pZCB7fVxuXG4gIC8qKlxuICAgKiBNZXRob2QgdGhhdCB3aWxsIGJlIGNhbGxlZCBvbmNlIGFsbCBub2RlcywgdGVtcGxhdGVzIGFuZCBzdHlsZXNoZWV0c1xuICAgKiBoYXZlIGJlZW4gdmlzaXRlZC5cbiAgICovXG4gIHBvc3RBbmFseXNpcygpOiB2b2lkIHt9XG5cbiAgLyoqXG4gICAqIE1ldGhvZCB0aGF0IHdpbGwgYmUgY2FsbGVkIGZvciBlYWNoIG5vZGUgaW4gYSBnaXZlbiBzb3VyY2UgZmlsZS4gVW5saWtlIHRzbGludCwgdGhpc1xuICAgKiBmdW5jdGlvbiB3aWxsIG9ubHkgcmV0cmlldmUgVHlwZVNjcmlwdCBub2RlcyB0aGF0IG5lZWQgdG8gYmUgY2FzdGVkIG1hbnVhbGx5LiBUaGlzXG4gICAqIGFsbG93cyB1cyB0byBvbmx5IHdhbGsgdGhlIHByb2dyYW0gc291cmNlIGZpbGVzIG9uY2UgcGVyIHByb2dyYW0gYW5kIG5vdCBwZXJcbiAgICogbWlncmF0aW9uIHJ1bGUgKHNpZ25pZmljYW50IHBlcmZvcm1hbmNlIGJvb3N0KS5cbiAgICovXG4gIHZpc2l0Tm9kZShub2RlOiB0cy5Ob2RlKTogdm9pZCB7fVxuXG4gIC8qKiBNZXRob2QgdGhhdCB3aWxsIGJlIGNhbGxlZCBmb3IgZWFjaCBBbmd1bGFyIHRlbXBsYXRlIGluIHRoZSBwcm9ncmFtLiAqL1xuICB2aXNpdFRlbXBsYXRlKHRlbXBsYXRlOiBSZXNvbHZlZFJlc291cmNlKTogdm9pZCB7fVxuXG4gIC8qKiBNZXRob2QgdGhhdCB3aWxsIGJlIGNhbGxlZCBmb3IgZWFjaCBzdHlsZXNoZWV0IGluIHRoZSBwcm9ncmFtLiAqL1xuICB2aXNpdFN0eWxlc2hlZXQoc3R5bGVzaGVldDogUmVzb2x2ZWRSZXNvdXJjZSk6IHZvaWQge31cblxuICAvKiogQ3JlYXRlcyBhIGZhaWx1cmUgd2l0aCBhIHNwZWNpZmllZCBtZXNzYWdlIGF0IHRoZSBnaXZlbiBub2RlIGxvY2F0aW9uLiAqL1xuICBwcm90ZWN0ZWQgY3JlYXRlRmFpbHVyZUF0Tm9kZShub2RlOiB0cy5Ob2RlLCBtZXNzYWdlOiBzdHJpbmcpIHtcbiAgICBjb25zdCBzb3VyY2VGaWxlID0gbm9kZS5nZXRTb3VyY2VGaWxlKCk7XG4gICAgdGhpcy5mYWlsdXJlcy5wdXNoKHtcbiAgICAgIGZpbGVQYXRoOiB0aGlzLmZpbGVTeXN0ZW0ucmVzb2x2ZShzb3VyY2VGaWxlLmZpbGVOYW1lKSxcbiAgICAgIHBvc2l0aW9uOiB0cy5nZXRMaW5lQW5kQ2hhcmFjdGVyT2ZQb3NpdGlvbihzb3VyY2VGaWxlLCBub2RlLmdldFN0YXJ0KCkpLFxuICAgICAgbWVzc2FnZTogbWVzc2FnZSxcbiAgICB9KTtcbiAgfVxufVxuIl19