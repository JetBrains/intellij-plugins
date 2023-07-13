import { _AbstractConstructor } from '@angular/material/core';
import { AfterContentInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanDisable } from '@angular/material/core';
import { CdkNestedTreeNode } from '@angular/cdk/tree';
import { CdkTree } from '@angular/cdk/tree';
import { CdkTreeNode } from '@angular/cdk/tree';
import { CdkTreeNodeDef } from '@angular/cdk/tree';
import { CdkTreeNodeOutlet } from '@angular/cdk/tree';
import { CdkTreeNodePadding } from '@angular/cdk/tree';
import { CdkTreeNodeToggle } from '@angular/cdk/tree';
import { CollectionViewer } from '@angular/cdk/collections';
import { _Constructor } from '@angular/material/core';
import { DataSource } from '@angular/cdk/collections';
import { ElementRef } from '@angular/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { HasTabIndex } from '@angular/material/core';
import * as i0 from '@angular/core';
import * as i6 from '@angular/cdk/tree';
import * as i7 from '@angular/material/core';
import { IterableDiffers } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { TreeControl } from '@angular/cdk/tree';
import { ViewContainerRef } from '@angular/core';

declare namespace i1 {
    export {
        MatTreeNode,
        MatTreeNodeDef,
        MatNestedTreeNode
    }
}

declare namespace i2 {
    export {
        MatTreeNodePadding
    }
}

declare namespace i3 {
    export {
        MatTreeNodeToggle
    }
}

declare namespace i4 {
    export {
        MatTree
    }
}

declare namespace i5 {
    export {
        MatTreeNodeOutlet
    }
}

/**
 * Wrapper for the CdkTree nested node with Material design styles.
 */
export declare class MatNestedTreeNode<T, K = T> extends CdkNestedTreeNode<T, K> implements AfterContentInit, OnDestroy, OnInit {
    node: T;
    /** Whether the node is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Tabindex for the node. */
    get tabIndex(): number;
    set tabIndex(value: number);
    private _tabIndex;
    constructor(elementRef: ElementRef<HTMLElement>, tree: CdkTree<T, K>, differs: IterableDiffers, tabIndex: string);
    ngOnInit(): void;
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatNestedTreeNode<any, any>, [null, null, null, { attribute: "tabindex"; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatNestedTreeNode<any, any>, "mat-nested-tree-node", ["matNestedTreeNode"], { "role": "role"; "disabled": "disabled"; "tabIndex": "tabIndex"; "node": "matNestedTreeNode"; }, {}, never, never, false, never>;
}

/**
 * Wrapper for the CdkTable with Material design styles.
 */
export declare class MatTree<T, K = T> extends CdkTree<T, K> {
    _nodeOutlet: MatTreeNodeOutlet;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTree<any, any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatTree<any, any>, "mat-tree", ["matTree"], {}, {}, never, never, false, never>;
}

/**
 * Data source for flat tree.
 * The data source need to handle expansion/collapsion of the tree node and change the data feed
 * to `MatTree`.
 * The nested tree nodes of type `T` are flattened through `MatTreeFlattener`, and converted
 * to type `F` for `MatTree` to consume.
 */
export declare class MatTreeFlatDataSource<T, F, K = F> extends DataSource<F> {
    private _treeControl;
    private _treeFlattener;
    private readonly _flattenedData;
    private readonly _expandedData;
    get data(): T[];
    set data(value: T[]);
    private readonly _data;
    constructor(_treeControl: FlatTreeControl<F, K>, _treeFlattener: MatTreeFlattener<T, F, K>, initialData?: T[]);
    connect(collectionViewer: CollectionViewer): Observable<F[]>;
    disconnect(): void;
}

/**
 * Tree flattener to convert a normal type of node to node with children & level information.
 * Transform nested nodes of type `T` to flattened nodes of type `F`.
 *
 * For example, the input data of type `T` is nested, and contains its children data:
 *   SomeNode: {
 *     key: 'Fruits',
 *     children: [
 *       NodeOne: {
 *         key: 'Apple',
 *       },
 *       NodeTwo: {
 *        key: 'Pear',
 *      }
 *    ]
 *  }
 *  After flattener flatten the tree, the structure will become
 *  SomeNode: {
 *    key: 'Fruits',
 *    expandable: true,
 *    level: 1
 *  },
 *  NodeOne: {
 *    key: 'Apple',
 *    expandable: false,
 *    level: 2
 *  },
 *  NodeTwo: {
 *   key: 'Pear',
 *   expandable: false,
 *   level: 2
 * }
 * and the output flattened type is `F` with additional information.
 */
export declare class MatTreeFlattener<T, F, K = F> {
    transformFunction: (node: T, level: number) => F;
    getLevel: (node: F) => number;
    isExpandable: (node: F) => boolean;
    getChildren: (node: T) => Observable<T[]> | T[] | undefined | null;
    constructor(transformFunction: (node: T, level: number) => F, getLevel: (node: F) => number, isExpandable: (node: F) => boolean, getChildren: (node: T) => Observable<T[]> | T[] | undefined | null);
    _flattenNode(node: T, level: number, resultNodes: F[], parentMap: boolean[]): F[];
    _flattenChildren(children: T[], level: number, resultNodes: F[], parentMap: boolean[]): void;
    /**
     * Flatten a list of node type T to flattened version of node F.
     * Please note that type T may be nested, and the length of `structuredData` may be different
     * from that of returned list `F[]`.
     */
    flattenNodes(structuredData: T[]): F[];
    /**
     * Expand flattened node with current expansion status.
     * The returned list may have different length.
     */
    expandFlattenedNodes(nodes: F[], treeControl: TreeControl<F, K>): F[];
}

export declare class MatTreeModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTreeModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatTreeModule, [typeof i1.MatNestedTreeNode, typeof i1.MatTreeNodeDef, typeof i2.MatTreeNodePadding, typeof i3.MatTreeNodeToggle, typeof i4.MatTree, typeof i1.MatTreeNode, typeof i5.MatTreeNodeOutlet], [typeof i6.CdkTreeModule, typeof i7.MatCommonModule], [typeof i7.MatCommonModule, typeof i1.MatNestedTreeNode, typeof i1.MatTreeNodeDef, typeof i2.MatTreeNodePadding, typeof i3.MatTreeNodeToggle, typeof i4.MatTree, typeof i1.MatTreeNode, typeof i5.MatTreeNodeOutlet]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatTreeModule>;
}

/**
 * Data source for nested tree.
 *
 * The data source for nested tree doesn't have to consider node flattener, or the way to expand
 * or collapse. The expansion/collapsion will be handled by TreeControl and each non-leaf node.
 */
export declare class MatTreeNestedDataSource<T> extends DataSource<T> {
    /**
     * Data for the nested tree
     */
    get data(): T[];
    set data(value: T[]);
    private readonly _data;
    connect(collectionViewer: CollectionViewer): Observable<T[]>;
    disconnect(): void;
}

/**
 * Wrapper for the CdkTree node with Material design styles.
 */
export declare class MatTreeNode<T, K = T> extends _MatTreeNodeBase<T, K> implements CanDisable, HasTabIndex, OnInit, OnDestroy {
    constructor(elementRef: ElementRef<HTMLElement>, tree: CdkTree<T, K>, tabIndex: string);
    ngOnInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTreeNode<any, any>, [null, null, { attribute: "tabindex"; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatTreeNode<any, any>, "mat-tree-node", ["matTreeNode"], { "role": "role"; "disabled": "disabled"; "tabIndex": "tabIndex"; }, {}, never, never, false, never>;
}

declare const _MatTreeNodeBase: _Constructor<HasTabIndex> & _AbstractConstructor<HasTabIndex> & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & typeof CdkTreeNode;

/**
 * Wrapper for the CdkTree node definition with Material design styles.
 * Captures the node's template and a when predicate that describes when this node should be used.
 */
export declare class MatTreeNodeDef<T> extends CdkTreeNodeDef<T> {
    data: T;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTreeNodeDef<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatTreeNodeDef<any>, "[matTreeNodeDef]", never, { "when": "matTreeNodeDefWhen"; "data": "matTreeNode"; }, {}, never, never, false, never>;
}

/**
 * Outlet for nested CdkNode. Put `[matTreeNodeOutlet]` on a tag to place children dataNodes
 * inside the outlet.
 */
export declare class MatTreeNodeOutlet implements CdkTreeNodeOutlet {
    viewContainer: ViewContainerRef;
    _node?: any;
    constructor(viewContainer: ViewContainerRef, _node?: any);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTreeNodeOutlet, [null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatTreeNodeOutlet, "[matTreeNodeOutlet]", never, {}, {}, never, never, false, never>;
}

/**
 * Wrapper for the CdkTree padding with Material design styles.
 */
export declare class MatTreeNodePadding<T, K = T> extends CdkTreeNodePadding<T, K> {
    /** The level of depth of the tree node. The padding will be `level * indent` pixels. */
    get level(): number;
    set level(value: NumberInput);
    /** The indent for each level. Default number 40px from material design menu sub-menu spec. */
    get indent(): number | string;
    set indent(indent: number | string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTreeNodePadding<any, any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatTreeNodePadding<any, any>, "[matTreeNodePadding]", never, { "level": "matTreeNodePadding"; "indent": "matTreeNodePaddingIndent"; }, {}, never, never, false, never>;
}

/**
 * Wrapper for the CdkTree's toggle with Material design styles.
 */
export declare class MatTreeNodeToggle<T, K = T> extends CdkTreeNodeToggle<T, K> {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTreeNodeToggle<any, any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatTreeNodeToggle<any, any>, "[matTreeNodeToggle]", never, { "recursive": "matTreeNodeToggleRecursive"; }, {}, never, never, false, never>;
}

export { }
