import Vue from 'vue';

declare type CreateElement = Vue['$createElement'];
declare const createElement: CreateElement;
export default createElement;
