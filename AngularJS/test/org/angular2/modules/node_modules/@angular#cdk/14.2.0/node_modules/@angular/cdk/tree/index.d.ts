import { AfterContentChecked } from '@angular/core';
import { AfterContentInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { CollectionViewer } from '@angular/cdk/collections';
import { DataSource } from '@angular/cdk/collections';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { FocusableOption } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import { InjectionToken } from '@angular/core';
import { IterableDiffer } from '@angular/core';
import { IterableDiffers } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';
import { SelectionModel } from '@angular/cdk/collections';
import { Subject } from 'rxjs';
import { TemplateRef } from '@angular/core';
import { TrackByFunction } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

/** Base tree control. It has basic toggle/expand/collapse operations on a single data node. */
export declare abstract class BaseTreeControl<T, K = T> implements TreeControl<T, K> {
    /** Gets a list of descendent data nodes of a subtree rooted at given data node recursively. */
    abstract getDescendants(dataNode: T): T[];
    /** Expands all data nodes in the tree. */
    abstract expandAll(): void;
    /** Saved data node for `expandAll` action. */
    dataNodes: T[];
    /** A selection model with multi-selection to track expansion status. */
    expansionModel: SelectionModel<K>;
    /**
     * Returns the identifier by which a dataNode should be tracked, should its
     * reference change.
     *
     * Similar to trackBy for *ngFor
     */
    trackBy?: (dataNode: T) => K;
    /** Get depth of a given data node, return the level number. This is for flat tree node. */
    getLevel: (dataNode: T) => number;
    /**
     * Whether the data node is expandable. Returns true if expandable.
     * This is for flat tree node.
     */
    isExpandable: (dataNode: T) => boolean;
    /** Gets a stream that emits whenever the given data node's children change. */
    getChildren: (dataNode: T) => Observable<T[]> | T[] | undefined | null;
    /** Toggles one single data node's expanded/collapsed state. */
    toggle(dataNode: T): void;
    /** Expands one single data node. */
    expand(dataNode: T): void;
    /** Collapses one single data node. */
    collapse(dataNode: T): void;
    /** Whether a given data node is expanded or not. Returns true if the data node is expanded. */
    isExpanded(dataNode: T): boolean;
    /** Toggles a subtree rooted at `node` recursively. */
    toggleDescendants(dataNode: T): void;
    /** Collapse all dataNodes in the tree. */
    collapseAll(): void;
    /** Expands a subtree rooted at given data node recursively. */
    expandDescendants(dataNode: T): void;
    /** Collapses a subtree rooted at given data node recursively. */
    collapseDescendants(dataNode: T): void;
    protected _trackByValue(value: T | K): K;
}

/**
 * Injection token used to provide a `CdkTreeNode` to its outlet.
 * Used primarily to avoid circular imports.
 * @docs-private
 */
export declare const CDK_TREE_NODE_OUTLET_NODE: InjectionToken<{}>;

/**
 * Nested node is a child of `<cdk-tree>`. It works with nested tree.
 * By using `cdk-nested-tree-node` component in tree node template, children of the parent node will
 * be added in the `cdkTreeNodeOutlet` in tree node template.
 * The children of node will be automatically added to `cdkTreeNodeOutlet`.
 */
export declare class CdkNestedTreeNode<T, K = T> extends CdkTreeNode<T, K> implements AfterContentInit, OnDestroy, OnInit {
    protected _differs: IterableDiffers;
    /** Differ used to find the changes in the data provided by the data source. */
    private _dataDiffer;
    /** The children data dataNodes of current node. They will be placed in `CdkTreeNodeOutlet`. */
    protected _children: T[];
    /** The children node placeholder. */
    nodeOutlet: QueryList<CdkTreeNodeOutlet>;
    constructor(elementRef: ElementRef<HTMLElement>, tree: CdkTree<T, K>, _differs: IterableDiffers);
    ngAfterContentInit(): void;
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Add children dataNodes to the NodeOutlet */
    protected updateChildrenNodes(children?: T[]): void;
    /** Clear the children dataNodes. */
    protected _clear(): void;
    /** Gets the outlet for the current node. */
    private _getNodeOutlet;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkNestedTreeNode<any, any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkNestedTreeNode<any, any>, "cdk-nested-tree-node", ["cdkNestedTreeNode"], { "role": "role"; "disabled": "disabled"; "tabIndex": "tabIndex"; }, {}, ["nodeOutlet"], never, false>;
}

/**
 * CDK tree component that connects with a data source to retrieve data of type `T` and renders
 * dataNodes with hierarchy. Updates the dataNodes when new data is provided by the data source.
 */
export declare class CdkTree<T, K = T> implements AfterContentChecked, CollectionViewer, OnDestroy, OnInit {
    private _differs;
    private _changeDetectorRef;
    /** Subject that emits when the component has been destroyed. */
    private readonly _onDestroy;
    /** Differ used to find the changes in the data provided by the data source. */
    private _dataDiffer;
    /** Stores the node definition that does not have a when predicate. */
    private _defaultNodeDef;
    /** Data subscription */
    private _dataSubscription;
    /** Level of nodes */
    private _levels;
    /**
     * Provides a stream containing the latest data array to render. Influenced by the tree's
     * stream of view window (what dataNodes are currently on screen).
     * Data source can be an observable of data array, or a data array to render.
     */
    get dataSource(): DataSource<T> | Observable<T[]> | T[];
    set dataSource(dataSource: DataSource<T> | Observable<T[]> | T[]);
    private _dataSource;
    /** The tree controller */
    treeControl: TreeControl<T, K>;
    /**
     * Tracking function that will be used to check the differences in data changes. Used similarly
     * to `ngFor` `trackBy` function. Optimize node operations by identifying a node based on its data
     * relative to the function to know if a node should be added/removed/moved.
     * Accepts a function that takes two parameters, `index` and `item`.
     */
    trackBy: TrackByFunction<T>;
    _nodeOutlet: CdkTreeNodeOutlet;
    /** The tree node template for the tree */
    _nodeDefs: QueryList<CdkTreeNodeDef<T>>;
    /**
     * Stream containing the latest information on what rows are being displayed on screen.
     * Can be used by the data source to as a heuristic of what data should be provided.
     */
    readonly viewChange: BehaviorSubject<{
        start: number;
        end: number;
    }>;
    constructor(_differs: IterableDiffers, _changeDetectorRef: ChangeDetectorRef);
    ngOnInit(): void;
    ngOnDestroy(): void;
    ngAfterContentChecked(): void;
    /**
     * Switch to the provided data source by resetting the data and unsubscribing from the current
     * render change subscription if one exists. If the data source is null, interpret this by
     * clearing the node outlet. Otherwise start listening for new data.
     */
    private _switchDataSource;
    /** Set up a subscription for the data provided by the data source. */
    private _observeRenderChanges;
    /** Check for changes made in the data and render each change (node added/removed/moved). */
    renderNodeChanges(data: readonly T[], dataDiffer?: IterableDiffer<T>, viewContainer?: ViewContainerRef, parentData?: T): void;
    /**
     * Finds the matching node definition that should be used for this node data. If there is only
     * one node definition, it is returned. Otherwise, find the node definition that has a when
     * predicate that returns true with the data. If none return true, return the default node
     * definition.
     */
    _getNodeDef(data: T, i: number): CdkTreeNodeDef<T>;
    /**
     * Create the embedded view for the data node template and place it in the correct index location
     * within the data node view container.
     */
    insertNode(nodeData: T, index: number, viewContainer?: ViewContainerRef, parentData?: T): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTree<any, any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkTree<any, any>, "cdk-tree", ["cdkTree"], { "dataSource": "dataSource"; "treeControl": "treeControl"; "trackBy": "trackBy"; }, {}, ["_nodeDefs"], never, false>;
}

export declare class CdkTreeModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTreeModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkTreeModule, [typeof i1.CdkNestedTreeNode, typeof i2.CdkTreeNodeDef, typeof i3.CdkTreeNodePadding, typeof i4.CdkTreeNodeToggle, typeof i5.CdkTree, typeof i5.CdkTreeNode, typeof i6.CdkTreeNodeOutlet], never, [typeof i1.CdkNestedTreeNode, typeof i2.CdkTreeNodeDef, typeof i3.CdkTreeNodePadding, typeof i4.CdkTreeNodeToggle, typeof i5.CdkTree, typeof i5.CdkTreeNode, typeof i6.CdkTreeNodeOutlet]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkTreeModule>;
}

/**
 * Tree node for CdkTree. It contains the data in the tree node.
 */
export declare class CdkTreeNode<T, K = T> implements FocusableOption, OnDestroy, OnInit {
    protected _elementRef: ElementRef<HTMLElement>;
    protected _tree: CdkTree<T, K>;
    /**
     * The role of the tree node.
     * @deprecated The correct role is 'treeitem', 'group' should not be used. This input will be
     *   removed in a future version.
     * @breaking-change 12.0.0 Remove this input
     */
    get role(): 'treeitem' | 'group';
    set role(_role: 'treeitem' | 'group');
    /**
     * The most recently created `CdkTreeNode`. We save it in static variable so we can retrieve it
     * in `CdkTree` and set the data to it.
     */
    static mostRecentTreeNode: CdkTreeNode<any> | null;
    /** Subject that emits when the component has been destroyed. */
    protected readonly _destroyed: Subject<void>;
    /** Emits when the node's data has changed. */
    readonly _dataChanges: Subject<void>;
    private _parentNodeAriaLevel;
    /** The tree node's data. */
    get data(): T;
    set data(value: T);
    protected _data: T;
    get isExpanded(): boolean;
    get level(): number;
    constructor(_elementRef: ElementRef<HTMLElement>, _tree: CdkTree<T, K>);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Focuses the menu item. Implements for FocusableOption. */
    focus(): void;
    protected _setRoleFromData(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTreeNode<any, any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTreeNode<any, any>, "cdk-tree-node", ["cdkTreeNode"], { "role": "role"; }, {}, never, never, false>;
}

/**
 * Data node definition for the CdkTree.
 * Captures the node's template and a when predicate that describes when this node should be used.
 */
export declare class CdkTreeNodeDef<T> {
    template: TemplateRef<any>;
    /**
     * Function that should return true if this node template should be used for the provided node
     * data and index. If left undefined, this node will be considered the default node template to
     * use when no other when functions return true for the data.
     * For every node, there must be at least one when function that passes or an undefined to
     * default.
     */
    when: (index: number, nodeData: T) => boolean;
    /** @docs-private */
    constructor(template: TemplateRef<any>);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTreeNodeDef<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTreeNodeDef<any>, "[cdkTreeNodeDef]", never, { "when": "cdkTreeNodeDefWhen"; }, {}, never, never, false>;
}

/**
 * Outlet for nested CdkNode. Put `[cdkTreeNodeOutlet]` on a tag to place children dataNodes
 * inside the outlet.
 */
export declare class CdkTreeNodeOutlet {
    viewContainer: ViewContainerRef;
    _node?: any;
    constructor(viewContainer: ViewContainerRef, _node?: any);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTreeNodeOutlet, [null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTreeNodeOutlet, "[cdkTreeNodeOutlet]", never, {}, {}, never, never, false>;
}

/** Context provided to the tree node component. */
export declare class CdkTreeNodeOutletContext<T> {
    /** Data for the node. */
    $implicit: T;
    /** Depth of the node. */
    level: number;
    /** Index location of the node. */
    index?: number;
    /** Length of the number of total dataNodes. */
    count?: number;
    constructor(data: T);
}

/**
 * Indent for the children tree dataNodes.
 * This directive will add left-padding to the node to show hierarchy.
 */
export declare class CdkTreeNodePadding<T, K = T> implements OnDestroy {
    private _treeNode;
    private _tree;
    private _element;
    private _dir;
    /** Current padding value applied to the element. Used to avoid unnecessarily hitting the DOM. */
    private _currentPadding;
    /** Subject that emits when the component has been destroyed. */
    private readonly _destroyed;
    /** CSS units used for the indentation value. */
    indentUnits: string;
    /** The level of depth of the tree node. The padding will be `level * indent` pixels. */
    get level(): number;
    set level(value: NumberInput);
    _level: number;
    /**
     * The indent for each level. Can be a number or a CSS string.
     * Default number 40px from material design menu sub-menu spec.
     */
    get indent(): number | string;
    set indent(indent: number | string);
    _indent: number;
    constructor(_treeNode: CdkTreeNode<T, K>, _tree: CdkTree<T, K>, _element: ElementRef<HTMLElement>, _dir: Directionality);
    ngOnDestroy(): void;
    /** The padding indent value for the tree node. Returns a string with px numbers if not null. */
    _paddingIndent(): string | null;
    _setPadding(forceChange?: boolean): void;
    /**
     * This has been extracted to a util because of TS 4 and VE.
     * View Engine doesn't support property rename inheritance.
     * TS 4.0 doesn't allow properties to override accessors or vice-versa.
     * @docs-private
     */
    protected _setLevelInput(value: NumberInput): void;
    /**
     * This has been extracted to a util because of TS 4 and VE.
     * View Engine doesn't support property rename inheritance.
     * TS 4.0 doesn't allow properties to override accessors or vice-versa.
     * @docs-private
     */
    protected _setIndentInput(indent: number | string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTreeNodePadding<any, any>, [null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTreeNodePadding<any, any>, "[cdkTreeNodePadding]", never, { "level": "cdkTreeNodePadding"; "indent": "cdkTreeNodePaddingIndent"; }, {}, never, never, false>;
}

/**
 * Node toggle to expand/collapse the node.
 */
export declare class CdkTreeNodeToggle<T, K = T> {
    protected _tree: CdkTree<T, K>;
    protected _treeNode: CdkTreeNode<T, K>;
    /** Whether expand/collapse the node recursively. */
    get recursive(): boolean;
    set recursive(value: BooleanInput);
    protected _recursive: boolean;
    constructor(_tree: CdkTree<T, K>, _treeNode: CdkTreeNode<T, K>);
    _toggle(event: Event): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTreeNodeToggle<any, any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTreeNodeToggle<any, any>, "[cdkTreeNodeToggle]", never, { "recursive": "cdkTreeNodeToggleRecursive"; }, {}, never, never, false>;
}

/** Flat tree control. Able to expand/collapse a subtree recursively for flattened tree. */
export declare class FlatTreeControl<T, K = T> extends BaseTreeControl<T, K> {
    getLevel: (dataNode: T) => number;
    isExpandable: (dataNode: T) => boolean;
    options?: FlatTreeControlOptions<T, K> | undefined;
    /** Construct with flat tree data node functions getLevel and isExpandable. */
    constructor(getLevel: (dataNode: T) => number, isExpandable: (dataNode: T) => boolean, options?: FlatTreeControlOptions<T, K> | undefined);
    /**
     * Gets a list of the data node's subtree of descendent data nodes.
     *
     * To make this working, the `dataNodes` of the TreeControl must be flattened tree nodes
     * with correct levels.
     */
    getDescendants(dataNode: T): T[];
    /**
     * Expands all data nodes in the tree.
     *
     * To make this working, the `dataNodes` variable of the TreeControl must be set to all flattened
     * data nodes of the tree.
     */
    expandAll(): void;
}

/** Optional set of configuration that can be provided to the FlatTreeControl. */
export declare interface FlatTreeControlOptions<T, K> {
    trackBy?: (dataNode: T) => K;
}

/**
 * Returns an error to be thrown when tree control did not implement functions for flat/nested node.
 * @docs-private
 */
export declare function getTreeControlFunctionsMissingError(): Error;

/**
 * Returns an error to be thrown when there are tree control.
 * @docs-private
 */
export declare function getTreeControlMissingError(): Error;

/**
 * Returns an error to be thrown when there are no matching node defs for a particular set of data.
 * @docs-private
 */
export declare function getTreeMissingMatchingNodeDefError(): Error;

/**
 * Returns an error to be thrown when there are multiple nodes that are missing a when function.
 * @docs-private
 */
export declare function getTreeMultipleDefaultNodeDefsError(): Error;


/**
 * Returns an error to be thrown when there is no usable data.
 * @docs-private
 */
export declare function getTreeNoValidDataSourceError(): Error;

declare namespace i1 {
    export {
        CdkNestedTreeNode
    }
}

declare namespace i2 {
    export {
        CdkTreeNodeOutletContext,
        CdkTreeNodeDef
    }
}

declare namespace i3 {
    export {
        CdkTreeNodePadding
    }
}

declare namespace i4 {
    export {
        CdkTreeNodeToggle
    }
}

declare namespace i5 {
    export {
        CdkTree,
        CdkTreeNode
    }
}

declare namespace i6 {
    export {
        CDK_TREE_NODE_OUTLET_NODE,
        CdkTreeNodeOutlet
    }
}

/** Nested tree control. Able to expand/collapse a subtree recursively for NestedNode type. */
export declare class NestedTreeControl<T, K = T> extends BaseTreeControl<T, K> {
    getChildren: (dataNode: T) => Observable<T[]> | T[] | undefined | null;
    options?: NestedTreeControlOptions<T, K> | undefined;
    /** Construct with nested tree function getChildren. */
    constructor(getChildren: (dataNode: T) => Observable<T[]> | T[] | undefined | null, options?: NestedTreeControlOptions<T, K> | undefined);
    /**
     * Expands all dataNodes in the tree.
     *
     * To make this working, the `dataNodes` variable of the TreeControl must be set to all root level
     * data nodes of the tree.
     */
    expandAll(): void;
    /** Gets a list of descendant dataNodes of a subtree rooted at given data node recursively. */
    getDescendants(dataNode: T): T[];
    /** A helper function to get descendants recursively. */
    protected _getDescendants(descendants: T[], dataNode: T): void;
}

/** Optional set of configuration that can be provided to the NestedTreeControl. */
export declare interface NestedTreeControlOptions<T, K> {
    trackBy?: (dataNode: T) => K;
}

/**
 * Tree control interface. User can implement TreeControl to expand/collapse dataNodes in the tree.
 * The CDKTree will use this TreeControl to expand/collapse a node.
 * User can also use it outside the `<cdk-tree>` to control the expansion status of the tree.
 */
export declare interface TreeControl<T, K = T> {
    /** The saved tree nodes data for `expandAll` action. */
    dataNodes: T[];
    /** The expansion model */
    expansionModel: SelectionModel<K>;
    /** Whether the data node is expanded or collapsed. Return true if it's expanded. */
    isExpanded(dataNode: T): boolean;
    /** Get all descendants of a data node */
    getDescendants(dataNode: T): any[];
    /** Expand or collapse data node */
    toggle(dataNode: T): void;
    /** Expand one data node */
    expand(dataNode: T): void;
    /** Collapse one data node */
    collapse(dataNode: T): void;
    /** Expand all the dataNodes in the tree */
    expandAll(): void;
    /** Collapse all the dataNodes in the tree */
    collapseAll(): void;
    /** Toggle a data node by expand/collapse it and all its descendants */
    toggleDescendants(dataNode: T): void;
    /** Expand a data node and all its descendants */
    expandDescendants(dataNode: T): void;
    /** Collapse a data node and all its descendants */
    collapseDescendants(dataNode: T): void;
    /** Get depth of a given data node, return the level number. This is for flat tree node. */
    readonly getLevel: (dataNode: T) => number;
    /**
     * Whether the data node is expandable. Returns true if expandable.
     * This is for flat tree node.
     */
    readonly isExpandable: (dataNode: T) => boolean;
    /** Gets a stream that emits whenever the given data node's children change. */
    readonly getChildren: (dataNode: T) => Observable<T[]> | T[] | undefined | null;
}

export { }
