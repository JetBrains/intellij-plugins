import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AgmDataLayer } from './../../directives/data-layer';
import { GoogleMapsAPIWrapper } from './../google-maps-api-wrapper';
import { Data, DataOptions, Feature } from './../google-maps-types';
/**
 * Manages all Data Layers for a Google Map instance.
 */
export declare class DataLayerManager {
    private _wrapper;
    private _zone;
    private _layers;
    constructor(_wrapper: GoogleMapsAPIWrapper, _zone: NgZone);
    /**
     * Adds a new Data Layer to the map.
     */
    addDataLayer(layer: AgmDataLayer): void;
    deleteDataLayer(layer: AgmDataLayer): void;
    updateGeoJson(layer: AgmDataLayer, geoJson: Object | string): void;
    setDataOptions(layer: AgmDataLayer, options: DataOptions): void;
    /**
     * Creates a Google Maps event listener for the given DataLayer as an Observable
     */
    createEventObservable<T>(eventName: string, layer: AgmDataLayer): Observable<T>;
    /**
     * Extract features from a geoJson using google.maps Data Class
     * @param d : google.maps.Data class instance
     * @param geoJson : url or geojson object
     */
    getDataFeatures(d: Data, geoJson: Object | string): Promise<Feature[]>;
}
