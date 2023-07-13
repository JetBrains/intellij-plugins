import { ElementRef, EventEmitter, OnChanges, OnDestroy, OnInit, SimpleChange } from '@angular/core';
import { InfoWindowManager } from '../services/managers/info-window-manager';
import { AgmMarker } from './marker';
/**
 * AgmInfoWindow renders a info window inside a {@link AgmMarker} or standalone.
 *
 * ### Example
 * ```typescript
 * import { Component } from '@angular/core';
 *
 * @Component({
 *  selector: 'my-map-cmp',
 *  styles: [`
 *    .agm-map-container {
 *      height: 300px;
 *    }
 * `],
 *  template: `
 *    <agm-map [latitude]="lat" [longitude]="lng" [zoom]="zoom">
 *      <agm-marker [latitude]="lat" [longitude]="lng" [label]="'M'">
 *        <agm-info-window [disableAutoPan]="true">
 *          Hi, this is the content of the <strong>info window</strong>
 *        </agm-info-window>
 *      </agm-marker>
 *    </agm-map>
 *  `
 * })
 * ```
 */
export declare class AgmInfoWindow implements OnDestroy, OnChanges, OnInit {
    private _infoWindowManager;
    private _el;
    /**
     * The latitude position of the info window (only usefull if you use it ouside of a {@link
     * AgmMarker}).
     */
    latitude: number;
    /**
     * The longitude position of the info window (only usefull if you use it ouside of a {@link
     * AgmMarker}).
     */
    longitude: number;
    /**
     * Disable auto-pan on open. By default, the info window will pan the map so that it is fully
     * visible when it opens.
     */
    disableAutoPan: boolean;
    /**
     * All InfoWindows are displayed on the map in order of their zIndex, with higher values
     * displaying in front of InfoWindows with lower values. By default, InfoWindows are displayed
     * according to their latitude, with InfoWindows of lower latitudes appearing in front of
     * InfoWindows at higher latitudes. InfoWindows are always displayed in front of markers.
     */
    zIndex: number;
    /**
     * Maximum width of the infowindow, regardless of content's width. This value is only considered
     * if it is set before a call to open. To change the maximum width when changing content, call
     * close, update maxWidth, and then open.
     */
    maxWidth: number;
    /**
     * Holds the marker that is the host of the info window (if available)
     */
    hostMarker: AgmMarker;
    /**
     * Holds the native element that is used for the info window content.
     */
    content: Node;
    /**
     * Sets the open state for the InfoWindow. You can also call the open() and close() methods.
     */
    isOpen: boolean;
    /**
     * Emits an event when the info window is closed.
     */
    infoWindowClose: EventEmitter<void>;
    private static _infoWindowOptionsInputs;
    private _infoWindowAddedToManager;
    private _id;
    constructor(_infoWindowManager: InfoWindowManager, _el: ElementRef);
    ngOnInit(): void;
    /** @internal */
    ngOnChanges(changes: {
        [key: string]: SimpleChange;
    }): void;
    private _registerEventListeners();
    private _updateOpenState();
    private _setInfoWindowOptions(changes);
    /**
     * Opens the info window.
     */
    open(): Promise<void>;
    /**
     * Closes the info window.
     */
    close(): Promise<void>;
    /** @internal */
    id(): string;
    /** @internal */
    toString(): string;
    /** @internal */
    ngOnDestroy(): void;
}
