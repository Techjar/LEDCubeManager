package com.techjar.ledcm.util;

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
            if (args[i].equalsIgnoreCase("--help")) {
                for (Argument obj : objects) {
                    StringBuilder sb = new StringBuilder();
                    for (String name : obj.getNames()) {
                        sb.append(name).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length()).append(' ');
                    sb.append(obj.getHelp());
                    System.out.println(sb.toString());
                    System.out.println("==================================================");
                }
                System.exit(0);
            }
            boolean found = false;
            argloop: for (Argument obj : objects) {
                for (String name : obj.getNames()) {
                    if (name.equalsIgnoreCase(args[i])) {
                        if (obj.getHasParameter()) {
                            obj.runAction(args[++i]);
                        } else obj.runAction(null);
                        found = true;
                        break argloop;
                    }
                }
            }
            if (!found) System.out.println("Unknown argument: " + args[i]);
        }
    }

    public static abstract class Argument {
        private final String[] names;
        private final String help;
        private final boolean hasParameter;

        public Argument(boolean hasParameter, String help, String... names) {
            this.hasParameter = hasParameter;
            this.help = help;
            this.names = names;
        }

        public String[] getNames() {
            return names;
        }

        public String getHelp() {
            return help;
        }

        public boolean getHasParameter() {
            return hasParameter;
        }

        public abstract void runAction(String paramater);
    }
}
