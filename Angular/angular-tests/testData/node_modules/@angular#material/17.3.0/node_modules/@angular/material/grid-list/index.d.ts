import { AfterContentChecked } from '@angular/core';
import { AfterContentInit } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import { MatLine } from '@angular/material/core';
import { NumberInput } from '@angular/cdk/coercion';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';

declare namespace i2 {
    export {
        MatGridList
    }
}

declare namespace i3 {
    export {
        MatGridTile,
        MatGridTileText,
        MatGridAvatarCssMatStyler,
        MatGridTileHeaderCssMatStyler,
        MatGridTileFooterCssMatStyler
    }
}

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatGridAvatarCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridAvatarCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatGridAvatarCssMatStyler, "[mat-grid-avatar], [matGridAvatar]", never, {}, {}, never, never, true, never>;
}

export declare class MatGridList implements MatGridListBase, OnInit, AfterContentChecked, TileStyleTarget {
    private _element;
    private _dir;
    /** Number of columns being rendered. */
    private _cols;
    /** Used for determining the position of each tile in the grid. */
    private _tileCoordinator;
    /**
     * Row height value passed in by user. This can be one of three types:
     * - Number value (ex: "100px"):  sets a fixed row height to that value
     * - Ratio value (ex: "4:3"): sets the row height based on width:height ratio
     * - "Fit" mode (ex: "fit"): sets the row height to total height divided by number of rows
     */
    private _rowHeight;
    /** The amount of space between tiles. This will be something like '5px' or '2em'. */
    private _gutter;
    /** Sets position and size styles for a tile */
    private _tileStyler;
    /** Query list of tiles that are being rendered. */
    _tiles: QueryList<MatGridTile>;
    constructor(_element: ElementRef<HTMLElement>, _dir: Directionality);
    /** Amount of columns in the grid list. */
    get cols(): number;
    set cols(value: NumberInput);
    /** Size of the grid list's gutter in pixels. */
    get gutterSize(): string;
    set gutterSize(value: string);
    /** Set internal representation of row height from the user-provided value. */
    get rowHeight(): string | number;
    set rowHeight(value: string | number);
    ngOnInit(): void;
    /**
     * The layout calculation is fairly cheap if nothing changes, so there's little cost
     * to run it frequently.
     */
    ngAfterContentChecked(): void;
    /** Throw a friendly error if cols property is missing */
    private _checkCols;
    /** Default to equal width:height if rowHeight property is missing */
    private _checkRowHeight;
    /** Creates correct Tile Styler subtype based on rowHeight passed in by user */
    private _setTileStyler;
    /** Computes and applies the size and position for all children grid tiles. */
    private _layoutTiles;
    /** Sets style on the main grid-list element, given the style name and value. */
    _setListStyle(style: [string, string | null] | null): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridList, [null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatGridList, "mat-grid-list", ["matGridList"], { "cols": { "alias": "cols"; "required": false; }; "gutterSize": { "alias": "gutterSize"; "required": false; }; "rowHeight": { "alias": "rowHeight"; "required": false; }; }, {}, ["_tiles"], ["*"], true, never>;
}

/**
 * Base interface for a `MatGridList`.
 * @docs-private
 */
declare interface MatGridListBase {
    cols: number;
    gutterSize: string;
    rowHeight: number | string;
}

export declare class MatGridListModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridListModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatGridListModule, never, [typeof i1.MatLineModule, typeof i1.MatCommonModule, typeof i2.MatGridList, typeof i3.MatGridTile, typeof i3.MatGridTileText, typeof i3.MatGridTileHeaderCssMatStyler, typeof i3.MatGridTileFooterCssMatStyler, typeof i3.MatGridAvatarCssMatStyler], [typeof i2.MatGridList, typeof i3.MatGridTile, typeof i3.MatGridTileText, typeof i1.MatLineModule, typeof i1.MatCommonModule, typeof i3.MatGridTileHeaderCssMatStyler, typeof i3.MatGridTileFooterCssMatStyler, typeof i3.MatGridAvatarCssMatStyler]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatGridListModule>;
}

export declare class MatGridTile {
    private _element;
    _gridList?: MatGridListBase | undefined;
    _rowspan: number;
    _colspan: number;
    constructor(_element: ElementRef<HTMLElement>, _gridList?: MatGridListBase | undefined);
    /** Amount of rows that the grid tile takes up. */
    get rowspan(): number;
    set rowspan(value: NumberInput);
    /** Amount of columns that the grid tile takes up. */
    get colspan(): number;
    set colspan(value: NumberInput);
    /**
     * Sets the style of the grid-tile element.  Needs to be set manually to avoid
     * "Changed after checked" errors that would occur with HostBinding.
     */
    _setStyle(property: string, value: any): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridTile, [null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatGridTile, "mat-grid-tile", ["matGridTile"], { "rowspan": { "alias": "rowspan"; "required": false; }; "colspan": { "alias": "colspan"; "required": false; }; }, {}, never, ["*"], true, never>;
}

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatGridTileFooterCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridTileFooterCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatGridTileFooterCssMatStyler, "mat-grid-tile-footer", never, {}, {}, never, never, true, never>;
}

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatGridTileHeaderCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridTileHeaderCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatGridTileHeaderCssMatStyler, "mat-grid-tile-header", never, {}, {}, never, never, true, never>;
}

export declare class MatGridTileText implements AfterContentInit {
    private _element;
    _lines: QueryList<MatLine>;
    constructor(_element: ElementRef<HTMLElement>);
    ngAfterContentInit(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatGridTileText, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatGridTileText, "mat-grid-tile-header, mat-grid-tile-footer", never, {}, {}, ["_lines"], ["[mat-grid-avatar], [matGridAvatar]", "[mat-line], [matLine]", "*"], true, never>;
}


/**
 * Interface describing a tile.
 * @docs-private
 */
declare interface Tile {
    /** Amount of rows that the tile takes up. */
    rowspan: number;
    /** Amount of columns that the tile takes up. */
    colspan: number;
}

/**
 * Class for determining, from a list of tiles, the (row, col) position of each of those tiles
 * in the grid. This is necessary (rather than just rendering the tiles in normal document flow)
 * because the tiles can have a rowspan.
 *
 * The positioning algorithm greedily places each tile as soon as it encounters a gap in the grid
 * large enough to accommodate it so that the tiles still render in the same order in which they
 * are given.
 *
 * The basis of the algorithm is the use of an array to track the already placed tiles. Each
 * element of the array corresponds to a column, and the value indicates how many cells in that
 * column are already occupied; zero indicates an empty cell. Moving "down" to the next row
 * decrements each value in the tracking array (indicating that the column is one cell closer to
 * being free).
 *
 * @docs-private
 */
declare class TileCoordinator {
    /** Tracking array (see class description). */
    tracker: number[];
    /** Index at which the search for the next gap will start. */
    columnIndex: number;
    /** The current row index. */
    rowIndex: number;
    /** Gets the total number of rows occupied by tiles */
    get rowCount(): number;
    /**
     * Gets the total span of rows occupied by tiles.
     * Ex: A list with 1 row that contains a tile with rowspan 2 will have a total rowspan of 2.
     */
    get rowspan(): number;
    /** The computed (row, col) position of each tile (the output). */
    positions: TilePosition[];
    /**
     * Updates the tile positions.
     * @param numColumns Amount of columns in the grid.
     * @param tiles Tiles to be positioned.
     */
    update(numColumns: number, tiles: Tile[]): void;
    /** Calculates the row and col position of a tile. */
    private _trackTile;
    /** Finds the next available space large enough to fit the tile. */
    private _findMatchingGap;
    /** Move "down" to the next row. */
    private _nextRow;
    /**
     * Finds the end index (exclusive) of a gap given the index from which to start looking.
     * The gap ends when a non-zero value is found.
     */
    private _findGapEndIndex;
    /** Update the tile tracker to account for the given tile in the given space. */
    private _markTilePosition;
}

/**
 * Simple data structure for tile position (row, col).
 * @docs-private
 */
declare class TilePosition {
    row: number;
    col: number;
    constructor(row: number, col: number);
}

/** Object that can be styled by the `TileStyler`. */
declare interface TileStyleTarget {
    _setListStyle(style: [string, string | null] | null): void;
    _tiles: QueryList<MatGridTile>;
}

export declare const ɵTileCoordinator: typeof TileCoordinator;

export { }
