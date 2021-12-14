/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
/**
 * Exported DOM accessor utility functions
 */
export declare const _dom: {
    hasStyle: typeof hasStyle;
    getDistributedNodes: typeof getDistributedNodes;
    getShadowRoot: typeof getShadowRoot;
    getText: typeof getText;
    getStyle: typeof getStyle;
    childNodes: typeof childNodes;
    childNodesAsList: typeof childNodesAsList;
    hasClass: typeof hasClass;
    hasAttribute: typeof hasAttribute;
    getAttribute: typeof getAttribute;
    hasShadowRoot: typeof hasShadowRoot;
    isCommentNode: typeof isCommentNode;
    isElementNode: typeof isElementNode;
    isPresent: typeof isPresent;
    isShadowRoot: typeof isShadowRoot;
    tagName: typeof tagName;
    lastElementChild: typeof lastElementChild;
};
declare function getStyle(element: any, stylename: string): string;
declare function hasStyle(element: any, styleName: string, styleValue?: string, inlineOnly?: boolean): boolean;
declare function getDistributedNodes(el: HTMLElement): Node[];
declare function getShadowRoot(el: HTMLElement): DocumentFragment;
declare function getText(el: Node): string;
declare function childNodesAsList(el: Node): any[];
declare function hasClass(element: any, className: string): boolean;
declare function hasAttribute(element: any, attributeName: string): boolean;
declare function getAttribute(element: any, attributeName: string): string;
declare function childNodes(el: any): Node[];
declare function hasShadowRoot(node: any): boolean;
declare function isCommentNode(node: Node): boolean;
declare function isElementNode(node: Node): boolean;
declare function isShadowRoot(node: any): boolean;
declare function isPresent(obj: any): boolean;
declare function tagName(element: any): string;
declare function lastElementChild(element: any): Node | null;
export {};
