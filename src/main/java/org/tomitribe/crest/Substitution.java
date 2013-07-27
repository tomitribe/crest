/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @version $Revision$ $Date$
*/
public class Substitution {

    private static final Pattern PATTERN = Pattern.compile("(\\$\\{)([\\w.]+)(})");

    public static String format(String input, Map<String, String> data) {
        return format(input, data, new HashSet<String>());
    }

    private static String format(String input, Map<String, String> data, Set<String> seen) {
        if (!seen.add(input)) throw new IllegalStateException("Circular reference in " + input);

        Matcher matcher = PATTERN.matcher(input);
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(2);
            String value = data.get(key);
            if (value != null) {
                value = format(value, data, seen);
                try {
                    matcher.appendReplacement(buf, value.toString());
                } catch (Exception e) {
                }
            }
        }
        matcher.appendTail(buf);
        return buf.toString();
    }

    public static Map<String, String> getSystemProperties() {
        final Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final String key = entry.getKey().toString();
            final String value = entry.getValue().toString();
            map.put(key, value);
        }
        return map;
    }
}
