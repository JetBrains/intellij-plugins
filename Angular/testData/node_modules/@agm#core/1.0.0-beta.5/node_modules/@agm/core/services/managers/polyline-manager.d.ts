import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { AgmPolyline } from '../../directives/polyline';
import { GoogleMapsAPIWrapper } from '../google-maps-api-wrapper';
export declare class PolylineManager {
    private _mapsWrapper;
    private _zone;
    private _polylines;
    constructor(_mapsWrapper: GoogleMapsAPIWrapper, _zone: NgZone);
    private static _convertPoints(line);
    addPolyline(line: AgmPolyline): void;
    updatePolylinePoints(line: AgmPolyline): Promise<void>;
    setPolylineOptions(line: AgmPolyline, options: {
        [propName: string]: any;
    }): Promise<void>;
    deletePolyline(line: AgmPolyline): Promise<void>;
    createEventObservable<T>(eventName: string, line: AgmPolyline): Observable<T>;
}
