package com.techjar.ledcm.util;

import com.techjar.ledcm.util.logging.LogHelper;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Techjar
 */
public class ArgumentParser {
    private ArgumentParser() {
    }

    public static void parse(String[] args, Argument... objects) {
        List<Argument> used = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            boolean found = false;
            argloop: for (Argument obj : objects) {
                for (String name : obj.getNames()) {
                    if (name.toLowerCase().equals(args[i].toLowerCase())) {
                        if (obj.getHasParameter()) {
                            obj.runAction(args[++i]);
                        } else obj.runAction(null);
                        found = true;
                        break argloop;
                    }
                }
            }
            if (!found) LogHelper.warning("Unknown argument: %s", args[i]);
        }
    }

    public static abstract class Argument {
        private final String[] names;
        private final boolean hasParameter;

        public Argument(boolean hasParameter, String... names) {
            this.hasParameter = hasParameter;
            this.names = names;
        }

        public String[] getNames() {
            return names;
        }

        public boolean getHasParameter() {
            return hasParameter;
        }

        public abstract void runAction(String paramater);
    }
}
