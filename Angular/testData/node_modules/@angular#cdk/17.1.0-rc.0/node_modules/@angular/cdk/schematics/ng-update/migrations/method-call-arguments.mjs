"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.MethodCallArgumentsMigration = void 0;
const ts = require("typescript");
const migration_1 = require("../../update-tool/migration");
const upgrade_data_1 = require("../upgrade-data");
/**
 * Migration that visits every TypeScript method call expression and checks if the
 * argument count is invalid and needs to be *manually* updated.
 */
class MethodCallArgumentsMigration extends migration_1.Migration {
    /** Change data that upgrades to the specified target version. */
    data = (0, upgrade_data_1.getVersionUpgradeData)(this, 'methodCallChecks');
    // Only enable the migration rule if there is upgrade data.
    enabled = this.data.length !== 0;
    visitNode(node) {
        if (ts.isCallExpression(node) && ts.isPropertyAccessExpression(node.expression)) {
            this._checkPropertyAccessMethodCall(node);
        }
    }
    _checkPropertyAccessMethodCall(node) {
        const propertyAccess = node.expression;
        if (!ts.isIdentifier(propertyAccess.name)) {
            return;
        }
        const hostType = this.typeChecker.getTypeAtLocation(propertyAccess.expression);
        const hostTypeName = hostType.symbol && hostType.symbol.name;
        const methodName = propertyAccess.name.text;
        if (!hostTypeName) {
            return;
        }
        // TODO(devversion): Revisit the implementation of this upgrade rule. It seems difficult
        // and ambiguous to maintain the data for this rule. e.g. consider a method which has the
        // same amount of arguments but just had a type change. In that case we could still add
        // new entries to the upgrade data that match the current argument length to just show
        // a failure message, but adding that data becomes painful if the method has optional
        // parameters and it would mean that the error message would always show up, even if the
        // argument is in some cases still assignable to the new parameter type. We could re-use
        // the logic we have in the constructor-signature checks to check for assignability and
        // to make the upgrade data less verbose.
        const failure = this.data
            .filter(data => data.method === methodName && data.className === hostTypeName)
            .map(data => data.invalidArgCounts.find(f => f.count === node.arguments.length))[0];
        if (!failure) {
            return;
        }
        this.createFailureAtNode(node, `Found call to "${hostTypeName + '.' + methodName}" ` +
            `with ${failure.count} arguments. Message: ${failure.message}`);
    }
}
exports.MethodCallArgumentsMigration = MethodCallArgumentsMigration;
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoibWV0aG9kLWNhbGwtYXJndW1lbnRzLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXMiOlsiLi4vLi4vLi4vLi4vLi4vLi4vLi4vLi4vc3JjL2Nkay9zY2hlbWF0aWNzL25nLXVwZGF0ZS9taWdyYXRpb25zL21ldGhvZC1jYWxsLWFyZ3VtZW50cy50cyJdLCJuYW1lcyI6W10sIm1hcHBpbmdzIjoiO0FBQUE7Ozs7OztHQU1HOzs7QUFFSCxpQ0FBaUM7QUFDakMsMkRBQXNEO0FBR3RELGtEQUFtRTtBQUVuRTs7O0dBR0c7QUFDSCxNQUFhLDRCQUE2QixTQUFRLHFCQUFzQjtJQUN0RSxpRUFBaUU7SUFDakUsSUFBSSxHQUE0QixJQUFBLG9DQUFxQixFQUFDLElBQUksRUFBRSxrQkFBa0IsQ0FBQyxDQUFDO0lBRWhGLDJEQUEyRDtJQUMzRCxPQUFPLEdBQUcsSUFBSSxDQUFDLElBQUksQ0FBQyxNQUFNLEtBQUssQ0FBQyxDQUFDO0lBRXhCLFNBQVMsQ0FBQyxJQUFhO1FBQzlCLElBQUksRUFBRSxDQUFDLGdCQUFnQixDQUFDLElBQUksQ0FBQyxJQUFJLEVBQUUsQ0FBQywwQkFBMEIsQ0FBQyxJQUFJLENBQUMsVUFBVSxDQUFDLEVBQUUsQ0FBQztZQUNoRixJQUFJLENBQUMsOEJBQThCLENBQUMsSUFBSSxDQUFDLENBQUM7UUFDNUMsQ0FBQztJQUNILENBQUM7SUFFTyw4QkFBOEIsQ0FBQyxJQUF1QjtRQUM1RCxNQUFNLGNBQWMsR0FBRyxJQUFJLENBQUMsVUFBeUMsQ0FBQztRQUV0RSxJQUFJLENBQUMsRUFBRSxDQUFDLFlBQVksQ0FBQyxjQUFjLENBQUMsSUFBSSxDQUFDLEVBQUUsQ0FBQztZQUMxQyxPQUFPO1FBQ1QsQ0FBQztRQUVELE1BQU0sUUFBUSxHQUFHLElBQUksQ0FBQyxXQUFXLENBQUMsaUJBQWlCLENBQUMsY0FBYyxDQUFDLFVBQVUsQ0FBQyxDQUFDO1FBQy9FLE1BQU0sWUFBWSxHQUFHLFFBQVEsQ0FBQyxNQUFNLElBQUksUUFBUSxDQUFDLE1BQU0sQ0FBQyxJQUFJLENBQUM7UUFDN0QsTUFBTSxVQUFVLEdBQUcsY0FBYyxDQUFDLElBQUksQ0FBQyxJQUFJLENBQUM7UUFFNUMsSUFBSSxDQUFDLFlBQVksRUFBRSxDQUFDO1lBQ2xCLE9BQU87UUFDVCxDQUFDO1FBRUQsd0ZBQXdGO1FBQ3hGLHlGQUF5RjtRQUN6Rix1RkFBdUY7UUFDdkYsc0ZBQXNGO1FBQ3RGLHFGQUFxRjtRQUNyRix3RkFBd0Y7UUFDeEYsd0ZBQXdGO1FBQ3hGLHVGQUF1RjtRQUN2Rix5Q0FBeUM7UUFDekMsTUFBTSxPQUFPLEdBQUcsSUFBSSxDQUFDLElBQUk7YUFDdEIsTUFBTSxDQUFDLElBQUksQ0FBQyxFQUFFLENBQUMsSUFBSSxDQUFDLE1BQU0sS0FBSyxVQUFVLElBQUksSUFBSSxDQUFDLFNBQVMsS0FBSyxZQUFZLENBQUM7YUFDN0UsR0FBRyxDQUFDLElBQUksQ0FBQyxFQUFFLENBQUMsSUFBSSxDQUFDLGdCQUFnQixDQUFDLElBQUksQ0FBQyxDQUFDLENBQUMsRUFBRSxDQUFDLENBQUMsQ0FBQyxLQUFLLEtBQUssSUFBSSxDQUFDLFNBQVMsQ0FBQyxNQUFNLENBQUMsQ0FBQyxDQUFDLENBQUMsQ0FBQyxDQUFDO1FBRXRGLElBQUksQ0FBQyxPQUFPLEVBQUUsQ0FBQztZQUNiLE9BQU87UUFDVCxDQUFDO1FBRUQsSUFBSSxDQUFDLG1CQUFtQixDQUN0QixJQUFJLEVBQ0osa0JBQWtCLFlBQVksR0FBRyxHQUFHLEdBQUcsVUFBVSxJQUFJO1lBQ25ELFFBQVEsT0FBTyxDQUFDLEtBQUssd0JBQXdCLE9BQU8sQ0FBQyxPQUFPLEVBQUUsQ0FDakUsQ0FBQztJQUNKLENBQUM7Q0FDRjtBQW5ERCxvRUFtREMiLCJzb3VyY2VzQ29udGVudCI6WyIvKipcbiAqIEBsaWNlbnNlXG4gKiBDb3B5cmlnaHQgR29vZ2xlIExMQyBBbGwgUmlnaHRzIFJlc2VydmVkLlxuICpcbiAqIFVzZSBvZiB0aGlzIHNvdXJjZSBjb2RlIGlzIGdvdmVybmVkIGJ5IGFuIE1JVC1zdHlsZSBsaWNlbnNlIHRoYXQgY2FuIGJlXG4gKiBmb3VuZCBpbiB0aGUgTElDRU5TRSBmaWxlIGF0IGh0dHBzOi8vYW5ndWxhci5pby9saWNlbnNlXG4gKi9cblxuaW1wb3J0ICogYXMgdHMgZnJvbSAndHlwZXNjcmlwdCc7XG5pbXBvcnQge01pZ3JhdGlvbn0gZnJvbSAnLi4vLi4vdXBkYXRlLXRvb2wvbWlncmF0aW9uJztcblxuaW1wb3J0IHtNZXRob2RDYWxsVXBncmFkZURhdGF9IGZyb20gJy4uL2RhdGEnO1xuaW1wb3J0IHtnZXRWZXJzaW9uVXBncmFkZURhdGEsIFVwZ3JhZGVEYXRhfSBmcm9tICcuLi91cGdyYWRlLWRhdGEnO1xuXG4vKipcbiAqIE1pZ3JhdGlvbiB0aGF0IHZpc2l0cyBldmVyeSBUeXBlU2NyaXB0IG1ldGhvZCBjYWxsIGV4cHJlc3Npb24gYW5kIGNoZWNrcyBpZiB0aGVcbiAqIGFyZ3VtZW50IGNvdW50IGlzIGludmFsaWQgYW5kIG5lZWRzIHRvIGJlICptYW51YWxseSogdXBkYXRlZC5cbiAqL1xuZXhwb3J0IGNsYXNzIE1ldGhvZENhbGxBcmd1bWVudHNNaWdyYXRpb24gZXh0ZW5kcyBNaWdyYXRpb248VXBncmFkZURhdGE+IHtcbiAgLyoqIENoYW5nZSBkYXRhIHRoYXQgdXBncmFkZXMgdG8gdGhlIHNwZWNpZmllZCB0YXJnZXQgdmVyc2lvbi4gKi9cbiAgZGF0YTogTWV0aG9kQ2FsbFVwZ3JhZGVEYXRhW10gPSBnZXRWZXJzaW9uVXBncmFkZURhdGEodGhpcywgJ21ldGhvZENhbGxDaGVja3MnKTtcblxuICAvLyBPbmx5IGVuYWJsZSB0aGUgbWlncmF0aW9uIHJ1bGUgaWYgdGhlcmUgaXMgdXBncmFkZSBkYXRhLlxuICBlbmFibGVkID0gdGhpcy5kYXRhLmxlbmd0aCAhPT0gMDtcblxuICBvdmVycmlkZSB2aXNpdE5vZGUobm9kZTogdHMuTm9kZSk6IHZvaWQge1xuICAgIGlmICh0cy5pc0NhbGxFeHByZXNzaW9uKG5vZGUpICYmIHRzLmlzUHJvcGVydHlBY2Nlc3NFeHByZXNzaW9uKG5vZGUuZXhwcmVzc2lvbikpIHtcbiAgICAgIHRoaXMuX2NoZWNrUHJvcGVydHlBY2Nlc3NNZXRob2RDYWxsKG5vZGUpO1xuICAgIH1cbiAgfVxuXG4gIHByaXZhdGUgX2NoZWNrUHJvcGVydHlBY2Nlc3NNZXRob2RDYWxsKG5vZGU6IHRzLkNhbGxFeHByZXNzaW9uKSB7XG4gICAgY29uc3QgcHJvcGVydHlBY2Nlc3MgPSBub2RlLmV4cHJlc3Npb24gYXMgdHMuUHJvcGVydHlBY2Nlc3NFeHByZXNzaW9uO1xuXG4gICAgaWYgKCF0cy5pc0lkZW50aWZpZXIocHJvcGVydHlBY2Nlc3MubmFtZSkpIHtcbiAgICAgIHJldHVybjtcbiAgICB9XG5cbiAgICBjb25zdCBob3N0VHlwZSA9IHRoaXMudHlwZUNoZWNrZXIuZ2V0VHlwZUF0TG9jYXRpb24ocHJvcGVydHlBY2Nlc3MuZXhwcmVzc2lvbik7XG4gICAgY29uc3QgaG9zdFR5cGVOYW1lID0gaG9zdFR5cGUuc3ltYm9sICYmIGhvc3RUeXBlLnN5bWJvbC5uYW1lO1xuICAgIGNvbnN0IG1ldGhvZE5hbWUgPSBwcm9wZXJ0eUFjY2Vzcy5uYW1lLnRleHQ7XG5cbiAgICBpZiAoIWhvc3RUeXBlTmFtZSkge1xuICAgICAgcmV0dXJuO1xuICAgIH1cblxuICAgIC8vIFRPRE8oZGV2dmVyc2lvbik6IFJldmlzaXQgdGhlIGltcGxlbWVudGF0aW9uIG9mIHRoaXMgdXBncmFkZSBydWxlLiBJdCBzZWVtcyBkaWZmaWN1bHRcbiAgICAvLyBhbmQgYW1iaWd1b3VzIHRvIG1haW50YWluIHRoZSBkYXRhIGZvciB0aGlzIHJ1bGUuIGUuZy4gY29uc2lkZXIgYSBtZXRob2Qgd2hpY2ggaGFzIHRoZVxuICAgIC8vIHNhbWUgYW1vdW50IG9mIGFyZ3VtZW50cyBidXQganVzdCBoYWQgYSB0eXBlIGNoYW5nZS4gSW4gdGhhdCBjYXNlIHdlIGNvdWxkIHN0aWxsIGFkZFxuICAgIC8vIG5ldyBlbnRyaWVzIHRvIHRoZSB1cGdyYWRlIGRhdGEgdGhhdCBtYXRjaCB0aGUgY3VycmVudCBhcmd1bWVudCBsZW5ndGggdG8ganVzdCBzaG93XG4gICAgLy8gYSBmYWlsdXJlIG1lc3NhZ2UsIGJ1dCBhZGRpbmcgdGhhdCBkYXRhIGJlY29tZXMgcGFpbmZ1bCBpZiB0aGUgbWV0aG9kIGhhcyBvcHRpb25hbFxuICAgIC8vIHBhcmFtZXRlcnMgYW5kIGl0IHdvdWxkIG1lYW4gdGhhdCB0aGUgZXJyb3IgbWVzc2FnZSB3b3VsZCBhbHdheXMgc2hvdyB1cCwgZXZlbiBpZiB0aGVcbiAgICAvLyBhcmd1bWVudCBpcyBpbiBzb21lIGNhc2VzIHN0aWxsIGFzc2lnbmFibGUgdG8gdGhlIG5ldyBwYXJhbWV0ZXIgdHlwZS4gV2UgY291bGQgcmUtdXNlXG4gICAgLy8gdGhlIGxvZ2ljIHdlIGhhdmUgaW4gdGhlIGNvbnN0cnVjdG9yLXNpZ25hdHVyZSBjaGVja3MgdG8gY2hlY2sgZm9yIGFzc2lnbmFiaWxpdHkgYW5kXG4gICAgLy8gdG8gbWFrZSB0aGUgdXBncmFkZSBkYXRhIGxlc3MgdmVyYm9zZS5cbiAgICBjb25zdCBmYWlsdXJlID0gdGhpcy5kYXRhXG4gICAgICAuZmlsdGVyKGRhdGEgPT4gZGF0YS5tZXRob2QgPT09IG1ldGhvZE5hbWUgJiYgZGF0YS5jbGFzc05hbWUgPT09IGhvc3RUeXBlTmFtZSlcbiAgICAgIC5tYXAoZGF0YSA9PiBkYXRhLmludmFsaWRBcmdDb3VudHMuZmluZChmID0+IGYuY291bnQgPT09IG5vZGUuYXJndW1lbnRzLmxlbmd0aCkpWzBdO1xuXG4gICAgaWYgKCFmYWlsdXJlKSB7XG4gICAgICByZXR1cm47XG4gICAgfVxuXG4gICAgdGhpcy5jcmVhdGVGYWlsdXJlQXROb2RlKFxuICAgICAgbm9kZSxcbiAgICAgIGBGb3VuZCBjYWxsIHRvIFwiJHtob3N0VHlwZU5hbWUgKyAnLicgKyBtZXRob2ROYW1lfVwiIGAgK1xuICAgICAgICBgd2l0aCAke2ZhaWx1cmUuY291bnR9IGFyZ3VtZW50cy4gTWVzc2FnZTogJHtmYWlsdXJlLm1lc3NhZ2V9YCxcbiAgICApO1xuICB9XG59XG4iXX0=