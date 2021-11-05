export declare type ScrollTarget = Window | Document | HTMLElement;
export declare function getScrollTop(target: ScrollTarget): number;
export declare function getRect(target: ScrollTarget): {
    top: number;
    bottom: number;
};
