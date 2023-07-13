import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AgmRectangle } from '../../directives/rectangle';
import { GoogleMapsAPIWrapper } from '../google-maps-api-wrapper';
import * as mapTypes from '../google-maps-types';
export declare class RectangleManager {
    private _apiWrapper;
    private _zone;
    private _rectangles;
    constructor(_apiWrapper: GoogleMapsAPIWrapper, _zone: NgZone);
    addRectangle(rectangle: AgmRectangle): void;
    /**
     * Removes the given rectangle from the map.
     */
    removeRectangle(rectangle: AgmRectangle): Promise<void>;
    setOptions(rectangle: AgmRectangle, options: mapTypes.RectangleOptions): Promise<void>;
    getBounds(rectangle: AgmRectangle): Promise<mapTypes.LatLngBounds>;
    setBounds(rectangle: AgmRectangle): Promise<void>;
    setEditable(rectangle: AgmRectangle): Promise<void>;
    setDraggable(rectangle: AgmRectangle): Promise<void>;
    setVisible(rectangle: AgmRectangle): Promise<void>;
    createEventObservable<T>(eventName: string, rectangle: AgmRectangle): Observable<T>;
}
