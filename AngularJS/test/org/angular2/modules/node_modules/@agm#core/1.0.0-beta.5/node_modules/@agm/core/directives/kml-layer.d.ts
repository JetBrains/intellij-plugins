import { EventEmitter, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { KmlMouseEvent } from './../services/google-maps-types';
import { KmlLayerManager } from './../services/managers/kml-layer-manager';
export declare class AgmKmlLayer implements OnInit, OnDestroy, OnChanges {
    private _manager;
    private _addedToManager;
    private _id;
    private _subscriptions;
    private static _kmlLayerOptions;
    /**
     * If true, the layer receives mouse events. Default value is true.
     */
    clickable: boolean;
    /**
     * By default, the input map is centered and zoomed to the bounding box of the contents of the
     * layer.
     * If this option is set to true, the viewport is left unchanged, unless the map's center and zoom
     * were never set.
     */
    preserveViewport: boolean;
    /**
     * Whether to render the screen overlays. Default true.
     */
    screenOverlays: boolean;
    /**
     * Suppress the rendering of info windows when layer features are clicked.
     */
    suppressInfoWindows: boolean;
    /**
     * The URL of the KML document to display.
     */
    url: string;
    /**
     * The z-index of the layer.
     */
    zIndex: number | null;
    /**
     * This event is fired when a feature in the layer is clicked.
     */
    layerClick: EventEmitter<KmlMouseEvent>;
    /**
     * This event is fired when the KML layers default viewport has changed.
     */
    defaultViewportChange: EventEmitter<void>;
    /**
     * This event is fired when the KML layer has finished loading.
     * At this point it is safe to read the status property to determine if the layer loaded
     * successfully.
     */
    statusChange: EventEmitter<void>;
    constructor(_manager: KmlLayerManager);
    ngOnInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    private _updatePolygonOptions(changes);
    private _addEventListeners();
    /** @internal */
    id(): string;
    /** @internal */
    toString(): string;
    /** @internal */
    ngOnDestroy(): void;
}
