import { EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { LatLngLiteral } from '../../core/services/google-maps-types';
/**
 * AgmPolylinePoint represents one element of a polyline within a  {@link
 * SembGoogleMapPolyline}
 */
export declare class AgmPolylinePoint implements OnChanges {
    /**
     * The latitude position of the point.
     */
    latitude: number;
    /**
     * The longitude position of the point;
     */
    longitude: number;
    /**
     * This event emitter gets emitted when the position of the point changed.
     */
    positionChanged: EventEmitter<LatLngLiteral>;
    constructor();
    ngOnChanges(changes: SimpleChanges): any;
}
