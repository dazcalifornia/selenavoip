/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnCredAuthParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OnCredAuthParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnCredAuthParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(OnCredAuthParam obj) {
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
        pjsua2JNI.delete_OnCredAuthParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setDigestChallenge(DigestChallenge value) {
    pjsua2JNI.OnCredAuthParam_digestChallenge_set(swigCPtr, this, DigestChallenge.getCPtr(value), value);
  }

  public DigestChallenge getDigestChallenge() {
    long cPtr = pjsua2JNI.OnCredAuthParam_digestChallenge_get(swigCPtr, this);
    return (cPtr == 0) ? null : new DigestChallenge(cPtr, false);
  }

  public void setCredentialInfo(AuthCredInfo value) {
    pjsua2JNI.OnCredAuthParam_credentialInfo_set(swigCPtr, this, AuthCredInfo.getCPtr(value), value);
  }

  public AuthCredInfo getCredentialInfo() {
    long cPtr = pjsua2JNI.OnCredAuthParam_credentialInfo_get(swigCPtr, this);
    return (cPtr == 0) ? null : new AuthCredInfo(cPtr, false);
  }

  public void setMethod(String value) {
    pjsua2JNI.OnCredAuthParam_method_set(swigCPtr, this, value);
  }

  public String getMethod() {
    return pjsua2JNI.OnCredAuthParam_method_get(swigCPtr, this);
  }

  public void setDigestCredential(DigestCredential value) {
    pjsua2JNI.OnCredAuthParam_digestCredential_set(swigCPtr, this, DigestCredential.getCPtr(value), value);
  }

  public DigestCredential getDigestCredential() {
    long cPtr = pjsua2JNI.OnCredAuthParam_digestCredential_get(swigCPtr, this);
    return (cPtr == 0) ? null : new DigestCredential(cPtr, false);
  }

  public OnCredAuthParam() {
    this(pjsua2JNI.new_OnCredAuthParam(), true);
  }

}
