import * as i0 from '@angular/core';
import { Type } from '@angular/core';

/**
 * Service that loads structural styles dynamically
 * and ensures that they're only loaded once per app.
 */
declare class _CdkPrivateStyleLoader {
    private _appRef;
    private _injector;
    private _environmentInjector;
    /**
     * Loads a set of styles.
     * @param loader Component which will be instantiated to load the styles.
     */
    load(loader: Type<unknown>): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<_CdkPrivateStyleLoader, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<_CdkPrivateStyleLoader>;
}

export { _CdkPrivateStyleLoader };
