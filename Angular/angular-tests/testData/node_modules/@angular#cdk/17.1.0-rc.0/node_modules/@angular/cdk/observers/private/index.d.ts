import * as i0 from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';

/**
 * Allows observing resize events on multiple elements using a shared set of ResizeObserver.
 * Sharing a ResizeObserver instance is recommended for better performance (see
 * https://github.com/WICG/resize-observer/issues/59).
 *
 * Rather than share a single `ResizeObserver`, this class creates one `ResizeObserver` per type
 * of observed box ('content-box', 'border-box', and 'device-pixel-content-box'). This avoids
 * later calls to `observe` with a different box type from influencing the events dispatched to
 * earlier calls.
 */
export declare class SharedResizeObserver implements OnDestroy {
    /** Map of box type to shared resize observer. */
    private _observers;
    /** The Angular zone. */
    private _ngZone;
    constructor();
    ngOnDestroy(): void;
    /**
     * Gets a stream of resize events for the given target element and box type.
     * @param target The element to observe for resizes.
     * @param options Options to pass to the `ResizeObserver`
     * @return The stream of resize events for the element.
     */
    observe(target: Element, options?: ResizeObserverOptions): Observable<ResizeObserverEntry[]>;
    static ɵfac: i0.ɵɵFactoryDeclaration<SharedResizeObserver, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<SharedResizeObserver>;
}

export { }
