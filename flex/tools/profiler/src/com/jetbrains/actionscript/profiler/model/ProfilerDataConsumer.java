package com.jetbrains.actionscript.profiler.model;

import com.jetbrains.actionscript.profiler.sampler.Sample;

/**
* Created by IntelliJ IDEA.
* User: maximmossienko
* Date: 2/15/11
* Time: 10:26 PM
* To change this template use File | Settings | File Templates.
*/
public interface ProfilerDataConsumer {
  void process(Sample sample);
  void referenced(int pid, int id);
}
