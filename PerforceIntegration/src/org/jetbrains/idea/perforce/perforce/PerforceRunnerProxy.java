package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsConnectionProblem;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.Convertor;
import org.jetbrains.idea.perforce.PerforceBundle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class PerforceRunnerProxy {
  private static final Logger LOG = Logger.getInstance(PerforceRunnerProxy.class);

  private final Map<String, Convertor<Object[], String>> myMethodName;
  private final PerforceRunnerI myProxy;

  private final Project myProject;

  public PerforceRunnerProxy(final Project project, final PerforceRunner runner) {
    myProject = project;

    myProxy = (PerforceRunnerI) Proxy.newProxyInstance(PerforceRunnerI.class.getClassLoader(), new Class[] {PerforceRunnerI.class},
                                     new MyPooledThreadProxy(runner));
    myMethodName = new HashMap<>();
    fillNames();
  }

  private String assumeFirstParamP4File(final Object[] o) {
    if (o.length > 0 && o[0] instanceof P4File) {
      return ((P4File) o[0]).getLocalPath();
    } else {
      return "";
    }
  }

  // further we can possibly use passed parameters
  private void fillNames() {
    myMethodName.put("fstat", o -> PerforceBundle.message("activity.retrieving.file.info", assumeFirstParamP4File(o)));
    myMethodName.put("edit", o -> PerforceBundle.message("activity.opening.file", assumeFirstParamP4File(o)));
    myMethodName.put("revert", o -> PerforceBundle.message("activity.reverting.file", assumeFirstParamP4File(o)));
    myMethodName.put("sync", o -> PerforceBundle.message("activity.sync.view"));
  }

  public PerforceRunnerI getProxy() {
    return myProxy;
  }

  private class MyPooledThreadProxy implements InvocationHandler {
    private final PerforceRunnerI myDelegate;
    private final ProgressManager myProgressManager;
    private final Application myApplication;

    MyPooledThreadProxy(final PerforceRunnerI delegate) {
      myDelegate = delegate;
      myProgressManager = ProgressManager.getInstance();
      myApplication = ApplicationManager.getApplication();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (! (PerforceRunnerI.class.isAssignableFrom(method.getDeclaringClass()))) {
        LOG.info("Proxy used for wrong class's method invocation: " + method.getDeclaringClass().getCanonicalName());
        return null;
      }

      final String title;
      final Convertor<Object[], String> getter = myMethodName.get(method.getName());
      if (getter != null) {
        //noinspection HardCodedStringLiteral
        title = getter.convert(args);
      } else {
        title = PerforceBundle.message("activity.performing");
      }

      // invoke on delegate
      final MyActuallyInvoked invoked = new MyActuallyInvoked(myDelegate, method, args);

      boolean bg = myApplication.isDispatchThread();
      if (bg) {
        // todo what about cancellation?
        myProgressManager.runProcessWithProgressSynchronously(invoked, title, false, myProject);
      } else {
        invoked.run();
      }

      VcsException vcsException = invoked.getVcsException();
      if (vcsException != null) {
        throw (vcsException instanceof VcsConnectionProblem || !bg ? vcsException : new VcsException(vcsException));
      }
      final RuntimeException runtimeException = invoked.getRuntimeException();
      if (runtimeException != null) {
        throw runtimeException;
      }

      return invoked.getResult();
    }
  }

  // todo more generic??
  private static final class MyActuallyInvoked implements Runnable {
    private final Method myMethod;
    private final Object myProxy;
    private final Object[] myArgs;

    private VcsException myVcsException;
    private RuntimeException myRuntimeException;
    private Object myResult;

    private MyActuallyInvoked(final Object proxy, final Method method, final Object[] args) {
      super();
      myMethod = method;
      myProxy = proxy;
      myArgs = args;
    }

    @Override
    public void run() {
      try {
        myResult = myMethod.invoke(myProxy, myArgs);
      }
      catch (IllegalAccessException e) {
        myRuntimeException = new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof VcsException) {
          myVcsException = (VcsException) cause;
        } else if (cause instanceof RuntimeException) {
          myRuntimeException = (RuntimeException) cause;
        } else if (cause != null) {
          myRuntimeException = new RuntimeException(cause);
        }
      }
    }

    public VcsException getVcsException() {
      return myVcsException;
    }

    public RuntimeException getRuntimeException() {
      return myRuntimeException;
    }

    public Object getResult() {
      return myResult;
    }
  }
}
