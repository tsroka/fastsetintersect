package org.bb.fsi.sets;


import org.bb.fsi.PartitionFunction;

import java.util.Arrays;
import java.util.List;

public class PartionSetUtils {

    /**
     * Realigns elements in set to partitions (0..partitionsCount). If set was sorted elements within partition
     * will be also sorted.
     * <p/>
     * E.g.
     * Given Set: [-1,0,1,2,4,5], and partition count = 3,
     * elemsAffiliation = [1,2,0,0,2,1] (this should be read as elem at index 0 belongs to part 1, elem and index 2 belong to 2, etc.)
     * and partInd = [0,2,4] (part 0 start span thru indexes <0-2), part 1 <2,4) etc
     * set will be reordered as follows:
     * [1, 2,  -1, 5,  0,4]
     * |      |      |
     * Part 0  Part 1 Part 2
     *
     * @param set              to realign
     * @param elemsAffiliation array of size = set.size(), having at index i number of partition to which elem set.get(i) belongs
     * @param partitionIndices arrray of size = partitionsCount, having at index i, start index of partition i
     */
    public static void alignSetElementsToPartitions(long[] set, PartitionsAffiliation affiliation) {
        //Sort set such that each element belongs to its group
        long[] oldSet = Arrays.copyOf(set, set.length);
        int[] groupSizes = new int[affiliation.partitionsCount()];
        for (int i = 0; i < set.length; i++) {
            int groupNo = affiliation.getElementsAffiliation()[i];
            int index = affiliation.getPartitionsIndices()[groupNo] + groupSizes[groupNo];
            set[index] = oldSet[i];

            groupSizes[groupNo] += 1;
        }

    }

    public static long fastHash(long k) {
        k *= 357913941;
        k ^= k << 56;
        k += ~357913941;
        k ^= k >> 63;
        k ^= k << 63;
        return k;
    }


    public static PartitionsAffiliation computeElemToPartitionAssignment(long[] set, PartitionFunction partFunc) {
        int[] partitionIndices = new int[partFunc.numberOfPartitions()];
        int[] elemsAffiliation = new int[set.length];

        //If more than just a one partition - else no work to be done here
        if (partitionIndices.length > 1) {

            //Compute partition of each element and no of elements in each group
            for (int i = 0; i < set.length; i++) {
                int partitionNo = partFunc.assignToParition(set[i]);
                elemsAffiliation[i] = partitionNo;
                partitionIndices[partitionNo] += 1;
            }
            computeIndicesFromSizes(partitionIndices);
        }

        return new PartitionsAffiliation(elemsAffiliation, partitionIndices);
    }

    public static void computeIndicesFromSizes(int[] partitionSizes) {
        int last = 0;
        for (int i = 1; i < partitionSizes.length; i++) {
            int tmp = partitionSizes[i];
            partitionSizes[i] = partitionSizes[i - 1] + last;
            last = tmp;

        }
        partitionSizes[0] = 0;
    }

    public static int ceilLog2(int i) {
        return i < 2 ? 1 : 32 - Integer.numberOfLeadingZeros(i - 1);
    }

    public static int indexedBinarySearch(List<Long> list, long key, int start, int end) {
        int low = start;
        int high = end;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            long midVal = list.get(mid);
            if (midVal < key)
                low = mid + 1;
            else if (midVal > key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    public static class PartitionsAffiliation {
        private final int[] elementsAffiliation;
        private final int[] partitionsIndices;

        public PartitionsAffiliation(int[] elementsAffiliation, int[] partitionsIndices) {
            this.elementsAffiliation = elementsAffiliation;
            this.partitionsIndices = partitionsIndices;
        }

        public int[] getElementsAffiliation() {
            return elementsAffiliation;
        }

        public int[] getPartitionsIndices() {
            return partitionsIndices;
        }

        public int partitionsCount() {
            return partitionsIndices.length;
        }

        public int endIndexOf(int partitionNo) {
            return partitionNo + 1 < partitionsIndices.length ? partitionsIndices[partitionNo + 1]
                    : elementsAffiliation.length;
        }

        public int startIndexOf(int partitionNo) {
            return partitionsIndices[partitionNo];
        }
    }
}
