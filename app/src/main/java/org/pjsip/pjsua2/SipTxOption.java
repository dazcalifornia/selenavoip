/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class SipTxOption {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SipTxOption(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SipTxOption obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(SipTxOption obj) {
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
        pjsua2JNI.delete_SipTxOption(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setTargetUri(String value) {
    pjsua2JNI.SipTxOption_targetUri_set(swigCPtr, this, value);
  }

  public String getTargetUri() {
    return pjsua2JNI.SipTxOption_targetUri_get(swigCPtr, this);
  }

  public void setLocalUri(String value) {
    pjsua2JNI.SipTxOption_localUri_set(swigCPtr, this, value);
  }

  public String getLocalUri() {
    return pjsua2JNI.SipTxOption_localUri_get(swigCPtr, this);
  }

  public void setHeaders(SipHeaderVector value) {
    pjsua2JNI.SipTxOption_headers_set(swigCPtr, this, SipHeaderVector.getCPtr(value), value);
  }

  public SipHeaderVector getHeaders() {
    long cPtr = pjsua2JNI.SipTxOption_headers_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SipHeaderVector(cPtr, false);
  }

  public void setContentType(String value) {
    pjsua2JNI.SipTxOption_contentType_set(swigCPtr, this, value);
  }

  public String getContentType() {
    return pjsua2JNI.SipTxOption_contentType_get(swigCPtr, this);
  }

  public void setMsgBody(String value) {
    pjsua2JNI.SipTxOption_msgBody_set(swigCPtr, this, value);
  }

  public String getMsgBody() {
    return pjsua2JNI.SipTxOption_msgBody_get(swigCPtr, this);
  }

  public void setMultipartContentType(SipMediaType value) {
    pjsua2JNI.SipTxOption_multipartContentType_set(swigCPtr, this, SipMediaType.getCPtr(value), value);
  }

  public SipMediaType getMultipartContentType() {
    long cPtr = pjsua2JNI.SipTxOption_multipartContentType_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SipMediaType(cPtr, false);
  }

  public void setMultipartParts(SipMultipartPartVector value) {
    pjsua2JNI.SipTxOption_multipartParts_set(swigCPtr, this, SipMultipartPartVector.getCPtr(value), value);
  }

  public SipMultipartPartVector getMultipartParts() {
    long cPtr = pjsua2JNI.SipTxOption_multipartParts_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SipMultipartPartVector(cPtr, false);
  }

  public boolean isEmpty() {
    return pjsua2JNI.SipTxOption_isEmpty(swigCPtr, this);
  }

  public SipTxOption() {
    this(pjsua2JNI.new_SipTxOption(), true);
  }

}
