import { ElementRef, IterableDiffers, NgZone, SimpleChanges, TrackByFunction } from '@angular/core';
import { HeaderFn, ItemHeightFn } from '@ionic/core';
import { VirtualFooter } from './virtual-footer';
import { VirtualHeader } from './virtual-header';
import { VirtualItem } from './virtual-item';
export declare interface IonVirtualScroll {
    /**
     * It is important to provide this
     * if virtual item height will be significantly larger than the default
     * The approximate height of each virtual item template's cell.
     * This dimension is used to help determine how many cells should
     * be created when initialized, and to help calculate the height of
     * the scrollable area. This height value can only use `px` units.
     * Note that the actual rendered size of each cell comes from the
     * app's CSS, whereas this approximation is used to help calculate
     * initial dimensions before the item has been rendered.
     */
    approxItemHeight: number;
    /**
     * The approximate height of each header template's cell.
     * This dimension is used to help determine how many cells should
     * be created when initialized, and to help calculate the height of
     * the scrollable area. This height value can only use `px` units.
     * Note that the actual rendered size of each cell comes from the
     * app's CSS, whereas this approximation is used to help calculate
     * initial dimensions before the item has been rendered.
     */
    approxHeaderHeight: number;
    /**
     * The approximate width of each footer template's cell.
     * This dimension is used to help determine how many cells should
     * be created when initialized, and to help calculate the height of
     * the scrollable area. This height value can only use `px` units.
     * Note that the actual rendered size of each cell comes from the
     * app's CSS, whereas this approximation is used to help calculate
     * initial dimensions before the item has been rendered.
     */
    approxFooterHeight: number;
    /**
     * Section headers and the data used within its given
     * template can be dynamically created by passing a function to `headerFn`.
     * For example, a large list of contacts usually has dividers between each
     * letter in the alphabet. App's can provide their own custom `headerFn`
     * which is called with each record within the dataset. The logic within
     * the header function can decide if the header template should be used,
     * and what data to give to the header template. The function must return
     * `null` if a header cell shouldn't be created.
     */
    headerFn?: HeaderFn;
    /**
     * Section footers and the data used within its given
     * template can be dynamically created by passing a function to `footerFn`.
     * The logic within the footer function can decide if the footer template
     * should be used, and what data to give to the footer template. The function
     * must return `null` if a footer cell shouldn't be created.
     */
    footerFn?: HeaderFn;
    /**
     * The data that builds the templates within the virtual scroll.
     * It's important to note that when this data has changed, then the
     * entire virtual scroll is reset, which is an expensive operation and
     * should be avoided if possible.
     */
    items?: any[];
    /**
     * An optional function that maps each item within their height.
     * When this function is provides, heavy optimizations and fast path can be taked by
     * `ion-virtual-scroll` leading to massive performance improvements.
     *
     * This function allows to skip all DOM reads, which can be Doing so leads
     * to massive performance
     */
    itemHeight?: ItemHeightFn;
    /**
     * Same as `ngForTrackBy` which can be used on `ngFor`.
     */
    trackBy: TrackByFunction<any>;
    /**
     * This method marks the tail the items array as dirty, so they can be re-rendered.  It's equivalent to calling:  ```js    * virtualScroll.checkRange(lastItemLen, items.length - lastItemLen);    * ```
     */
    'checkEnd': () => void;
    /**
     * This method marks a subset of items as dirty, so they can be re-rendered. Items should be marked as dirty any time the content or their style changes.  The subset of items to be updated can are specifing by an offset and a length.
     */
    'checkRange': (offset: number, len?: number) => void;
    /**
     * Returns the position of the virtual item at the given index.
     */
    'positionForItem': (index: number) => Promise<number>;
}
export declare class IonVirtualScroll {
    private zone;
    private iterableDiffers;
    private differ?;
    private el;
    private refMap;
    itmTmp: VirtualItem;
    hdrTmp: VirtualHeader;
    ftrTmp: VirtualFooter;
    constructor(zone: NgZone, iterableDiffers: IterableDiffers, elementRef: ElementRef);
    ngOnChanges(changes: SimpleChanges): void;
    ngDoCheck(): void;
    private nodeRender;
    private getComponent;
}
