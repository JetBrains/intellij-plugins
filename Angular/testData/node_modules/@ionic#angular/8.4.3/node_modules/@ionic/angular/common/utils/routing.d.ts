import { ActivatedRouteSnapshot, DetachedRouteHandle, RouteReuseStrategy } from '@angular/router';
/**
 * Provides a way to customize when activated routes get reused.
 */
export declare class IonicRouteStrategy implements RouteReuseStrategy {
    /**
     * Whether the given route should detach for later reuse.
     */
    shouldDetach(_route: ActivatedRouteSnapshot): boolean;
    /**
     * Returns `false`, meaning the route (and its subtree) is never reattached
     */
    shouldAttach(_route: ActivatedRouteSnapshot): boolean;
    /**
     * A no-op; the route is never stored since this strategy never detaches routes for later re-use.
     */
    store(_route: ActivatedRouteSnapshot, _detachedTree: DetachedRouteHandle): void;
    /**
     * Returns `null` because this strategy does not store routes for later re-use.
     */
    retrieve(_route: ActivatedRouteSnapshot): DetachedRouteHandle | null;
    /**
     * Determines if a route should be reused.
     * This strategy returns `true` when the future route config and
     * current route config are identical and all route parameters are identical.
     */
    shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean;
}
