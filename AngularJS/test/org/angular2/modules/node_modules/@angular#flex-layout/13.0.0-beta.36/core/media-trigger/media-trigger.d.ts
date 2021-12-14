import { MatchMedia } from '../match-media/match-media';
import { BreakPointRegistry } from '../breakpoints/break-point-registry';
import { LayoutConfigOptions } from '../tokens/library-config';
import * as i0 from "@angular/core";
/**
 * Class
 */
export declare class MediaTrigger {
    protected breakpoints: BreakPointRegistry;
    protected matchMedia: MatchMedia;
    protected layoutConfig: LayoutConfigOptions;
    protected _platformId: Object;
    protected _document: any;
    constructor(breakpoints: BreakPointRegistry, matchMedia: MatchMedia, layoutConfig: LayoutConfigOptions, _platformId: Object, _document: any);
    /**
     * Manually activate range of breakpoints
     * @param list array of mediaQuery or alias strings
     */
    activate(list: string[]): void;
    /**
     * Restore original, 'real' breakpoints and emit events
     * to trigger stream notification
     */
    restore(): void;
    /**
     * Whenever window resizes, immediately auto-restore original
     * activations (if we are simulating activations)
     */
    private prepareAutoRestore;
    /**
     * Notify all matchMedia subscribers of de-activations
     *
     * Note: we must force 'matches' updates for
     *       future matchMedia::activation lookups
     */
    private deactivateAll;
    /**
     * Cache current activations as sorted, prioritized list of MediaChanges
     */
    private saveActivations;
    /**
     * Force set manual activations for specified mediaQuery list
     */
    private setActivations;
    /**
     * For specified mediaQuery list manually simulate activations or deactivations
     */
    private simulateMediaChanges;
    /**
     * Replace current registry with simulated registry...
     * Note: this is required since MediaQueryList::matches is 'readOnly'
     */
    private forceRegistryMatches;
    /**
     * Save current MatchMedia::registry items.
     */
    private cacheRegistryMatches;
    /**
     * Restore original, 'true' registry
     */
    private restoreRegistryMatches;
    /**
     * Manually emit a MediaChange event via the MatchMedia to MediaMarshaller and MediaObserver
     */
    private emitChangeEvent;
    private get currentActivations();
    private hasCachedRegistryMatches;
    private originalActivations;
    private originalRegistry;
    private resizeSubscription;
    static ɵfac: i0.ɵɵFactoryDeclaration<MediaTrigger, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MediaTrigger>;
}
