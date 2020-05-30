import { OnInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { FitBoundsService, FitBoundsAccessor } from '../services/fit-bounds';
/**
 * Adds the given directive to the auto fit bounds feature when the value is true.
 * To make it work with you custom AGM component, you also have to implement the {@link FitBoundsAccessor} abstract class.
 * @example
 * <agm-marker [agmFitBounds]="true"></agm-marker>
 */
export declare class AgmFitBounds implements OnInit, OnDestroy, OnChanges {
    private readonly _fitBoundsAccessor;
    private readonly _fitBoundsService;
    /**
     * If the value is true, the element gets added to the bounds of the map.
     * Default: true.
     */
    agmFitBounds: boolean;
    private _destroyed$;
    private _latestFitBoundsDetails;
    constructor(_fitBoundsAccessor: FitBoundsAccessor, _fitBoundsService: FitBoundsService);
    /**
     * @internal
     */
    ngOnChanges(changes: SimpleChanges): void;
    /**
     * @internal
     */
    ngOnInit(): void;
    private _updateBounds(newFitBoundsDetails?);
    /**
     * @internal
     */
    ngOnDestroy(): void;
}
