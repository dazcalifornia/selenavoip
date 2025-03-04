/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class AccountInfo {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected AccountInfo(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(AccountInfo obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(AccountInfo obj) {
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
        pjsua2JNI.delete_AccountInfo(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setId(int value) {
    pjsua2JNI.AccountInfo_id_set(swigCPtr, this, value);
  }

  public int getId() {
    return pjsua2JNI.AccountInfo_id_get(swigCPtr, this);
  }

  public void setIsDefault(boolean value) {
    pjsua2JNI.AccountInfo_isDefault_set(swigCPtr, this, value);
  }

  public boolean getIsDefault() {
    return pjsua2JNI.AccountInfo_isDefault_get(swigCPtr, this);
  }

  public void setUri(String value) {
    pjsua2JNI.AccountInfo_uri_set(swigCPtr, this, value);
  }

  public String getUri() {
    return pjsua2JNI.AccountInfo_uri_get(swigCPtr, this);
  }

  public void setRegIsConfigured(boolean value) {
    pjsua2JNI.AccountInfo_regIsConfigured_set(swigCPtr, this, value);
  }

  public boolean getRegIsConfigured() {
    return pjsua2JNI.AccountInfo_regIsConfigured_get(swigCPtr, this);
  }

  public void setRegIsActive(boolean value) {
    pjsua2JNI.AccountInfo_regIsActive_set(swigCPtr, this, value);
  }

  public boolean getRegIsActive() {
    return pjsua2JNI.AccountInfo_regIsActive_get(swigCPtr, this);
  }

  public void setRegExpiresSec(long value) {
    pjsua2JNI.AccountInfo_regExpiresSec_set(swigCPtr, this, value);
  }

  public long getRegExpiresSec() {
    return pjsua2JNI.AccountInfo_regExpiresSec_get(swigCPtr, this);
  }

  public void setRegStatus(int value) {
    pjsua2JNI.AccountInfo_regStatus_set(swigCPtr, this, value);
  }

  public int getRegStatus() {
    return pjsua2JNI.AccountInfo_regStatus_get(swigCPtr, this);
  }

  public void setRegStatusText(String value) {
    pjsua2JNI.AccountInfo_regStatusText_set(swigCPtr, this, value);
  }

  public String getRegStatusText() {
    return pjsua2JNI.AccountInfo_regStatusText_get(swigCPtr, this);
  }

  public void setRegLastErr(int value) {
    pjsua2JNI.AccountInfo_regLastErr_set(swigCPtr, this, value);
  }

  public int getRegLastErr() {
    return pjsua2JNI.AccountInfo_regLastErr_get(swigCPtr, this);
  }

  public void setOnlineStatus(boolean value) {
    pjsua2JNI.AccountInfo_onlineStatus_set(swigCPtr, this, value);
  }

  public boolean getOnlineStatus() {
    return pjsua2JNI.AccountInfo_onlineStatus_get(swigCPtr, this);
  }

  public void setOnlineStatusText(String value) {
    pjsua2JNI.AccountInfo_onlineStatusText_set(swigCPtr, this, value);
  }

  public String getOnlineStatusText() {
    return pjsua2JNI.AccountInfo_onlineStatusText_get(swigCPtr, this);
  }

  public AccountInfo() {
    this(pjsua2JNI.new_AccountInfo(), true);
  }

}
