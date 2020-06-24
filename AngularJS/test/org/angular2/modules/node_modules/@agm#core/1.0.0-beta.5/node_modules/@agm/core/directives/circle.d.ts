import { EventEmitter, OnChanges, OnDestroy, OnInit, SimpleChange } from '@angular/core';
import { MouseEvent } from '../map-types';
import { LatLng, LatLngBounds, LatLngLiteral } from '../services/google-maps-types';
import { CircleManager } from '../services/managers/circle-manager';
export declare class AgmCircle implements OnInit, OnChanges, OnDestroy {
    private _manager;
    /**
     * The latitude position of the circle (required).
     */
    latitude: number;
    /**
     * The clickable position of the circle (required).
     */
    longitude: number;
    /**
     * Indicates whether this Circle handles mouse events. Defaults to true.
     */
    clickable: boolean;
    /**
     * If set to true, the user can drag this circle over the map. Defaults to false.
     */
    draggable: boolean;
    /**
     * If set to true, the user can edit this circle by dragging the control points shown at
     * the center and around the circumference of the circle. Defaults to false.
     */
    editable: boolean;
    /**
     * The fill color. All CSS3 colors are supported except for extended named colors.
     */
    fillColor: string;
    /**
     * The fill opacity between 0.0 and 1.0.
     */
    fillOpacity: number;
    /**
     * The radius in meters on the Earth's surface.
     */
    radius: number;
    /**
     * The stroke color. All CSS3 colors are supported except for extended named colors.
     */
    strokeColor: string;
    /**
     * The stroke opacity between 0.0 and 1.0
     */
    strokeOpacity: number;
    /**
     * The stroke position. Defaults to CENTER.
     * This property is not supported on Internet Explorer 8 and earlier.
     */
    strokePosition: 'CENTER' | 'INSIDE' | 'OUTSIDE';
    /**
     * The stroke width in pixels.
     */
    strokeWeight: number;
    /**
     * Whether this circle is visible on the map. Defaults to true.
     */
    visible: boolean;
    /**
     * The zIndex compared to other polys.
     */
    zIndex: number;
    /**
     * This event is fired when the circle's center is changed.
     */
    centerChange: EventEmitter<LatLngLiteral>;
    /**
     * This event emitter gets emitted when the user clicks on the circle.
     */
    circleClick: EventEmitter<MouseEvent>;
    /**
     * This event emitter gets emitted when the user clicks on the circle.
     */
    circleDblClick: EventEmitter<MouseEvent>;
    /**
     * This event is repeatedly fired while the user drags the circle.
     */
    drag: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the user stops dragging the circle.
     */
    dragEnd: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the user starts dragging the circle.
     */
    dragStart: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the DOM mousedown event is fired on the circle.
     */
    mouseDown: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the DOM mousemove event is fired on the circle.
     */
    mouseMove: EventEmitter<MouseEvent>;
    /**
     * This event is fired on circle mouseout.
     */
    mouseOut: EventEmitter<MouseEvent>;
    /**
     * This event is fired on circle mouseover.
     */
    mouseOver: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the DOM mouseup event is fired on the circle.
     */
    mouseUp: EventEmitter<MouseEvent>;
    /**
     * This event is fired when the circle's radius is changed.
     */
    radiusChange: EventEmitter<number>;
    /**
     * This event is fired when the circle is right-clicked on.
     */
    rightClick: EventEmitter<MouseEvent>;
    private _circleAddedToManager;
    private static _mapOptions;
    private _eventSubscriptions;
    constructor(_manager: CircleManager);
    /** @internal */
    ngOnInit(): void;
    /** @internal */
    ngOnChanges(changes: {
        [key: string]: SimpleChange;
    }): void;
    private _updateCircleOptionsChanges(changes);
    private _registerEventListeners();
    /** @internal */
    ngOnDestroy(): void;
    /**
     * Gets the LatLngBounds of this Circle.
     */
    getBounds(): Promise<LatLngBounds>;
    getCenter(): Promise<LatLng>;
}
