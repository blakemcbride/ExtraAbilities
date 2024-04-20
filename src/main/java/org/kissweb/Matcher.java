package org.kissweb;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Author: Blake McBride
 * Date: 12/17/19
 */
public class Matcher {

    private final static HashMap<String, Pattern> patCache = new HashMap<>();
    private final static Object lock = new Object();
    private boolean caseInsensitive;
    private String [] mr;

    public Matcher() {
        this.caseInsensitive = true;
    }

    public Matcher(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    public boolean match(String str, String spat) {
        Pattern pat;
        synchronized (lock) {
            pat = patCache.get(spat);
            if (pat == null) {
                if (caseInsensitive)
                    pat = Pattern.compile(spat, Pattern.CASE_INSENSITIVE);
                else
                    pat = Pattern.compile(spat);
                patCache.put(spat, pat);
            }
        }
        java.util.regex.Matcher m = pat.matcher(str);
        if (m.find()) {
            int n = m.groupCount();
            mr = new String[n + 1];
            mr[0] = str.substring(m.start(), m.end());
            for (int i=1 ; i <= n ; i++)
                mr[i] = m.group(i);
            return true;
        }
        mr = null;
        return false;
    }

    public String matchingLine() {
        return mr[0];
    }

    public String matchingSegment(int i) {
        return mr[i+1];
    }

    public int numMatchingSegments() {
        return mr == null ? 0 : mr.length - 1;
    }

    public static void main(String [] argv) {
        String pat = "^(in|on) list (.+)$";
        String str = "in list test";

        Matcher m = new Matcher(false);

        if (m.match(str, pat)) {
            System.out.println(m.matchingLine());
            for (int i=0, max=m.numMatchingSegments() ; i < max ; i++)
                System.out.println(m.matchingSegment(i));
            System.out.println();
        }
    }

}
