"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.AttributeSelectorsMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const literal_1 = require("../typescript/literal");
const upgrade_data_1 = require("../upgrade-data");
/**
 * Migration that walks through every string literal, template and stylesheet
 * in order to switch deprecated attribute selectors to the updated selector.
 */
class AttributeSelectorsMigration extends migration_1.Migration {
    /** Required upgrade changes for specified target version. */
    data = (0, upgrade_data_1.getVersionUpgradeData)(this, 'attributeSelectors');
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (ts.isStringLiteralLike(node)) {
            this._visitStringLiteralLike(node);
        }
    }
    visitTemplate(template) {
        this.data.forEach(selector => {
            (0, literal_1.findAllSubstringIndices)(template.content, selector.replace)
                .map(offset => template.start + offset)
                .forEach(start => this._replaceSelector(template.filePath, start, selector));
        });
    }
    visitStylesheet(stylesheet) {
        this.data.forEach(selector => {
            const currentSelector = `[${selector.replace}]`;
            const updatedSelector = `[${selector.replaceWith}]`;
            (0, literal_1.findAllSubstringIndices)(stylesheet.content, currentSelector)
                .map(offset => stylesheet.start + offset)
                .forEach(start => this._replaceSelector(stylesheet.filePath, start, {
                replace: currentSelector,
                replaceWith: updatedSelector,
            }));
        });
    }
    _visitStringLiteralLike(literal) {
        if (literal.parent && literal.parent.kind !== ts.SyntaxKind.CallExpression) {
            return;
        }
        const literalText = literal.getText();
        const filePath = this.fileSystem.resolve(literal.getSourceFile().fileName);
        this.data.forEach(selector => {
            (0, literal_1.findAllSubstringIndices)(literalText, selector.replace)
                .map(offset => literal.getStart() + offset)
                .forEach(start => this._replaceSelector(filePath, start, selector));
        });
    }
    _replaceSelector(filePath, start, data) {
        this.fileSystem
            .edit(filePath)
            .remove(start, data.replace.length)
            .insertRight(start, data.replaceWith);
    }
}
exports.AttributeSelectorsMigration = AttributeSelectorsMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiYXR0cmlidXRlLXNlbGVjdG9ycy5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy9uZy11cGRhdGUvbWlncmF0aW9ucy9hdHRyaWJ1dGUtc2VsZWN0b3JzLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7OztBQUVILGlDQUFpQztBQUdqQywyREFBc0Q7QUFFdEQsbURBQThEO0FBQzlELGtEQUFtRTtBQUVuRTs7O0dBR0c7QUFDSCxNQUFhLDJCQUE0QixTQUFRLHFCQUFzQjtJQUNyRSw2REFBNkQ7SUFDN0QsSUFBSSxHQUFHLElBQUEsb0NBQXFCLEVBQUMsSUFBSSxFQUFFLG9CQUFvQixDQUFDLENBQUM7SUFFekQsMkRBQTJEO0lBQzNELE9BQU8sR0FBWSxJQUFJLENBQUMsSUFBSSxDQUFDLE1BQU0sS0FBSyxDQUFDLENBQUM7SUFFakMsU0FBUyxDQUFDLElBQWE7UUFDOUIsSUFBSSxFQUFFLENBQUMsbUJBQW1CLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQztZQUNqQyxJQUFJLENBQUMsdUJBQXVCLENBQUMsSUFBSSxDQUFDLENBQUM7UUFDckMsQ0FBQztJQUNILENBQUM7SUFFUSxhQUFhLENBQUMsUUFBMEI7UUFDL0MsSUFBSSxDQUFDLElBQUksQ0FBQyxPQUFPLENBQUMsUUFBUSxDQUFDLEVBQUU7WUFDM0IsSUFBQSxpQ0FBdUIsRUFBQyxRQUFRLENBQUMsT0FBTyxFQUFFLFFBQVEsQ0FBQyxPQUFPLENBQUM7aUJBQ3hELEdBQUcsQ0FBQyxNQUFNLENBQUMsRUFBRSxDQUFDLFFBQVEsQ0FBQyxLQUFLLEdBQUcsTUFBTSxDQUFDO2lCQUN0QyxPQUFPLENBQUMsS0FBSyxDQUFDLEVBQUUsQ0FBQyxJQUFJLENBQUMsZ0JBQWdCLENBQUMsUUFBUSxDQUFDLFFBQVEsRUFBRSxLQUFLLEVBQUUsUUFBUSxDQUFDLENBQUMsQ0FBQztRQUNqRixDQUFDLENBQUMsQ0FBQztJQUNMLENBQUM7SUFFUSxlQUFlLENBQUMsVUFBNEI7UUFDbkQsSUFBSSxDQUFDLElBQUksQ0FBQyxPQUFPLENBQUMsUUFBUSxDQUFDLEVBQUU7WUFDM0IsTUFBTSxlQUFlLEdBQUcsSUFBSSxRQUFRLENBQUMsT0FBTyxHQUFHLENBQUM7WUFDaEQsTUFBTSxlQUFlLEdBQUcsSUFBSSxRQUFRLENBQUMsV0FBVyxHQUFHLENBQUM7WUFFcEQsSUFBQSxpQ0FBdUIsRUFBQyxVQUFVLENBQUMsT0FBTyxFQUFFLGVBQWUsQ0FBQztpQkFDekQsR0FBRyxDQUFDLE1BQU0sQ0FBQyxFQUFFLENBQUMsVUFBVSxDQUFDLEtBQUssR0FBRyxNQUFNLENBQUM7aUJBQ3hDLE9BQU8sQ0FBQyxLQUFLLENBQUMsRUFBRSxDQUNmLElBQUksQ0FBQyxnQkFBZ0IsQ0FBQyxVQUFVLENBQUMsUUFBUSxFQUFFLEtBQUssRUFBRTtnQkFDaEQsT0FBTyxFQUFFLGVBQWU7Z0JBQ3hCLFdBQVcsRUFBRSxlQUFlO2FBQzdCLENBQUMsQ0FDSCxDQUFDO1FBQ04sQ0FBQyxDQUFDLENBQUM7SUFDTCxDQUFDO0lBRU8sdUJBQXVCLENBQUMsT0FBNkI7UUFDM0QsSUFBSSxPQUFPLENBQUMsTUFBTSxJQUFJLE9BQU8sQ0FBQyxNQUFNLENBQUMsSUFBSSxLQUFLLEVBQUUsQ0FBQyxVQUFVLENBQUMsY0FBYyxFQUFFLENBQUM7WUFDM0UsT0FBTztRQUNULENBQUM7UUFFRCxNQUFNLFdBQVcsR0FBRyxPQUFPLENBQUMsT0FBTyxFQUFFLENBQUM7UUFDdEMsTUFBTSxRQUFRLEdBQUcsSUFBSSxDQUFDLFVBQVUsQ0FBQyxPQUFPLENBQUMsT0FBTyxDQUFDLGFBQWEsRUFBRSxDQUFDLFFBQVEsQ0FBQyxDQUFDO1FBRTNFLElBQUksQ0FBQyxJQUFJLENBQUMsT0FBTyxDQUFDLFFBQVEsQ0FBQyxFQUFFO1lBQzNCLElBQUEsaUNBQXVCLEVBQUMsV0FBVyxFQUFFLFFBQVEsQ0FBQyxPQUFPLENBQUM7aUJBQ25ELEdBQUcsQ0FBQyxNQUFNLENBQUMsRUFBRSxDQUFDLE9BQU8sQ0FBQyxRQUFRLEVBQUUsR0FBRyxNQUFNLENBQUM7aUJBQzFDLE9BQU8sQ0FBQyxLQUFLLENBQUMsRUFBRSxDQUFDLElBQUksQ0FBQyxnQkFBZ0IsQ0FBQyxRQUFRLEVBQUUsS0FBSyxFQUFFLFFBQVEsQ0FBQyxDQUFDLENBQUM7UUFDeEUsQ0FBQyxDQUFDLENBQUM7SUFDTCxDQUFDO0lBRU8sZ0JBQWdCLENBQ3RCLFFBQXVCLEVBQ3ZCLEtBQWEsRUFDYixJQUFrQztRQUVsQyxJQUFJLENBQUMsVUFBVTthQUNaLElBQUksQ0FBQyxRQUFRLENBQUM7YUFDZCxNQUFNLENBQUMsS0FBSyxFQUFFLElBQUksQ0FBQyxPQUFPLENBQUMsTUFBTSxDQUFDO2FBQ2xDLFdBQVcsQ0FBQyxLQUFLLEVBQUUsSUFBSSxDQUFDLFdBQVcsQ0FBQyxDQUFDO0lBQzFDLENBQUM7Q0FDRjtBQTlERCxrRUE4REMiLCJzb3VyY2VzQ29udGVudCI6WyIvKipcbiAqIEBsaWNlbnNlXG4gKiBDb3B5cmlnaHQgR29vZ2xlIExMQyBBbGwgUmlnaHRzIFJlc2VydmVkLlxuICpcbiAqIFVzZSBvZiB0aGlzIHNvdXJjZSBjb2RlIGlzIGdvdmVybmVkIGJ5IGFuIE1JVC1zdHlsZSBsaWNlbnNlIHRoYXQgY2FuIGJlXG4gKiBmb3VuZCBpbiB0aGUgTElDRU5TRSBmaWxlIGF0IGh0dHBzOi8vYW5ndWxhci5pby9saWNlbnNlXG4gKi9cblxuaW1wb3J0ICogYXMgdHMgZnJvbSAndHlwZXNjcmlwdCc7XG5pbXBvcnQge1dvcmtzcGFjZVBhdGh9IGZyb20gJy4uLy4uL3VwZGF0ZS10b29sL2ZpbGUtc3lzdGVtJztcbmltcG9ydCB7UmVzb2x2ZWRSZXNvdXJjZX0gZnJvbSAnLi4vLi4vdXBkYXRlLXRvb2wvY29tcG9uZW50LXJlc291cmNlLWNvbGxlY3Rvcic7XG5pbXBvcnQge01pZ3JhdGlvbn0gZnJvbSAnLi4vLi4vdXBkYXRlLXRvb2wvbWlncmF0aW9uJztcbmltcG9ydCB7QXR0cmlidXRlU2VsZWN0b3JVcGdyYWRlRGF0YX0gZnJvbSAnLi4vZGF0YS9hdHRyaWJ1dGUtc2VsZWN0b3JzJztcbmltcG9ydCB7ZmluZEFsbFN1YnN0cmluZ0luZGljZXN9IGZyb20gJy4uL3R5cGVzY3JpcHQvbGl0ZXJhbCc7XG5pbXBvcnQge2dldFZlcnNpb25VcGdyYWRlRGF0YSwgVXBncmFkZURhdGF9IGZyb20gJy4uL3VwZ3JhZGUtZGF0YSc7XG5cbi8qKlxuICogTWlncmF0aW9uIHRoYXQgd2Fsa3MgdGhyb3VnaCBldmVyeSBzdHJpbmcgbGl0ZXJhbCwgdGVtcGxhdGUgYW5kIHN0eWxlc2hlZXRcbiAqIGluIG9yZGVyIHRvIHN3aXRjaCBkZXByZWNhdGVkIGF0dHJpYnV0ZSBzZWxlY3RvcnMgdG8gdGhlIHVwZGF0ZWQgc2VsZWN0b3IuXG4gKi9cbmV4cG9ydCBjbGFzcyBBdHRyaWJ1dGVTZWxlY3RvcnNNaWdyYXRpb24gZXh0ZW5kcyBNaWdyYXRpb248VXBncmFkZURhdGE+IHtcbiAgLyoqIFJlcXVpcmVkIHVwZ3JhZGUgY2hhbmdlcyBmb3Igc3BlY2lmaWVkIHRhcmdldCB2ZXJzaW9uLiAqL1xuICBkYXRhID0gZ2V0VmVyc2lvblVwZ3JhZGVEYXRhKHRoaXMsICdhdHRyaWJ1dGVTZWxlY3RvcnMnKTtcblxuICAvLyBPbmx5IGVuYWJsZSB0aGUgbWlncmF0aW9uIHJ1bGUgaWYgdGhlcmUgaXMgdXBncmFkZSBkYXRhLlxuICBlbmFibGVkOiBib29sZWFuID0gdGhpcy5kYXRhLmxlbmd0aCAhPT0gMDtcblxuICBvdmVycmlkZSB2aXNpdE5vZGUobm9kZTogdHMuTm9kZSkge1xuICAgIGlmICh0cy5pc1N0cmluZ0xpdGVyYWxMaWtlKG5vZGUpKSB7XG4gICAgICB0aGlzLl92aXNpdFN0cmluZ0xpdGVyYWxMaWtlKG5vZGUpO1xuICAgIH1cbiAgfVxuXG4gIG92ZXJyaWRlIHZpc2l0VGVtcGxhdGUodGVtcGxhdGU6IFJlc29sdmVkUmVzb3VyY2UpIHtcbiAgICB0aGlzLmRhdGEuZm9yRWFjaChzZWxlY3RvciA9PiB7XG4gICAgICBmaW5kQWxsU3Vic3RyaW5nSW5kaWNlcyh0ZW1wbGF0ZS5jb250ZW50LCBzZWxlY3Rvci5yZXBsYWNlKVxuICAgICAgICAubWFwKG9mZnNldCA9PiB0ZW1wbGF0ZS5zdGFydCArIG9mZnNldClcbiAgICAgICAgLmZvckVhY2goc3RhcnQgPT4gdGhpcy5fcmVwbGFjZVNlbGVjdG9yKHRlbXBsYXRlLmZpbGVQYXRoLCBzdGFydCwgc2VsZWN0b3IpKTtcbiAgICB9KTtcbiAgfVxuXG4gIG92ZXJyaWRlIHZpc2l0U3R5bGVzaGVldChzdHlsZXNoZWV0OiBSZXNvbHZlZFJlc291cmNlKTogdm9pZCB7XG4gICAgdGhpcy5kYXRhLmZvckVhY2goc2VsZWN0b3IgPT4ge1xuICAgICAgY29uc3QgY3VycmVudFNlbGVjdG9yID0gYFske3NlbGVjdG9yLnJlcGxhY2V9XWA7XG4gICAgICBjb25zdCB1cGRhdGVkU2VsZWN0b3IgPSBgWyR7c2VsZWN0b3IucmVwbGFjZVdpdGh9XWA7XG5cbiAgICAgIGZpbmRBbGxTdWJzdHJpbmdJbmRpY2VzKHN0eWxlc2hlZXQuY29udGVudCwgY3VycmVudFNlbGVjdG9yKVxuICAgICAgICAubWFwKG9mZnNldCA9PiBzdHlsZXNoZWV0LnN0YXJ0ICsgb2Zmc2V0KVxuICAgICAgICAuZm9yRWFjaChzdGFydCA9PlxuICAgICAgICAgIHRoaXMuX3JlcGxhY2VTZWxlY3RvcihzdHlsZXNoZWV0LmZpbGVQYXRoLCBzdGFydCwge1xuICAgICAgICAgICAgcmVwbGFjZTogY3VycmVudFNlbGVjdG9yLFxuICAgICAgICAgICAgcmVwbGFjZVdpdGg6IHVwZGF0ZWRTZWxlY3RvcixcbiAgICAgICAgICB9KSxcbiAgICAgICAgKTtcbiAgICB9KTtcbiAgfVxuXG4gIHByaXZhdGUgX3Zpc2l0U3RyaW5nTGl0ZXJhbExpa2UobGl0ZXJhbDogdHMuU3RyaW5nTGl0ZXJhbExpa2UpIHtcbiAgICBpZiAobGl0ZXJhbC5wYXJlbnQgJiYgbGl0ZXJhbC5wYXJlbnQua2luZCAhPT0gdHMuU3ludGF4S2luZC5DYWxsRXhwcmVzc2lvbikge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIGNvbnN0IGxpdGVyYWxUZXh0ID0gbGl0ZXJhbC5nZXRUZXh0KCk7XG4gICAgY29uc3QgZmlsZVBhdGggPSB0aGlzLmZpbGVTeXN0ZW0ucmVzb2x2ZShsaXRlcmFsLmdldFNvdXJjZUZpbGUoKS5maWxlTmFtZSk7XG5cbiAgICB0aGlzLmRhdGEuZm9yRWFjaChzZWxlY3RvciA9PiB7XG4gICAgICBmaW5kQWxsU3Vic3RyaW5nSW5kaWNlcyhsaXRlcmFsVGV4dCwgc2VsZWN0b3IucmVwbGFjZSlcbiAgICAgICAgLm1hcChvZmZzZXQgPT4gbGl0ZXJhbC5nZXRTdGFydCgpICsgb2Zmc2V0KVxuICAgICAgICAuZm9yRWFjaChzdGFydCA9PiB0aGlzLl9yZXBsYWNlU2VsZWN0b3IoZmlsZVBhdGgsIHN0YXJ0LCBzZWxlY3RvcikpO1xuICAgIH0pO1xuICB9XG5cbiAgcHJpdmF0ZSBfcmVwbGFjZVNlbGVjdG9yKFxuICAgIGZpbGVQYXRoOiBXb3Jrc3BhY2VQYXRoLFxuICAgIHN0YXJ0OiBudW1iZXIsXG4gICAgZGF0YTogQXR0cmlidXRlU2VsZWN0b3JVcGdyYWRlRGF0YSxcbiAgKSB7XG4gICAgdGhpcy5maWxlU3lzdGVtXG4gICAgICAuZWRpdChmaWxlUGF0aClcbiAgICAgIC5yZW1vdmUoc3RhcnQsIGRhdGEucmVwbGFjZS5sZW5ndGgpXG4gICAgICAuaW5zZXJ0UmlnaHQoc3RhcnQsIGRhdGEucmVwbGFjZVdpdGgpO1xuICB9XG59XG4iXX0=