package org.bb.fsi;


import java.util.Arrays;

public class SortedArrayIterator implements FastIterator {
    private final long[] set;
    private final int endInd;
    private final int startInd;
    private int currInd;

    public SortedArrayIterator(final int startInd, final int endInd, final long[] set) {
        this.startInd = startInd;
        this.currInd = startInd;
        this.endInd = endInd;
        this.set = set;
    }
//
    public IterResult skipTo(long target) {
        while (currInd < endInd) {
            long currElem = set[currInd];
            if (target < currElem) {
                return IterResult.NOT_FOUND;
            }
            if (target == currElem) {
                return IterResult.FOUND;
            }
            currInd++;
        }
        return IterResult.EOF;
    }
//
//        public FastIterator.IterResult skipTo(long target) {
//            int insElem = Arrays.binarySearch(set, currInd, endInd, target);
//            if (insElem >= 0) {
//                currInd = insElem;
//                return FastIterator.IterResult.FOUND;
//            } else {
//                insElem = (-insElem) - 1;
//                currInd = insElem;
//                if (currInd < endInd) {
//                    return FastIterator.IterResult.NOT_FOUND;
//                } else {
//                    return FastIterator.IterResult.EOF;
//                }
//            }
//        }


    public long next() {
        if (hasNext()) {
            return set[currInd++];
        } else {
            throw new IllegalStateException("Cannot fetch next elem");
        }
    }

    public boolean hasNext() {
        return currInd < endInd;
    }

    @Override
    public int size() {
        return endInd - startInd;
    }

    @Override
    public int compareTo(FastIterator o) {
        return this.size() - o.size();
    }
}