package org.bb.fsi.sets;


import org.bb.fsi.PartitionFunction;
import org.bb.fsi.PartitionFunction32;

public class BitImagePartitionedSet extends PartitionedSet {
    public static final int MW_SQRT = 8;


    //[x][y] - x -part number, y-image number
    private final long[][] partitionImages;
    private final int noOfImages;

    public BitImagePartitionedSet(long[] set, PartitionFunction partFunc, int noOfImages) {
        super(set, partFunc);
        this.noOfImages = noOfImages;
        this.partitionImages = new long[affiliation.partitionsCount()][noOfImages];
        computeBitImagesForAllPartitions();
    }

    public long[] bitImagesForPartitions(int partNo) {
        return partitionImages[partNo];
    }

    private void computeBitImagesForAllPartitions() {
        for (int i = 0; i < affiliation.partitionsCount(); i++) {
            computeBitImagesForPartition(i);
        }
    }

    public static BitImagePartitionedSet creatRandPartSet(long[] set, int noOfImages) {
        int noOfGroups = 1 << PartionSetUtils.ceilLog2(set.length / MW_SQRT);
        return new BitImagePartitionedSet(set, new PartitionFunction32(noOfGroups), noOfImages);
    }

    private void computeBitImagesForPartition(int partitionNo) {
        for (int i = affiliation.startIndexOf(partitionNo); i < affiliation.endIndexOf(partitionNo); i++) {
            //Generate one big hash
//            long elHash = hash.hashLong(set[i]).asLong();
            long elHash = PartionSetUtils.fastHash(set[i]);
            for (int hNum = 0; hNum < noOfImages; hNum++) {
                //This will map each element to a bit
                //Take n-th group of six bix (so we get ranges 0-63)
                long bitToSet = elHash >>> hNum * 6 & 0x3F;
                partitionImages[partitionNo][hNum] |= 1 << bitToSet;
            }
        }
    }
}
