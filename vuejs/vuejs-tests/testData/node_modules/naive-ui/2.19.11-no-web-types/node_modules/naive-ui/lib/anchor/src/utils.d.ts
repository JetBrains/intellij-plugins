export declare type OffsetTarget = Window | Document | HTMLElement;
export declare function getOffset(el: HTMLElement, scrollTarget: OffsetTarget): {
    top: number;
    height: number;
};
