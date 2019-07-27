import { AfterContentInit, EventEmitter, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { LatLng, LatLngLiteral, PolyMouseEvent } from '../services/google-maps-types';
import { PolygonManager } from '../services/managers/polygon-manager';
/**
 * AgmPolygon renders a polygon on a {@link AgmMap}
 *
 * ### Example
 * ```typescript
 * import { Component } from '@angular/core';
 *
 * @Component({
 *  selector: 'my-map-cmp',
 *  styles: [`
 *    agm-map {
 *      height: 300px;
 *    }
 * `],
 *  template: `
 *    <agm-map [latitude]="lat" [longitude]="lng" [zoom]="zoom">
 *      <agm-polygon [paths]="paths">
 *      </agm-polygon>
 *    </agm-map>
 *  `
 * })
 * export class MyMapCmp {
 *   lat: number = 0;
 *   lng: number = 0;
 *   zoom: number = 10;
 *   paths: Array<LatLngLiteral> = [
 *     { lat: 0,  lng: 10 },
 *     { lat: 0,  lng: 20 },
 *     { lat: 10, lng: 20 },
 *     { lat: 10, lng: 10 },
 *     { lat: 0,  lng: 10 }
 *   ]
 *   // Nesting paths will create a hole where they overlap;
 *   nestedPaths: Array<Array<LatLngLiteral>> = [[
 *     { lat: 0,  lng: 10 },
 *     { lat: 0,  lng: 20 },
 *     { lat: 10, lng: 20 },
 *     { lat: 10, lng: 10 },
 *     { lat: 0,  lng: 10 }
 *   ], [
 *     { lat: 0, lng: 15 },
 *     { lat: 0, lng: 20 },
 *     { lat: 5, lng: 20 },
 *     { lat: 5, lng: 15 },
 *     { lat: 0, lng: 15 }
 *   ]]
 * }
 * ```
 */
export declare class AgmPolygon implements OnDestroy, OnChanges, AfterContentInit {
    private _polygonManager;
    /**
     * Indicates whether this Polygon handles mouse events. Defaults to true.
     */
    clickable: boolean;
    /**
     * If set to true, the user can drag this shape over the map. The geodesic
     * property defines the mode of dragging. Defaults to false.
     */
    draggable: boolean;
    /**
     * If set to true, the user can edit this shape by dragging the control
     * points shown at the vertices and on each segment. Defaults to false.
     */
    editable: boolean;
    /**
     * The fill color. All CSS3 colors are supported except for extended
     * named colors.
     */
    fillColor: string;
    /**
     * The fill opacity between 0.0 and 1.0
     */
    fillOpacity: number;
    /**
     * When true, edges of the polygon are interpreted as geodesic and will
     * follow the curvature of the Earth. When false, edges of the polygon are
     * rendered as straight lines in screen space. Note that the shape of a
     * geodesic polygon may appear to change when dragged, as the dimensions
     * are maintained relative to the surface of the earth. Defaults to false.
     */
    geodesic: boolean;
    /**
     * The ordered sequence of coordinates that designates a closed loop.
     * Unlike polylines, a polygon may consist of one or more paths.
     *  As a result, the paths property may specify one or more arrays of
     * LatLng coordinates. Paths are closed automatically; do not repeat the
     * first vertex of the path as the last vertex. Simple polygons may be
     * defined using a single array of LatLngs. More complex polygons may
     * specify an array of arrays. Any simple arrays are converted into Arrays.
     * Inserting or removing LatLngs from the Array will automatically update
     * the polygon on the map.
     */
    paths: Array<LatLng | LatLngLiteral> | Array<Array<LatLng | LatLngLiteral>>;
    /**
     * The stroke color. All CSS3 colors are supported except for extended
     * named colors.
     */
    strokeColor: string;
    /**
     * The stroke opacity between 0.0 and 1.0
     */
    strokeOpacity: number;
    /**
     * The stroke width in pixels.
     */
    strokeWeight: number;
    /**
     * Whether this polygon is visible on the map. Defaults to true.
     */
    visible: boolean;
    /**
     * The zIndex compared to other polys.
     */
    zIndex: number;
    /**
     * This event is fired when the DOM click event is fired on the Polygon.
     */
    polyClick: EventEmitter<PolyMouseEvent>;
    /**
     * This event is fired when the DOM dblclick event is fired on the Polygon.
     */
    polyDblClick: EventEmitter<PolyMouseEvent>;
    /**
     * This event is repeatedly fired while the user drags the polygon.
     */
    polyDrag: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the user stops dragging the polygon.
     */
    polyDragEnd: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the user starts dragging the polygon.
     */
    polyDragStart: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the DOM mousedown event is fired on the Polygon.
     */
    polyMouseDown: EventEmitter<PolyMouseEvent>;
    /**
     * This event is fired when the DOM mousemove event is fired on the Polygon.
     */
    polyMouseMove: EventEmitter<PolyMouseEvent>;
    /**
     * This event is fired on Polygon mouseout.
     */
    polyMouseOut: EventEmitter<PolyMouseEvent>;
    /**
     * This event is fired on Polygon mouseover.
     */
    polyMouseOver: EventEmitter<PolyMouseEvent>;
    /**
     * This event is fired whe the DOM mouseup event is fired on the Polygon
     */
    polyMouseUp: EventEmitter<PolyMouseEvent>;
    /**
     * This even is fired when the Polygon is right-clicked on.
     */
    polyRightClick: EventEmitter<PolyMouseEvent>;
    private static _polygonOptionsAttributes;
    private _id;
    private _polygonAddedToManager;
    private _subscriptions;
    constructor(_polygonManager: PolygonManager);
    /** @internal */
    ngAfterContentInit(): void;
    ngOnChanges(changes: SimpleChanges): any;
    private _init();
    private _addEventListeners();
    private _updatePolygonOptions(changes);
    /** @internal */
    id(): string;
    /** @internal */
    ngOnDestroy(): void;
}
