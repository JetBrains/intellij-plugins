/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { Import } from './imports';
export type CallExpressionDecorator = ts.Decorator & {
    expression: ts.CallExpression;
};
export interface NgDecorator {
    name: string;
    node: CallExpressionDecorator;
}
/**
 * Gets all decorators which are imported from an Angular package
 * (e.g. "@angular/core") from a list of decorators.
 */
export declare function getAngularDecorators(typeChecker: ts.TypeChecker, decorators: readonly ts.Decorator[]): readonly NgDecorator[];
export declare function getCallDecoratorImport(typeChecker: ts.TypeChecker, decorator: ts.Decorator): Import | null;
