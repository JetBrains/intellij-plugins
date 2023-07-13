/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Type, DebugElement } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
export declare type ComponentClazzFn = () => Type<any>;
/**
 * Function generator that captures a Component Type accessor and enables
 * `createTestComponent()` to be reusable for *any* captured Component class.
 */
export declare function makeCreateTestComponent(getClass: ComponentClazzFn): (template: string, styles?: any) => ComponentFixture<Type<any>>;
/**
 *
 */
export declare function expectNativeEl(fixture: ComponentFixture<any>, instanceOptions?: any): any;
/**
 *
 */
export declare function expectEl(debugEl: DebugElement): any;
export declare function queryFor(fixture: ComponentFixture<any>, selector: string): DebugElement[];
