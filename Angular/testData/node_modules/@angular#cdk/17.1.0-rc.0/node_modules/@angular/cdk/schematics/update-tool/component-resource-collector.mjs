"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.ComponentResourceCollector = void 0;
const path_1 = require("path");
const ts = require("typescript");
const decorators_1 = require("./utils/decorators");
const functions_1 = require("./utils/functions");
const line_mappings_1 = require("./utils/line-mappings");
const property_name_1 = require("./utils/property-name");
/**
 * Collector that can be used to find Angular templates and stylesheets referenced within
 * given TypeScript source files (inline or external referenced files)
 */
class ComponentResourceCollector {
    typeChecker;
    _fileSystem;
    resolvedTemplates = [];
    resolvedStylesheets = [];
    constructor(typeChecker, _fileSystem) {
        this.typeChecker = typeChecker;
        this._fileSystem = _fileSystem;
    }
    visitNode(node) {
        if (node.kind === ts.SyntaxKind.ClassDeclaration) {
            this._visitClassDeclaration(node);
        }
    }
    _visitClassDeclaration(node) {
        const decorators = ts.getDecorators(node);
        if (!decorators || !decorators.length) {
            return;
        }
        const ngDecorators = (0, decorators_1.getAngularDecorators)(this.typeChecker, decorators);
        const componentDecorator = ngDecorators.find(dec => dec.name === 'Component');
        // In case no "@Component" decorator could be found on the current class, skip.
        if (!componentDecorator) {
            return;
        }
        const decoratorCall = componentDecorator.node.expression;
        // In case the component decorator call is not valid, skip this class declaration.
        if (decoratorCall.arguments.length !== 1) {
            return;
        }
        const componentMetadata = (0, functions_1.unwrapExpression)(decoratorCall.arguments[0]);
        // Ensure that the component metadata is an object literal expression.
        if (!ts.isObjectLiteralExpression(componentMetadata)) {
            return;
        }
        const sourceFile = node.getSourceFile();
        const filePath = this._fileSystem.resolve(sourceFile.fileName);
        const sourceFileDirPath = (0, path_1.dirname)(sourceFile.fileName);
        // Walk through all component metadata properties and determine the referenced
        // HTML templates (either external or inline)
        componentMetadata.properties.forEach(property => {
            if (!ts.isPropertyAssignment(property)) {
                return;
            }
            const propertyName = (0, property_name_1.getPropertyNameText)(property.name);
            if (propertyName === 'styles') {
                const elements = ts.isArrayLiteralExpression(property.initializer)
                    ? property.initializer.elements
                    : [property.initializer];
                elements.forEach(el => {
                    if (ts.isStringLiteralLike(el)) {
                        // Need to add an offset of one to the start because the template quotes are
                        // not part of the template content.
                        const templateStartIdx = el.getStart() + 1;
                        const content = stripBom(el.text);
                        this.resolvedStylesheets.push({
                            filePath,
                            container: node,
                            content,
                            inline: true,
                            start: templateStartIdx,
                            getCharacterAndLineOfPosition: pos => ts.getLineAndCharacterOfPosition(sourceFile, pos + templateStartIdx),
                        });
                    }
                });
            }
            // In case there is an inline template specified, ensure that the value is statically
            // analyzable by checking if the initializer is a string literal-like node.
            if (propertyName === 'template' && ts.isStringLiteralLike(property.initializer)) {
                // Need to add an offset of one to the start because the template quotes are
                // not part of the template content.
                const templateStartIdx = property.initializer.getStart() + 1;
                this.resolvedTemplates.push({
                    filePath,
                    container: node,
                    content: property.initializer.text,
                    inline: true,
                    start: templateStartIdx,
                    getCharacterAndLineOfPosition: pos => ts.getLineAndCharacterOfPosition(sourceFile, pos + templateStartIdx),
                });
            }
            if (propertyName === 'styleUrls' && ts.isArrayLiteralExpression(property.initializer)) {
                property.initializer.elements.forEach(el => {
                    if (ts.isStringLiteralLike(el)) {
                        this._trackExternalStylesheet(sourceFileDirPath, el, node);
                    }
                });
            }
            if (propertyName === 'styleUrl' && ts.isStringLiteralLike(property.initializer)) {
                this._trackExternalStylesheet(sourceFileDirPath, property.initializer, node);
            }
            if (propertyName === 'templateUrl' && ts.isStringLiteralLike(property.initializer)) {
                const templateUrl = property.initializer.text;
                const templatePath = this._fileSystem.resolve(sourceFileDirPath, templateUrl);
                // In case the template does not exist in the file system, skip this
                // external template.
                if (!this._fileSystem.fileExists(templatePath)) {
                    return;
                }
                const fileContent = stripBom(this._fileSystem.read(templatePath) || '');
                if (fileContent) {
                    const lineStartsMap = (0, line_mappings_1.computeLineStartsMap)(fileContent);
                    this.resolvedTemplates.push({
                        filePath: templatePath,
                        container: node,
                        content: fileContent,
                        inline: false,
                        start: 0,
                        getCharacterAndLineOfPosition: p => (0, line_mappings_1.getLineAndCharacterFromPosition)(lineStartsMap, p),
                    });
                }
            }
        });
    }
    /** Resolves an external stylesheet by reading its content and computing line mappings. */
    resolveExternalStylesheet(filePath, container) {
        // Strip the BOM to avoid issues with the Sass compiler. See:
        // https://github.com/angular/components/issues/24227#issuecomment-1200934258
        const fileContent = stripBom(this._fileSystem.read(filePath) || '');
        if (!fileContent) {
            return null;
        }
        const lineStartsMap = (0, line_mappings_1.computeLineStartsMap)(fileContent);
        return {
            filePath: filePath,
            container: container,
            content: fileContent,
            inline: false,
            start: 0,
            getCharacterAndLineOfPosition: pos => (0, line_mappings_1.getLineAndCharacterFromPosition)(lineStartsMap, pos),
        };
    }
    _trackExternalStylesheet(sourceFileDirPath, node, container) {
        const stylesheetPath = this._fileSystem.resolve(sourceFileDirPath, node.text);
        const stylesheet = this.resolveExternalStylesheet(stylesheetPath, container);
        if (stylesheet) {
            this.resolvedStylesheets.push(stylesheet);
        }
    }
}
exports.ComponentResourceCollector = ComponentResourceCollector;
/** Strips the BOM from a string. */
function stripBom(content) {
    return content.replace(/\uFEFF/g, '');
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiY29tcG9uZW50LXJlc291cmNlLWNvbGxlY3Rvci5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy91cGRhdGUtdG9vbC9jb21wb25lbnQtcmVzb3VyY2UtY29sbGVjdG9yLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7OztBQUVILCtCQUE2QjtBQUM3QixpQ0FBaUM7QUFFakMsbURBQXdEO0FBQ3hELGlEQUFtRDtBQUNuRCx5REFJK0I7QUFDL0IseURBQTBEO0FBcUIxRDs7O0dBR0c7QUFDSCxNQUFhLDBCQUEwQjtJQUs1QjtJQUNDO0lBTFYsaUJBQWlCLEdBQXVCLEVBQUUsQ0FBQztJQUMzQyxtQkFBbUIsR0FBdUIsRUFBRSxDQUFDO0lBRTdDLFlBQ1MsV0FBMkIsRUFDMUIsV0FBdUI7UUFEeEIsZ0JBQVcsR0FBWCxXQUFXLENBQWdCO1FBQzFCLGdCQUFXLEdBQVgsV0FBVyxDQUFZO0lBQzlCLENBQUM7SUFFSixTQUFTLENBQUMsSUFBYTtRQUNyQixJQUFJLElBQUksQ0FBQyxJQUFJLEtBQUssRUFBRSxDQUFDLFVBQVUsQ0FBQyxnQkFBZ0IsRUFBRSxDQUFDO1lBQ2pELElBQUksQ0FBQyxzQkFBc0IsQ0FBQyxJQUEyQixDQUFDLENBQUM7UUFDM0QsQ0FBQztJQUNILENBQUM7SUFFTyxzQkFBc0IsQ0FBQyxJQUF5QjtRQUN0RCxNQUFNLFVBQVUsR0FBRyxFQUFFLENBQUMsYUFBYSxDQUFDLElBQUksQ0FBQyxDQUFDO1FBRTFDLElBQUksQ0FBQyxVQUFVLElBQUksQ0FBQyxVQUFVLENBQUMsTUFBTSxFQUFFLENBQUM7WUFDdEMsT0FBTztRQUNULENBQUM7UUFFRCxNQUFNLFlBQVksR0FBRyxJQUFBLGlDQUFvQixFQUFDLElBQUksQ0FBQyxXQUFXLEVBQUUsVUFBVSxDQUFDLENBQUM7UUFDeEUsTUFBTSxrQkFBa0IsR0FBRyxZQUFZLENBQUMsSUFBSSxD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QUNMLFFBQVEsRUFBRSxRQUFRO1lBQ2xCLFNBQVMsRUFBRSxTQUFTO1lBQ3BCLE9BQU8sRUFBRSxXQUFXO1lBQ3BCLE1BQU0sRUFBRSxLQUFLO1lBQ2IsS0FBSyxFQUFFLENBQUM7WUFDUiw2QkFBNkIsRUFBRSxHQUFHLENBQUMsRUFBRSxDQUFDLElBQUEsK0NBQStCLEVBQUMsYUFBYSxFQUFFLEdBQUcsQ0FBQztTQUMxRixDQUFDO0lBQ0osQ0FBQztJQUVPLHdCQUF3QixDQUM5QixpQkFBeUIsRUFDekIsSUFBMEIsRUFDMUIsU0FBOEI7UUFFOUIsTUFBTSxjQUFjLEdBQUcsSUFBSSxDQUFDLFdBQVcsQ0FBQyxPQUFPLENBQUMsaUJBQWlCLEVBQUUsSUFBSSxDQUFDLElBQUksQ0FBQyxDQUFDO1FBQzlFLE1BQU0sVUFBVSxHQUFHLElBQUksQ0FBQyx5QkFBeUIsQ0FBQyxjQUFjLEVBQUUsU0FBUyxDQUFDLENBQUM7UUFFN0UsSUFBSSxVQUFVLEVBQUUsQ0FBQztZQUNmLElBQUksQ0FBQyxtQkFBbUIsQ0FBQyxJQUFJLENBQUMsVUFBVSxDQUFDLENBQUM7UUFDNUMsQ0FBQztJQUNILENBQUM7Q0FDRjtBQS9LRCxnRUErS0M7QUFFRCxvQ0FBb0M7QUFDcEMsU0FBUyxRQUFRLENBQUMsT0FBZTtJQUMvQixPQUFPLE9BQU8sQ0FBQyxPQUFPLENBQUMsU0FBUyxFQUFFLEVBQUUsQ0FBQyxDQUFDO0FBQ3hDLENBQUMiLCJzb3VyY2VzQ29udGVudCI6WyIvKipcbiAqIEBsaWNlbnNlXG4gKiBDb3B5cmlnaHQgR29vZ2xlIExMQyBBbGwgUmlnaHRzIFJlc2VydmVkLlxuICpcbiAqIFVzZSBvZiB0aGlzIHNvdXJjZSBjb2RlIGlzIGdvdmVybmVkIGJ5IGFuIE1JVC1zdHlsZSBsaWNlbnNlIHRoYXQgY2FuIGJlXG4gKiBmb3VuZCBpbiB0aGUgTElDRU5TRSBmaWxlIGF0IGh0dHBzOi8vYW5ndWxhci5pby9saWNlbnNlXG4gKi9cblxuaW1wb3J0IHtkaXJuYW1lfSBmcm9tICdwYXRoJztcbmltcG9ydCAqIGFzIHRzIGZyb20gJ3R5cGVzY3JpcHQnO1xuaW1wb3J0IHtGaWxlU3lzdGVtLCBXb3Jrc3BhY2VQYXRofSBmcm9tICcuL2ZpbGUtc3lzdGVtJztcbmltcG9ydCB7Z2V0QW5ndWxhckRlY29yYXRvcnN9IGZyb20gJy4vdXRpbHMvZGVjb3JhdG9ycyc7XG5pbXBvcnQge3Vud3JhcEV4cHJlc3Npb259IGZyb20gJy4vdXRpbHMvZnVuY3Rpb25zJztcbmltcG9ydCB7XG4gIGNvbXB1dGVMaW5lU3RhcnRzTWFwLFxuICBnZXRMaW5lQW5kQ2hhcmFjdGVyRnJvbVBvc2l0aW9uLFxuICBMaW5lQW5kQ2hhcmFjdGVyLFxufSBmcm9tICcuL3V0aWxzL2xpbmUtbWFwcGluZ3MnO1xuaW1wb3J0IHtnZXRQcm9wZXJ0eU5hbWVUZXh0fSBmcm9tICcuL3V0aWxzL3Byb3BlcnR5LW5hbWUnO1xuXG5leHBvcnQgaW50ZXJmYWNlIFJlc29sdmVkUmVzb3VyY2Uge1xuICAvKiogQ2xhc3MgZGVjbGFyYXRpb24gdGhhdCBjb250YWlucyB0aGlzIHJlc291cmNlLiAqL1xuICBjb250YWluZXI6IHRzLkNsYXNzRGVjbGFyYXRpb24gfCBudWxsO1xuICAvKiogRmlsZSBjb250ZW50IG9mIHRoZSBnaXZlbiB0ZW1wbGF0ZS4gKi9cbiAgY29udGVudDogc3RyaW5nO1xuICAvKiogU3RhcnQgb2Zmc2V0IG9mIHRoZSByZXNvdXJjZSBjb250ZW50IChlLmcuIGluIHRoZSBpbmxpbmUgc291cmNlIGZpbGUpICovXG4gIHN0YXJ0OiBudW1iZXI7XG4gIC8qKiBXaGV0aGVyIHRoZSBnaXZlbiByZXNvdXJjZSBpcyBpbmxpbmUgb3Igbm90LiAqL1xuICBpbmxpbmU6IGJvb2xlYW47XG4gIC8qKiBQYXRoIHRvIHRoZSBmaWxlIHRoYXQgY29udGFpbnMgdGhpcyByZXNvdXJjZS4gKi9cbiAgZmlsZVBhdGg6IFdvcmtzcGFjZVBhdGg7XG4gIC8qKlxuICAgKiBHZXRzIHRoZSBjaGFyYWN0ZXIgYW5kIGxpbmUgb2YgYSBnaXZlbiBwb3NpdGlvbiBpbmRleCBpbiB0aGUgcmVzb3VyY2UuXG4gICAqIElmIHRoZSByZXNvdXJjZSBpcyBkZWNsYXJlZCBpbmxpbmUgd2l0aGluIGEgVHlwZVNjcmlwdCBzb3VyY2UgZmlsZSwgdGhlIGxpbmUgYW5kXG4gICAqIGNoYXJhY3RlciBhcmUgYmFzZWQgb24gdGhlIGZ1bGwgc291cmNlIGZpbGUgY29udGVudC5cbiAgICovXG4gIGdldENoYXJhY3RlckFuZExpbmVPZlBvc2l0aW9uOiAocG9zOiBudW1iZXIpID0+IExpbmVBbmRDaGFyYWN0ZXI7XG59XG5cbi8qKlxuICogQ29sbGVjdG9yIHRoYXQgY2FuIGJlIHVzZWQgdG8gZmluZCBBbmd1bGFyIHRlbXBsYXRlcyBhbmQgc3R5bGVzaGVldHMgcmVmZXJlbmNlZCB3aXRoaW5cbiAqIGdpdmVuIFR5cGVTY3JpcHQgc291cmNlIGZpbGVzIChpbmxpbmUgb3IgZXh0ZXJuYWwgcmVmZXJlbmNlZCBmaWxlcylcbiAqL1xuZXhwb3J0IGNsYXNzIENvbXBvbmVudFJlc291cmNlQ29sbGVjdG9yIHtcbiAgcmVzb2x2ZWRUZW1wbGF0ZXM6IFJlc29sdmVkUmVzb3VyY2VbXSA9IFtdO1xuICByZXNvbHZlZFN0eWxlc2hlZXRzOiBSZXNvbHZlZFJlc291cmNlW10gPSBbXTtcblxuICBjb25zdHJ1Y3RvcihcbiAgICBwdWJsaWMgdHlwZUNoZWNrZXI6IHRzLlR5cGVDaGVja2VyLFxuICAgIHByaXZhdGUgX2ZpbGVTeXN0ZW06IEZpbGVTeXN0ZW0sXG4gICkge31cblxuICB2aXNpdE5vZGUobm9kZTogdHMuTm9kZSkge1xuICAgIGlmIChub2RlLmtpbmQgPT09IHRzLlN5bnRheEtpbmQuQ2xhc3NEZWNsYXJhdGlvbikge1xuICAgICAgdGhpcy5fdmlzaXRDbGFzc0RlY2xhcmF0aW9uKG5vZGUgYXMgdHMuQ2xhc3NEZWNsYXJhdGlvbik7XG4gICAgfVxuICB9XG5cbiAgcHJpdmF0ZSBfdmlzaXRDbGFzc0RlY2xhcmF0aW9uKG5vZGU6IHRzLkNsYXNzRGVjbGFyYXRpb24pIHtcbiAgICBjb25zdCBkZWNvcmF0b3JzID0gdHMuZ2V0RGVjb3JhdG9ycyhub2RlKTtcblxuICAgIGlmICghZGVjb3JhdG9ycyB8fCAhZGVjb3JhdG9ycy5sZW5ndGgpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBjb25zdCBuZ0RlY29yYXRvcnMgPSBnZXRBbmd1bGFyRGVjb3JhdG9ycyh0aGlzLnR5cGVDaGVja2VyLCBkZWNvcmF0b3JzKTtcbiAgICBjb25zdCBjb21wb25lbnREZWNvcmF0b3IgPSBuZ0RlY29yYXRvcnMuZmluZChkZWMgPT4gZGVjLm5hbWUgPT09ICdDb21wb25lbnQnKTtcblxuICAgIC8vIEluIGNhc2Ugbm8gXCJAQ29tcG9uZW50XCIgZGVjb3JhdG9yIGNvdWxkIGJlIGZvdW5kIG9uIHRoZSBjdXJyZW50IGNsYXNzLCBza2lwLlxuICAgIGlmICghY29tcG9uZW50RGVjb3JhdG9yKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgY29uc3QgZGVjb3JhdG9yQ2FsbCA9IGNvbXBvbmVudERlY29yYXRvci5ub2RlLmV4cHJlc3Npb247XG5cbiAgICAvLyBJbiBjYXNlIHRoZSBjb21wb25lbnQgZGVjb3JhdG9yIGNhbGwgaXMgbm90IHZhbGlkLCBza2lwIHRoaXMgY2xhc3MgZGVjbGFyYXRpb24uXG4gICAgaWYgKGRlY29yYXRvckNhbGwuYXJndW1lbnRzLmxlbmd0aCAhPT0gMSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNvbnN0IGNvbXBvbmVudE1ldGFkYXRhID0gdW53cmFwRXhwcmVzc2lvbihkZWNvcmF0b3JDYWxsLmFyZ3VtZW50c1swXSk7XG5cbiAgICAvLyBFbnN1cmUgdGhhdCB0aGUgY29tcG9uZW50IG1ldGFkYXRhIGlzIGFuIG9iamVjdCBsaXRlcmFsIGV4cHJlc3Npb24uXG4gICAgaWYgKCF0cy5pc09iamVjdExpdGVyYWxFeHByZXNzaW9uKGNvbXBvbmVudE1ldGFkYXRhKSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNvbnN0IHNvdXJjZUZpbGUgPSBub2RlLmdldFNvdXJjZUZpbGUoKTtcbiAgICBjb25zdCBmaWxlUGF0aCA9IHRoaXMuX2ZpbGVTeXN0ZW0ucmVzb2x2ZShzb3VyY2VGaWxlLmZpbGVOYW1lKTtcbiAgICBjb25zdCBzb3VyY2VGaWxlRGlyUGF0aCA9IGRpcm5hbWUoc291cmNlRmlsZS5maWxlTmFtZSk7XG5cbiAgICAvLyBXYWxrIHRocm91Z2ggYWxsIGNvbXBvbmVudCBtZXRhZGF0YSBwcm9wZXJ0aWVzIGFuZCBkZXRlcm1pbmUgdGhlIHJlZmVyZW5jZWRcbiAgICAvLyBIVE1MIHRlbXBsYXRlcyAoZWl0aGVyIGV4dGVybmFsIG9yIGlubGluZSlcbiAgICBjb21wb25lbnRNZXRhZGF0YS5wcm9wZXJ0aWVzLmZvckVhY2gocHJvcGVydHkgPT4ge1xuICAgICAgaWYgKCF0cy5pc1Byb3BlcnR5QXNzaWdubWVudChwcm9wZXJ0eSkpIHtcbiAgICAgICAgcmV0dXJuO1xuICAgICAgfVxuXG4gICAgICBjb25zdCBwcm9wZXJ0eU5hbWUgPSBnZXRQcm9wZXJ0eU5hbWVUZXh0KHByb3BlcnR5Lm5hbWUpO1xuXG4gICAgICBpZiAocHJvcGVydHlOYW1lID09PSAnc3R5bGVzJykge1xuICAgICAgICBjb25zdCBlbGVtZW50cyA9IHRzLmlzQXJyYXlMaXRlcmFsRXhwcmVzc2lvbihwcm9wZXJ0eS5pbml0aWFsaXplcilcbiAgICAgICAgICA/IHByb3BlcnR5LmluaXRpYWxpemVyLmVsZW1lbnRzXG4gICAgICAgICAgOiBbcHJvcGVydHkuaW5pdGlhbGl6ZXJdO1xuXG4gICAgICAgIGVsZW1lbnRzLmZvckVhY2goZWwgPT4ge1xuICAgICAgICAgIGlmICh0cy5pc1N0cmluZ0xpdGVyYWxMaWtlKGVsKSkge1xuICAgICAgICAgICAgLy8gTmVlZCB0byBhZGQgYW4gb2Zmc2V0IG9mIG9uZSB0byB0aGUgc3RhcnQgYmVjYXVzZSB0aGUgdGVtcGxhdGUgcXVvdGVzIGFyZVxuICAgICAgICAgICAgLy8gbm90IHBhcnQgb2YgdGhlIHRlbXBsYXRlIGNvbnRlbnQuXG4gICAgICAgICAgICBjb25zdCB0ZW1wbGF0ZVN0YXJ0SWR4ID0gZWwuZ2V0U3RhcnQoKSArIDE7XG4gICAgICAgICAgICBjb25zdCBjb250ZW50ID0gc3RyaXBCb20oZWwudGV4dCk7XG4gICAgICAgICAgICB0aGlzLnJlc29sdmVkU3R5bGVzaGVldHMucHVzaCh7XG4gICAgICAgICAgICAgIGZpbGVQYXRoLFxuICAgICAgICAgICAgICBjb250YWluZXI6IG5vZGUsXG4gICAgICAgICAgICAgIGNvbnRlbnQsXG4gICAgICAgICAgICAgIGlubGluZTogdHJ1ZSxcbiAgICAgICAgICAgICAgc3RhcnQ6IHRlbXBsYXRlU3RhcnRJZHgsXG4gICAgICAgICAgICAgIGdldENoYXJhY3RlckFuZExpbmVPZlBvc2l0aW9uOiBwb3MgPT5cbiAgICAgICAgICAgICAgICB0cy5nZXRMaW5lQW5kQ2hhcmFjdGVyT2ZQb3NpdGlvbihzb3VyY2VGaWxlLCBwb3MgKyB0ZW1wbGF0ZVN0YXJ0SWR4KSxcbiAgICAgICAgICAgIH0pO1xuICAgICAgICAgIH1cbiAgICAgICAgfSk7XG4gICAgICB9XG5cbiAgICAgIC8vIEluIGNhc2UgdGhlcmUgaXMgYW4gaW5saW5lIHRlbXBsYXRlIHNwZWNpZmllZCwgZW5zdXJlIHRoYXQgdGhlIHZhbHVlIGlzIHN0YXRpY2FsbHlcbiAgICAgIC8vIGFuYWx5emFibGUgYnkgY2hlY2tpbmcgaWYgdGhlIGluaXRpYWxpemVyIGlzIGEgc3RyaW5nIGxpdGVyYWwtbGlrZSBub2RlLlxuICAgICAgaWYgKHByb3BlcnR5TmFtZSA9PT0gJ3RlbXBsYXRlJyAmJiB0cy5pc1N0cmluZ0xpdGVyYWxMaWtlKHByb3BlcnR5LmluaXRpYWxpemVyKSkge1xuICAgICAgICAvLyBOZWVkIHRvIGFkZCBhbiBvZmZzZXQgb2Ygb25lIHRvIHRoZSBzdGFydCBiZWNhdXNlIHRoZSB0ZW1wbGF0ZSBxdW90ZXMgYXJlXG4gICAgICAgIC8vIG5vdCBwYXJ0IG9mIHRoZSB0ZW1wbGF0ZSBjb250ZW50LlxuICAgICAgICBjb25zdCB0ZW1wbGF0ZVN0YXJ0SWR4ID0gcHJvcGVydHkuaW5pdGlhbGl6ZXIuZ2V0U3RhcnQoKSArIDE7XG4gICAgICAgIHRoaXMucmVzb2x2ZWRUZW1wbGF0ZXMucHVzaCh7XG4gICAgICAgICAgZmlsZVBhdGgsXG4gICAgICAgICAgY29udGFpbmVyOiBub2RlLFxuICAgICAgICAgIGNvbnRlbnQ6IHByb3BlcnR5LmluaXRpYWxpemVyLnRleHQsXG4gICAgICAgICAgaW5saW5lOiB0cnVlLFxuICAgICAgICAgIHN0YXJ0OiB0ZW1wbGF0ZVN0YXJ0SWR4LFxuICAgICAgICAgIGdldENoYXJhY3RlckFuZExpbmVPZlBvc2l0aW9uOiBwb3MgPT5cbiAgICAgICAgICAgIHRzLmdldExpbmVBbmRDaGFyYWN0ZXJPZlBvc2l0aW9uKHNvdXJjZUZpbGUsIHBvcyArIHRlbXBsYXRlU3RhcnRJZHgpLFxuICAgICAgICB9KTtcbiAgICAgIH1cblxuICAgICAgaWYgKHByb3BlcnR5TmFtZSA9PT0gJ3N0eWxlVXJscycgJiYgdHMuaXNBcnJheUxpdGVyYWxFeHByZXNzaW9uKHByb3BlcnR5LmluaXRpYWxpemVyKSkge1xuICAgICAgICBwcm9wZXJ0eS5pbml0aWFsaXplci5lbGVtZW50cy5mb3JFYWNoKGVsID0+IHtcbiAgICAgICAgICBpZiAodHMuaXNTdHJpbmdMaXRlcmFsTGlrZShlbCkpIHtcbiAgICAgICAgICAgIHRoaXMuX3RyYWNrRXh0ZXJuYWxTdHlsZXNoZWV0KHNvdXJjZUZpbGVEaXJQYXRoLCBlbCwgbm9kZSk7XG4gICAgICAgICAgfVxuICAgICAgICB9KTtcbiAgICAgIH1cblxuICAgICAgaWYgKHByb3BlcnR5TmFtZSA9PT0gJ3N0eWxlVXJsJyAmJiB0cy5pc1N0cmluZ0xpdGVyYWxMaWtlKHByb3BlcnR5LmluaXRpYWxpemVyKSkge1xuICAgICAgICB0aGlzLl90cmFja0V4dGVybmFsU3R5bGVzaGVldChzb3VyY2VGaWxlRGlyUGF0aCwgcHJvcGVydHkuaW5pdGlhbGl6ZXIsIG5vZGUpO1xuICAgICAgfVxuXG4gICAgICBpZiAocHJvcGVydHlOYW1lID09PSAndGVtcGxhdGVVcmwnICYmIHRzLmlzU3RyaW5nTGl0ZXJhbExpa2UocHJvcGVydHkuaW5pdGlhbGl6ZXIpKSB7XG4gICAgICAgIGNvbnN0IHRlbXBsYXRlVXJsID0gcHJvcGVydHkuaW5pdGlhbGl6ZXIudGV4dDtcbiAgICAgICAgY29uc3QgdGVtcGxhdGVQYXRoID0gdGhpcy5fZmlsZVN5c3RlbS5yZXNvbHZlKHNvdXJjZUZpbGVEaXJQYXRoLCB0ZW1wbGF0ZVVybCk7XG5cbiAgICAgICAgLy8gSW4gY2FzZSB0aGUgdGVtcGxhdGUgZG9lcyBub3QgZXhpc3QgaW4gdGhlIGZpbGUgc3lzdGVtLCBza2lwIHRoaXNcbiAgICAgICAgLy8gZXh0ZXJuYWwgdGVtcGxhdGUuXG4gICAgICAgIGlmICghdGhpcy5fZmlsZVN5c3RlbS5maWxlRXhpc3RzKHRlbXBsYXRlUGF0aCkpIHtcbiAgICAgICAgICByZXR1cm47XG4gICAgICAgIH1cblxuICAgICAgICBjb25zdCBmaWxlQ29udGVudCA9IHN0cmlwQm9tKHRoaXMuX2ZpbGVTeXN0ZW0ucmVhZCh0ZW1wbGF0ZVBhdGgpIHx8ICcnKTtcblxuICAgICAgICBpZiAoZmlsZUNvbnRlbnQpIHtcbiAgICAgICAgICBjb25zdCBsaW5lU3RhcnRzTWFwID0gY29tcHV0ZUxpbmVTdGFydHNNYXAoZmlsZUNvbnRlbnQpO1xuXG4gICAgICAgICAgdGhpcy5yZXNvbHZlZFRlbXBsYXRlcy5wdXNoKHtcbiAgICAgICAgICAgIGZpbGVQYXRoOiB0ZW1wbGF0ZVBhdGgsXG4gICAgICAgICAgICBjb250YWluZXI6IG5vZGUsXG4gICAgICAgICAgICBjb250ZW50OiBmaWxlQ29udGVudCxcbiAgICAgICAgICAgIGlubGluZTogZmFsc2UsXG4gICAgICAgICAgICBzdGFydDogMCxcbiAgICAgICAgICAgIGdldENoYXJhY3RlckFuZExpbmVPZlBvc2l0aW9uOiBwID0+IGdldExpbmVBbmRDaGFyYWN0ZXJGcm9tUG9zaXRpb24obGluZVN0YXJ0c01hcCwgcCksXG4gICAgICAgICAgfSk7XG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9KTtcbiAgfVxuXG4gIC8qKiBSZXNvbHZlcyBhbiBleHRlcm5hbCBzdHlsZXNoZWV0IGJ5IHJlYWRpbmcgaXRzIGNvbnRlbnQgYW5kIGNvbXB1dGluZyBsaW5lIG1hcHBpbmdzLiAqL1xuICByZXNvbHZlRXh0ZXJuYWxTdHlsZXNoZWV0KFxuICAgIGZpbGVQYXRoOiBXb3Jrc3BhY2VQYXRoLFxuICAgIGNvbnRhaW5lcjogdHMuQ2xhc3NEZWNsYXJhdGlvbiB8IG51bGwsXG4gICk6IFJlc29sdmVkUmVzb3VyY2UgfCBudWxsIHtcbiAgICAvLyBTdHJpcCB0aGUgQk9NIHRvIGF2b2lkIGlzc3VlcyB3aXRoIHRoZSBTYXNzIGNvbXBpbGVyLiBTZWU6XG4gICAgLy8gaHR0cHM6Ly9naXRodWIuY29tL2FuZ3VsYXIvY29tcG9uZW50cy9pc3N1ZXMvMjQyMjcjaXNzdWVjb21tZW50LTEyMDA5MzQyNThcbiAgICBjb25zdCBmaWxlQ29udGVudCA9IHN0cmlwQm9tKHRoaXMuX2ZpbGVTeXN0ZW0ucmVhZChmaWxlUGF0aCkgfHwgJycpO1xuXG4gICAgaWYgKCFmaWxlQ29udGVudCkge1xuICAgICAgcmV0dXJuIG51bGw7XG4gICAgfVxuXG4gICAgY29uc3QgbGluZVN0YXJ0c01hcCA9IGNvbXB1dGVMaW5lU3RhcnRzTWFwKGZpbGVDb250ZW50KTtcblxuICAgIHJldHVybiB7XG4gICAgICBmaWxlUGF0aDogZmlsZVBhdGgsXG4gICAgICBjb250YWluZXI6IGNvbnRhaW5lcixcbiAgICAgIGNvbnRlbnQ6IGZpbGVDb250ZW50LFxuICAgICAgaW5saW5lOiBmYWxzZSxcbiAgICAgIHN0YXJ0OiAwLFxuICAgICAgZ2V0Q2hhcmFjdGVyQW5kTGluZU9mUG9zaXRpb246IHBvcyA9PiBnZXRMaW5lQW5kQ2hhcmFjdGVyRnJvbVBvc2l0aW9uKGxpbmVTdGFydHNNYXAsIHBvcyksXG4gICAgfTtcbiAgfVxuXG4gIHByaXZhdGUgX3RyYWNrRXh0ZXJuYWxTdHlsZXNoZWV0KFxuICAgIHNvdXJjZUZpbGVEaXJQYXRoOiBzdHJpbmcsXG4gICAgbm9kZTogdHMuU3RyaW5nTGl0ZXJhbExpa2UsXG4gICAgY29udGFpbmVyOiB0cy5DbGFzc0RlY2xhcmF0aW9uLFxuICApIHtcbiAgICBjb25zdCBzdHlsZXNoZWV0UGF0aCA9IHRoaXMuX2ZpbGVTeXN0ZW0ucmVzb2x2ZShzb3VyY2VGaWxlRGlyUGF0aCwgbm9kZS50ZXh0KTtcbiAgICBjb25zdCBzdHlsZXNoZWV0ID0gdGhpcy5yZXNvbHZlRXh0ZXJuYWxTdHlsZXNoZWV0KHN0eWxlc2hlZXRQYXRoLCBjb250YWluZXIpO1xuXG4gICAgaWYgKHN0eWxlc2hlZXQpIHtcbiAgICAgIHRoaXMucmVzb2x2ZWRTdHlsZXNoZWV0cy5wdXNoKHN0eWxlc2hlZXQpO1xuICAgIH1cbiAgfVxufVxuXG4vKiogU3RyaXBzIHRoZSBCT00gZnJvbSBhIHN0cmluZy4gKi9cbmZ1bmN0aW9uIHN0cmlwQm9tKGNvbnRlbnQ6IHN0cmluZyk6IHN0cmluZyB7XG4gIHJldHVybiBjb250ZW50LnJlcGxhY2UoL1xcdUZFRkYvZywgJycpO1xufVxuIl19