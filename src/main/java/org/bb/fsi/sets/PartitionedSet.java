package org.bb.fsi.sets;


import org.bb.fsi.FastIterator;
import org.bb.fsi.PartitionFunction;
import org.bb.fsi.SortedArrayIterator;

import static org.bb.fsi.sets.PartionSetUtils.*;

public class PartitionedSet implements Comparable<PartitionedSet> {
    protected final long[] set;


    protected final PartitionFunction partitionFunction;
    private final int partWordWidth;
    protected PartitionsAffiliation affiliation;

    public PartitionedSet(long set[], PartitionFunction partFunc) {
        this.set = set;
        this.partitionFunction = partFunc;
        this.affiliation = partitionSet();
        this.partWordWidth = partFunc.wordWidth();
    }

    public int partitionsCount() {
        return affiliation.partitionsCount();
    }

    public int partitionWordWidth() {
        return partWordWidth;
    }

    /**
     * Creates partitions for set and sorts it corresponding to groups.
     *
     * @return array of size corresponding to no of small groups, i-th index contains starting index of group number i
     */
    private PartitionsAffiliation partitionSet() {
        int numPart = partitionFunction.numberOfPartitions();
        PartitionsAffiliation affiliation = computeElemToPartitionAssignment(set, partitionFunction);
        if (numPart > 1) {
            alignSetElementsToPartitions(set, affiliation);
        }
        return affiliation;
    }

    public FastIterator iteratorFor(int partitionNo) {
        return new SortedArrayIterator(affiliation.startIndexOf(partitionNo), affiliation.endIndexOf(partitionNo),
                this.set);
    }

    @Override
    public int compareTo(PartitionedSet o) {
        return this.set.length - o.set.length;
    }


}
