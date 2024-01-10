import { BreakPoint } from './break-point';
import * as i0 from "@angular/core";
export declare type OptionalBreakPoint = BreakPoint | null;
/**
 * Registry of 1..n MediaQuery breakpoint ranges
 * This is published as a provider and may be overridden from custom, application-specific ranges
 *
 */
export declare class BreakPointRegistry {
    readonly items: BreakPoint[];
    constructor(list: BreakPoint[]);
    /**
     * Search breakpoints by alias (e.g. gt-xs)
     */
    findByAlias(alias: string): OptionalBreakPoint;
    findByQuery(query: string): OptionalBreakPoint;
    /**
     * Get all the breakpoints whose ranges could overlapping `normal` ranges;
     * e.g. gt-sm overlaps md, lg, and xl
     */
    get overlappings(): BreakPoint[];
    /**
     * Get list of all registered (non-empty) breakpoint aliases
     */
    get aliases(): string[];
    /**
     * Aliases are mapped to properties using suffixes
     * e.g.  'gt-sm' for property 'layout'  uses suffix 'GtSm'
     * for property layoutGtSM.
     */
    get suffixes(): string[];
    /**
     * Memoized lookup using custom predicate function
     */
    private findWithPredicate;
    /**
     * Memoized BreakPoint Lookups
     */
    private readonly findByMap;
    static ɵfac: i0.ɵɵFactoryDeclaration<BreakPointRegistry, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<BreakPointRegistry>;
}
