import type * as TS from 'typescript'

export function hasComponentDecorator(ts: typeof TS, scriptId: string, snapshot: TS.IScriptSnapshot, scriptTarget: ts.ScriptTarget): boolean {
  const text = snapshot.getText(0, snapshot.getLength())
  if (text.indexOf("@Component") < 0) return false
  const file = ts.createSourceFile(scriptId, text, scriptTarget,
                                   false, ts.ScriptKind.TS)
  return fileHasComponent(ts, file)
}

function fileHasComponent(ts: typeof TS, file: TS.SourceFile): boolean {
  return ts.forEachChild(file, (node) => {
    if (node.kind === ts.SyntaxKind.ClassDeclaration) {
      for (node of (node as TS.ClassDeclaration).modifiers ?? []) {
        if (node.kind === ts.SyntaxKind.Decorator) {
          return isComponentDecorator(ts, node as TS.Decorator)
        }
      }
    }
    return false
  }) ?? false
}

function isComponentDecorator(ts: typeof TS, decorator: TS.Decorator): boolean {
  const expression = decorator.expression
  if (expression.kind !== ts.SyntaxKind.CallExpression) return false;
  const identifier = (expression as TS.CallExpression).expression
  if (identifier.kind !== ts.SyntaxKind.Identifier) return false;
  return (identifier as TS.Identifier).text === 'Component';
}