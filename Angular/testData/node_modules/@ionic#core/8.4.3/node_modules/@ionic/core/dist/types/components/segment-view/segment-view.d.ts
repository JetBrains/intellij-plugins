import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { SegmentViewScrollEvent } from './segment-view-interface';
export declare class SegmentView implements ComponentInterface {
    private scrollEndTimeout;
    private isTouching;
    el: HTMLElement;
    /**
     * If `true`, the segment view cannot be interacted with.
     */
    disabled: boolean;
    /**
     * @internal
     *
     * If `true`, the segment view is scrollable.
     * If `false`, pointer events will be disabled. This is to prevent issues with
     * quickly scrolling after interacting with a segment button.
     */
    isManualScroll?: boolean;
    /**
     * Emitted when the segment view is scrolled.
     */
    ionSegmentViewScroll: EventEmitter<SegmentViewScrollEvent>;
    handleScroll(ev: Event): void;
    /**
     * Handle touch start event to know when the user is actively dragging the segment view.
     */
    handleScrollStart(): void;
    /**
     * Handle touch end event to know when the user is no longer dragging the segment view.
     */
    handleTouchEnd(): void;
    /**
     * Reset the scroll end detection timer. This is called on every scroll event.
     */
    private resetScrollEndTimeout;
    /**
     * Check if the scroll has ended and the user is not actively touching.
     * If the conditions are met (active content is enabled and no active touch),
     * reset the scroll position and emit the scroll end event.
     */
    private checkForScrollEnd;
    /**
     * @internal
     *
     * This method is used to programmatically set the displayed segment content
     * in the segment view. Calling this method will update the `value` of the
     * corresponding segment button.
     *
     * @param id: The id of the segment content to display.
     * @param smoothScroll: Whether to animate the scroll transition.
     */
    setContent(id: string, smoothScroll?: boolean): Promise<void>;
    private getSegmentContents;
    render(): any;
}
