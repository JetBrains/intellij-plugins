package {
import flash.display.LoaderInfo;
import flash.display.Sprite;
import flash.display.Stage;
import flash.events.Event;
import flash.events.IOErrorEvent;
import flash.events.ProgressEvent;
import flash.events.SecurityErrorEvent;
import flash.net.Socket;
import flash.sampler.DeleteObjectSample;
import flash.sampler.NewObjectSample;
import flash.sampler.Sample;
import flash.sampler.clearSamples;
import flash.sampler.getMemberNames;
import flash.sampler.getSamples;
import flash.sampler.getSize;
import flash.sampler.isGetterSetter;
import flash.sampler.pauseSampling;
import flash.sampler.startSampling;
import flash.system.System;
import flash.utils.Dictionary;
import flash.utils.getQualifiedClassName;

public class ProfilerAgent extends Sprite {
  private var connected:Boolean;
  private var socket:Socket = new Socket();
  private var running:Boolean = false;

  private var stringDict:Object = {};
  private var stringDictSize:uint = 0;
  private var lastCPUSample:Sample = null;
  private var id2SampleInfo:Dictionary = new Dictionary();
  private var object2Id:Dictionary = new Dictionary(true);
  private var clsStat:Dictionary = new Dictionary();
  private var cpuSamplingStarted:Boolean;

  private static const ALL_COMPLETE_TYPE:String = "allComplete";
  private var startedTime:Number;

  public function ProfilerAgent() {
//    socket.timeout = 1000; // Flex 3 does not have timeout for socket
    socket.addEventListener(ProgressEvent.SOCKET_DATA, socketDataHandler);
    socket.addEventListener(Event.CLOSE, closeHandler);
    socket.addEventListener(Event.CONNECT, connectHandler);
    socket.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
    socket.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);

    addEventListener(ALL_COMPLETE_TYPE, allComplete);

    try {
      socket.connect(loaderInfo.parameters.host || "127.0.0.1", loaderInfo.parameters.port || 1310);
    } catch(e:Error) {
      trace("unexpected connect exception:"+e);
    }
  }

  private function clearSlidingStuff():void {
    stringDict = {};
    stringDictSize = 0;
    lastCPUSample = null;
  }

  private function allComplete(e:Event):void {
    try {
      var stage:Stage = Sprite(LoaderInfo(e.target).content.root).stage;
      stage.addEventListener(Event.ENTER_FRAME, onEnterFrame);

      removeEventListener(ALL_COMPLETE_TYPE, allComplete);
      trace("profiler is live");
    } catch(e:Error) {
      trace(e);
      return;
    }

    startedTime = new Date().getTime();
    running = true;
    startSampling();
  }

  private function onEnterFrame(event:Event):void {
    pauseSampling();
    running = true;

    for each(var s:Sample in getSamples()) {
      if (s == null) continue; // observed for player 10.1 COMPILE::PLAYER10_1 ?
      
      if (s is NewObjectSample) {
        var nos:NewObjectSample = NewObjectSample(s);

        if (nos.object == undefined) continue; // object already gc'd
        if (!(nos.object is QName)) object2Id[nos.object] = nos.id; // TODO:
        id2SampleInfo[nos.id] = [nos.type, nos.stack, getSize(nos.object)];
        var object:* = clsStat[nos.type];
        if (object == null) object = 0;
        clsStat[nos.type] = object + 1;
      } else if (s is DeleteObjectSample) {
        var deletedObjectSample:DeleteObjectSample = DeleteObjectSample(s);
        var info:Array = id2SampleInfo[deletedObjectSample.id];
        if (info == null) continue; // skip just collected or not registered
        delete id2SampleInfo[deletedObjectSample.id];
        var cls:Class = info[0];
        var object:* = clsStat[cls];
        if (object == null) object = 0;
        clsStat[cls] = object - 1;
      } else if (cpuSamplingStarted && connected) {
        var lastSampleToCheck:Sample = lastCPUSample;
        lastCPUSample = s;
        var stackFrameCount:uint = (s.stack == null) ? 0:s.stack.length;
        socket.writeUTF("s\x00" + (s.time - startedTime) + " " + s.stack.length );

        var matchedCount:int = 0;
        var key:String;

        if (lastSampleToCheck != null && lastSampleToCheck.stack != null && s.stack != null) {
          for(var i:int = lastSampleToCheck.stack.length - 1, j:int = s.stack.length - 1; i >= 0 && j >=0; --i, --j) {
            key = s.stack[j].toString();
            var key2:String = lastSampleToCheck.stack[i].toString();
            if (key != key2) {
              break;
            }
            matchedCount++;
          }
        }

        if (stackFrameCount > matchedCount) {
          writeStack(s.stack, stackFrameCount, matchedCount);
        }
        if (matchedCount != 0) socket.writeUTF("u>:" + matchedCount);
      }
    }

    if (connected && cpuSamplingStarted) {
      socket.flush();
    }

    clearSamples();
    startSampling();
  }

  private function writeStack(stack:Object, stackFrameCount:int, matchedCount:int = 0):void {
    var key:String;
    var value:String;

    for each(var frame:* in stack) {
      key = frame.toString();
      value = stringDict[key];
      if (value == null) {
        ++stringDictSize;
        stringDict[key] = stringDictSize.toString();
        value = key;
      }
      socket.writeUTF(value);
      --stackFrameCount;

      if (stackFrameCount == matchedCount) break;
    }
  }

  private function securityErrorHandler(event:SecurityErrorEvent):void {
    trace("security error:"+event.toString());
  }

  private function ioErrorHandler(event:IOErrorEvent):void {
    trace("io error:"+event.toString());
    connected = false;
  }

  private function connectHandler(event:Event):void {
    trace("connected:"+event.toString());
    connected = true;
    socket.writeUTF(VERSION_COMMAND_MARKER + " "+VERSION);
    socket.flush()
  }

  private function closeHandler(event:Event):void {
    trace("disconnected:"+event.toString());
    connected = false;
  }

  private static const START_CPU_PROFILING:int = 1;
  private static const STOP_CPU_PROFILING:int = 2;
  private static const CAPTURE_MEMORY_SNAPSHOT:int = 3;

  private static const DO_GC:int = 4;
  private static const VERSION_COMMAND_MARKER:String = "v\x00 ";

  private static const VERSION:int = 3;    

  private static const END_COMMAND_MARKER:String = "e\x00 ";
  private static const SI_COMMAND_MARKER:String = "si\x00 ";

  private function socketDataHandler(event:ProgressEvent):void {
    if (!connected) return;
    trace("socketDataHandler: " + event);
    var i:int = socket.readByte();
    trace("received byte " + i);

    if (i == START_CPU_PROFILING) {
      if (!cpuSamplingStarted) {
        trace("started cpu profiling");
        socket.writeUTF(END_COMMAND_MARKER + i);
        socket.flush();
        cpuSamplingStarted = true;
      }
    } else if (i == STOP_CPU_PROFILING) {
      if (cpuSamplingStarted) {
        cpuSamplingStarted = false;
        pauseSampling();
        trace("stopped cpu profiling");
        socket.writeUTF(END_COMMAND_MARKER + i);
        socket.flush();
        clearSlidingStuff();
        startSampling();
      }
    } else if (i == CAPTURE_MEMORY_SNAPSHOT) {
      pauseSampling();

      var typeDict:Object = {};
      var typeDictSize:uint = 0;
      var objectCount:uint = 0;

      for(var l:* in id2SampleInfo) {
        var a:Array = id2SampleInfo[l];
        var c:Class = a[0];
        var encodedType:String = typeDict[c];
        if (encodedType == null) {
          typeDict[c] = String(typeDictSize ++);
          encodedType = getQualifiedClassName(c);
        }
        var stackFrameSize:uint = a[1] == null ? 0:a[1].length;
        socket.writeUTF("c\x00" + stackFrameSize + " " + l + " "+encodedType + " " + a[2]);
        if (stackFrameSize > 0) writeStack(a[1], a[1].length);

        ++objectCount;
        if (objectCount == 500) {
          socket.flush();
          objectCount = 0;
        }
      }

      doReachabilityDump();

      socket.writeUTF(END_COMMAND_MARKER + i);
      socket.flush();
      startSampling();
    } else if (i == DO_GC) {
      pauseSampling();
      var totalMemory:uint = System.totalMemory;
      System.gc();
      socket.writeUTF(END_COMMAND_MARKER + i + " "+totalMemory + "/" + System.totalMemory);
      socket.flush();
      startSampling();
    } else {
      trace("unrecognized:"+i);
    }
  }

  private function doReachabilityDump():void {
    socket.writeUTF(SI_COMMAND_MARKER);
    var usedClasses:Dictionary = new Dictionary();

    for (var o:Object in object2Id) {
      var id:uint = object2Id[o];
      var a:Array = id2SampleInfo[id];
      if (a == null) continue;
      var cls:Class = a[0];
      var dump:String;

      var qName:String = getQualifiedClassName(o).replace("::", ":");

      if (usedClasses[cls] == undefined) {
        dump = "";

        for each(var v:QName in getMemberNames(o)) {
          var i:int = dumpIt(v, o);
          if (i != -1) {
            if (dump.length == 0) dump = "cls:" + cls + "," + qName;
            dump += "," + i;
          }
        }
        if (dump.length != 0) socket.writeUTF(dump);
      }

      usedClasses[cls] = "";
      if (o is String || o is Class) continue;

      dump = "";


      for each (var v:QName in getMemberNames(o, true)) {
        var i:int = dumpIt(v, o);
        if (i != -1) {
          if (dump.length == 0) dump = String(id);
          dump+=","+i;
        }
      }

      if (dump.length > 0) socket.writeUTF(dump)
    }

    socket.writeUTF("EndSnapshot");
    socket.flush();
  }

  private function dumpIt(v:QName, o:Object):int {
    var vName:QName = acceptablePropertyName(v, o);
    if (vName == null) return -1;

    var vValue:* = o[vName];
    if (vValue == undefined) {
      return -1;
    }

    var key;

    try {
      key = object2Id[vValue];
    } catch(e:ReferenceError) {
      key = null;
    }
    if (key == null) return -1;
    return key;
  }

  private function acceptablePropertyName(v:QName, o:Object):QName {
    if (isGetterSetter(o, v)) return null;
    try {
      if(o[v] is Function) return null;
    } catch(e:ReferenceError) {
      return null;
    }

    return v;
  }
}
}