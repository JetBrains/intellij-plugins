package com.jetbrains.actionscript.profiler;

/**
* Created by IntelliJ IDEA.
* User: maximmossienko
* Date: 2/15/11
* Time: 10:26 PM
* To change this template use File | Settings | File Templates.
*/
interface ProfilerDataConsumer {
  void process(Sample sample);
  void referenced(int pid, int id);
}
