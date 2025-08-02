import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { PickerColumn } from '../picker-legacy/picker-interface';
/**
 * @internal
 */
export declare class PickerColumnCmp implements ComponentInterface {
    private bounceFrom;
    private lastIndex?;
    private minY;
    private maxY;
    private optHeight;
    private rotateFactor;
    private scaleFactor;
    private velocity;
    private y;
    private optsEl?;
    private gesture?;
    private rafId?;
    private tmrId?;
    private noAnimate;
    private colDidChange;
    el: HTMLElement;
    /**
     * Emitted when the selected value has changed
     * @internal
     */
    ionPickerColChange: EventEmitter<PickerColumn>;
    /** Picker column data */
    col: PickerColumn;
    protected colChanged(): void;
    connectedCallback(): Promise<void>;
    componentDidLoad(): void;
    componentDidUpdate(): void;
    disconnectedCallback(): void;
    private emitColChange;
    private setSelected;
    private update;
    private decelerate;
    private indexForY;
    private onStart;
    private onMove;
    private onEnd;
    private refresh;
    private onDomChange;
    render(): any;
}
