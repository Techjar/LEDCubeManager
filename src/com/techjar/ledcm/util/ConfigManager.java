package com.techjar.ledcm.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A Yaml configuration manager that supports tree-based structures by placing periods in the key.
 * @author Techjar
 */
public class ConfigManager {
    /**
     * The file to load and save.
     */
    public File file;
    private boolean autoSave;
    private final Yaml yaml;
    private ConfigMap config;
    private boolean changed;
    
    
    /**
     * Creates a new ConfigManager using the specified {@link File} as the file. Auto-saving is determined by the specified boolean value.
     * @param file The file to load and save.
     * @param autoSave <tt>true</tt> to enable auto-saving.
     */
    public ConfigManager(File file, boolean autoSave) {
        DumperOptions dumper = new DumperOptions();
        dumper.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        this.yaml = new Yaml(dumper);
        
        this.autoSave = autoSave;
        this.file = file;
        this.config = new ConfigMap(this, new HashMap<String, Object>());
    }

    /**
     * Creates a new ConfigManager using the specified {@link File} as the file. Auto-saving is off by default, see {@link #ConfigManager(java.lang.String, boolean)} for enabling it.
     * @param file The file to load and save.
     */
    public ConfigManager(File file) {
        this(file, false);
    }
    
    /**
     * Creates a new ConfigManager using the specified {@link String} as the file. Auto-saving is determined by the specified boolean value.
     * @param data Raw Yaml data to load.
     */
    public ConfigManager(String data) {
        DumperOptions dumper = new DumperOptions();
        dumper.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        this.yaml = new Yaml(dumper);

        this.autoSave = false;
        this.file = null;
        this.config = new ConfigMap(this, (Map)yaml.load(data));
    }
    
    /**
     * Returns whether the Yaml file exists.
     * @return <tt>true</tt> if the file exists.
     */
    public boolean fileExists() {
        return file != null && file.exists();
    }

    /**
     * Returns whether the data has changed.
     * @return <tt>true</tt> if the data has changed.
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * See {@link ConfigMap#getProperty(String, Object) ConfigMap.getProperty}
     */
    public Object getProperty(String name, Object def) {
        return config.getProperty(name, def);
    }

    /**
     * See {@link ConfigMap#getProperty(String) ConfigMap.getProperty}
     */
    public Object getProperty(String name) {
        return config.getProperty(name);
    }

    /**
     * See {@link ConfigMap#getList}
     */
    public List getList(String name) {
        return config.getList(name);
    }

    /**
     * See {@link ConfigMap#getMapList}
     */
    public List<ConfigMap> getMapList(String name) {
        return config.getMapList(name);
    }

    /**
     * See {@link ConfigMap#getString}
     */
    public String getString(String name) {
        return config.getString(name);
    }

    /**
     * See {@link ConfigMap#getBoolean}
     */
    public boolean getBoolean(String name) {
        return config.getBoolean(name);
    }

    /**
     * See {@link ConfigMap#getInteger}
     */
    public int getInteger(String name) {
        return config.getInteger(name);
    }

    /**
     * See {@link ConfigMap#getLong}
     */
    public long getLong(String name) {
        return config.getLong(name);
    }

    /**
     * See {@link ConfigMap#getFloat}
     */
    public float getFloat(String name) {
        return config.getFloat(name);
    }

    /**
     * See {@link ConfigMap#getDouble}
     */
    public double getDouble(String name) {
        return config.getDouble(name);
    }

    /**
     * See {@link ConfigMap#setProperty}
     */
    public void setProperty(String name, Object value) {
        config.setProperty(name, value);
    }

    /**
     * See {@link ConfigMap#removeProperty}
     */
    public void removeProperty(String name) {
        config.removeProperty(name);
    }

    /**
     * See {@link ConfigMap#defaultProperty}
     */
    public void defaultProperty(String name, Object value) {
        config.defaultProperty(name, value);
    }

    /**
     * See {@link ConfigMap#propertyExists}
     */
    public boolean propertyExists(String name) {
        return config.propertyExists(name);
    }
    
    /**
     * Load the Yaml file.
     */
    public void load() {
        if (file == null) throw new UnsupportedOperationException("ConfigManager does not have a file");
        try {
            if (!file.exists()) {
                file.createNewFile();
                return;
            }
            FileReader fr = new FileReader(file);
            Map<String, Object> map = (Map)yaml.load(fr);
            if (map == null) map = new HashMap<>();
            config = new ConfigMap(this, map);
            fr.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Save the Yaml file.
     */
    public void save() {
        if (file == null) throw new UnsupportedOperationException("ConfigManager does not have a file");
        try {
            FileWriter fw = new FileWriter(file);
            yaml.dump(config.map, fw);
            fw.flush();
            fw.close();
            changed = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static class ConfigMap {
        private final ConfigManager manager;
        private final Map<String, Object> map;

        private ConfigMap(ConfigManager manager, Map<String, Object> map) {
            this.manager = manager;
            this.map = map;
        }

        /**
         * Returns the {@link ConfigManager} responsible for this ConfigMap.
         * @return the {@link ConfigManager}
         */
        public ConfigManager getManager() {
            return manager;
        }

        /**
         * Gets the property or returns the value of <tt>def</tt>. Only intended for internal use, you should call {@link #defaultProperty} in your config initialization instead of using this.
         * @param name The property key.
         * @param def The default value
         * @return The value of the property or the value of <tt>def</tt> if it doesn't exist.
         */
        public Object getProperty(String name, Object def) {
            if (containsYamlKey(map, name))
                return getYamlKey(map, name);
            return def;
        }

        /**
         * Gets the property.
         * @param name The property key.
         * @return The value of the property or <tt>null</tt> if it doesn't exist.
         */
        public Object getProperty(String name) {
            return getProperty(name, null);
        }

        /**
         * Gets the property as a {@link List}.
         * @param name The property key.
         * @return {@link List} value of the property or <tt>null</tt> if it's not a list.
         */
        public List getList(String name) {
            Object obj = getProperty(name, null);
            return obj instanceof List ? (List)obj : null;
        }

        /**
         * Gets the property as a {@link List} containing only maps, as {@link ConfigMap} instances for ease of use. The returned list is not modifiable.
         * @param name The property key.
         * @return The {@link List} or <tt>null</tt> if it's not a list.
         */
        public List<ConfigMap> getMapList(String name) {
            Object obj = getProperty(name, null);
            if (obj instanceof List) {
                List<ConfigMap> list = new ArrayList<>();
                for (Object item : (List)obj) {
                    if (item instanceof Map) {
                        list.add(new ConfigMap(manager, (Map)item));
                    }
                }
                return Collections.unmodifiableList(list);
            } else return null;
        }

        /**
         * Gets the property as a {@link String}.
         * @param name The property key.
         * @return {@link String} value of the property.
         */
        public String getString(String name) {
            return getProperty(name, "").toString();
        }

        /**
         * Gets the property as a boolean.
         * @param name The property key.
         * @return Boolean value of the property.
         */
        public boolean getBoolean(String name) {
            return Boolean.parseBoolean(getProperty(name, false).toString());
        }

        /**
         * Gets the property as an integer.
         * @param name The property key.
         * @return Integer value of the property.
         */
        public int getInteger(String name) {
            return Integer.parseInt(getProperty(name, 0).toString());
        }

        /**
         * Gets the property as a long.
         * @param name The property key.
         * @return Long value of the property.
         */
        public long getLong(String name) {
            return Long.parseLong(getProperty(name, 0).toString());
        }

        /**
         * Gets a property as a float.
         * @param name The property key.
         * @return Float value of the property.
         */
        public float getFloat(String name) {
            return Float.parseFloat(getProperty(name, 0).toString());
        }

        /**
         * Gets a property as a double.
         * @param name The property key.
         * @return Double value of the property.
         */
        public double getDouble(String name) {
            return Double.parseDouble(getProperty(name, 0).toString());
        }

        /**
         * Sets a property.
         * @param name The property key.
         * @param value The value to be set.
         */
        public void setProperty(String name, Object value) {
            putYamlKey(map, name, value);
            if (manager.autoSave) manager.save();
        }

        /**
         * Removes a property.
         * @param name The property key.
         */
        public void removeProperty(String name) {
            if (containsYamlKey(map, name)) {
                removeYamlKey(map, name);
                if (manager.autoSave) manager.save();
            }
        }

        /**
         * Set a property only if it doesn't exist already.
         * @param name The property key.
         * @param value The value to be set.
         */
        public void defaultProperty(String name, Object value) {
            if (!containsYamlKey(map, name)) {
                putYamlKey(map, name, value);
                if (manager.autoSave) manager.save();
            }
        }

        /**
         * Returns whether or not the property exists.
         * @param name The property key.
         * @return <tt>true</tt> if the specified property exists.
         */
        public boolean propertyExists(String name) {
            return containsYamlKey(map, name);
        }

        private Object getYamlKey(Map<String, Object> map, String key) {
            if (key.indexOf('.') == -1) {
                return map.get(key);
            }
            Map<String, Object> curmap = map;
            while (key.indexOf('.') != -1) {
                String subkey = key.substring(0, key.indexOf('.'));
                key = key.substring(key.indexOf('.') + 1);
                if (curmap.get(subkey) == null) return null;
                if (!(curmap.get(subkey) instanceof Map)) {
                    throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
                }
                curmap = (Map)curmap.get(subkey);
            }
            return curmap.get(key);
        }

        private Object putYamlKey(Map<String, Object> map, String key, Object value) {
            if (key.indexOf('.') == -1) {
                manager.changed = true;
                return map.put(key, value);
            }
            Map<String, Object> curmap = map;
            while (key.indexOf('.') != -1) {
                String subkey = key.substring(0, key.indexOf('.'));
                key = key.substring(key.indexOf('.') + 1);
                if (curmap.get(subkey) == null) curmap.put(subkey, new HashMap<String, Object>());
                if (!(curmap.get(subkey) instanceof Map)) {
                    throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
                }
                curmap = (Map)curmap.get(subkey);
            }
            manager.changed = true;
            return curmap.put(key, value);
        }

        private Object removeYamlKey(Map<String, Object> map, String key) {
            if (key.indexOf('.') == -1) {
                manager.changed = true;
                return map.remove(key);
            }
            Map<String, Object> curmap = map;
            while (key.indexOf('.') != -1) {
                String subkey = key.substring(0, key.indexOf('.'));
                key = key.substring(key.indexOf('.') + 1);
                if (curmap.get(subkey) == null) return null;
                if (!(curmap.get(subkey) instanceof Map)) {
                    throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
                }
                curmap = (Map)curmap.get(subkey);
            }
            manager.changed = true;
            return curmap.remove(key);
        }

        private boolean containsYamlKey(Map<String, Object> map, String key) {
            if (key.indexOf('.') == -1) {
                return map.containsKey(key);
            }
            Map<String, Object> curmap = map;
            while (key.indexOf('.') != -1) {
                String subkey = key.substring(0, key.indexOf('.'));
                key = key.substring(key.indexOf('.') + 1);
                if (curmap.get(subkey) == null) return false;
                if (!(curmap.get(subkey) instanceof Map)) {
                    throw new IllegalArgumentException("Key '" + subkey + "' is not a key group!");
                }
                curmap = (Map)curmap.get(subkey);
            }
            return curmap.containsKey(key);
        }
    }
}
