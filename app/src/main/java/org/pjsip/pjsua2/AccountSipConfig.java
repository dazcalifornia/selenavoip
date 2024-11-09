/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class AccountSipConfig extends PersistentObject {
  private transient long swigCPtr;

  protected AccountSipConfig(long cPtr, boolean cMemoryOwn) {
    super(pjsua2JNI.AccountSipConfig_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(AccountSipConfig obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(AccountSipConfig obj) {
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
        pjsua2JNI.delete_AccountSipConfig(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setAuthCreds(AuthCredInfoVector value) {
    pjsua2JNI.AccountSipConfig_authCreds_set(swigCPtr, this, AuthCredInfoVector.getCPtr(value), value);
  }

  public AuthCredInfoVector getAuthCreds() {
    long cPtr = pjsua2JNI.AccountSipConfig_authCreds_get(swigCPtr, this);
    return (cPtr == 0) ? null : new AuthCredInfoVector(cPtr, false);
  }

  public void setProxies(StringVector value) {
    pjsua2JNI.AccountSipConfig_proxies_set(swigCPtr, this, StringVector.getCPtr(value), value);
  }

  public StringVector getProxies() {
    long cPtr = pjsua2JNI.AccountSipConfig_proxies_get(swigCPtr, this);
    return (cPtr == 0) ? null : new StringVector(cPtr, false);
  }

  public void setContactForced(String value) {
    pjsua2JNI.AccountSipConfig_contactForced_set(swigCPtr, this, value);
  }

  public String getContactForced() {
    return pjsua2JNI.AccountSipConfig_contactForced_get(swigCPtr, this);
  }

  public void setContactParams(String value) {
    pjsua2JNI.AccountSipConfig_contactParams_set(swigCPtr, this, value);
  }

  public String getContactParams() {
    return pjsua2JNI.AccountSipConfig_contactParams_get(swigCPtr, this);
  }

  public void setContactUriParams(String value) {
    pjsua2JNI.AccountSipConfig_contactUriParams_set(swigCPtr, this, value);
  }

  public String getContactUriParams() {
    return pjsua2JNI.AccountSipConfig_contactUriParams_get(swigCPtr, this);
  }

  public void setAuthInitialEmpty(boolean value) {
    pjsua2JNI.AccountSipConfig_authInitialEmpty_set(swigCPtr, this, value);
  }

  public boolean getAuthInitialEmpty() {
    return pjsua2JNI.AccountSipConfig_authInitialEmpty_get(swigCPtr, this);
  }

  public void setAuthInitialAlgorithm(String value) {
    pjsua2JNI.AccountSipConfig_authInitialAlgorithm_set(swigCPtr, this, value);
  }

  public String getAuthInitialAlgorithm() {
    return pjsua2JNI.AccountSipConfig_authInitialAlgorithm_get(swigCPtr, this);
  }

  public void setTransportId(int value) {
    pjsua2JNI.AccountSipConfig_transportId_set(swigCPtr, this, value);
  }

  public int getTransportId() {
    return pjsua2JNI.AccountSipConfig_transportId_get(swigCPtr, this);
  }

  public void setIpv6Use(int value) {
    pjsua2JNI.AccountSipConfig_ipv6Use_set(swigCPtr, this, value);
  }

  public int getIpv6Use() {
    return pjsua2JNI.AccountSipConfig_ipv6Use_get(swigCPtr, this);
  }

  public void readObject(ContainerNode node) throws Exception {
    pjsua2JNI.AccountSipConfig_readObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public void writeObject(ContainerNode node) throws Exception {
    pjsua2JNI.AccountSipConfig_writeObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public AccountSipConfig() {
    this(pjsua2JNI.new_AccountSipConfig(), true);
  }

}
