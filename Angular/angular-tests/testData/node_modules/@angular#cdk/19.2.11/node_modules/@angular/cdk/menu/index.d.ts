import * as i0 from '@angular/core';
import { ElementRef, Renderer2, QueryList, InjectionToken, Optional, OnDestroy, Injector, ViewContainerRef, EventEmitter, TemplateRef, OnChanges, SimpleChanges, NgZone, AfterContentInit } from '@angular/core';
import { ScrollStrategy, ConnectedPosition, OverlayRef, OverlayModule } from '../overlay-module.d-CSrPj90C.js';
import * as rxjs from 'rxjs';
import { Observable, Subject } from 'rxjs';
import { FocusOrigin } from '../focus-monitor.d-BBkiOKUH.js';
import { FocusableOption, FocusKeyManager } from '../focus-key-manager.d-DCiEwxN7.js';
import { Directionality } from '../bidi-module.d-BSI86Zrk.js';
import { TemplatePortal } from '../portal-directives.d-C698lRc2.js';
import '../scrolling-module.d-CUKr8D_p.js';
import '../data-source.d-DAIyaEMO.js';
import '../number-property.d-BzBQchZ2.js';
import '@angular/common';
import '../scrolling/index.js';
import '../platform.d-cnFZCLss.js';
import '../style-loader.d-DbvWk0ty.js';
import '../list-key-manager.d-CylnKWfo.js';

/** Item to track for mouse focus events. */
interface FocusableElement {
    /** A reference to the element to be tracked. */
    _elementRef: ElementRef<HTMLElement>;
}
/**
 * PointerFocusTracker keeps track of the currently active item under mouse focus. It also has
 * observables which emit when the users mouse enters and leaves a tracked element.
 */
declare class PointerFocusTracker<T extends FocusableElement> {
    private _renderer;
    private readonly _items;
    private _eventCleanups;
    private _itemsSubscription;
    /** Emits when an element is moused into. */
    readonly entered: Observable<T>;
    /** Emits when an element is moused out. */
    readonly exited: Observable<T>;
    /** The element currently under mouse focus. */
    activeElement?: T;
    /** The element previously under mouse focus. */
    previousElement?: T;
    constructor(_renderer: Renderer2, _items: QueryList<T>);
    /** Stop the managers listeners. */
    destroy(): void;
    /** Binds the enter/exit events on all the items. */
    private _bindEvents;
    /** Cleans up the currently-bound events. */
    private _cleanupEvents;
}

/** The relative item in the inline menu to focus after closing all popup menus. */
declare enum FocusNext {
    nextItem = 0,
    previousItem = 1,
    currentItem = 2
}
/** A single item (menu) in the menu stack. */
interface MenuStackItem {
    /** A reference to the menu stack this menu stack item belongs to. */
    menuStack?: MenuStack;
}
/** Injection token used for an implementation of MenuStack. */
declare const MENU_STACK: InjectionToken<MenuStack>;
/** Provider that provides the parent menu stack, or a new menu stack if there is no parent one. */
declare const PARENT_OR_NEW_MENU_STACK_PROVIDER: {
    provide: InjectionToken<MenuStack>;
    deps: Optional[][];
    useFactory: (parentMenuStack?: MenuStack) => MenuStack;
};
/** Provider that provides the parent menu stack, or a new inline menu stack if there is no parent one. */
declare const PARENT_OR_NEW_INLINE_MENU_STACK_PROVIDER: (orientation: "vertical" | "horizontal") => {
    provide: InjectionToken<MenuStack>;
    deps: Optional[][];
    useFactory: (parentMenuStack?: MenuStack) => MenuStack;
};
/** Options that can be provided to the close or closeAll methods. */
interface CloseOptions {
    /** The element to focus next if the close operation causes the menu stack to become empty. */
    focusNextOnEmpty?: FocusNext;
    /** Whether to focus the parent trigger after closing the menu. */
    focusParentTrigger?: boolean;
}
/** Event dispatched when a menu is closed. */
interface MenuStackCloseEvent {
    /** The menu being closed. */
    item: MenuStackItem;
    /** Whether to focus the parent trigger after closing the menu. */
    focusParentTrigger?: boolean;
}
/**
 * MenuStack allows subscribers to listen for close events (when a MenuStackItem is popped off
 * of the stack) in order to perform closing actions. Upon the MenuStack being empty it emits
 * from the `empty` observable specifying the next focus action which the listener should perform
 * as requested by the closer.
 */
declare class MenuStack {
    /** The ID of this menu stack. */
    readonly id: string;
    /** All MenuStackItems tracked by this MenuStack. */
    private readonly _elements;
    /** Emits the element which was popped off of the stack when requested by a closer. */
    private readonly _close;
    /** Emits once the MenuStack has become empty after popping off elements. */
    private readonly _empty;
    /** Emits whether any menu in the menu stack has focus. */
    private readonly _hasFocus;
    /** Observable which emits the MenuStackItem which has been requested to close. */
    readonly closed: Observable<MenuStackCloseEvent>;
    /** Observable which emits whether any menu in the menu stack has focus. */
    readonly hasFocus: Observable<boolean>;
    /**
     * Observable which emits when the MenuStack is empty after popping off the last element. It
     * emits a FocusNext event which specifies the action the closer has requested the listener
     * perform.
     */
    readonly emptied: Observable<FocusNext | undefined>;
    /**
     * Whether the inline menu associated with this menu stack is vertical or horizontal.
     * `null` indicates there is no inline menu associated with this menu stack.
     */
    private _inlineMenuOrientation;
    /** Creates a menu stack that originates from an inline menu. */
    static inline(orientation: 'vertical' | 'horizontal'): MenuStack;
    /**
     * Adds an item to the menu stack.
     * @param menu the MenuStackItem to put on the stack.
     */
    push(menu: MenuStackItem): void;
    /**
     * Pop items off of the stack up to and including `lastItem` and emit each on the close
     * observable. If the stack is empty or `lastItem` is not on the stack it does nothing.
     * @param lastItem the last item to pop off the stack.
     * @param options Options that configure behavior on close.
     */
    close(lastItem: MenuStackItem, options?: CloseOptions): void;
    /**
     * Pop items off of the stack up to but excluding `lastItem` and emit each on the close
     * observable. If the stack is empty or `lastItem` is not on the stack it does nothing.
     * @param lastItem the element which should be left on the stack
     * @return whether or not an item was removed from the stack
     */
    closeSubMenuOf(lastItem: MenuStackItem): boolean;
    /**
     * Pop off all MenuStackItems and emit each one on the `close` observable one by one.
     * @param options Options that configure behavior on close.
     */
    closeAll(options?: CloseOptions): void;
    /** Return true if this stack is empty. */
    isEmpty(): boolean;
    /** Return the length of the stack. */
    length(): number;
    /** Get the top most element on the stack. */
    peek(): MenuStackItem | undefined;
    /** Whether the menu stack is associated with an inline menu. */
    hasInlineMenu(): boolean;
    /** The orientation of the associated inline menu. */
    inlineMenuOrientation(): "vertical" | "horizontal" | null;
    /** Sets whether the menu stack contains the focused element. */
    setHasFocus(hasFocus: boolean): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MenuStack, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MenuStack>;
}

/** Injection token used to return classes implementing the Menu interface */
declare const CDK_MENU: InjectionToken<Menu>;
/** Interface which specifies Menu operations and used to break circular dependency issues */
interface Menu extends MenuStackItem {
    /** The id of the menu's host element. */
    id: string;
    /** The menu's native DOM host element. */
    nativeElement: HTMLElement;
    /** The direction items in the menu flow. */
    readonly orientation: 'horizontal' | 'vertical';
    /** Place focus on the first MenuItem in the menu. */
    focusFirstItem(focusOrigin: FocusOrigin): void;
    /** Place focus on the last MenuItem in the menu. */
    focusLastItem(focusOrigin: FocusOrigin): void;
}

/**
 * MenuAim is responsible for determining if a sibling menuitem's menu should be closed when a
 * Toggler item is hovered into. It is up to the hovered in item to call the MenuAim service in
 * order to determine if it may perform its close actions.
 */
interface MenuAim {
    /**
     * Set the Menu and its PointerFocusTracker.
     * @param menu The menu that this menu aim service controls.
     * @param pointerTracker The `PointerFocusTracker` for the given menu.
     */
    initialize(menu: Menu, pointerTracker: PointerFocusTracker<FocusableElement & Toggler>): void;
    /**
     * Calls the `doToggle` callback when it is deemed that the user is not moving towards
     * the submenu.
     * @param doToggle the function called when the user is not moving towards the submenu.
     */
    toggle(doToggle: () => void): void;
}
/** Injection token used for an implementation of MenuAim. */
declare const MENU_AIM: InjectionToken<MenuAim>;
/** An element which when hovered over may open or close a menu. */
interface Toggler {
    /** Gets the open menu, or undefined if no menu is open. */
    getMenu(): Menu | undefined;
}
/**
 * TargetMenuAim predicts if a user is moving into a submenu. It calculates the
 * trajectory of the user's mouse movement in the current menu to determine if the
 * mouse is moving towards an open submenu.
 *
 * The determination is made by calculating the slope of the users last NUM_POINTS moves where each
 * pair of points determines if the trajectory line points into the submenu. It uses consensus
 * approach by checking if at least NUM_POINTS / 2 pairs determine that the user is moving towards
 * to submenu.
 */
declare class TargetMenuAim implements MenuAim, OnDestroy {
    private readonly _ngZone;
    private readonly _renderer;
    private _cleanupMousemove;
    /** The last NUM_POINTS mouse move events. */
    private readonly _points;
    /** Reference to the root menu in which we are tracking mouse moves. */
    private _menu;
    /** Reference to the root menu's mouse manager. */
    private _pointerTracker;
    /** The id associated with the current timeout call waiting to resolve. */
    private _timeoutId;
    /** Emits when this service is destroyed. */
    private readonly _destroyed;
    ngOnDestroy(): void;
    /**
     * Set the Menu and its PointerFocusTracker.
     * @param menu The menu that this menu aim service controls.
     * @param pointerTracker The `PointerFocusTracker` for the given menu.
     */
    initialize(menu: Menu, pointerTracker: PointerFocusTracker<FocusableElement & Toggler>): void;
    /**
     * Calls the `doToggle` callback when it is deemed that the user is not moving towards
     * the submenu.
     * @param doToggle the function called when the user is not moving towards the submenu.
     */
    toggle(doToggle: () => void): void;
    /**
     * Start the delayed toggle handler if one isn't running already.
     *
     * The delayed toggle handler executes the `doToggle` callback after some period of time iff the
     * users mouse is on an item in the current menu.
     *
     * @param doToggle the function called when the user is not moving towards the submenu.
     */
    private _startTimeout;
    /** Whether the user is heading towards the open submenu. */
    private _isMovingToSubmenu;
    /** Get the bounding DOMRect for the open submenu. */
    private _getSubmenuBounds;
    /**
     * Check if a reference to the PointerFocusTracker and menu element is provided.
     * @throws an error if neither reference is provided.
     */
    private _checkConfigured;
    /** Subscribe to the root menus mouse move events and update the tracked mouse points. */
    private _subscribeToMouseMoves;
    static ɵfac: i0.ɵɵFactoryDeclaration<TargetMenuAim, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<TargetMenuAim>;
}
/**
 * CdkTargetMenuAim is a provider for the TargetMenuAim service. It can be added to an
 * element with either the `cdkMenu` or `cdkMenuBar` directive and child menu items.
 */
declare class CdkTargetMenuAim {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTargetMenuAim, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTargetMenuAim, "[cdkTargetMenuAim]", ["cdkTargetMenuAim"], {}, {}, never, never, true, never>;
}

/**
 * A grouping container for `CdkMenuItemRadio` instances, similar to a `role="radiogroup"` element.
 */
declare class CdkMenuGroup {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuGroup, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuGroup, "[cdkMenuGroup]", ["cdkMenuGroup"], {}, {}, never, never, true, never>;
}

/** Injection token used for an implementation of MenuStack. */
declare const MENU_TRIGGER: InjectionToken<CdkMenuTriggerBase>;
/** Injection token used to configure the behavior of the menu when the page is scrolled. */
declare const MENU_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;
/**
 * Abstract directive that implements shared logic common to all menu triggers.
 * This class can be extended to create custom menu trigger types.
 */
declare abstract class CdkMenuTriggerBase implements OnDestroy {
    /** The DI injector for this component. */
    readonly injector: Injector;
    /** The view container ref for this component */
    protected readonly viewContainerRef: ViewContainerRef;
    /** The menu stack in which this menu resides. */
    protected readonly menuStack: MenuStack;
    /** Function used to configure the scroll strategy for the menu. */
    protected readonly menuScrollStrategy: () => ScrollStrategy;
    /**
     * A list of preferred menu positions to be used when constructing the
     * `FlexibleConnectedPositionStrategy` for this trigger's menu.
     */
    menuPosition: ConnectedPosition[];
    /** Emits when the attached menu is requested to open */
    readonly opened: EventEmitter<void>;
    /** Emits when the attached menu is requested to close */
    readonly closed: EventEmitter<void>;
    /** Template reference variable to the menu this trigger opens */
    menuTemplateRef: TemplateRef<unknown> | null;
    /** Context data to be passed along to the menu template */
    menuData: unknown;
    /** A reference to the overlay which manages the triggered menu */
    protected overlayRef: OverlayRef | null;
    /** Emits when this trigger is destroyed. */
    protected readonly destroyed: Subject<void>;
    /** Emits when the outside pointer events listener on the overlay should be stopped. */
    protected readonly stopOutsideClicksListener: rxjs.Observable<void>;
    /** The child menu opened by this trigger. */
    protected childMenu?: Menu;
    /** The content of the menu panel opened by this trigger. */
    private _menuPortal;
    /** The injector to use for the child menu opened by this trigger. */
    private _childMenuInjector?;
    ngOnDestroy(): void;
    /** Whether the attached menu is open. */
    isOpen(): boolean;
    /** Registers a child menu as having been opened by this trigger. */
    registerChildMenu(child: Menu): void;
    /**
     * Get the portal to be attached to the overlay which contains the menu. Allows for the menu
     * content to change dynamically and be reflected in the application.
     */
    protected getMenuContentPortal(): TemplatePortal<any>;
    /**
     * Whether the given element is inside the scope of this trigger's menu stack.
     * @param element The element to check.
     * @return Whether the element is inside the scope of this trigger's menu stack.
     */
    protected isElementInsideMenuStack(element: Element): boolean;
    /** Destroy and unset the overlay reference it if exists */
    private _destroyOverlay;
    /** Gets the injector to use when creating a child menu. */
    private _getChildMenuInjector;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuTriggerBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuTriggerBase, never, never, {}, {}, never, never, true, never>;
}

/**
 * A directive that turns its host element into a trigger for a popup menu.
 * It can be combined with cdkMenuItem to create sub-menus. If the element is in a top level
 * MenuBar it will open the menu on click, or if a sibling is already opened it will open on hover.
 * If it is inside of a Menu it will open the attached Submenu on hover regardless of its sibling
 * state.
 */
declare class CdkMenuTrigger extends CdkMenuTriggerBase implements OnChanges, OnDestroy {
    private readonly _elementRef;
    private readonly _overlay;
    private readonly _ngZone;
    private readonly _changeDetectorRef;
    private readonly _inputModalityDetector;
    private readonly _directionality;
    private readonly _renderer;
    private _cleanupMouseenter;
    /** The parent menu this trigger belongs to. */
    private readonly _parentMenu;
    /** The menu aim service used by this menu. */
    private readonly _menuAim;
    constructor();
    /** Toggle the attached menu. */
    toggle(): void;
    /** Open the attached menu. */
    open(): void;
    /** Close the opened menu. */
    close(): void;
    /**
     * Get a reference to the rendered Menu if the Menu is open and rendered in the DOM.
     */
    getMenu(): Menu | undefined;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /**
     * Handles keyboard events for the menu item.
     * @param event The keyboard event to handle
     */
    _toggleOnKeydown(event: KeyboardEvent): void;
    /** Handles clicks on the menu trigger. */
    _handleClick(): void;
    /**
     * Sets whether the trigger's menu stack has focus.
     * @param hasFocus Whether the menu stack has focus.
     */
    _setHasFocus(hasFocus: boolean): void;
    /**
     * Subscribe to the mouseenter events and close any sibling menu items if this element is moused
     * into.
     */
    private _subscribeToMouseEnter;
    /** Close out any sibling menu trigger menus. */
    private _closeSiblingTriggers;
    /** Get the configuration object used to create the overlay. */
    private _getOverlayConfig;
    /** Build the position strategy for the overlay which specifies where to place the menu. */
    private _getOverlayPositionStrategy;
    /** Get the preferred positions for the opened menu relative to the menu item. */
    private _getOverlayPositions;
    /**
     * Subscribe to the MenuStack close events if this is a standalone trigger and close out the menu
     * this triggers when requested.
     */
    private _registerCloseHandler;
    /**
     * Subscribe to the overlays outside pointer events stream and handle closing out the stack if a
     * click occurs outside the menus.
     */
    private _subscribeToOutsideClicks;
    /** Subscribe to the MenuStack hasFocus events. */
    private _subscribeToMenuStackHasFocus;
    /** Subscribe to the MenuStack closed events. */
    private _subscribeToMenuStackClosed;
    /** Sets the role attribute for this trigger if needed. */
    private _setRole;
    /** Sets thte `type` attribute of the trigger. */
    private _setType;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuTrigger, "[cdkMenuTriggerFor]", ["cdkMenuTriggerFor"], { "menuTemplateRef": { "alias": "cdkMenuTriggerFor"; "required": false; }; "menuPosition": { "alias": "cdkMenuPosition"; "required": false; }; "menuData": { "alias": "cdkMenuTriggerData"; "required": false; }; }, { "opened": "cdkMenuOpened"; "closed": "cdkMenuClosed"; }, never, never, true, never>;
}

/**
 * Directive which provides the ability for an element to be focused and navigated to using the
 * keyboard when residing in a CdkMenu, CdkMenuBar, or CdkMenuGroup. It performs user defined
 * behavior when clicked.
 */
declare class CdkMenuItem implements FocusableOption, FocusableElement, Toggler, OnDestroy {
    protected readonly _dir: Directionality | null;
    readonly _elementRef: ElementRef<HTMLElement>;
    protected _ngZone: NgZone;
    private readonly _inputModalityDetector;
    private readonly _renderer;
    private _cleanupMouseEnter;
    /** The menu aim service used by this menu. */
    private readonly _menuAim;
    /** The stack of menus this menu belongs to. */
    private readonly _menuStack;
    /** The parent menu in which this menuitem resides. */
    private readonly _parentMenu;
    /** Reference to the CdkMenuItemTrigger directive if one is added to the same element */
    private readonly _menuTrigger;
    /**  Whether the CdkMenuItem is disabled - defaults to false */
    disabled: boolean;
    /**
     * The text used to locate this item during menu typeahead. If not specified,
     * the `textContent` of the item will be used.
     */
    typeaheadLabel: string | null;
    /**
     * If this MenuItem is a regular MenuItem, outputs when it is triggered by a keyboard or mouse
     * event.
     */
    readonly triggered: EventEmitter<void>;
    /** Whether the menu item opens a menu. */
    get hasMenu(): boolean;
    /**
     * The tabindex for this menu item managed internally and used for implementing roving a
     * tab index.
     */
    _tabindex: 0 | -1;
    /** Whether the item should close the menu if triggered by the spacebar. */
    protected closeOnSpacebarTrigger: boolean;
    /** Emits when the menu item is destroyed. */
    protected readonly destroyed: Subject<void>;
    constructor();
    ngOnDestroy(): void;
    /** Place focus on the element. */
    focus(): void;
    /**
     * If the menu item is not disabled and the element does not have a menu trigger attached, emit
     * on the cdkMenuItemTriggered emitter and close all open menus.
     * @param options Options the configure how the item is triggered
     *   - keepOpen: specifies that the menu should be kept open after triggering the item.
     */
    trigger(options?: {
        keepOpen: boolean;
    }): void;
    /** Return true if this MenuItem has an attached menu and it is open. */
    isMenuOpen(): boolean;
    /**
     * Get a reference to the rendered Menu if the Menu is open and it is visible in the DOM.
     * @return the menu if it is open, otherwise undefined.
     */
    getMenu(): Menu | undefined;
    /** Get the CdkMenuTrigger associated with this element. */
    getMenuTrigger(): CdkMenuTrigger | null;
    /** Get the label for this element which is required by the FocusableOption interface. */
    getLabel(): string;
    /** Reset the tabindex to -1. */
    _resetTabIndex(): void;
    /**
     * Set the tab index to 0 if not disabled and it's a focus event, or a mouse enter if this element
     * is not in a menu bar.
     */
    _setTabIndex(event?: MouseEvent): void;
    /**
     * Handles keyboard events for the menu item, specifically either triggering the user defined
     * callback or opening/closing the current menu based on whether the left or right arrow key was
     * pressed.
     * @param event the keyboard event to handle
     */
    _onKeydown(event: KeyboardEvent): void;
    /** Whether this menu item is standalone or within a menu or menu bar. */
    private _isStandaloneItem;
    /**
     * Handles the user pressing the back arrow key.
     * @param event The keyboard event.
     */
    private _backArrowPressed;
    /**
     * Handles the user pressing the forward arrow key.
     * @param event The keyboard event.
     */
    private _forwardArrowPressed;
    /**
     * Subscribe to the mouseenter events and close any sibling menu items if this element is moused
     * into.
     */
    private _setupMouseEnter;
    /**
     * Return true if the enclosing parent menu is configured in a horizontal orientation, false
     * otherwise or if no parent.
     */
    private _isParentVertical;
    /** Sets the `type` attribute of the menu item. */
    private _setType;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuItem, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuItem, "[cdkMenuItem]", ["cdkMenuItem"], { "disabled": { "alias": "cdkMenuItemDisabled"; "required": false; }; "typeaheadLabel": { "alias": "cdkMenuitemTypeaheadLabel"; "required": false; }; }, { "triggered": "cdkMenuItemTriggered"; }, never, never, true, never>;
    static ngAcceptInputType_disabled: unknown;
}

/**
 * Abstract directive that implements shared logic common to all menus.
 * This class can be extended to create custom menu types.
 */
declare abstract class CdkMenuBase extends CdkMenuGroup implements Menu, AfterContentInit, OnDestroy {
    private _focusMonitor;
    protected ngZone: NgZone;
    private _renderer;
    /** The menu's native DOM host element. */
    readonly nativeElement: HTMLElement;
    /** The stack of menus this menu belongs to. */
    readonly menuStack: MenuStack;
    /** The menu aim service used by this menu. */
    protected readonly menuAim: MenuAim | null;
    /** The directionality (text direction) of the current page. */
    protected readonly dir: Directionality | null;
    /** The id of the menu's host element. */
    id: string;
    /** All child MenuItem elements nested in this Menu. */
    readonly items: QueryList<CdkMenuItem>;
    /** The direction items in the menu flow. */
    orientation: 'horizontal' | 'vertical';
    /**
     * Whether the menu is displayed inline (i.e. always present vs a conditional popup that the
     * user triggers with a trigger element).
     */
    isInline: boolean;
    /** Handles keyboard events for the menu. */
    protected keyManager: FocusKeyManager<CdkMenuItem>;
    /** Emits when the MenuBar is destroyed. */
    protected readonly destroyed: Subject<void>;
    /** The Menu Item which triggered the open submenu. */
    protected triggerItem?: CdkMenuItem;
    /** Tracks the users mouse movements over the menu. */
    protected pointerTracker?: PointerFocusTracker<CdkMenuItem>;
    /** Whether this menu's menu stack has focus. */
    private _menuStackHasFocus;
    private _tabIndexSignal;
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /**
     * Place focus on the first MenuItem in the menu and set the focus origin.
     * @param focusOrigin The origin input mode of the focus event.
     */
    focusFirstItem(focusOrigin?: FocusOrigin): void;
    /**
     * Place focus on the last MenuItem in the menu and set the focus origin.
     * @param focusOrigin The origin input mode of the focus event.
     */
    focusLastItem(focusOrigin?: FocusOrigin): void;
    /** Gets the tabindex for this menu. */
    _getTabIndex(): 0 | -1 | null;
    /**
     * Close the open menu if the current active item opened the requested MenuStackItem.
     * @param menu The menu requested to be closed.
     * @param options Options to configure the behavior on close.
     *   - `focusParentTrigger` Whether to focus the parent trigger after closing the menu.
     */
    protected closeOpenMenu(menu: MenuStackItem, options?: {
        focusParentTrigger?: boolean;
    }): void;
    /** Setup the FocusKeyManager with the correct orientation for the menu. */
    private _setKeyManager;
    /**
     * Subscribe to the menu trigger's open events in order to track the trigger which opened the menu
     * and stop tracking it when the menu is closed.
     */
    private _subscribeToMenuOpen;
    /** Subscribe to the MenuStack close events. */
    private _subscribeToMenuStackClosed;
    /** Subscribe to the MenuStack hasFocus events. */
    private _subscribeToMenuStackHasFocus;
    /**
     * Set the PointerFocusTracker and ensure that when mouse focus changes the key manager is updated
     * with the latest menu item under mouse focus.
     */
    private _setUpPointerTracker;
    /** Handles focus landing on the host element of the menu. */
    private _handleFocus;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuBase, never, never, { "id": { "alias": "id"; "required": false; }; }, {}, ["items"], never, true, never>;
}

/**
 * Directive applied to an element which configures it as a MenuBar by setting the appropriate
 * role, aria attributes, and accessible keyboard and mouse handling logic. The component that
 * this directive is applied to should contain components marked with CdkMenuItem.
 *
 */
declare class CdkMenuBar extends CdkMenuBase implements AfterContentInit {
    /** The direction items in the menu flow. */
    readonly orientation = "horizontal";
    /** Whether the menu is displayed inline (i.e. always present vs a conditional popup that the user triggers with a trigger element). */
    readonly isInline = true;
    ngAfterContentInit(): void;
    /**
     * Handle keyboard events for the Menu.
     * @param event The keyboard event to be handled.
     */
    _handleKeyEvent(event: KeyboardEvent): void;
    /**
     * Set focus to either the current, previous or next item based on the FocusNext event, then
     * open the previous or next item.
     * @param focusNext The element to focus.
     */
    private _toggleOpenMenu;
    /** Subscribe to the MenuStack emptied events. */
    private _subscribeToMenuStackEmptied;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuBar, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuBar, "[cdkMenuBar]", ["cdkMenuBar"], {}, {}, never, never, true, never>;
}

/**
 * Directive which configures the element as a Menu which should contain child elements marked as
 * CdkMenuItem or CdkMenuGroup. Sets the appropriate role and aria-attributes for a menu and
 * contains accessible keyboard and mouse handling logic.
 *
 * It also acts as a RadioGroup for elements marked with role `menuitemradio`.
 */
declare class CdkMenu extends CdkMenuBase implements AfterContentInit, OnDestroy {
    private _parentTrigger;
    /** Event emitted when the menu is closed. */
    readonly closed: EventEmitter<void>;
    /** The direction items in the menu flow. */
    readonly orientation = "vertical";
    /** Whether the menu is displayed inline (i.e. always present vs a conditional popup that the user triggers with a trigger element). */
    readonly isInline: boolean;
    constructor();
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /**
     * Handle keyboard events for the Menu.
     * @param event The keyboard event to be handled.
     */
    _handleKeyEvent(event: KeyboardEvent): void;
    /**
     * Set focus the either the current, previous or next item based on the FocusNext event.
     * @param focusNext The element to focus.
     */
    private _toggleMenuFocus;
    /** Subscribe to the MenuStack emptied events. */
    private _subscribeToMenuStackEmptied;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenu, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenu, "[cdkMenu]", ["cdkMenu"], {}, { "closed": "closed"; }, never, never, true, never>;
}

/** Base class providing checked state for selectable MenuItems. */
declare abstract class CdkMenuItemSelectable extends CdkMenuItem {
    /** Whether the element is checked */
    checked: boolean;
    /** Whether the item should close the menu if triggered by the spacebar. */
    protected closeOnSpacebarTrigger: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuItemSelectable, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuItemSelectable, never, never, { "checked": { "alias": "cdkMenuItemChecked"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_checked: unknown;
}

/**
 * A directive providing behavior for the "menuitemradio" ARIA role, which behaves similarly to
 * a conventional radio-button. Any sibling `CdkMenuItemRadio` instances within the same `CdkMenu`
 * or `CdkMenuGroup` comprise a radio group with unique selection enforced.
 */
declare class CdkMenuItemRadio extends CdkMenuItemSelectable implements OnDestroy {
    /** The unique selection dispatcher for this radio's `CdkMenuGroup`. */
    private readonly _selectionDispatcher;
    /** An ID to identify this radio item to the `UniqueSelectionDispatcher`. */
    private _id;
    /** Function to unregister the selection dispatcher */
    private _removeDispatcherListener;
    constructor();
    ngOnDestroy(): void;
    /**
     * Toggles the checked state of the radio-button.
     * @param options Options the configure how the item is triggered
     *   - keepOpen: specifies that the menu should be kept open after triggering the item.
     */
    trigger(options?: {
        keepOpen: boolean;
    }): void;
    /** Configure the unique selection dispatcher listener in order to toggle the checked state  */
    private _registerDispatcherListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuItemRadio, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuItemRadio, "[cdkMenuItemRadio]", ["cdkMenuItemRadio"], {}, {}, never, never, true, never>;
}

/**
 * A directive providing behavior for the "menuitemcheckbox" ARIA role, which behaves similarly to a
 * conventional checkbox.
 */
declare class CdkMenuItemCheckbox extends CdkMenuItemSelectable {
    /**
     * Toggle the checked state of the checkbox.
     * @param options Options the configure how the item is triggered
     *   - keepOpen: specifies that the menu should be kept open after triggering the item.
     */
    trigger(options?: {
        keepOpen: boolean;
    }): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuItemCheckbox, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMenuItemCheckbox, "[cdkMenuItemCheckbox]", ["cdkMenuItemCheckbox"], {}, {}, never, never, true, never>;
}

/** Tracks the last open context menu trigger across the entire application. */
declare class ContextMenuTracker {
    /** The last open context menu trigger. */
    private static _openContextMenuTrigger?;
    /**
     * Close the previous open context menu and set the given one as being open.
     * @param trigger The trigger for the currently open Context Menu.
     */
    update(trigger: CdkContextMenuTrigger): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<ContextMenuTracker, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ContextMenuTracker>;
}
/** The coordinates where the context menu should open. */
type ContextMenuCoordinates = {
    x: number;
    y: number;
};
/**
 * A directive that opens a menu when a user right-clicks within its host element.
 * It is aware of nested context menus and will trigger only the lowest level non-disabled context menu.
 */
declare class CdkContextMenuTrigger extends CdkMenuTriggerBase implements OnDestroy {
    /** The CDK overlay service. */
    private readonly _overlay;
    /** The directionality of the page. */
    private readonly _directionality;
    /** The app's context menu tracking registry */
    private readonly _contextMenuTracker;
    private readonly _changeDetectorRef;
    /** Whether the context menu is disabled. */
    disabled: boolean;
    constructor();
    /**
     * Open the attached menu at the specified location.
     * @param coordinates where to open the context menu
     */
    open(coordinates: ContextMenuCoordinates): void;
    /** Close the currently opened context menu. */
    close(): void;
    /**
     * Open the context menu and closes any previously open menus.
     * @param event the mouse event which opens the context menu.
     */
    _openOnContextMenu(event: MouseEvent): void;
    /**
     * Get the configuration object used to create the overlay.
     * @param coordinates the location to place the opened menu
     */
    private _getOverlayConfig;
    /**
     * Get the position strategy for the overlay which specifies where to place the menu.
     * @param coordinates the location to place the opened menu
     */
    private _getOverlayPositionStrategy;
    /** Subscribe to the menu stack close events and close this menu when requested. */
    private _setMenuStackCloseListener;
    /**
     * Subscribe to the overlays outside pointer events stream and handle closing out the stack if a
     * click occurs outside the menus.
     * @param userEvent User-generated event that opened the menu.
     */
    private _subscribeToOutsideClicks;
    /**
     * Open the attached menu at the specified location.
     * @param userEvent User-generated event that opened the menu
     * @param coordinates where to open the context menu
     */
    private _open;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkContextMenuTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkContextMenuTrigger, "[cdkContextMenuTriggerFor]", ["cdkContextMenuTriggerFor"], { "menuTemplateRef": { "alias": "cdkContextMenuTriggerFor"; "required": false; }; "menuPosition": { "alias": "cdkContextMenuPosition"; "required": false; }; "menuData": { "alias": "cdkContextMenuTriggerData"; "required": false; }; "disabled": { "alias": "cdkContextMenuDisabled"; "required": false; }; }, { "opened": "cdkContextMenuOpened"; "closed": "cdkContextMenuClosed"; }, never, never, true, never>;
    static ngAcceptInputType_disabled: unknown;
}

/** Module that declares components and directives for the CDK menu. */
declare class CdkMenuModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMenuModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkMenuModule, never, [typeof OverlayModule, typeof CdkMenuBar, typeof CdkMenu, typeof CdkMenuItem, typeof CdkMenuItemRadio, typeof CdkMenuItemCheckbox, typeof CdkMenuTrigger, typeof CdkMenuGroup, typeof CdkContextMenuTrigger, typeof CdkTargetMenuAim], [typeof CdkMenuBar, typeof CdkMenu, typeof CdkMenuItem, typeof CdkMenuItemRadio, typeof CdkMenuItemCheckbox, typeof CdkMenuTrigger, typeof CdkMenuGroup, typeof CdkContextMenuTrigger, typeof CdkTargetMenuAim]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkMenuModule>;
}

export { CDK_MENU, CdkContextMenuTrigger, CdkMenu, CdkMenuBar, CdkMenuBase, CdkMenuGroup, CdkMenuItem, CdkMenuItemCheckbox, CdkMenuItemRadio, CdkMenuItemSelectable, CdkMenuModule, CdkMenuTrigger, CdkMenuTriggerBase, CdkTargetMenuAim, ContextMenuTracker, FocusNext, MENU_AIM, MENU_SCROLL_STRATEGY, MENU_STACK, MENU_TRIGGER, MenuStack, PARENT_OR_NEW_INLINE_MENU_STACK_PROVIDER, PARENT_OR_NEW_MENU_STACK_PROVIDER, PointerFocusTracker, TargetMenuAim };
export type { CloseOptions, ContextMenuCoordinates, FocusableElement, Menu, MenuAim, MenuStackCloseEvent, MenuStackItem, Toggler };
