import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { StyleEventDetail } from '../../interface';
export declare class SkeletonText implements ComponentInterface {
    el: HTMLElement;
    /**
     * If `true`, the skeleton text will animate.
     */
    animated: boolean;
    /**
     * Emitted when the styles change.
     * @internal
     */
    ionStyle: EventEmitter<StyleEventDetail>;
    componentWillLoad(): void;
    private emitStyle;
    render(): any;
}
