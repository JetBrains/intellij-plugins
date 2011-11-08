import flash.display.LoaderInfo;

import mx.core.RSLData;

public function addPreloadedRSL(loaderInfo:LoaderInfo, rsl:Vector.<RSLData>):void {
  throw new Error("forbidden");
}

public function get allowDomainsInNewRSLs():Boolean {
  return true;
}

public function set allowDomainsInNewRSLs(value:Boolean):void {
}

public function get allowInsecureDomainsInNewRSLs():Boolean {
  return true;
}

public function set allowInsecureDomainsInNewRSLs(value:Boolean):void {
}