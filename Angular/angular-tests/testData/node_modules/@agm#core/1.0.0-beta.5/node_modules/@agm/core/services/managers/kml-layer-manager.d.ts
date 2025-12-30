import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AgmKmlLayer } from './../../directives/kml-layer';
import { GoogleMapsAPIWrapper } from './../google-maps-api-wrapper';
import { KmlLayerOptions } from './../google-maps-types';
/**
 * Manages all KML Layers for a Google Map instance.
 */
export declare class KmlLayerManager {
    private _wrapper;
    private _zone;
    private _layers;
    constructor(_wrapper: GoogleMapsAPIWrapper, _zone: NgZone);
    /**
     * Adds a new KML Layer to the map.
     */
    addKmlLayer(layer: AgmKmlLayer): void;
    setOptions(layer: AgmKmlLayer, options: KmlLayerOptions): void;
    deleteKmlLayer(layer: AgmKmlLayer): void;
    /**
     * Creates a Google Maps event listener for the given KmlLayer as an Observable
     */
    createEventObservable<T>(eventName: string, layer: AgmKmlLayer): Observable<T>;
}
