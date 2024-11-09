/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class CodecOpusConfig {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CodecOpusConfig(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CodecOpusConfig obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(CodecOpusConfig obj) {
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
        pjsua2JNI.delete_CodecOpusConfig(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setSample_rate(long value) {
    pjsua2JNI.CodecOpusConfig_sample_rate_set(swigCPtr, this, value);
  }

  public long getSample_rate() {
    return pjsua2JNI.CodecOpusConfig_sample_rate_get(swigCPtr, this);
  }

  public void setChannel_cnt(long value) {
    pjsua2JNI.CodecOpusConfig_channel_cnt_set(swigCPtr, this, value);
  }

  public long getChannel_cnt() {
    return pjsua2JNI.CodecOpusConfig_channel_cnt_get(swigCPtr, this);
  }

  public void setFrm_ptime(long value) {
    pjsua2JNI.CodecOpusConfig_frm_ptime_set(swigCPtr, this, value);
  }

  public long getFrm_ptime() {
    return pjsua2JNI.CodecOpusConfig_frm_ptime_get(swigCPtr, this);
  }

  public void setFrm_ptime_denum(long value) {
    pjsua2JNI.CodecOpusConfig_frm_ptime_denum_set(swigCPtr, this, value);
  }

  public long getFrm_ptime_denum() {
    return pjsua2JNI.CodecOpusConfig_frm_ptime_denum_get(swigCPtr, this);
  }

  public void setBit_rate(long value) {
    pjsua2JNI.CodecOpusConfig_bit_rate_set(swigCPtr, this, value);
  }

  public long getBit_rate() {
    return pjsua2JNI.CodecOpusConfig_bit_rate_get(swigCPtr, this);
  }

  public void setPacket_loss(long value) {
    pjsua2JNI.CodecOpusConfig_packet_loss_set(swigCPtr, this, value);
  }

  public long getPacket_loss() {
    return pjsua2JNI.CodecOpusConfig_packet_loss_get(swigCPtr, this);
  }

  public void setComplexity(long value) {
    pjsua2JNI.CodecOpusConfig_complexity_set(swigCPtr, this, value);
  }

  public long getComplexity() {
    return pjsua2JNI.CodecOpusConfig_complexity_get(swigCPtr, this);
  }

  public void setCbr(boolean value) {
    pjsua2JNI.CodecOpusConfig_cbr_set(swigCPtr, this, value);
  }

  public boolean getCbr() {
    return pjsua2JNI.CodecOpusConfig_cbr_get(swigCPtr, this);
  }

  public CodecOpusConfig() {
    this(pjsua2JNI.new_CodecOpusConfig(), true);
  }

}
