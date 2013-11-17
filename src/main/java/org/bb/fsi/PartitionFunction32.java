package org.bb.fsi;


import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import static org.bb.fsi.sets.PartionSetUtils.fastHash;

public class PartitionFunction32 implements PartitionFunction {
    private final static int HASH_WORD_WIDTH = 32;

    private final HashFunction hash = Hashing.murmur3_32();
    private final int noOfPartitions;
    private final int bitShift;
    private final int wordWidth;

    public PartitionFunction32(int noOfPartitions) {
        this.noOfPartitions = noOfPartitions;
        this.wordWidth = 32 - Integer.numberOfLeadingZeros(noOfPartitions - 1);
        //We take only n most significatant bits from hash function
        this.bitShift = HASH_WORD_WIDTH - wordWidth;
    }

    @Override
    public int numberOfPartitions() {
        return noOfPartitions;
    }

    @Override
    public int wordWidth() {
        return wordWidth;
    }

    @Override
    public int assignToParition(long elem) {
//        return hash.hashLong(elem).asInt() >>> bitShift;
        long l = fastHash(elem);
        l |= l >> 32;
        return ((int) l) >>> bitShift;
    }
}
