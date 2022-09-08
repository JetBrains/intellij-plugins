import type { VNode } from 'vue';
export declare function addDuplicateSlides(slides: VNode[]): VNode[];
export declare function getDisplayIndex(current: number, length: number, duplicatedable?: boolean): number;
export declare function getRealIndex(current: number, duplicatedable?: boolean): number;
export declare function getPrevIndex(current: number, length: number, duplicatedable?: boolean): number | null;
export declare function getNextIndex(current: number, length: number, duplicatedable?: boolean): number | null;
export declare function getDisplayTotalView(total: number, duplicatedable?: boolean): number;
