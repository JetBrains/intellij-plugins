import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import * as mapTypes from './google-maps-types';
import { Polyline } from './google-maps-types';
import { PolylineOptions } from './google-maps-types';
import { MapsAPILoader } from './maps-api-loader/maps-api-loader';
/**
 * Wrapper class that handles the communication with the Google Maps Javascript
 * API v3
 */
export declare class GoogleMapsAPIWrapper {
    private _loader;
    private _zone;
    private _map;
    private _mapResolver;
    constructor(_loader: MapsAPILoader, _zone: NgZone);
    createMap(el: HTMLElement, mapOptions: mapTypes.MapOptions): Promise<void>;
    setMapOptions(options: mapTypes.MapOptions): void;
    /**
     * Creates a google map marker with the map context
     */
    createMarker(options?: mapTypes.MarkerOptions, addToMap?: boolean): Promise<mapTypes.Marker>;
    createInfoWindow(options?: mapTypes.InfoWindowOptions): Promise<mapTypes.InfoWindow>;
    /**
     * Creates a google.map.Circle for the current map.
     */
    createCircle(options: mapTypes.CircleOptions): Promise<mapTypes.Circle>;
    /**
     * Creates a google.map.Rectangle for the current map.
     */
    createRectangle(options: mapTypes.RectangleOptions): Promise<mapTypes.Rectangle>;
    createPolyline(options: PolylineOptions): Promise<Polyline>;
    createPolygon(options: mapTypes.PolygonOptions): Promise<mapTypes.Polygon>;
    /**
     * Creates a new google.map.Data layer for the current map
     */
    createDataLayer(options?: mapTypes.DataOptions): Promise<mapTypes.Data>;
    /**
     * Determines if given coordinates are insite a Polygon path.
     */
    containsLocation(latLng: mapTypes.LatLngLiteral, polygon: mapTypes.Polygon): Promise<boolean>;
    subscribeToMapEvent<E>(eventName: string): Observable<E>;
    clearInstanceListeners(): void;
    setCenter(latLng: mapTypes.LatLngLiteral): Promise<void>;
    getZoom(): Promise<number>;
    getBounds(): Promise<mapTypes.LatLngBounds>;
    getMapTypeId(): Promise<mapTypes.MapTypeId>;
    setZoom(zoom: number): Promise<void>;
    getCenter(): Promise<mapTypes.LatLng>;
    panTo(latLng: mapTypes.LatLng | mapTypes.LatLngLiteral): Promise<void>;
    panBy(x: number, y: number): Promise<void>;
    fitBounds(latLng: mapTypes.LatLngBounds | mapTypes.LatLngBoundsLiteral): Promise<void>;
    panToBounds(latLng: mapTypes.LatLngBounds | mapTypes.LatLngBoundsLiteral): Promise<void>;
    /**
     * Returns the native Google Maps Map instance. Be careful when using this instance directly.
     */
    getNativeMap(): Promise<mapTypes.GoogleMap>;
    /**
     * Triggers the given event name on the map instance.
     */
    triggerMapEvent(eventName: string): Promise<void>;
}
