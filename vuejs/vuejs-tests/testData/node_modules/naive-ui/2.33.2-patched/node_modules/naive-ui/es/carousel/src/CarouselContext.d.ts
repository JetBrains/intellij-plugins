import type { ComputedRef } from 'vue';
export interface CarouselContextValue {
    currentIndexRef: ComputedRef<number>;
    to: (index: number) => void;
    prev: () => void;
    next: () => void;
    isVertical: () => boolean;
    isHorizontal: () => boolean;
    isPrev: (slideOrIndex: HTMLElement | number) => boolean;
    isNext: (slideOrIndex: HTMLElement | number) => boolean;
    isActive: (slideOrIndex: HTMLElement | number) => boolean;
    isPrevDisabled: () => boolean;
    isNextDisabled: () => boolean;
    getSlideIndex: (slideOrIndex?: HTMLElement | number) => number;
    getSlideStyle: (slideOrIndex: HTMLElement | number) => string | Record<string, string | number> | undefined;
    addSlide: (slide?: HTMLElement) => void;
    removeSlide: (slide?: HTMLElement) => void;
    onCarouselItemClick: (index: number, event: MouseEvent) => void;
}
export declare const provideCarouselContext: (contextValue: CarouselContextValue) => void;
export declare const useCarouselContext: (location?: string, component?: string) => CarouselContextValue;
