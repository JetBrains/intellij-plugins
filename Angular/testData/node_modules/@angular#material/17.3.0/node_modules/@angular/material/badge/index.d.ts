import { AriaDescriber } from '@angular/cdk/a11y';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i1 from '@angular/cdk/a11y';
import * as i2 from '@angular/material/core';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Renderer2 } from '@angular/core';
import { ThemePalette } from '@angular/material/core';

declare namespace i3 {
    export {
        MatBadgePosition,
        MatBadgeSize,
        _MatBadgeStyleLoader,
        MatBadge
    }
}

/** Directive to display a text badge. */
export declare class MatBadge implements OnInit, OnDestroy {
    private _ngZone;
    private _elementRef;
    private _ariaDescriber;
    private _renderer;
    private _animationMode?;
    /** The color of the badge. Can be `primary`, `accent`, or `warn`. */
    get color(): ThemePalette;
    set color(value: ThemePalette);
    private _color;
    /** Whether the badge should overlap its contents or not */
    overlap: boolean;
    /** Whether the badge is disabled. */
    disabled: boolean;
    /**
     * Position the badge should reside.
     * Accepts any combination of 'above'|'below' and 'before'|'after'
     */
    position: MatBadgePosition;
    /** The content for the badge */
    get content(): string | number | undefined | null;
    set content(newContent: string | number | undefined | null);
    private _content;
    /** Message used to describe the decorated element via aria-describedby */
    get description(): string;
    set description(newDescription: string);
    private _description;
    /** Size of the badge. Can be 'small', 'medium', or 'large'. */
    size: MatBadgeSize;
    /** Whether the badge is hidden. */
    hidden: boolean;
    /** Unique id for the badge */
    _id: number;
    /** Visible badge element. */
    private _badgeElement;
    /** Inline badge description. Used when the badge is applied to non-interactive host elements. */
    private _inlineBadgeDescription;
    /** Whether the OnInit lifecycle hook has run yet */
    private _isInitialized;
    /** InteractivityChecker to determine if the badge host is focusable. */
    private _interactivityChecker;
    private _document;
    constructor(_ngZone: NgZone, _elementRef: ElementRef<HTMLElement>, _ariaDescriber: AriaDescriber, _renderer: Renderer2, _animationMode?: string | undefined);
    /** Whether the badge is above the host or not */
    isAbove(): boolean;
    /** Whether the badge is after the host or not */
    isAfter(): boolean;
    /**
     * Gets the element into which the badge's content is being rendered. Undefined if the element
     * hasn't been created (e.g. if the badge doesn't have content).
     */
    getBadgeElement(): HTMLElement | undefined;
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Gets whether the badge's host element is interactive. */
    private _isHostInteractive;
    /** Creates the badge element */
    private _createBadgeElement;
    /** Update the text content of the badge element in the DOM, creating the element if necessary. */
    private _updateRenderedContent;
    /** Updates the host element's aria description via AriaDescriber. */
    private _updateDescription;
    private _updateInlineDescription;
    private _removeInlineDescription;
    /** Adds css theme class given the color to the component host */
    private _setColor;
    /** Clears any existing badges that might be left over from server-side rendering. */
    private _clearExistingBadges;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatBadge, [null, null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatBadge, "[matBadge]", never, { "color": { "alias": "matBadgeColor"; "required": false; }; "overlap": { "alias": "matBadgeOverlap"; "required": false; }; "disabled": { "alias": "matBadgeDisabled"; "required": false; }; "position": { "alias": "matBadgePosition"; "required": false; }; "content": { "alias": "matBadge"; "required": false; }; "description": { "alias": "matBadgeDescription"; "required": false; }; "size": { "alias": "matBadgeSize"; "required": false; }; "hidden": { "alias": "matBadgeHidden"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_overlap: unknown;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_hidden: unknown;
}

export declare class MatBadgeModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatBadgeModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatBadgeModule, never, [typeof i1.A11yModule, typeof i2.MatCommonModule, typeof i3.MatBadge, typeof i3._MatBadgeStyleLoader], [typeof i3.MatBadge, typeof i2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatBadgeModule>;
}

/** Allowed position options for matBadgePosition */
export declare type MatBadgePosition = 'above after' | 'above before' | 'below before' | 'below after' | 'before' | 'after' | 'above' | 'below';

/** Allowed size options for matBadgeSize */
export declare type MatBadgeSize = 'small' | 'medium' | 'large';

/**
 * Component used to load the structural styles of the badge.
 * @docs-private
 */
declare class _MatBadgeStyleLoader {
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatBadgeStyleLoader, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<_MatBadgeStyleLoader, "ng-component", never, {}, {}, never, never, true, never>;
}

export { }
