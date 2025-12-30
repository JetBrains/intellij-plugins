import { ElementRef, EventEmitter, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { MouseEvent } from '../map-types';
import { GoogleMapsAPIWrapper } from '../services/google-maps-api-wrapper';
import { FullscreenControlOptions, LatLngLiteral, MapTypeControlOptions, MapTypeId, PanControlOptions, RotateControlOptions, ScaleControlOptions, StreetViewControlOptions, ZoomControlOptions } from '../services/google-maps-types';
import { LatLngBounds, LatLngBoundsLiteral, MapTypeStyle } from '../services/google-maps-types';
import { FitBoundsService } from '../services/fit-bounds';
/**
 * AgmMap renders a Google Map.
 * **Important note**: To be able see a map in the browser, you have to define a height for the
 * element `agm-map`.
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
 *    </agm-map>
 *  `
 * })
 * ```
 */
export declare class AgmMap implements OnChanges, OnInit, OnDestroy {
    private _elem;
    private _mapsWrapper;
    protected _fitBoundsService: FitBoundsService;
    /**
     * The longitude that defines the center of the map.
     */
    longitude: number;
    /**
     * The latitude that defines the center of the map.
     */
    latitude: number;
    /**
     * The zoom level of the map. The default zoom level is 8.
     */
    zoom: number;
    /**
     * The minimal zoom level of the map allowed. When not provided, no restrictions to the zoom level
     * are enforced.
     */
    minZoom: number;
    /**
     * The maximal zoom level of the map allowed. When not provided, no restrictions to the zoom level
     * are enforced.
     */
    maxZoom: number;
    /**
     * Enables/disables if map is draggable.
     */
    draggable: boolean;
    /**
     * Enables/disables zoom and center on double click. Enabled by default.
     */
    disableDoubleClickZoom: boolean;
    /**
     * Enables/disables all default UI of the Google map. Please note: When the map is created, this
     * value cannot get updated.
     */
    disableDefaultUI: boolean;
    /**
     * If false, disables scrollwheel zooming on the map. The scrollwheel is enabled by default.
     */
    scrollwheel: boolean;
    /**
     * Color used for the background of the Map div. This color will be visible when tiles have not
     * yet loaded as the user pans. This option can only be set when the map is initialized.
     */
    backgroundColor: string;
    /**
     * The name or url of the cursor to display when mousing over a draggable map. This property uses
     * the css  * cursor attribute to change the icon. As with the css property, you must specify at
     * least one fallback cursor that is not a URL. For example:
     * [draggableCursor]="'url(http://www.example.com/icon.png), auto;'"
     */
    draggableCursor: string;
    /**
     * The name or url of the cursor to display when the map is being dragged. This property uses the
     * css cursor attribute to change the icon. As with the css property, you must specify at least
     * one fallback cursor that is not a URL. For example:
     * [draggingCursor]="'url(http://www.example.com/icon.png), auto;'"
     */
    draggingCursor: string;
    /**
     * If false, prevents the map from being controlled by the keyboard. Keyboard shortcuts are
     * enabled by default.
     */
    keyboardShortcuts: boolean;
    /**
     * The enabled/disabled state of the Zoom control.
     */
    zoomControl: boolean;
    /**
     * Options for the Zoom control.
     */
    zoomControlOptions: ZoomControlOptions;
    /**
     * Styles to apply to each of the default map types. Note that for Satellite/Hybrid and Terrain
     * modes, these styles will only apply to labels and geometry.
     */
    styles: MapTypeStyle[];
    /**
     * When true and the latitude and/or longitude values changes, the Google Maps panTo method is
     * used to
     * center the map. See: https://developers.google.com/maps/documentation/javascript/reference#Map
     */
    usePanning: boolean;
    /**
     * The initial enabled/disabled state of the Street View Pegman control.
     * This control is part of the default UI, and should be set to false when displaying a map type
     * on which the Street View road overlay should not appear (e.g. a non-Earth map type).
     */
    streetViewControl: boolean;
    /**
     * Options for the Street View control.
     */
    streetViewControlOptions: StreetViewControlOptions;
    /**
     * Sets the viewport to contain the given bounds.
     * If this option to `true`, the bounds get automatically computed from all elements that use the {@link AgmFitBounds} directive.
     */
    fitBounds: LatLngBoundsLiteral | LatLngBounds | boolean;
    /**
     * The initial enabled/disabled state of the Scale control. This is disabled by default.
     */
    scaleControl: boolean;
    /**
     * Options for the scale control.
     */
    scaleControlOptions: ScaleControlOptions;
    /**
     * The initial enabled/disabled state of the Map type control.
     */
    mapTypeControl: boolean;
    /**
     * Options for the Map type control.
     */
    mapTypeControlOptions: MapTypeControlOptions;
    /**
     * The initial enabled/disabled state of the Pan control.
     */
    panControl: boolean;
    /**
     * Options for the Pan control.
     */
    panControlOptions: PanControlOptions;
    /**
     * The initial enabled/disabled state of the Rotate control.
     */
    rotateControl: boolean;
    /**
     * Options for the Rotate control.
     */
    rotateControlOptions: RotateControlOptions;
    /**
     * The initial enabled/disabled state of the Fullscreen control.
     */
    fullscreenControl: boolean;
    /**
     * Options for the Fullscreen control.
     */
    fullscreenControlOptions: FullscreenControlOptions;
    /**
     * The map mapTypeId. Defaults to 'roadmap'.
     */
    mapTypeId: 'roadmap' | 'hybrid' | 'satellite' | 'terrain' | string;
    /**
     * When false, map icons are not clickable. A map icon represents a point of interest,
     * also known as a POI. By default map icons are clickable.
     */
    clickableIcons: boolean;
    /**
     * This setting controls how gestures on the map are handled.
     * Allowed values:
     * - 'cooperative' (Two-finger touch gestures pan and zoom the map. One-finger touch gestures are not handled by the map.)
     * - 'greedy'      (All touch gestures pan or zoom the map.)
     * - 'none'        (The map cannot be panned or zoomed by user gestures.)
     * - 'auto'        [default] (Gesture handling is either cooperative or greedy, depending on whether the page is scrollable or not.
     */
    gestureHandling: 'cooperative' | 'greedy' | 'none' | 'auto';
    /**
     * Map option attributes that can change over time
     */
    private static _mapOptionsAttributes;
    private _observableSubscriptions;
    private _fitBoundsSubscription;
    /**
     * This event emitter gets emitted when the user clicks on the map (but not when they click on a
     * marker or infoWindow).
     */
    mapClick: EventEmitter<MouseEvent>;
    /**
     * This event emitter gets emitted when the user right-clicks on the map (but not when they click
     * on a marker or infoWindow).
     */
    mapRightClick: EventEmitter<MouseEvent>;
    /**
     * This event emitter gets emitted when the user double-clicks on the map (but not when they click
     * on a marker or infoWindow).
     */
    mapDblClick: EventEmitter<MouseEvent>;
    /**
     * This event emitter is fired when the map center changes.
     */
    centerChange: EventEmitter<LatLngLiteral>;
    /**
     * This event is fired when the viewport bounds have changed.
     */
    boundsChange: EventEmitter<LatLngBounds>;
    /**
     * This event is fired when the mapTypeId property changes.
     */
    mapTypeIdChange: EventEmitter<MapTypeId>;
    /**
     * This event is fired when the map becomes idle after panning or zooming.
     */
    idle: EventEmitter<void>;
    /**
     * This event is fired when the zoom level has changed.
     */
    zoomChange: EventEmitter<number>;
    /**
     * This event is fired when the google map is fully initialized.
     * You get the google.maps.Map instance as a result of this EventEmitter.
     */
    mapReady: EventEmitter<any>;
    constructor(_elem: ElementRef, _mapsWrapper: GoogleMapsAPIWrapper, _fitBoundsService: FitBoundsService);
    /** @internal */
    ngOnInit(): void;
    private _initMapInstance(el);
    /** @internal */
    ngOnDestroy(): void;
    ngOnChanges(changes: SimpleChanges): void;
    private _updateMapOptionsChanges(changes);
    /**
     * Triggers a resize event on the google map instance.
     * When recenter is true, the of the google map gets called with the current lat/lng values or fitBounds value to recenter the map.
     * Returns a promise that gets resolved after the event was triggered.
     */
    triggerResize(recenter?: boolean): Promise<void>;
    private _updatePosition(changes);
    private _setCenter();
    private _fitBounds();
    private _subscribeToFitBoundsUpdates();
    protected _updateBounds(bounds: LatLngBounds | LatLngBoundsLiteral): void;
    private _isLatLngBoundsLiteral(bounds);
    private _handleMapCenterChange();
    private _handleBoundsChange();
    private _handleMapTypeIdChange();
    private _handleMapZoomChange();
    private _handleIdleEvent();
    private _handleMapMouseEvents();
}
