/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.4
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package edu.cmu.pocketsphinx;

public class pocketsphinxJNI {
  public final static native void Hypothesis_hypstr_set(long jarg1, Hypothesis jarg1_, String jarg2);
  public final static native String Hypothesis_hypstr_get(long jarg1, Hypothesis jarg1_);
  public final static native void Hypothesis_uttid_set(long jarg1, Hypothesis jarg1_, String jarg2);
  public final static native String Hypothesis_uttid_get(long jarg1, Hypothesis jarg1_);
  public final static native void Hypothesis_best_score_set(long jarg1, Hypothesis jarg1_, int jarg2);
  public final static native int Hypothesis_best_score_get(long jarg1, Hypothesis jarg1_);
  public final static native long new_Hypothesis(String jarg1, String jarg2, int jarg3);
  public final static native void delete_Hypothesis(long jarg1);
  public final static native long new_Config__SWIG_0();
  public final static native long new_Config__SWIG_1(String jarg1);
  public final static native void delete_Config(long jarg1);
  public final static native void Config_setBoolean(long jarg1, Config jarg1_, String jarg2, boolean jarg3);
  public final static native void Config_setInt(long jarg1, Config jarg1_, String jarg2, int jarg3);
  public final static native void Config_setFloat(long jarg1, Config jarg1_, String jarg2, double jarg3);
  public final static native void Config_setString(long jarg1, Config jarg1_, String jarg2, String jarg3);
  public final static native boolean Config_exists(long jarg1, Config jarg1_, String jarg2);
  public final static native boolean Config_getBoolean(long jarg1, Config jarg1_, String jarg2);
  public final static native int Config_getInt(long jarg1, Config jarg1_, String jarg2);
  public final static native double Config_getFloat(long jarg1, Config jarg1_, String jarg2);
  public final static native String Config_getString(long jarg1, Config jarg1_, String jarg2);
  public final static native long new_SegmentIterator();
  public final static native void delete_SegmentIterator(long jarg1);
  public final static native long new_Lattice();
  public final static native void delete_Lattice(long jarg1);
  public final static native long new_Decoder__SWIG_0();
  public final static native long new_Decoder__SWIG_1(long jarg1, Config jarg1_);
  public final static native long Decoder_getConfig(long jarg1, Decoder jarg1_);
  public final static native int Decoder_startUtt__SWIG_0(long jarg1, Decoder jarg1_);
  public final static native int Decoder_startUtt__SWIG_1(long jarg1, Decoder jarg1_, String jarg2);
  public final static native String Decoder_getUttid(long jarg1, Decoder jarg1_);
  public final static native int Decoder_endUtt(long jarg1, Decoder jarg1_);
  public final static native int Decoder_processRaw__SWIG_0(long jarg1, Decoder jarg1_, short[] jarg2, boolean jarg4, boolean jarg5);
  public final static native int Decoder_processRaw__SWIG_1(long jarg1, Decoder jarg1_, short[] jarg2, long jarg3, boolean jarg4, boolean jarg5);
  public final static native long Decoder_getHyp(long jarg1, Decoder jarg1_);
  public final static native void delete_Decoder(long jarg1);
  public final static native void setLogfile(String jarg1);
}
