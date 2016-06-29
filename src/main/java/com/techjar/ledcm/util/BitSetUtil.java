
package com.techjar.ledcm.util;

import java.util.BitSet;

/**
 *
 * @author Techjar
 */
public class BitSetUtil {
    public static void not(BitSet bs) {
        for (int i = 0; i < bs.length(); i++) {
            if (bs.get(i)) bs.clear(i);
            else bs.set(i);
        }
    }

    public static void shiftLeft(BitSet bs, int n) {
        for (int i = bs.length() - 1; i >= 0; i = bs.previousSetBit(i - 1)) {
            bs.clear(i);
            bs.set(i + 1);
        }
    }

    public static void shiftRight(BitSet bs, int n) {
        for (int i = 0; i < bs.length() && i > -1; i = bs.nextSetBit(i + 1)) {
            bs.clear(i);
            if (i > 0) bs.set(i - 1);
        }
    }

    public static String toBinaryString(BitSet bs) {
        StringBuilder sb = new StringBuilder(bs.length());
        for (int i = bs.length() - 1; i >= 0; i--) sb.append(bs.get(i) ? 1 : 0);
        return sb.toString();
    }
}
