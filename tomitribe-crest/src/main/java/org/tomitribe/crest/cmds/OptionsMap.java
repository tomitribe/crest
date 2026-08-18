/*
 * Copyright 2026 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.util.Join;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The option values of one command invocation, keyed by option name.
 * Exposed to interceptors as CrestContext.getOptions().
 *
 * Contains every option declared by the command method or by any
 * interceptor bound to it.  Where the command method declares the option,
 * the entry is a live view over the command's parameter list — the same
 * storage CrestContext.getParameters() exposes positionally — so a write
 * through either is seen through both.  Options the command method does
 * not declare are stored here.
 *
 * The key set is fixed at the command's declared option names: values may
 * be replaced but entries cannot be added or removed.  Writes are type
 * checked against the option's declared type.
 */
public class OptionsMap extends AbstractMap<String, Object> {

    private final Map<String, OptionParam> declarations;
    private final Map<String, Slot> slots = new LinkedHashMap<>();

    /**
     * Names supplied on the command line or written by an interceptor,
     * i.e. not merely carrying their declared default
     */
    private final Set<String> provided;

    /**
     * Names written after parsing.  Beans derived from these constituents
     * are rebuilt before their receiver sees them.
     */
    private final Set<String> dirty = new HashSet<>();

    public OptionsMap(final Map<String, OptionParam> declarations, final Set<String> provided) {
        this.declarations = declarations;
        this.provided = provided;
    }

    void addListSlot(final String name, final List<Object> parameters, final int index) {
        slots.put(name, new Slot() {
            @Override
            public Object get() {
                return parameters.get(index);
            }

            @Override
            public void set(final Object value) {
                parameters.set(index, value);
            }
        });
    }

    void addLocalSlot(final String name, final Object initial) {
        slots.put(name, new Slot() {
            private Object value = initial;

            @Override
            public Object get() {
                return value;
            }

            @Override
            public void set(final Object value) {
                this.value = value;
            }
        });
    }

    @Override
    public Object get(final Object key) {
        final Slot slot = slots.get(key);
        return slot == null ? null : slot.get();
    }

    @Override
    public boolean containsKey(final Object key) {
        return slots.containsKey(key);
    }

    @Override
    public Object put(final String name, final Object value) {
        final Slot slot = slots.get(name);

        if (slot == null) {
            throw new IllegalArgumentException(String.format("No such option \"%s\".  The options of this" +
                    " command are: %s.  Option entries may be replaced but not added or removed", name,
                    Join.join(", ", slots.keySet())));
        }

        final OptionParam declaration = declarations.get(name);
        if (value != null && declaration != null && !boxed(declaration.getType()).isInstance(value)) {
            throw new IllegalArgumentException(String.format("Option \"%s\" is declared as %s and cannot" +
                    " be set to an instance of %s.  Pass a %s", name, declaration.getType().getName(),
                    value.getClass().getName(), declaration.getType().getSimpleName()));
        }

        final Object previous = slot.get();
        slot.set(value);
        provided.add(name);
        dirty.add(name);
        return previous;
    }

    private static Class<?> boxed(final Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }

    public boolean isProvided(final String name) {
        return provided.contains(name);
    }

    public Set<String> getDirty() {
        return dirty;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new AbstractSet<Entry<String, Object>>() {
            @Override
            public Iterator<Entry<String, Object>> iterator() {
                final Iterator<Map.Entry<String, Slot>> iterator = slots.entrySet().iterator();
                return new Iterator<Entry<String, Object>>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Entry<String, Object> next() {
                        final Map.Entry<String, Slot> entry = iterator.next();
                        return new Entry<String, Object>() {
                            @Override
                            public String getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public Object getValue() {
                                return entry.getValue().get();
                            }

                            @Override
                            public Object setValue(final Object value) {
                                return put(entry.getKey(), value);
                            }
                        };
                    }
                };
            }

            @Override
            public int size() {
                return slots.size();
            }
        };
    }

    private interface Slot {
        Object get();

        void set(Object value);
    }
}
