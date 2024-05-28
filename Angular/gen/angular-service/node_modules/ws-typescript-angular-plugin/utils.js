"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.hasComponentDecorator = void 0;
function hasComponentDecorator(ts, scriptId, snapshot, scriptTarget) {
    const text = snapshot.getText(0, snapshot.getLength());
    if (text.indexOf("@Component") < 0)
        return false;
    const file = ts.createSourceFile(scriptId, text, scriptTarget, false, ts.ScriptKind.TS);
    return fileHasComponent(ts, file);
}
exports.hasComponentDecorator = hasComponentDecorator;
function fileHasComponent(ts, file) {
    var _a;
    return (_a = ts.forEachChild(file, (node) => {
        var _a;
        if (node.kind === ts.SyntaxKind.ClassDeclaration) {
            for (node of (_a = node.modifiers) !== null && _a !== void 0 ? _a : []) {
                if (node.kind === ts.SyntaxKind.Decorator) {
                    return isComponentDecorator(ts, node);
                }
            }
        }
        return false;
    })) !== null && _a !== void 0 ? _a : false;
}
function isComponentDecorator(ts, decorator) {
    const expression = decorator.expression;
    if (expression.kind !== ts.SyntaxKind.CallExpression)
        return false;
    const identifier = expression.expression;
    if (identifier.kind !== ts.SyntaxKind.Identifier)
        return false;
    return identifier.text === 'Component';
}
