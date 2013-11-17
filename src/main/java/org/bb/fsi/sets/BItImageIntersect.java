package org.bb.fsi.sets;

import org.bb.fsi.FastIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bb.fsi.FastIterator.IterResult.EOF;
import static org.bb.fsi.FastIterator.IterResult.NOT_FOUND;

public class BItImageIntersect {

    public static List<Long> intersect(BitImagePartitionedSet... sets) {
        Arrays.sort(sets);
        List<Long> hits = new ArrayList<>();
        int lastSet = sets.length - 1;
        int maxPartitions = sets[lastSet].partitionsCount();

        for (int k = 0; k < maxPartitions; k++) {
            long bitImage[] = sets[lastSet].bitImagesForPartitions(k);
            boolean hasIntersect = true;
            int[] tPrefixes = tPrefixesFor(sets, k);

            inter:
            for (int setNo = 0; setNo < lastSet; setNo++) {
                long[] toAnd = sets[setNo].bitImagesForPartitions(tPrefixes[setNo]);
                for (int imgNo = 0; imgNo < bitImage.length; imgNo++) {
                    bitImage[imgNo] &= toAnd[imgNo];
                    if (bitImage[imgNo] == 0) {
                        hasIntersect = false;
                        break inter;
                    }
                }
            }
            if (hasIntersect) {
                hits.addAll(linearIntersect(sets, k, tPrefixes));
            }

        }
        return hits;
    }

    private static int[] tPrefixesFor(BitImagePartitionedSet[] sets, int tk) {
        final int lastInd = sets.length - 1;
        int[] tPrefixes = new int[lastInd];
        for (int i = 0; i < sets.length - 1; i++) {
            tPrefixes[i] = tk >>> (sets[lastInd].partitionWordWidth() - sets[i].partitionWordWidth());
        }
        return tPrefixes;
    }

    private static List<Long> linearIntersect(BitImagePartitionedSet[] sets, int lastk, int[] tPrefixes) {
        List<Long> matches = new ArrayList<>();
        int last = sets.length - 1;
        FastIterator lastSetIterator = sets[last].iteratorFor(lastk);
        FastIterator[] iterators = new FastIterator[last];


        for (int i = 0; i < last; i++) {
            iterators[i] = sets[i].iteratorFor(tPrefixes[i]);
        }

        while (lastSetIterator.hasNext()) {
            long elem = lastSetIterator.next();
            boolean allFound = true;
            for (int i = 0; i < iterators.length; i++) {
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
