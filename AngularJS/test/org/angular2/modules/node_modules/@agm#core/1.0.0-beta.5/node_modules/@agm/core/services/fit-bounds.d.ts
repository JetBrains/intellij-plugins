import { BehaviorSubject, Observable } from 'rxjs';
import { LatLng, LatLngBounds, LatLngLiteral } from './google-maps-types';
import { MapsAPILoader } from './maps-api-loader/maps-api-loader';
export interface FitBoundsDetails {
    latLng: LatLng | LatLngLiteral;
}
/**
 * @internal
 */
export declare type BoundsMap = Map<string, LatLng | LatLngLiteral>;
/**
 * Class to implement when you what to be able to make it work with the auto fit bounds feature
 * of AGM.
 */
export declare abstract class FitBoundsAccessor {
    abstract getFitBoundsDetails$(): Observable<FitBoundsDetails>;
}
/**
 * The FitBoundsService is responsible for computing the bounds of the a single map.
 */
export declare class FitBoundsService {
    protected readonly bounds$: Observable<LatLngBounds>;
    protected readonly _boundsChangeSampleTime$: BehaviorSubject<number>;
    protected readonly _includeInBounds$: BehaviorSubject<Map<string, LatLng | LatLngLiteral>>;
    constructor(loader: MapsAPILoader);
    private _generateBounds(includeInBounds);
    addToBounds(latLng: LatLng | LatLngLiteral): void;
    removeFromBounds(latLng: LatLng | LatLngLiteral): void;
    changeFitBoundsChangeSampleTime(timeMs: number): void;
    getBounds$(): Observable<LatLngBounds>;
    protected _createIdentifier(latLng: LatLng | LatLngLiteral): string;
}
