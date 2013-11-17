package org.bb.fsi;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bb.fsi.FastIterator.IterResult.EOF;
import static org.bb.fsi.FastIterator.IterResult.NOT_FOUND;

public class IntersectUtils {
    public static List<Long> linearIntersect(FastIterator... iterators) {
        Arrays.sort(iterators);
        FastIterator firstIterator = iterators[0];
        List<Long> matches = new ArrayList<>(firstIterator.size());

        while (firstIterator.hasNext()) {
            long elem = firstIterator.next();
            boolean allFound = true;
            for (int i = 1; i < iterators.length; i++) {
                FastIterator.IterResult iterResult = iterators[i].skipTo(elem);
                if (EOF == iterResult) {
                    return matches;
                } else if (NOT_FOUND == iterResult) {
                    allFound = false;
                    break;
                }
            }
            if (allFound) {
                matches.add(elem);
            }
        }
        return matches;
    }
}
