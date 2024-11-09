/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnIpChangeProgressParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OnIpChangeProgressParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnIpChangeProgressParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(OnIpChangeProgressParam obj) {
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
        pjsua2JNI.delete_OnIpChangeProgressParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setOp(int value) {
    pjsua2JNI.OnIpChangeProgressParam_op_set(swigCPtr, this, value);
  }

  public int getOp() {
    return pjsua2JNI.OnIpChangeProgressParam_op_get(swigCPtr, this);
  }

  public void setStatus(int value) {
    pjsua2JNI.OnIpChangeProgressParam_status_set(swigCPtr, this, value);
  }

  public int getStatus() {
    return pjsua2JNI.OnIpChangeProgressParam_status_get(swigCPtr, this);
  }

  public void setTransportId(int value) {
    pjsua2JNI.OnIpChangeProgressParam_transportId_set(swigCPtr, this, value);
  }

  public int getTransportId() {
    return pjsua2JNI.OnIpChangeProgressParam_transportId_get(swigCPtr, this);
  }

  public void setAccId(int value) {
    pjsua2JNI.OnIpChangeProgressParam_accId_set(swigCPtr, this, value);
  }

  public int getAccId() {
    return pjsua2JNI.OnIpChangeProgressParam_accId_get(swigCPtr, this);
  }

  public void setCallId(int value) {
    pjsua2JNI.OnIpChangeProgressParam_callId_set(swigCPtr, this, value);
  }

  public int getCallId() {
    return pjsua2JNI.OnIpChangeProgressParam_callId_get(swigCPtr, this);
  }

  public void setRegInfo(RegProgressParam value) {
    pjsua2JNI.OnIpChangeProgressParam_regInfo_set(swigCPtr, this, RegProgressParam.getCPtr(value), value);
  }

  public RegProgressParam getRegInfo() {
    long cPtr = pjsua2JNI.OnIpChangeProgressParam_regInfo_get(swigCPtr, this);
    return (cPtr == 0) ? null : new RegProgressParam(cPtr, false);
  }

  public OnIpChangeProgressParam() {
    this(pjsua2JNI.new_OnIpChangeProgressParam(), true);
  }

}
