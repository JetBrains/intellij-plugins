import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { Direction } from '@angular/cdk/bidi';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i7 from '@angular/cdk/scrolling';
import { InjectionToken } from '@angular/core';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { QueryList } from '@angular/core';
import { ScrollDispatcher } from '@angular/cdk/scrolling';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';
import { ViewportRuler } from '@angular/cdk/scrolling';

/**
 * Injection token that can be used to configure the
 * behavior of the drag&drop-related components.
 */
export declare const CDK_DRAG_CONFIG: InjectionToken<DragDropConfig>;

/**
 * Injection token that can be used to reference instances of `CdkDragHandle`. It serves as
 * alternative token to the actual `CdkDragHandle` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const CDK_DRAG_HANDLE: InjectionToken<CdkDragHandle>;

/**
 * Injection token that can be used for a `CdkDrag` to provide itself as a parent to the
 * drag-specific child directive (`CdkDragHandle`, `CdkDragPreview` etc.). Used primarily
 * to avoid circular imports.
 * @docs-private
 */
export declare const CDK_DRAG_PARENT: InjectionToken<{}>;

/**
 * Injection token that can be used to reference instances of `CdkDragPlaceholder`. It serves as
 * alternative token to the actual `CdkDragPlaceholder` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const CDK_DRAG_PLACEHOLDER: InjectionToken<CdkDragPlaceholder<any>>;

/**
 * Injection token that can be used to reference instances of `CdkDragPreview`. It serves as
 * alternative token to the actual `CdkDragPreview` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const CDK_DRAG_PREVIEW: InjectionToken<CdkDragPreview<any>>;

/**
 * Injection token that can be used to reference instances of `CdkDropList`. It serves as
 * alternative token to the actual `CdkDropList` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const CDK_DROP_LIST: InjectionToken<CdkDropList<any>>;

/**
 * Injection token that can be used to reference instances of `CdkDropListGroup`. It serves as
 * alternative token to the actual `CdkDropListGroup` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const CDK_DROP_LIST_GROUP: InjectionToken<CdkDropListGroup<unknown>>;

/** Element that can be moved inside a CdkDropList container. */
export declare class CdkDrag<T = any> implements AfterViewInit, OnChanges, OnDestroy {
    /** Element that the draggable is attached to. */
    element: ElementRef<HTMLElement>;
    /** Droppable container that the draggable is a part of. */
    dropContainer: CdkDropListInternal;
    private _ngZone;
    private _viewContainerRef;
    private _dir;
    private _changeDetectorRef;
    private _selfHandle?;
    private _parentDrag?;
    private readonly _destroyed;
    private static _dragInstances;
    /** Reference to the underlying drag instance. */
    _dragRef: DragRef<CdkDrag<T>>;
    /** Elements that can be used to drag the draggable item. */
    _handles: QueryList<CdkDragHandle>;
    /** Element that will be used as a template to create the draggable item's preview. */
    _previewTemplate: CdkDragPreview;
    /** Template for placeholder element rendered to show where a draggable would be dropped. */
    _placeholderTemplate: CdkDragPlaceholder;
    /** Arbitrary data to attach to this drag instance. */
    data: T;
    /** Locks the position of the dragged element along the specified axis. */
    lockAxis: DragAxis;
    /**
     * Selector that will be used to determine the root draggable element, starting from
     * the `cdkDrag` element and going up the DOM. Passing an alternate root element is useful
     * when trying to enable dragging on an element that you might not have access to.
     */
    rootElementSelector: string;
    /**
     * Node or selector that will be used to determine the element to which the draggable's
     * position will be constrained. If a string is passed in, it'll be used as a selector that
     * will be matched starting from the element's parent and going up the DOM until a match
     * has been found.
     */
    boundaryElement: string | ElementRef<HTMLElement> | HTMLElement;
    /**
     * Amount of milliseconds to wait after the user has put their
     * pointer down before starting to drag the element.
     */
    dragStartDelay: DragStartDelay;
    /**
     * Sets the position of a `CdkDrag` that is outside of a drop container.
     * Can be used to restore the element's position for a returning user.
     */
    freeDragPosition: Point;
    /** Whether starting to drag this element is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /**
     * Function that can be used to customize the logic of how the position of the drag item
     * is limited while it's being dragged. Gets called with a point containing the current position
     * of the user's pointer on the page, a reference to the item being dragged and its dimensions.
     * Should return a point describing where the item should be rendered.
     */
    constrainPosition?: (userPointerPosition: Point, dragRef: DragRef, dimensions: ClientRect, pickupPositionInElement: Point) => Point;
    /** Class to be added to the preview element. */
    previewClass: string | string[];
    /**
     * Configures the place into which the preview of the item will be inserted. Can be configured
     * globally through `CDK_DROP_LIST`. Possible values:
     * - `global` - Preview will be inserted at the bottom of the `<body>`. The advantage is that
     * you don't have to worry about `overflow: hidden` or `z-index`, but the item won't retain
     * its inherited styles.
     * - `parent` - Preview will be inserted into the parent of the drag item. The advantage is that
     * inherited styles will be preserved, but it may be clipped by `overflow: hidden` or not be
     * visible due to `z-index`. Furthermore, the preview is going to have an effect over selectors
     * like `:nth-child` and some flexbox configurations.
     * - `ElementRef<HTMLElement> | HTMLElement` - Preview will be inserted into a specific element.
     * Same advantages and disadvantages as `parent`.
     */
    previewContainer: PreviewContainer;
    /** Emits when the user starts dragging the item. */
    readonly started: EventEmitter<CdkDragStart>;
    /** Emits when the user has released a drag item, before any animations have started. */
    readonly released: EventEmitter<CdkDragRelease>;
    /** Emits when the user stops dragging an item in the container. */
    readonly ended: EventEmitter<CdkDragEnd>;
    /** Emits when the user has moved the item into a new container. */
    readonly entered: EventEmitter<CdkDragEnter<any>>;
    /** Emits when the user removes the item its container by dragging it into another container. */
    readonly exited: EventEmitter<CdkDragExit<any>>;
    /** Emits when the user drops the item inside a container. */
    readonly dropped: EventEmitter<CdkDragDrop<any>>;
    /**
     * Emits as the user is dragging the item. Use with caution,
     * because this event will fire for every pixel that the user has dragged.
     */
    readonly moved: Observable<CdkDragMove<T>>;
    constructor(
    /** Element that the draggable is attached to. */
    element: ElementRef<HTMLElement>, 
    /** Droppable container that the draggable is a part of. */
    dropContainer: CdkDropListInternal, 
    /**
     * @deprecated `_document` parameter no longer being used and will be removed.
     * @breaking-change 12.0.0
     */
    _document: any, _ngZone: NgZone, _viewContainerRef: ViewContainerRef, config: DragDropConfig, _dir: Directionality, dragDrop: DragDrop, _changeDetectorRef: ChangeDetectorRef, _selfHandle?: CdkDragHandle | undefined, _parentDrag?: CdkDrag<any> | undefined);
    /**
     * Returns the element that is being used as a placeholder
     * while the current element is being dragged.
     */
    getPlaceholderElement(): HTMLElement;
    /** Returns the root draggable element. */
    getRootElement(): HTMLElement;
    /** Resets a standalone drag item to its initial position. */
    reset(): void;
    /**
     * Gets the pixel coordinates of the draggable outside of a drop container.
     */
    getFreeDragPosition(): Readonly<Point>;
    /**
     * Sets the current position in pixels the draggable outside of a drop container.
     * @param value New position to be set.
     */
    setFreeDragPosition(value: Point): void;
    ngAfterViewInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Syncs the root element with the `DragRef`. */
    private _updateRootElement;
    /** Gets the boundary element, based on the `boundaryElement` value. */
    private _getBoundaryElement;
    /** Syncs the inputs of the CdkDrag with the options of the underlying DragRef. */
    private _syncInputs;
    /** Handles the events from the underlying `DragRef`. */
    private _handleEvents;
    /** Assigns the default input values based on a provided config object. */
    private _assignDefaults;
    /** Sets up the listener that syncs the handles with the drag ref. */
    private _setupHandlesListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDrag<any>, [null, { optional: true; skipSelf: true; }, null, null, null, { optional: true; }, { optional: true; }, null, null, { optional: true; self: true; }, { optional: true; skipSelf: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkDrag<any>, "[cdkDrag]", ["cdkDrag"], { "data": "cdkDragData"; "lockAxis": "cdkDragLockAxis"; "rootElementSelector": "cdkDragRootElement"; "boundaryElement": "cdkDragBoundary"; "dragStartDelay": "cdkDragStartDelay"; "freeDragPosition": "cdkDragFreeDragPosition"; "disabled": "cdkDragDisabled"; "constrainPosition": "cdkDragConstrainPosition"; "previewClass": "cdkDragPreviewClass"; "previewContainer": "cdkDragPreviewContainer"; }, { "started": "cdkDragStarted"; "released": "cdkDragReleased"; "ended": "cdkDragEnded"; "entered": "cdkDragEntered"; "exited": "cdkDragExited"; "dropped": "cdkDragDropped"; "moved": "cdkDragMoved"; }, ["_previewTemplate", "_placeholderTemplate", "_handles"], never, false>;
}

/** Event emitted when the user drops a draggable item inside a drop container. */
export declare interface CdkDragDrop<T, O = T, I = any> {
    /** Index of the item when it was picked up. */
    previousIndex: number;
    /** Current index of the item. */
    currentIndex: number;
    /** Item that is being dropped. */
    item: CdkDrag<I>;
    /** Container in which the item was dropped. */
    container: CdkDropList<T>;
    /** Container from which the item was picked up. Can be the same as the `container`. */
    previousContainer: CdkDropList<O>;
    /** Whether the user's pointer was over the container when the item was dropped. */
    isPointerOverContainer: boolean;
    /** Distance in pixels that the user has dragged since the drag sequence started. */
    distance: {
        x: number;
        y: number;
    };
    /** Position where the pointer was when the item was dropped */
    dropPoint: {
        x: number;
        y: number;
    };
    /** Native event that caused the drop event. */
    event: MouseEvent | TouchEvent;
}

/** Event emitted when the user stops dragging a draggable. */
export declare interface CdkDragEnd<T = any> {
    /** Draggable that emitted the event. */
    source: CdkDrag<T>;
    /** Distance in pixels that the user has dragged since the drag sequence started. */
    distance: {
        x: number;
        y: number;
    };
    /** Position where the pointer was when the item was dropped */
    dropPoint: {
        x: number;
        y: number;
    };
    /** Native event that caused the dragging to stop. */
    event: MouseEvent | TouchEvent;
}

/** Event emitted when the user moves an item into a new drop container. */
export declare interface CdkDragEnter<T = any, I = T> {
    /** Container into which the user has moved the item. */
    container: CdkDropList<T>;
    /** Item that was moved into the container. */
    item: CdkDrag<I>;
    /** Index at which the item has entered the container. */
    currentIndex: number;
}

/**
 * Event emitted when the user removes an item from a
 * drop container by moving it into another one.
 */
export declare interface CdkDragExit<T = any, I = T> {
    /** Container from which the user has a removed an item. */
    container: CdkDropList<T>;
    /** Item that was removed from the container. */
    item: CdkDrag<I>;
}

/** Handle that can be used to drag a CdkDrag instance. */
export declare class CdkDragHandle implements OnDestroy {
    element: ElementRef<HTMLElement>;
    /** Closest parent draggable instance. */
    _parentDrag: {} | undefined;
    /** Emits when the state of the handle has changed. */
    readonly _stateChanges: Subject<CdkDragHandle>;
    /** Whether starting to drag through this handle is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    constructor(element: ElementRef<HTMLElement>, parentDrag?: any);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDragHandle, [null, { optional: true; skipSelf: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkDragHandle, "[cdkDragHandle]", never, { "disabled": "cdkDragHandleDisabled"; }, {}, never, never, false>;
}

/** Event emitted as the user is dragging a draggable item. */
export declare interface CdkDragMove<T = any> {
    /** Item that is being dragged. */
    source: CdkDrag<T>;
    /** Position of the user's pointer on the page. */
    pointerPosition: {
        x: number;
        y: number;
    };
    /** Native event that is causing the dragging. */
    event: MouseEvent | TouchEvent;
    /** Distance in pixels that the user has dragged since the drag sequence started. */
    distance: {
        x: number;
        y: number;
    };
    /**
     * Indicates the direction in which the user is dragging the element along each axis.
     * `1` means that the position is increasing (e.g. the user is moving to the right or downwards),
     * whereas `-1` means that it's decreasing (they're moving to the left or upwards). `0` means
     * that the position hasn't changed.
     */
    delta: {
        x: -1 | 0 | 1;
        y: -1 | 0 | 1;
    };
}

/**
 * Element that will be used as a template for the placeholder of a CdkDrag when
 * it is being dragged. The placeholder is displayed in place of the element being dragged.
 */
export declare class CdkDragPlaceholder<T = any> {
    templateRef: TemplateRef<T>;
    /** Context data to be added to the placeholder template instance. */
    data: T;
    constructor(templateRef: TemplateRef<T>);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDragPlaceholder<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkDragPlaceholder<any>, "ng-template[cdkDragPlaceholder]", never, { "data": "data"; }, {}, never, never, false>;
}

/**
 * Element that will be used as a template for the preview
 * of a CdkDrag when it is being dragged.
 */
export declare class CdkDragPreview<T = any> {
    templateRef: TemplateRef<T>;
    /** Context data to be added to the preview template instance. */
    data: T;
    /** Whether the preview should preserve the same size as the item that is being dragged. */
    get matchSize(): boolean;
    set matchSize(value: BooleanInput);
    private _matchSize;
    constructor(templateRef: TemplateRef<T>);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDragPreview<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkDragPreview<any>, "ng-template[cdkDragPreview]", never, { "data": "data"; "matchSize": "matchSize"; }, {}, never, never, false>;
}

/** Event emitted when the user releases an item, before any animations have started. */
export declare interface CdkDragRelease<T = any> {
    /** Draggable that emitted the event. */
    source: CdkDrag<T>;
    /** Native event that caused the release event. */
    event: MouseEvent | TouchEvent;
}

/** Event emitted when the user swaps the position of two drag items. */
export declare interface CdkDragSortEvent<T = any, I = T> {
    /** Index from which the item was sorted previously. */
    previousIndex: number;
    /** Index that the item is currently in. */
    currentIndex: number;
    /** Container that the item belongs to. */
    container: CdkDropList<T>;
    /** Item that is being sorted. */
    item: CdkDrag<I>;
}

/** Event emitted when the user starts dragging a draggable. */
export declare interface CdkDragStart<T = any> {
    /** Draggable that emitted the event. */
    source: CdkDrag<T>;
    /** Native event that started the drag sequence. */
    event: MouseEvent | TouchEvent;
}

/** Container that wraps a set of draggable items. */
export declare class CdkDropList<T = any> implements OnDestroy {
    /** Element that the drop list is attached to. */
    element: ElementRef<HTMLElement>;
    private _changeDetectorRef;
    private _scrollDispatcher;
    private _dir?;
    private _group?;
    /** Emits when the list has been destroyed. */
    private readonly _destroyed;
    /** Whether the element's scrollable parents have been resolved. */
    private _scrollableParentsResolved;
    /** Keeps track of the drop lists that are currently on the page. */
    private static _dropLists;
    /** Reference to the underlying drop list instance. */
    _dropListRef: DropListRef<CdkDropList<T>>;
    /**
     * Other draggable containers that this container is connected to and into which the
     * container's items can be transferred. Can either be references to other drop containers,
     * or their unique IDs.
     */
    connectedTo: (CdkDropList | string)[] | CdkDropList | string;
    /** Arbitrary data to attach to this container. */
    data: T;
    /** Direction in which the list is oriented. */
    orientation: DropListOrientation;
    /**
     * Unique ID for the drop zone. Can be used as a reference
     * in the `connectedTo` of another `CdkDropList`.
     */
    id: string;
    /** Locks the position of the draggable elements inside the container along the specified axis. */
    lockAxis: DragAxis;
    /** Whether starting a dragging sequence from this container is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Whether sorting within this drop list is disabled. */
    sortingDisabled: BooleanInput;
    /**
     * Function that is used to determine whether an item
     * is allowed to be moved into a drop container.
     */
    enterPredicate: (drag: CdkDrag, drop: CdkDropList) => boolean;
    /** Functions that is used to determine whether an item can be sorted into a particular index. */
    sortPredicate: (index: number, drag: CdkDrag, drop: CdkDropList) => boolean;
    /** Whether to auto-scroll the view when the user moves their pointer close to the edges. */
    autoScrollDisabled: BooleanInput;
    /** Number of pixels to scroll for each frame when auto-scrolling an element. */
    autoScrollStep: NumberInput;
    /** Emits when the user drops an item inside the container. */
    readonly dropped: EventEmitter<CdkDragDrop<T, any>>;
    /**
     * Emits when the user has moved a new drag item into this container.
     */
    readonly entered: EventEmitter<CdkDragEnter<T>>;
    /**
     * Emits when the user removes an item from the container
     * by dragging it into another container.
     */
    readonly exited: EventEmitter<CdkDragExit<T>>;
    /** Emits as the user is swapping items while actively dragging. */
    readonly sorted: EventEmitter<CdkDragSortEvent<T>>;
    /**
     * Keeps track of the items that are registered with this container. Historically we used to
     * do this with a `ContentChildren` query, however queries don't handle transplanted views very
     * well which means that we can't handle cases like dragging the headers of a `mat-table`
     * correctly. What we do instead is to have the items register themselves with the container
     * and then we sort them based on their position in the DOM.
     */
    private _unsortedItems;
    constructor(
    /** Element that the drop list is attached to. */
    element: ElementRef<HTMLElement>, dragDrop: DragDrop, _changeDetectorRef: ChangeDetectorRef, _scrollDispatcher: ScrollDispatcher, _dir?: Directionality | undefined, _group?: CdkDropListGroup<CdkDropList<any>> | undefined, config?: DragDropConfig);
    /** Registers an items with the drop list. */
    addItem(item: CdkDrag): void;
    /** Removes an item from the drop list. */
    removeItem(item: CdkDrag): void;
    /** Gets the registered items in the list, sorted by their position in the DOM. */
    getSortedItems(): CdkDrag[];
    ngOnDestroy(): void;
    /** Syncs the inputs of the CdkDropList with the options of the underlying DropListRef. */
    private _setupInputSyncSubscription;
    /** Handles events from the underlying DropListRef. */
    private _handleEvents;
    /** Assigns the default input values based on a provided config object. */
    private _assignDefaults;
    /** Syncs up the registered drag items with underlying drop list ref. */
    private _syncItemsWithRef;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDropList<any>, [null, null, null, null, { optional: true; }, { optional: true; skipSelf: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkDropList<any>, "[cdkDropList], cdk-drop-list", ["cdkDropList"], { "connectedTo": "cdkDropListConnectedTo"; "data": "cdkDropListData"; "orientation": "cdkDropListOrientation"; "id": "id"; "lockAxis": "cdkDropListLockAxis"; "disabled": "cdkDropListDisabled"; "sortingDisabled": "cdkDropListSortingDisabled"; "enterPredicate": "cdkDropListEnterPredicate"; "sortPredicate": "cdkDropListSortPredicate"; "autoScrollDisabled": "cdkDropListAutoScrollDisabled"; "autoScrollStep": "cdkDropListAutoScrollStep"; }, { "dropped": "cdkDropListDropped"; "entered": "cdkDropListEntered"; "exited": "cdkDropListExited"; "sorted": "cdkDropListSorted"; }, never, never, false>;
}

/**
 * Declaratively connects sibling `cdkDropList` instances together. All of the `cdkDropList`
 * elements that are placed inside a `cdkDropListGroup` will be connected to each other
 * automatically. Can be used as an alternative to the `cdkDropListConnectedTo` input
 * from `cdkDropList`.
 */
export declare class CdkDropListGroup<T> implements OnDestroy {
    /** Drop lists registered inside the group. */
    readonly _items: Set<T>;
    /** Whether starting a dragging sequence from inside this group is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDropListGroup<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkDropListGroup<any>, "[cdkDropListGroup]", ["cdkDropListGroup"], { "disabled": "cdkDropListGroupDisabled"; }, {}, never, never, false>;
}

/**
 * Internal compile-time-only representation of a `CdkDropList`.
 * Used to avoid circular import issues between the `CdkDropList` and the `CdkDrag`.
 * @docs-private
 */
declare interface CdkDropListInternal extends CdkDropList {
}

/**
 * Copies an item from one array to another, leaving it in its
 * original position in current array.
 * @param currentArray Array from which to copy the item.
 * @param targetArray Array into which is copy the item.
 * @param currentIndex Index of the item in its current array.
 * @param targetIndex Index at which to insert the item.
 *
 */
export declare function copyArrayItem<T = any>(currentArray: T[], targetArray: T[], currentIndex: number, targetIndex: number): void;

/** Possible axis along which dragging can be locked. */
export declare type DragAxis = 'x' | 'y';

/** Function that can be used to constrain the position of a dragged element. */
export declare type DragConstrainPosition = (point: Point, dragRef: DragRef) => Point;

/**
 * Service that allows for drag-and-drop functionality to be attached to DOM elements.
 */
export declare class DragDrop {
    private _document;
    private _ngZone;
    private _viewportRuler;
    private _dragDropRegistry;
    constructor(_document: any, _ngZone: NgZone, _viewportRuler: ViewportRuler, _dragDropRegistry: DragDropRegistry<DragRef, DropListRef>);
    /**
     * Turns an element into a draggable item.
     * @param element Element to which to attach the dragging functionality.
     * @param config Object used to configure the dragging behavior.
     */
    createDrag<T = any>(element: ElementRef<HTMLElement> | HTMLElement, config?: DragRefConfig): DragRef<T>;
    /**
     * Turns an element into a drop list.
     * @param element Element to which to attach the drop list functionality.
     */
    createDropList<T = any>(element: ElementRef<HTMLElement> | HTMLElement): DropListRef<T>;
    static ɵfac: i0.ɵɵFactoryDeclaration<DragDrop, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<DragDrop>;
}

/**
 * Object that can be used to configure the drag
 * items and drop lists within a module or a component.
 */
export declare interface DragDropConfig extends Partial<DragRefConfig> {
    lockAxis?: DragAxis;
    dragStartDelay?: DragStartDelay;
    constrainPosition?: DragConstrainPosition;
    previewClass?: string | string[];
    boundaryElement?: string;
    rootElementSelector?: string;
    draggingDisabled?: boolean;
    sortingDisabled?: boolean;
    listAutoScrollDisabled?: boolean;
    listOrientation?: DropListOrientation;
    zIndex?: number;
    previewContainer?: 'global' | 'parent';
}

export declare class DragDropModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<DragDropModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<DragDropModule, [typeof i1.CdkDropList, typeof i2.CdkDropListGroup, typeof i3.CdkDrag, typeof i4.CdkDragHandle, typeof i5.CdkDragPreview, typeof i6.CdkDragPlaceholder], never, [typeof i7.CdkScrollableModule, typeof i1.CdkDropList, typeof i2.CdkDropListGroup, typeof i3.CdkDrag, typeof i4.CdkDragHandle, typeof i5.CdkDragPreview, typeof i6.CdkDragPlaceholder]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<DragDropModule>;
}

/**
 * Service that keeps track of all the drag item and drop container
 * instances, and manages global event listeners on the `document`.
 * @docs-private
 */
export declare class DragDropRegistry<I extends {
    isDragging(): boolean;
}, C> implements OnDestroy {
    private _ngZone;
    private _document;
    /** Registered drop container instances. */
    private _dropInstances;
    /** Registered drag item instances. */
    private _dragInstances;
    /** Drag item instances that are currently being dragged. */
    private _activeDragInstances;
    /** Keeps track of the event listeners that we've bound to the `document`. */
    private _globalListeners;
    /**
     * Predicate function to check if an item is being dragged.  Moved out into a property,
     * because it'll be called a lot and we don't want to create a new function every time.
     */
    private _draggingPredicate;
    /**
     * Emits the `touchmove` or `mousemove` events that are dispatched
     * while the user is dragging a drag item instance.
     */
    readonly pointerMove: Subject<TouchEvent | MouseEvent>;
    /**
     * Emits the `touchend` or `mouseup` events that are dispatched
     * while the user is dragging a drag item instance.
     */
    readonly pointerUp: Subject<TouchEvent | MouseEvent>;
    /**
     * Emits when the viewport has been scrolled while the user is dragging an item.
     * @deprecated To be turned into a private member. Use the `scrolled` method instead.
     * @breaking-change 13.0.0
     */
    readonly scroll: Subject<Event>;
    constructor(_ngZone: NgZone, _document: any);
    /** Adds a drop container to the registry. */
    registerDropContainer(drop: C): void;
    /** Adds a drag item instance to the registry. */
    registerDragItem(drag: I): void;
    /** Removes a drop container from the registry. */
    removeDropContainer(drop: C): void;
    /** Removes a drag item instance from the registry. */
    removeDragItem(drag: I): void;
    /**
     * Starts the dragging sequence for a drag instance.
     * @param drag Drag instance which is being dragged.
     * @param event Event that initiated the dragging.
     */
    startDragging(drag: I, event: TouchEvent | MouseEvent): void;
    /** Stops dragging a drag item instance. */
    stopDragging(drag: I): void;
    /** Gets whether a drag item instance is currently being dragged. */
    isDragging(drag: I): boolean;
    /**
     * Gets a stream that will emit when any element on the page is scrolled while an item is being
     * dragged.
     * @param shadowRoot Optional shadow root that the current dragging sequence started from.
     *   Top-level listeners won't pick up events coming from the shadow DOM so this parameter can
     *   be used to include an additional top-level listener at the shadow root level.
     */
    scrolled(shadowRoot?: DocumentOrShadowRoot | null): Observable<Event>;
    ngOnDestroy(): void;
    /**
     * Event listener that will prevent the default browser action while the user is dragging.
     * @param event Event whose default action should be prevented.
     */
    private _preventDefaultWhileDragging;
    /** Event listener for `touchmove` that is bound even if no dragging is happening. */
    private _persistentTouchmoveListener;
    /** Clears out the global event listeners from the `document`. */
    private _clearGlobalListeners;
    static ɵfac: i0.ɵɵFactoryDeclaration<DragDropRegistry<any, any>, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<DragDropRegistry<any, any>>;
}

/** Template that can be used to create a drag helper element (e.g. a preview or a placeholder). */
declare interface DragHelperTemplate<T = any> {
    template: TemplateRef<T> | null;
    viewContainer: ViewContainerRef;
    context: T;
}

/** Template that can be used to create a drag preview element. */
declare interface DragPreviewTemplate<T = any> extends DragHelperTemplate<T> {
    matchSize?: boolean;
}

/**
 * Reference to a draggable item. Used to manipulate or dispose of the item.
 */
export declare class DragRef<T = any> {
    private _config;
    private _document;
    private _ngZone;
    private _viewportRuler;
    private _dragDropRegistry;
    /** Element displayed next to the user's pointer while the element is dragged. */
    private _preview;
    /** Reference to the view of the preview element. */
    private _previewRef;
    /** Container into which to insert the preview. */
    private _previewContainer;
    /** Reference to the view of the placeholder element. */
    private _placeholderRef;
    /** Element that is rendered instead of the draggable item while it is being sorted. */
    private _placeholder;
    /** Coordinates within the element at which the user picked up the element. */
    private _pickupPositionInElement;
    /** Coordinates on the page at which the user picked up the element. */
    private _pickupPositionOnPage;
    /**
     * Anchor node used to save the place in the DOM where the element was
     * picked up so that it can be restored at the end of the drag sequence.
     */
    private _anchor;
    /**
     * CSS `transform` applied to the element when it isn't being dragged. We need a
     * passive transform in order for the dragged element to retain its new position
     * after the user has stopped dragging and because we need to know the relative
     * position in case they start dragging again. This corresponds to `element.style.transform`.
     */
    private _passiveTransform;
    /** CSS `transform` that is applied to the element while it's being dragged. */
    private _activeTransform;
    /** Inline `transform` value that the element had before the first dragging sequence. */
    private _initialTransform?;
    /**
     * Whether the dragging sequence has been started. Doesn't
     * necessarily mean that the element has been moved.
     */
    private _hasStartedDragging;
    /** Whether the element has moved since the user started dragging it. */
    private _hasMoved;
    /** Drop container in which the DragRef resided when dragging began. */
    private _initialContainer;
    /** Index at which the item started in its initial container. */
    private _initialIndex;
    /** Cached positions of scrollable parent elements. */
    private _parentPositions;
    /** Emits when the item is being moved. */
    private readonly _moveEvents;
    /** Keeps track of the direction in which the user is dragging along each axis. */
    private _pointerDirectionDelta;
    /** Pointer position at which the last change in the delta occurred. */
    private _pointerPositionAtLastDirectionChange;
    /** Position of the pointer at the last pointer event. */
    private _lastKnownPointerPosition;
    /**
     * Root DOM node of the drag instance. This is the element that will
     * be moved around as the user is dragging.
     */
    private _rootElement;
    /**
     * Nearest ancestor SVG, relative to which coordinates are calculated if dragging SVGElement
     */
    private _ownerSVGElement;
    /**
     * Inline style value of `-webkit-tap-highlight-color` at the time the
     * dragging was started. Used to restore the value once we're done dragging.
     */
    private _rootElementTapHighlight;
    /** Subscription to pointer movement events. */
    private _pointerMoveSubscription;
    /** Subscription to the event that is dispatched when the user lifts their pointer. */
    private _pointerUpSubscription;
    /** Subscription to the viewport being scrolled. */
    private _scrollSubscription;
    /** Subscription to the viewport being resized. */
    private _resizeSubscription;
    /**
     * Time at which the last touch event occurred. Used to avoid firing the same
     * events multiple times on touch devices where the browser will fire a fake
     * mouse event for each touch event, after a certain time.
     */
    private _lastTouchEventTime;
    /** Time at which the last dragging sequence was started. */
    private _dragStartTime;
    /** Cached reference to the boundary element. */
    private _boundaryElement;
    /** Whether the native dragging interactions have been enabled on the root element. */
    private _nativeInteractionsEnabled;
    /** Client rect of the root element when the dragging sequence has started. */
    private _initialClientRect?;
    /** Cached dimensions of the preview element. Should be read via `_getPreviewRect`. */
    private _previewRect?;
    /** Cached dimensions of the boundary element. */
    private _boundaryRect?;
    /** Element that will be used as a template to create the draggable item's preview. */
    private _previewTemplate?;
    /** Template for placeholder element rendered to show where a draggable would be dropped. */
    private _placeholderTemplate?;
    /** Elements that can be used to drag the draggable item. */
    private _handles;
    /** Registered handles that are currently disabled. */
    private _disabledHandles;
    /** Droppable container that the draggable is a part of. */
    private _dropContainer?;
    /** Layout direction of the item. */
    private _direction;
    /** Ref that the current drag item is nested in. */
    private _parentDragRef;
    /**
     * Cached shadow root that the element is placed in. `null` means that the element isn't in
     * the shadow DOM and `undefined` means that it hasn't been resolved yet. Should be read via
     * `_getShadowRoot`, not directly.
     */
    private _cachedShadowRoot;
    /** Axis along which dragging is locked. */
    lockAxis: 'x' | 'y';
    /**
     * Amount of milliseconds to wait after the user has put their
     * pointer down before starting to drag the element.
     */
    dragStartDelay: number | {
        touch: number;
        mouse: number;
    };
    /** Class to be added to the preview element. */
    previewClass: string | string[] | undefined;
    /** Whether starting to drag this element is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** Emits as the drag sequence is being prepared. */
    readonly beforeStarted: Subject<void>;
    /** Emits when the user starts dragging the item. */
    readonly started: Subject<{
        source: DragRef;
        event: MouseEvent | TouchEvent;
    }>;
    /** Emits when the user has released a drag item, before any animations have started. */
    readonly released: Subject<{
        source: DragRef;
        event: MouseEvent | TouchEvent;
    }>;
    /** Emits when the user stops dragging an item in the container. */
    readonly ended: Subject<{
        source: DragRef;
        distance: Point;
        dropPoint: Point;
        event: MouseEvent | TouchEvent;
    }>;
    /** Emits when the user has moved the item into a new container. */
    readonly entered: Subject<{
        container: DropListRefInternal;
        item: DragRef;
        currentIndex: number;
    }>;
    /** Emits when the user removes the item its container by dragging it into another container. */
    readonly exited: Subject<{
        container: DropListRefInternal;
        item: DragRef;
    }>;
    /** Emits when the user drops the item inside a container. */
    readonly dropped: Subject<{
        previousIndex: number;
        currentIndex: number;
        item: DragRef;
        container: DropListRefInternal;
        previousContainer: DropListRefInternal;
        distance: Point;
        dropPoint: Point;
        isPointerOverContainer: boolean;
        event: MouseEvent | TouchEvent;
    }>;
    /**
     * Emits as the user is dragging the item. Use with caution,
     * because this event will fire for every pixel that the user has dragged.
     */
    readonly moved: Observable<{
        source: DragRef;
        pointerPosition: {
            x: number;
            y: number;
        };
        event: MouseEvent | TouchEvent;
        distance: Point;
        delta: {
            x: -1 | 0 | 1;
            y: -1 | 0 | 1;
        };
    }>;
    /** Arbitrary data that can be attached to the drag item. */
    data: T;
    /**
     * Function that can be used to customize the logic of how the position of the drag item
     * is limited while it's being dragged. Gets called with a point containing the current position
     * of the user's pointer on the page, a reference to the item being dragged and its dimensions.
     * Should return a point describing where the item should be rendered.
     */
    constrainPosition?: (userPointerPosition: Point, dragRef: DragRef, dimensions: ClientRect, pickupPositionInElement: Point) => Point;
    constructor(element: ElementRef<HTMLElement> | HTMLElement, _config: DragRefConfig, _document: Document, _ngZone: NgZone, _viewportRuler: ViewportRuler, _dragDropRegistry: DragDropRegistry<DragRef, DropListRefInternal>);
    /**
     * Returns the element that is being used as a placeholder
     * while the current element is being dragged.
     */
    getPlaceholderElement(): HTMLElement;
    /** Returns the root draggable element. */
    getRootElement(): HTMLElement;
    /**
     * Gets the currently-visible element that represents the drag item.
     * While dragging this is the placeholder, otherwise it's the root element.
     */
    getVisibleElement(): HTMLElement;
    /** Registers the handles that can be used to drag the element. */
    withHandles(handles: (HTMLElement | ElementRef<HTMLElement>)[]): this;
    /**
     * Registers the template that should be used for the drag preview.
     * @param template Template that from which to stamp out the preview.
     */
    withPreviewTemplate(template: DragPreviewTemplate | null): this;
    /**
     * Registers the template that should be used for the drag placeholder.
     * @param template Template that from which to stamp out the placeholder.
     */
    withPlaceholderTemplate(template: DragHelperTemplate | null): this;
    /**
     * Sets an alternate drag root element. The root element is the element that will be moved as
     * the user is dragging. Passing an alternate root element is useful when trying to enable
     * dragging on an element that you might not have access to.
     */
    withRootElement(rootElement: ElementRef<HTMLElement> | HTMLElement): this;
    /**
     * Element to which the draggable's position will be constrained.
     */
    withBoundaryElement(boundaryElement: ElementRef<HTMLElement> | HTMLElement | null): this;
    /** Sets the parent ref that the ref is nested in.  */
    withParent(parent: DragRef<unknown> | null): this;
    /** Removes the dragging functionality from the DOM element. */
    dispose(): void;
    /** Checks whether the element is currently being dragged. */
    isDragging(): boolean;
    /** Resets a standalone drag item to its initial position. */
    reset(): void;
    /**
     * Sets a handle as disabled. While a handle is disabled, it'll capture and interrupt dragging.
     * @param handle Handle element that should be disabled.
     */
    disableHandle(handle: HTMLElement): void;
    /**
     * Enables a handle, if it has been disabled.
     * @param handle Handle element to be enabled.
     */
    enableHandle(handle: HTMLElement): void;
    /** Sets the layout direction of the draggable item. */
    withDirection(direction: Direction): this;
    /** Sets the container that the item is part of. */
    _withDropContainer(container: DropListRefInternal): void;
    /**
     * Gets the current position in pixels the draggable outside of a drop container.
     */
    getFreeDragPosition(): Readonly<Point>;
    /**
     * Sets the current position in pixels the draggable outside of a drop container.
     * @param value New position to be set.
     */
    setFreeDragPosition(value: Point): this;
    /**
     * Sets the container into which to insert the preview element.
     * @param value Container into which to insert the preview.
     */
    withPreviewContainer(value: PreviewContainer): this;
    /** Updates the item's sort order based on the last-known pointer position. */
    _sortFromLastPointerPosition(): void;
    /** Unsubscribes from the global subscriptions. */
    private _removeSubscriptions;
    /** Destroys the preview element and its ViewRef. */
    private _destroyPreview;
    /** Destroys the placeholder element and its ViewRef. */
    private _destroyPlaceholder;
    /** Handler for the `mousedown`/`touchstart` events. */
    private _pointerDown;
    /** Handler that is invoked when the user moves their pointer after they've initiated a drag. */
    private _pointerMove;
    /** Handler that is invoked when the user lifts their pointer up, after initiating a drag. */
    private _pointerUp;
    /**
     * Clears subscriptions and stops the dragging sequence.
     * @param event Browser event object that ended the sequence.
     */
    private _endDragSequence;
    /** Starts the dragging sequence. */
    private _startDragSequence;
    /**
     * Sets up the different variables and subscriptions
     * that will be necessary for the dragging sequence.
     * @param referenceElement Element that started the drag sequence.
     * @param event Browser event object that started the sequence.
     */
    private _initializeDragSequence;
    /** Cleans up the DOM artifacts that were added to facilitate the element being dragged. */
    private _cleanupDragArtifacts;
    /**
     * Updates the item's position in its drop container, or moves it
     * into a new one, depending on its current drag position.
     */
    private _updateActiveDropContainer;
    /**
     * Creates the element that will be rendered next to the user's pointer
     * and will be used as a preview of the element that is being dragged.
     */
    private _createPreviewElement;
    /**
     * Animates the preview element from its current position to the location of the drop placeholder.
     * @returns Promise that resolves when the animation completes.
     */
    private _animatePreviewToPlaceholder;
    /** Creates an element that will be shown instead of the current element while dragging. */
    private _createPlaceholderElement;
    /**
     * Figures out the coordinates at which an element was picked up.
     * @param referenceElement Element that initiated the dragging.
     * @param event Event that initiated the dragging.
     */
    private _getPointerPositionInElement;
    /** Determines the point of the page that was touched by the user. */
    private _getPointerPositionOnPage;
    /** Gets the pointer position on the page, accounting for any position constraints. */
    private _getConstrainedPointerPosition;
    /** Updates the current drag delta, based on the user's current pointer position on the page. */
    private _updatePointerDirectionDelta;
    /** Toggles the native drag interactions, based on how many handles are registered. */
    private _toggleNativeDragInteractions;
    /** Removes the manually-added event listeners from the root element. */
    private _removeRootElementListeners;
    /**
     * Applies a `transform` to the root element, taking into account any existing transforms on it.
     * @param x New transform value along the X axis.
     * @param y New transform value along the Y axis.
     */
    private _applyRootElementTransform;
    /**
     * Applies a `transform` to the preview, taking into account any existing transforms on it.
     * @param x New transform value along the X axis.
     * @param y New transform value along the Y axis.
     */
    private _applyPreviewTransform;
    /**
     * Gets the distance that the user has dragged during the current drag sequence.
     * @param currentPosition Current position of the user's pointer.
     */
    private _getDragDistance;
    /** Cleans up any cached element dimensions that we don't need after dragging has stopped. */
    private _cleanupCachedDimensions;
    /**
     * Checks whether the element is still inside its boundary after the viewport has been resized.
     * If not, the position is adjusted so that the element fits again.
     */
    private _containInsideBoundaryOnResize;
    /** Gets the drag start delay, based on the event type. */
    private _getDragStartDelay;
    /** Updates the internal state of the draggable element when scrolling has occurred. */
    private _updateOnScroll;
    /** Gets the scroll position of the viewport. */
    private _getViewportScrollPosition;
    /**
     * Lazily resolves and returns the shadow root of the element. We do this in a function, rather
     * than saving it in property directly on init, because we want to resolve it as late as possible
     * in order to ensure that the element has been moved into the shadow DOM. Doing it inside the
     * constructor might be too early if the element is inside of something like `ngFor` or `ngIf`.
     */
    private _getShadowRoot;
    /** Gets the element into which the drag preview should be inserted. */
    private _getPreviewInsertionPoint;
    /** Lazily resolves and returns the dimensions of the preview. */
    private _getPreviewRect;
    /** Handles a native `dragstart` event. */
    private _nativeDragStart;
    /** Gets a handle that is the target of an event. */
    private _getTargetHandle;
}

/** Object that can be used to configure the behavior of DragRef. */
export declare interface DragRefConfig {
    /**
     * Minimum amount of pixels that the user should
     * drag, before the CDK initiates a drag sequence.
     */
    dragStartThreshold: number;
    /**
     * Amount the pixels the user should drag before the CDK
     * considers them to have changed the drag direction.
     */
    pointerDirectionChangeThreshold: number;
    /** `z-index` for the absolutely-positioned elements that are created by the drag item. */
    zIndex?: number;
    /** Ref that the current drag item is nested in. */
    parentDragRef?: DragRef;
}

/**
 * Internal compile-time-only representation of a `DragRef`.
 * Used to avoid circular import issues between the `DragRef` and the `DropListRef`.
 * @docs-private
 */
declare interface DragRefInternal extends DragRef {
}

/** Possible values that can be used to configure the drag start delay. */
export declare type DragStartDelay = number | {
    touch: number;
    mouse: number;
};

/** Possible orientations for a drop list. */
export declare type DropListOrientation = 'horizontal' | 'vertical';

/**
 * Reference to a drop list. Used to manipulate or dispose of the container.
 */
export declare class DropListRef<T = any> {
    private _dragDropRegistry;
    private _ngZone;
    private _viewportRuler;
    /** Element that the drop list is attached to. */
    element: HTMLElement | ElementRef<HTMLElement>;
    /** Whether starting a dragging sequence from this container is disabled. */
    disabled: boolean;
    /** Whether sorting items within the list is disabled. */
    sortingDisabled: boolean;
    /** Locks the position of the draggable elements inside the container along the specified axis. */
    lockAxis: 'x' | 'y';
    /**
     * Whether auto-scrolling the view when the user
     * moves their pointer close to the edges is disabled.
     */
    autoScrollDisabled: boolean;
    /** Number of pixels to scroll for each frame when auto-scrolling an element. */
    autoScrollStep: number;
    /**
     * Function that is used to determine whether an item
     * is allowed to be moved into a drop container.
     */
    enterPredicate: (drag: DragRefInternal, drop: DropListRef) => boolean;
    /** Function that is used to determine whether an item can be sorted into a particular index. */
    sortPredicate: (index: number, drag: DragRefInternal, drop: DropListRef) => boolean;
    /** Emits right before dragging has started. */
    readonly beforeStarted: Subject<void>;
    /**
     * Emits when the user has moved a new drag item into this container.
     */
    readonly entered: Subject<{
        item: DragRefInternal;
        container: DropListRef;
        currentIndex: number;
    }>;
    /**
     * Emits when the user removes an item from the container
     * by dragging it into another container.
     */
    readonly exited: Subject<{
        item: DragRefInternal;
        container: DropListRef;
    }>;
    /** Emits when the user drops an item inside the container. */
    readonly dropped: Subject<{
        item: DragRefInternal;
        currentIndex: number;
        previousIndex: number;
        container: DropListRef;
        previousContainer: DropListRef;
        isPointerOverContainer: boolean;
        distance: Point;
        dropPoint: Point;
        event: MouseEvent | TouchEvent;
    }>;
    /** Emits as the user is swapping items while actively dragging. */
    readonly sorted: Subject<{
        previousIndex: number;
        currentIndex: number;
        container: DropListRef;
        item: DragRefInternal;
    }>;
    /** Arbitrary data that can be attached to the drop list. */
    data: T;
    /** Whether an item in the list is being dragged. */
    private _isDragging;
    /** Keeps track of the positions of any parent scrollable elements. */
    private _parentPositions;
    /** Strategy being used to sort items within the list. */
    private _sortStrategy;
    /** Cached `ClientRect` of the drop list. */
    private _clientRect;
    /** Draggable items in the container. */
    private _draggables;
    /** Drop lists that are connected to the current one. */
    private _siblings;
    /** Connected siblings that currently have a dragged item. */
    private _activeSiblings;
    /** Subscription to the window being scrolled. */
    private _viewportScrollSubscription;
    /** Vertical direction in which the list is currently scrolling. */
    private _verticalScrollDirection;
    /** Horizontal direction in which the list is currently scrolling. */
    private _horizontalScrollDirection;
    /** Node that is being auto-scrolled. */
    private _scrollNode;
    /** Used to signal to the current auto-scroll sequence when to stop. */
    private readonly _stopScrollTimers;
    /** Shadow root of the current element. Necessary for `elementFromPoint` to resolve correctly. */
    private _cachedShadowRoot;
    /** Reference to the document. */
    private _document;
    /** Elements that can be scrolled while the user is dragging. */
    private _scrollableElements;
    /** Initial value for the element's `scroll-snap-type` style. */
    private _initialScrollSnap;
    constructor(element: ElementRef<HTMLElement> | HTMLElement, _dragDropRegistry: DragDropRegistry<DragRefInternal, DropListRef>, _document: any, _ngZone: NgZone, _viewportRuler: ViewportRuler);
    /** Removes the drop list functionality from the DOM element. */
    dispose(): void;
    /** Whether an item from this list is currently being dragged. */
    isDragging(): boolean;
    /** Starts dragging an item. */
    start(): void;
    /**
     * Attempts to move an item into the container.
     * @param item Item that was moved into the container.
     * @param pointerX Position of the item along the X axis.
     * @param pointerY Position of the item along the Y axis.
     * @param index Index at which the item entered. If omitted, the container will try to figure it
     *   out automatically.
     */
    enter(item: DragRefInternal, pointerX: number, pointerY: number, index?: number): void;
    /**
     * Removes an item from the container after it was dragged into another container by the user.
     * @param item Item that was dragged out.
     */
    exit(item: DragRefInternal): void;
    /**
     * Drops an item into this container.
     * @param item Item being dropped into the container.
     * @param currentIndex Index at which the item should be inserted.
     * @param previousIndex Index of the item when dragging started.
     * @param previousContainer Container from which the item got dragged in.
     * @param isPointerOverContainer Whether the user's pointer was over the
     *    container when the item was dropped.
     * @param distance Distance the user has dragged since the start of the dragging sequence.
     * @param event Event that triggered the dropping sequence.
     *
     * @breaking-change 15.0.0 `previousIndex` and `event` parameters to become required.
     */
    drop(item: DragRefInternal, currentIndex: number, previousIndex: number, previousContainer: DropListRef, isPointerOverContainer: boolean, distance: Point, dropPoint: Point, event?: MouseEvent | TouchEvent): void;
    /**
     * Sets the draggable items that are a part of this list.
     * @param items Items that are a part of this list.
     */
    withItems(items: DragRefInternal[]): this;
    /** Sets the layout direction of the drop list. */
    withDirection(direction: Direction): this;
    /**
     * Sets the containers that are connected to this one. When two or more containers are
     * connected, the user will be allowed to transfer items between them.
     * @param connectedTo Other containers that the current containers should be connected to.
     */
    connectedTo(connectedTo: DropListRef[]): this;
    /**
     * Sets the orientation of the container.
     * @param orientation New orientation for the container.
     */
    withOrientation(orientation: 'vertical' | 'horizontal'): this;
    /**
     * Sets which parent elements are can be scrolled while the user is dragging.
     * @param elements Elements that can be scrolled.
     */
    withScrollableParents(elements: HTMLElement[]): this;
    /** Gets the scrollable parents that are registered with this drop container. */
    getScrollableParents(): readonly HTMLElement[];
    /**
     * Figures out the index of an item in the container.
     * @param item Item whose index should be determined.
     */
    getItemIndex(item: DragRefInternal): number;
    /**
     * Whether the list is able to receive the item that
     * is currently being dragged inside a connected drop list.
     */
    isReceiving(): boolean;
    /**
     * Sorts an item inside the container based on its position.
     * @param item Item to be sorted.
     * @param pointerX Position of the item along the X axis.
     * @param pointerY Position of the item along the Y axis.
     * @param pointerDelta Direction in which the pointer is moving along each axis.
     */
    _sortItem(item: DragRefInternal, pointerX: number, pointerY: number, pointerDelta: {
        x: number;
        y: number;
    }): void;
    /**
     * Checks whether the user's pointer is close to the edges of either the
     * viewport or the drop list and starts the auto-scroll sequence.
     * @param pointerX User's pointer position along the x axis.
     * @param pointerY User's pointer position along the y axis.
     */
    _startScrollingIfNecessary(pointerX: number, pointerY: number): void;
    /** Stops any currently-running auto-scroll sequences. */
    _stopScrolling(): void;
    /** Starts the dragging sequence within the list. */
    private _draggingStarted;
    /** Caches the positions of the configured scrollable parents. */
    private _cacheParentPositions;
    /** Resets the container to its initial state. */
    private _reset;
    /** Starts the interval that'll auto-scroll the element. */
    private _startScrollInterval;
    /**
     * Checks whether the user's pointer is positioned over the container.
     * @param x Pointer position along the X axis.
     * @param y Pointer position along the Y axis.
     */
    _isOverContainer(x: number, y: number): boolean;
    /**
     * Figures out whether an item should be moved into a sibling
     * drop container, based on its current position.
     * @param item Drag item that is being moved.
     * @param x Position of the item along the X axis.
     * @param y Position of the item along the Y axis.
     */
    _getSiblingContainerFromPosition(item: DragRefInternal, x: number, y: number): DropListRef | undefined;
    /**
     * Checks whether the drop list can receive the passed-in item.
     * @param item Item that is being dragged into the list.
     * @param x Position of the item along the X axis.
     * @param y Position of the item along the Y axis.
     */
    _canReceive(item: DragRefInternal, x: number, y: number): boolean;
    /**
     * Called by one of the connected drop lists when a dragging sequence has started.
     * @param sibling Sibling in which dragging has started.
     */
    _startReceiving(sibling: DropListRef, items: DragRefInternal[]): void;
    /**
     * Called by a connected drop list when dragging has stopped.
     * @param sibling Sibling whose dragging has stopped.
     */
    _stopReceiving(sibling: DropListRef): void;
    /**
     * Starts listening to scroll events on the viewport.
     * Used for updating the internal state of the list.
     */
    private _listenToScrollEvents;
    /**
     * Lazily resolves and returns the shadow root of the element. We do this in a function, rather
     * than saving it in property directly on init, because we want to resolve it as late as possible
     * in order to ensure that the element has been moved into the shadow DOM. Doing it inside the
     * constructor might be too early if the element is inside of something like `ngFor` or `ngIf`.
     */
    private _getShadowRoot;
    /** Notifies any siblings that may potentially receive the item. */
    private _notifyReceivingSiblings;
}

/**
 * Internal compile-time-only representation of a `DropListRef`.
 * Used to avoid circular import issues between the `DropListRef` and the `DragRef`.
 * @docs-private
 */
declare interface DropListRefInternal extends DropListRef {
}

declare namespace i1 {
    export {
        CdkDropListInternal,
        CDK_DROP_LIST,
        CdkDropList
    }
}

declare namespace i2 {
    export {
        CDK_DROP_LIST_GROUP,
        CdkDropListGroup
    }
}

declare namespace i3 {
    export {
        CdkDrag
    }
}

declare namespace i4 {
    export {
        CDK_DRAG_HANDLE,
        CdkDragHandle
    }
}

declare namespace i5 {
    export {
        CDK_DRAG_PREVIEW,
        CdkDragPreview
    }
}

declare namespace i6 {
    export {
        CDK_DRAG_PLACEHOLDER,
        CdkDragPlaceholder
    }
}


/**
 * Moves an item one index in an array to another.
 * @param array Array in which to move the item.
 * @param fromIndex Starting index of the item.
 * @param toIndex Index to which the item should be moved.
 */
export declare function moveItemInArray<T = any>(array: T[], fromIndex: number, toIndex: number): void;

/** Point on the page or within an element. */
export declare interface Point {
    x: number;
    y: number;
}

/**
 * Possible places into which the preview of a drag item can be inserted.
 * - `global` - Preview will be inserted at the bottom of the `<body>`. The advantage is that
 * you don't have to worry about `overflow: hidden` or `z-index`, but the item won't retain
 * its inherited styles.
 * - `parent` - Preview will be inserted into the parent of the drag item. The advantage is that
 * inherited styles will be preserved, but it may be clipped by `overflow: hidden` or not be
 * visible due to `z-index`. Furthermore, the preview is going to have an effect over selectors
 * like `:nth-child` and some flexbox configurations.
 * - `ElementRef<HTMLElement> | HTMLElement` - Preview will be inserted into a specific element.
 * Same advantages and disadvantages as `parent`.
 */
export declare type PreviewContainer = 'global' | 'parent' | ElementRef<HTMLElement> | HTMLElement;

/**
 * Moves an item from one array to another.
 * @param currentArray Array from which to transfer the item.
 * @param targetArray Array into which to put the item.
 * @param currentIndex Index of the item in its current array.
 * @param targetIndex Index at which to insert the item.
 */
export declare function transferArrayItem<T = any>(currentArray: T[], targetArray: T[], currentIndex: number, targetIndex: number): void;

export { }
