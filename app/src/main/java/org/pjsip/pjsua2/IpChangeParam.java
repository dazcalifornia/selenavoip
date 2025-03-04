/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class IpChangeParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected IpChangeParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(IpChangeParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(IpChangeParam obj) {
    long ptr = 0;
    if (obj != null) {
      if (!obj.swigCMemOwn)
        throw new RuntimeException("Cannot release ownership as memory is not owned");
      ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.delete();
    }
    return ptr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_IpChangeParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setRestartListener(boolean value) {
    pjsua2JNI.IpChangeParam_restartListener_set(swigCPtr, this, value);
  }

  public boolean getRestartListener() {
    return pjsua2JNI.IpChangeParam_restartListener_get(swigCPtr, this);
  }

  public void setRestartLisDelay(long value) {
    pjsua2JNI.IpChangeParam_restartLisDelay_set(swigCPtr, this, value);
  }

  public long getRestartLisDelay() {
    return pjsua2JNI.IpChangeParam_restartLisDelay_get(swigCPtr, this);
  }

  public void setShutdownTransport(boolean value) {
    pjsua2JNI.IpChangeParam_shutdownTransport_set(swigCPtr, this, value);
  }

  public boolean getShutdownTransport() {
    return pjsua2JNI.IpChangeParam_shutdownTransport_get(swigCPtr, this);
  }

  public IpChangeParam() {
    this(pjsua2JNI.new_IpChangeParam(), true);
  }

}
