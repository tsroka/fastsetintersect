package org.bb.fsi;


public interface PartitionFunction {
    /**
     * Number of partitions created by this function.
     * @return
     */
    int numberOfPartitions();

    /**
     * Width of word(no of bits) required to encode noOfPartions
     * @return
     */
    int wordWidth();

    /**
     * Assigns element to a given partition number
     * @param elem to assign
     * @return number of partition this element belongs to
     */
    int assignToParition(long elem);
}
