import { OverlayContainer } from '../overlay-module.d-CSrPj90C.js';
export { CdkConnectedOverlay, CdkOverlayOrigin, ConnectedOverlayPositionChange, ConnectedPosition, ConnectionPositionPair, FlexibleConnectedPositionStrategy, FlexibleConnectedPositionStrategyOrigin, HorizontalConnectionPos, OriginConnectionPosition, OverlayConfig, OverlayConnectionPosition, OverlayKeyboardDispatcher, OverlayModule, OverlayOutsideClickDispatcher, OverlayRef, OverlaySizeConfig, PositionStrategy, STANDARD_DROPDOWN_ADJACENT_POSITIONS, STANDARD_DROPDOWN_BELOW_POSITIONS, ScrollStrategy, ScrollingVisibility, VerticalConnectionPos, validateHorizontalPosition, validateVerticalPosition } from '../overlay-module.d-CSrPj90C.js';
export { CdkScrollable, ScrollDispatcher, CdkFixedSizeVirtualScroll as ɵɵCdkFixedSizeVirtualScroll, CdkScrollableModule as ɵɵCdkScrollableModule, CdkVirtualForOf as ɵɵCdkVirtualForOf, CdkVirtualScrollViewport as ɵɵCdkVirtualScrollViewport, CdkVirtualScrollableElement as ɵɵCdkVirtualScrollableElement, CdkVirtualScrollableWindow as ɵɵCdkVirtualScrollableWindow } from '../scrolling-module.d-CUKr8D_p.js';
export { ViewportRuler } from '../scrolling/index.js';
export { BlockScrollStrategy, CloseScrollStrategy, GlobalPositionStrategy, NoopScrollStrategy, Overlay, OverlayPositionBuilder, RepositionScrollStrategy, RepositionScrollStrategyConfig, ScrollStrategyOptions } from '../overlay.d-CPV_bcvH.js';
import * as i0 from '@angular/core';
import { OnDestroy } from '@angular/core';
export { ComponentType } from '../portal-directives.d-C698lRc2.js';
export { Dir as ɵɵDir } from '../bidi-module.d-BSI86Zrk.js';
import '@angular/common';
import 'rxjs';
import '../platform.d-cnFZCLss.js';
import '../style-loader.d-DbvWk0ty.js';
import '../data-source.d-DAIyaEMO.js';
import '../number-property.d-BzBQchZ2.js';

/**
 * Alternative to OverlayContainer that supports correct displaying of overlay elements in
 * Fullscreen mode
 * https://developer.mozilla.org/en-US/docs/Web/API/Element/requestFullScreen
 *
 * Should be provided in the root component.
 */
declare class FullscreenOverlayContainer extends OverlayContainer implements OnDestroy {
    private _renderer;
    private _fullScreenEventName;
    private _cleanupFullScreenListener;
    constructor(...args: unknown[]);
    ngOnDestroy(): void;
    protected _createContainer(): void;
    private _adjustParentForFullscreenChange;
    private _getEventName;
    /**
     * When the page is put into fullscreen mode, a specific element is specified.
     * Only that element and its children are visible when in fullscreen mode.
     */
    getFullscreenElement(): Element;
    static ɵfac: i0.ɵɵFactoryDeclaration<FullscreenOverlayContainer, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FullscreenOverlayContainer>;
}

export { FullscreenOverlayContainer, OverlayContainer };
