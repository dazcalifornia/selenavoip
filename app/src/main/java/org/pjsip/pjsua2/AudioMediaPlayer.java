/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class AudioMediaPlayer extends AudioMedia {
  private transient long swigCPtr;

  protected AudioMediaPlayer(long cPtr, boolean cMemoryOwn) {
    super(pjsua2JNI.AudioMediaPlayer_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(AudioMediaPlayer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(AudioMediaPlayer obj) {
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
        pjsua2JNI.delete_AudioMediaPlayer(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  protected void swigDirectorDisconnect() {
    swigCMemOwn = false;
    delete();
  }

  public void swigReleaseOwnership() {
    swigCMemOwn = false;
    pjsua2JNI.AudioMediaPlayer_change_ownership(this, swigCPtr, false);
  }

  public void swigTakeOwnership() {
    swigCMemOwn = true;
    pjsua2JNI.AudioMediaPlayer_change_ownership(this, swigCPtr, true);
  }

  public AudioMediaPlayer() {
    this(pjsua2JNI.new_AudioMediaPlayer(), true);
    pjsua2JNI.AudioMediaPlayer_director_connect(this, swigCPtr, true, true);
  }

  public void createPlayer(String file_name, long options) throws Exception {
    pjsua2JNI.AudioMediaPlayer_createPlayer__SWIG_0(swigCPtr, this, file_name, options);
  }

  public void createPlayer(String file_name) throws Exception {
    pjsua2JNI.AudioMediaPlayer_createPlayer__SWIG_1(swigCPtr, this, file_name);
  }

  public void createPlaylist(StringVector file_names, String label, long options) throws Exception {
    pjsua2JNI.AudioMediaPlayer_createPlaylist__SWIG_0(swigCPtr, this, StringVector.getCPtr(file_names), file_names, label, options);
  }

  public void createPlaylist(StringVector file_names, String label) throws Exception {
    pjsua2JNI.AudioMediaPlayer_createPlaylist__SWIG_1(swigCPtr, this, StringVector.getCPtr(file_names), file_names, label);
  }

  public void createPlaylist(StringVector file_names) throws Exception {
    pjsua2JNI.AudioMediaPlayer_createPlaylist__SWIG_2(swigCPtr, this, StringVector.getCPtr(file_names), file_names);
  }

  public AudioMediaPlayerInfo getInfo() throws Exception {
    return new AudioMediaPlayerInfo(pjsua2JNI.AudioMediaPlayer_getInfo(swigCPtr, this), true);
  }

  public long getPos() throws Exception {
    return pjsua2JNI.AudioMediaPlayer_getPos(swigCPtr, this);
  }

  public void setPos(long samples) throws Exception {
    pjsua2JNI.AudioMediaPlayer_setPos(swigCPtr, this, samples);
  }

  public static AudioMediaPlayer typecastFromAudioMedia(AudioMedia media) {
    long cPtr = pjsua2JNI.AudioMediaPlayer_typecastFromAudioMedia(AudioMedia.getCPtr(media), media);
    return (cPtr == 0) ? null : new AudioMediaPlayer(cPtr, false);
  }

  public void onEof2() {
    if (getClass() == AudioMediaPlayer.class) pjsua2JNI.AudioMediaPlayer_onEof2(swigCPtr, this); else pjsua2JNI.AudioMediaPlayer_onEof2SwigExplicitAudioMediaPlayer(swigCPtr, this);
  }

}
