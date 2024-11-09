/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class RtcpFbConfig extends PersistentObject {
  private transient long swigCPtr;

  protected RtcpFbConfig(long cPtr, boolean cMemoryOwn) {
    super(pjsua2JNI.RtcpFbConfig_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(RtcpFbConfig obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(RtcpFbConfig obj) {
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
        pjsua2JNI.delete_RtcpFbConfig(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setDontUseAvpf(boolean value) {
    pjsua2JNI.RtcpFbConfig_dontUseAvpf_set(swigCPtr, this, value);
  }

  public boolean getDontUseAvpf() {
    return pjsua2JNI.RtcpFbConfig_dontUseAvpf_get(swigCPtr, this);
  }

  public void setCaps(RtcpFbCapVector value) {
    pjsua2JNI.RtcpFbConfig_caps_set(swigCPtr, this, RtcpFbCapVector.getCPtr(value), value);
  }

  public RtcpFbCapVector getCaps() {
    long cPtr = pjsua2JNI.RtcpFbConfig_caps_get(swigCPtr, this);
    return (cPtr == 0) ? null : new RtcpFbCapVector(cPtr, false);
  }

  public RtcpFbConfig() {
    this(pjsua2JNI.new_RtcpFbConfig(), true);
  }

  public void readObject(ContainerNode node) throws Exception {
    pjsua2JNI.RtcpFbConfig_readObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public void writeObject(ContainerNode node) throws Exception {
    pjsua2JNI.RtcpFbConfig_writeObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

}
