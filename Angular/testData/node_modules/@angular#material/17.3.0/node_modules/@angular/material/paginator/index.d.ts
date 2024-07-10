import { ChangeDetectorRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/button';
import * as i2 from '@angular/material/select';
import * as i3 from '@angular/material/tooltip';
import { InjectionToken } from '@angular/core';
import { MatFormFieldAppearance } from '@angular/material/form-field';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Optional } from '@angular/core';
import { Subject } from 'rxjs';
import { ThemePalette } from '@angular/material/core';

declare namespace i4 {
    export {
        MatPaginatorSelectConfig,
        PageEvent,
        MatPaginatorDefaultOptions,
        MAT_PAGINATOR_DEFAULT_OPTIONS,
        MatPaginator
    }
}

/** Injection token that can be used to provide the default options for the paginator module. */
export declare const MAT_PAGINATOR_DEFAULT_OPTIONS: InjectionToken<MatPaginatorDefaultOptions>;

/** @docs-private */
export declare const MAT_PAGINATOR_INTL_PROVIDER: {
    provide: typeof MatPaginatorIntl;
    deps: Optional[][];
    useFactory: typeof MAT_PAGINATOR_INTL_PROVIDER_FACTORY;
};

/** @docs-private */
export declare function MAT_PAGINATOR_INTL_PROVIDER_FACTORY(parentIntl: MatPaginatorIntl): MatPaginatorIntl;

/**
 * Component to provide navigation between paged information. Displays the size of the current
 * page, user-selectable options to change that size, what items are being shown, and
 * navigational button to go to the previous or next page.
 */
export declare class MatPaginator implements OnInit, OnDestroy {
    _intl: MatPaginatorIntl;
    private _changeDetectorRef;
    /** If set, styles the "page size" form field with the designated style. */
    _formFieldAppearance?: MatFormFieldAppearance;
    /** ID for the DOM node containing the paginator's items per page label. */
    readonly _pageSizeLabelId: string;
    private _intlChanges;
    private _isInitialized;
    private _initializedStream;
    /** Theme color to be used for the underlying form controls. */
    color: ThemePalette;
    /** The zero-based page index of the displayed list of items. Defaulted to 0. */
    get pageIndex(): number;
    set pageIndex(value: number);
    private _pageIndex;
    /** The length of the total number of items that are being paginated. Defaulted to 0. */
    get length(): number;
    set length(value: number);
    private _length;
    /** Number of items to display on a page. By default set to 50. */
    get pageSize(): number;
    set pageSize(value: number);
    private _pageSize;
    /** The set of provided page size options to display to the user. */
    get pageSizeOptions(): number[];
    set pageSizeOptions(value: number[] | readonly number[]);
    private _pageSizeOptions;
    /** Whether to hide the page size selection UI from the user. */
    hidePageSize: boolean;
    /** Whether to show the first/last buttons UI to the user. */
    showFirstLastButtons: boolean;
    /** Used to configure the underlying `MatSelect` inside the paginator. */
    selectConfig: MatPaginatorSelectConfig;
    /** Whether the paginator is disabled. */
    disabled: boolean;
    /** Event emitted when the paginator changes the page size or page index. */
    readonly page: EventEmitter<PageEvent>;
    /** Displayed set of page size options. Will be sorted and include current page size. */
    _displayedPageSizeOptions: number[];
    /** Emits when the paginator is initialized. */
    initialized: Observable<void>;
    constructor(_intl: MatPaginatorIntl, _changeDetectorRef: ChangeDetectorRef, defaults?: MatPaginatorDefaultOptions);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Advances to the next page if it exists. */
    nextPage(): void;
    /** Move back to the previous page if it exists. */
    previousPage(): void;
    /** Move to the first page if not already there. */
    firstPage(): void;
    /** Move to the last page if not already there. */
    lastPage(): void;
    /** Whether there is a previous page. */
    hasPreviousPage(): boolean;
    /** Whether there is a next page. */
    hasNextPage(): boolean;
    /** Calculate the number of pages */
    getNumberOfPages(): number;
    /**
     * Changes the page size so that the first item displayed on the page will still be
     * displayed using the new page size.
     *
     * For example, if the page size is 10 and on the second page (items indexed 10-19) then
     * switching so that the page size is 5 will set the third page as the current page so
     * that the 10th item will still be displayed.
     */
    _changePageSize(pageSize: number): void;
    /** Checks whether the buttons for going forwards should be disabled. */
    _nextButtonsDisabled(): boolean;
    /** Checks whether the buttons for going backwards should be disabled. */
    _previousButtonsDisabled(): boolean;
    /**
     * Updates the list of page size options to display to the user. Includes making sure that
     * the page size is an option and that the list is sorted.
     */
    private _updateDisplayedPageSizeOptions;
    /** Emits an event notifying that a change of the paginator's properties has been triggered. */
    private _emitPageEvent;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatPaginator, [null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatPaginator, "mat-paginator", ["matPaginator"], { "color": { "alias": "color"; "required": false; }; "pageIndex": { "alias": "pageIndex"; "required": false; }; "length": { "alias": "length"; "required": false; }; "pageSize": { "alias": "pageSize"; "required": false; }; "pageSizeOptions": { "alias": "pageSizeOptions"; "required": false; }; "hidePageSize": { "alias": "hidePageSize"; "required": false; }; "showFirstLastButtons": { "alias": "showFirstLastButtons"; "required": false; }; "selectConfig": { "alias": "selectConfig"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, { "page": "page"; }, never, never, true, never>;
    static ngAcceptInputType_pageIndex: unknown;
    static ngAcceptInputType_length: unknown;
    static ngAcceptInputType_pageSize: unknown;
    static ngAcceptInputType_hidePageSize: unknown;
    static ngAcceptInputType_showFirstLastButtons: unknown;
    static ngAcceptInputType_disabled: unknown;
}

/** Object that can be used to configure the default options for the paginator module. */
export declare interface MatPaginatorDefaultOptions {
    /** Number of items to display on a page. By default set to 50. */
    pageSize?: number;
    /** The set of provided page size options to display to the user. */
    pageSizeOptions?: number[];
    /** Whether to hide the page size selection UI from the user. */
    hidePageSize?: boolean;
    /** Whether to show the first/last buttons UI to the user. */
    showFirstLastButtons?: boolean;
    /** The default form-field appearance to apply to the page size options selector. */
    formFieldAppearance?: MatFormFieldAppearance;
}

/**
 * To modify the labels and text displayed, create a new instance of MatPaginatorIntl and
 * include it in a custom provider
 */
export declare class MatPaginatorIntl {
    /**
     * Stream to emit from when labels are changed. Use this to notify components when the labels have
     * changed after initialization.
     */
    readonly changes: Subject<void>;
    /** A label for the page size selector. */
    itemsPerPageLabel: string;
    /** A label for the button that increments the current page. */
    nextPageLabel: string;
    /** A label for the button that decrements the current page. */
    previousPageLabel: string;
    /** A label for the button that moves to the first page. */
    firstPageLabel: string;
    /** A label for the button that moves to the last page. */
    lastPageLabel: string;
    /** A label for the range of items within the current page and the length of the whole list. */
    getRangeLabel: (page: number, pageSize: number, length: number) => string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatPaginatorIntl, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatPaginatorIntl>;
}

export declare class MatPaginatorModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatPaginatorModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatPaginatorModule, never, [typeof i1.MatButtonModule, typeof i2.MatSelectModule, typeof i3.MatTooltipModule, typeof i4.MatPaginator], [typeof i4.MatPaginator]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatPaginatorModule>;
}

/** Object that can used to configure the underlying `MatSelect` inside a `MatPaginator`. */
export declare interface MatPaginatorSelectConfig {
    /** Whether to center the active option over the trigger. */
    disableOptionCentering?: boolean;
    /** Classes to be passed to the select panel. */
    panelClass?: string | string[] | Set<string> | {
        [key: string]: any;
    };
}

/**
 * Change event object that is emitted when the user selects a
 * different page size or navigates to another page.
 */
export declare class PageEvent {
    /** The current page index. */
    pageIndex: number;
    /**
     * Index of the page that was selected previously.
     * @breaking-change 8.0.0 To be made into a required property.
     */
    previousPageIndex?: number;
    /** The current page size. */
    pageSize: number;
    /** The current total number of items being paged. */
    length: number;
}

export { }
