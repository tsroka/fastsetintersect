package org.bb.fsi;


public interface FastIterator extends Comparable<FastIterator>{
    enum IterResult {NOT_FOUND, FOUND, EOF}

    IterResult skipTo(long target);
    long next();
    boolean hasNext();
    int size();

}
