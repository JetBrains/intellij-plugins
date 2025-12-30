import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AgmCircle } from '../../directives/circle';
import { GoogleMapsAPIWrapper } from '../google-maps-api-wrapper';
import * as mapTypes from '../google-maps-types';
export declare class CircleManager {
    private _apiWrapper;
    private _zone;
    private _circles;
    constructor(_apiWrapper: GoogleMapsAPIWrapper, _zone: NgZone);
    addCircle(circle: AgmCircle): void;
    /**
     * Removes the given circle from the map.
     */
    removeCircle(circle: AgmCircle): Promise<void>;
    setOptions(circle: AgmCircle, options: mapTypes.CircleOptions): Promise<void>;
    getBounds(circle: AgmCircle): Promise<mapTypes.LatLngBounds>;
    getCenter(circle: AgmCircle): Promise<mapTypes.LatLng>;
    getRadius(circle: AgmCircle): Promise<number>;
    setCenter(circle: AgmCircle): Promise<void>;
    setEditable(circle: AgmCircle): Promise<void>;
    setDraggable(circle: AgmCircle): Promise<void>;
    setVisible(circle: AgmCircle): Promise<void>;
    setRadius(circle: AgmCircle): Promise<void>;
    createEventObservable<T>(eventName: string, circle: AgmCircle): Observable<T>;
}
