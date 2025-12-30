import { ModuleWithProviders } from '@angular/core';
import { AgmMap } from './directives/map';
import { AgmCircle } from './directives/circle';
import { AgmRectangle } from './directives/rectangle';
import { AgmInfoWindow } from './directives/info-window';
import { AgmMarker } from './directives/marker';
import { AgmPolygon } from './directives/polygon';
import { AgmPolyline } from './directives/polyline';
import { AgmPolylinePoint } from './directives/polyline-point';
import { AgmKmlLayer } from './directives/kml-layer';
import { AgmDataLayer } from './directives/data-layer';
import { LazyMapsAPILoaderConfigLiteral } from './services/maps-api-loader/lazy-maps-api-loader';
import { AgmFitBounds } from '@agm/core/directives/fit-bounds';
/**
 * @internal
 */
export declare function coreDirectives(): (typeof AgmCircle | typeof AgmRectangle | typeof AgmMarker | typeof AgmInfoWindow | typeof AgmPolygon | typeof AgmPolylinePoint | typeof AgmPolyline | typeof AgmKmlLayer | typeof AgmDataLayer | typeof AgmMap | typeof AgmFitBounds)[];
/**
 * The angular-google-maps core module. Contains all Directives/Services/Pipes
 * of the core module. Please use `AgmCoreModule.forRoot()` in your app module.
 */
export declare class AgmCoreModule {
    /**
     * Please use this method when you register the module at the root level.
     */
    static forRoot(lazyMapsAPILoaderConfig?: LazyMapsAPILoaderConfigLiteral): ModuleWithProviders;
}
