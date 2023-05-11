// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {Component, Input} from "@angular/core"

export enum DroidType {
  PROTOCOL = 'Protocol',
  MEDICAL = 'Medical',
  ASTROMECH = 'Astromech',
  ASSASSIN = 'Assassin',
}

export interface BaseDroid {
  color: string;
  name: string;
}

export enum Languages {
  DROIDSPEAK = 'Droidspeak',
  EWOKESE = 'Ewokese',
  HUTTESE = 'Huttese',
  JAWAESE = 'Jawaese',
  SITH = 'Sith',
  SHYRIIWOOK = 'Shyriiwook',
}

export interface ProtocolDroid extends BaseDroid {
  droidType: DroidType.PROTOCOL;
  spokenLanguages: Languages[];
}

export interface MedicalDroid extends BaseDroid {
  droidType: DroidType.MEDICAL;
  canHealHumans: boolean;
  canFixRobots: boolean;
}

export enum AstromechDroidShape {
  REGULAR = 'Regular',
  SPHERE = 'Sphere',
}

export interface AstromechDroid extends BaseDroid {
  droidType: DroidType.ASTROMECH;
  numberOfToolsCarried: number;
  shape: AstromechDroidShape;
}

export enum AssassinDroidWeapon {
  SABER = 'Saber',
  FLAME_THROWER = 'FlameThrower',
  GUN = 'Gun',
  AXE = 'Axe',
}

export interface AssassinDroid extends BaseDroid {
  droidType: DroidType.ASSASSIN;
  weapons: AssassinDroidWeapon[];
}

export type OneDroid = ProtocolDroid | MedicalDroid | AstromechDroid | AssassinDroid;

export enum VehicleType {
  SPACESHIP = 'Spaceship',
  SPEEDER = 'Speeder',
}

export interface BaseVehicle {
  color: string;
  canFire: boolean;
  numberOfPeopleOnBoard: number;
}

export interface Spaceship extends BaseVehicle {
  vehicleType: VehicleType.SPACESHIP;
  numberOfWings: number;
}

export interface Speeder extends BaseVehicle {
  vehicleType: VehicleType.SPEEDER;
  maximumSpeed: number;
}

export type OneVehicle = Spaceship | Speeder;

export enum ListingType {
  VEHICLE = 'Vehicle',
  DROID = 'Droid',
}

export interface BaseListing {
  id: string;
  title: string;
  imageUrl: string;
  price: number;
}

export interface VehicleListing extends BaseListing {
  listingType: ListingType.VEHICLE;
  product: OneVehicle;
}

export interface DroidListing extends BaseListing {
  listingType: ListingType.DROID;
  product: OneDroid;
}

export type OneListing = VehicleListing | DroidListing;

@Component({
  selector: 'app-listings',
  template: `
    <a *ngFor="let listing of listings">
      <h4>{{ listing.title }} ({{ listing.listingType }}) £{{ listing.price }}</h4>
      <!-- As of Angular 16 NgSwitch still doesn't narrow types -->
      <!-- this example can't work in strict templates, you need to use multiple NgIfs -->
      <p [ngSwitch]="listing.listingType">
        <span *ngSwitchCase="ListingType.DROID">
          <span [style.background-color]="listing.product.color">{{ listing.product.droidType }}</span> -
  
          <span [ngSwitch]="listing.product.droidType">
            <span *ngSwitchCase="DroidType.ASSASSIN"> Weapons: {{ listing.product.weapons.join(', ') }} </span>
            <!-- with support for type guards there would be an error on line below -->
            <span *ngSwitchCase="DroidType.ASSASSIN"> Weapons: {{ listing.product.numberOfToolsCarried }} </span>
  
            <span *ngSwitchCase="DroidType.ASTROMECH"> Number of tools: {{ listing.product.numberOfToolsCarried }} </span>
            <span *ngSwitchCase="DroidType.ASTROMECH"> Number of tools: {{ listing.product.<weak_warning descr="Unresolved variable numberOfToolsCard">numberOfToolsCard</weak_warning> }} </span>
          </span>
        </span>
      </p>
    </a>

  `
})
export class ListingsComponent {
  @Input() listings: OneListing[] = [];

  public ListingType = ListingType;

  public DroidType = DroidType;

  public VehicleType = VehicleType;
}
