/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class TransportConfig extends PersistentObject {
  private transient long swigCPtr;

  protected TransportConfig(long cPtr, boolean cMemoryOwn) {
    super(pjsua2JNI.TransportConfig_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(TransportConfig obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(TransportConfig obj) {
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
        pjsua2JNI.delete_TransportConfig(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setPort(long value) {
    pjsua2JNI.TransportConfig_port_set(swigCPtr, this, value);
  }

  public long getPort() {
    return pjsua2JNI.TransportConfig_port_get(swigCPtr, this);
  }

  public void setPortRange(long value) {
    pjsua2JNI.TransportConfig_portRange_set(swigCPtr, this, value);
  }

  public long getPortRange() {
    return pjsua2JNI.TransportConfig_portRange_get(swigCPtr, this);
  }

  public void setRandomizePort(boolean value) {
    pjsua2JNI.TransportConfig_randomizePort_set(swigCPtr, this, value);
  }

  public boolean getRandomizePort() {
    return pjsua2JNI.TransportConfig_randomizePort_get(swigCPtr, this);
  }

  public void setPublicAddress(String value) {
    pjsua2JNI.TransportConfig_publicAddress_set(swigCPtr, this, value);
  }

  public String getPublicAddress() {
    return pjsua2JNI.TransportConfig_publicAddress_get(swigCPtr, this);
  }

  public void setBoundAddress(String value) {
    pjsua2JNI.TransportConfig_boundAddress_set(swigCPtr, this, value);
  }

  public String getBoundAddress() {
    return pjsua2JNI.TransportConfig_boundAddress_get(swigCPtr, this);
  }

  public void setTlsConfig(TlsConfig value) {
    pjsua2JNI.TransportConfig_tlsConfig_set(swigCPtr, this, TlsConfig.getCPtr(value), value);
  }

  public TlsConfig getTlsConfig() {
    long cPtr = pjsua2JNI.TransportConfig_tlsConfig_get(swigCPtr, this);
    return (cPtr == 0) ? null : new TlsConfig(cPtr, false);
  }

  public void setQosType(int value) {
    pjsua2JNI.TransportConfig_qosType_set(swigCPtr, this, value);
  }

  public int getQosType() {
    return pjsua2JNI.TransportConfig_qosType_get(swigCPtr, this);
  }

  public void setQosParams(pj_qos_params value) {
    pjsua2JNI.TransportConfig_qosParams_set(swigCPtr, this, pj_qos_params.getCPtr(value), value);
  }

  public pj_qos_params getQosParams() {
    long cPtr = pjsua2JNI.TransportConfig_qosParams_get(swigCPtr, this);
    return (cPtr == 0) ? null : new pj_qos_params(cPtr, false);
  }

  public TransportConfig() {
    this(pjsua2JNI.new_TransportConfig(), true);
  }

  public void readObject(ContainerNode node) throws Exception {
    pjsua2JNI.TransportConfig_readObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public void writeObject(ContainerNode node) throws Exception {
    pjsua2JNI.TransportConfig_writeObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

}
