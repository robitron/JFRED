// @(#)Degree.java 97/12/07 1.1 1997, FringeWare Inc. Released under the GNU Public License

package com.jfred;
import java.util.*;

public class Degree extends Object {
  public final static long TICKS = 240000;

  protected long value = 0;

  public Degree (Date date) {
    value = date.getTime() / TICKS;
  }

  public String toString () {
    return String.valueOf(value);
  }
}
