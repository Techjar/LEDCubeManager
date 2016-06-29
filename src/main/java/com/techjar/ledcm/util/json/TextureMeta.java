
package com.techjar.ledcm.util.json;

/**
 *
 * @author Techjar
 */
public class TextureMeta {
    public Animation animation;

    public static class Animation {
        public int width;
        public int height;
        public float frametime;
        public Frame[] frames;

        public static class Frame {
            public int index;
            public float time;
        }
    }
}
