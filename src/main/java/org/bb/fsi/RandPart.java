package org.bb.fsi;


import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.*;

public class RandPart {
    public static final int MACHINE_WORD = 32;
    public static final int MW_SQRT = 5;
    public static final int N_IMAGES = 2;

    HashFunction hash = Hashing.murmur3_32();

    private final List<Long>[] sets;

    private int[][] groupRanges;
    private int[] wordWidths;
    private int[][][] groupUnionSets;

    public RandPart(List<Long>... toMerge) {
        sets = Arrays.copyOf(toMerge, toMerge.length);

        groupRanges = new int[sets.length][];
        wordWidths = new int[sets.length];
        groupUnionSets = new int[sets.length][][];

        Arrays.sort(sets, new Comparator<Collection<Long>>() {
            @Override
            public int compare(Collection<Long> o1, Collection<Long> o2) {
                return o1.size() - o2.size();
            }
        });
        preprocessSmallSets();
    }

    public List<Long> intersect() {
        List<Long> hits = new ArrayList<>();

        int lastSet = sets.length - 1;
        int maxGroups = groupRanges[lastSet].length;
        for (int k = 0; k < maxGroups; k++) {
            int bitInt = groupUnionSets[lastSet][k][0];
            for (int setNo = lastSet - 1; setNo >= 0; setNo--) {
                int tPref = tPrefixFor(setNo, k);
                bitInt &= groupUnionSets[setNo][tPref][0];
                if (bitInt == 0) {
                    break;
                }
            }
            if (bitInt != 0) {
                hits.addAll(linearIntersect(k));
            }

        }
        return hits;

    }

    private List<Long> linearIntersect(int k) {
        List<Long> matches = new ArrayList<>();
        int last = sets.length - 1;
        SetGroupIterator lastSetIterator = new SetGroupIterator(last, k);
        SetGroupIterator[] iterators = new SetGroupIterator[last];


        for (int i = 0; i < last; i++) {
            iterators[i] = new SetGroupIterator(i, tPrefixFor(i, k));
        }

        while (lastSetIterator.hasNext()) {
            long elem = lastSetIterator.next();
            boolean allFound = true;
            for (int i = 0; i < iterators.length; i++) {
                IterResult iterResult = iterators[i].skipTo(elem);
                if (IterResult.EOF == iterResult) {
                    return matches;
                } else if (IterResult.NOT_FOUND == iterResult) {
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

    private int tPrefixFor(int setNo, int tk) {
        return tk >>> (wordWidths[sets.length - 1] - wordWidths[setNo]);
    }

    private void preprocessSmallSets() {
        for (int i = 0; i < sets.length; i++) {
            wordWidths[i] = ceilLog2(sets[i].size() / MW_SQRT);
            groupRanges[i] = assignElemsToSmallGroups(sets[i], wordWidths[i]);
            computeUnionSets(i);
        }
    }

    private void computeUnionSets(int setNo) {
        List<Long> set = sets[setNo];
        int noOfGroups = groupRanges[setNo].length;
        groupUnionSets[setNo] = new int[noOfGroups][];

        for (int group = 0; group < noOfGroups; group++) {
            groupUnionSets[setNo][group] = computeUnionSet(set, groupRanges[setNo][group], endRange(setNo, group));
        }

    }

    private int[] computeUnionSet(List<Long> set, int groupStart, int groupEnd) {
        int[] images = new int[N_IMAGES];
        for (int i = groupStart; i < groupEnd; i++) {
            //Generate one big hash
            int elHash = hash.hashLong(set.get(i)).asInt();
            for (int hNum = 0; hNum < N_IMAGES; hNum++) {
                //This will map each element to a bit
                int bitToSet = elHash >>> hNum * 5 & 0x1f;

                images[hNum] |= 1 << bitToSet;
            }
        }
        return images;
    }

    private int endRange(int setNo, int groupNo) {
        return groupNo + 1 < groupRanges[setNo].length ? groupRanges[setNo][groupNo + 1] : sets[setNo].size();
    }


    /**
     * Creates groups for each set and sorts set to correspond to groups.
     *
     * @param set
     * @return array of size corresponding to no of small groups, i-th index contains starting index of group number i
     */
    private int[] assignElemsToSmallGroups(List<Long> set, int wordWidth) {

        int noOfSmallGroups = 1 << wordWidth;

        int[] perGroupIndexes = new int[noOfSmallGroups];
        if (noOfSmallGroups < 2) {
            //Just one group - no work to be done here
            return perGroupIndexes;
        }
        int bitShift = 32 - wordWidth;
        //Compute group of each element and no of elements in each group
        int[] groupsAssign = new int[set.size()];
        for (int i = 0; i < set.size(); i++) {
            int groupNo = hash.hashLong(set.get(i)).asInt() >>> bitShift;
            groupsAssign[i] = groupNo;
            perGroupIndexes[groupNo] += 1;
        }

        computeStartingIndexOfGroups(noOfSmallGroups, perGroupIndexes);
        sortSetToMatchGroups(set, noOfSmallGroups, groupsAssign, perGroupIndexes);

        return perGroupIndexes;
    }

    private void sortSetToMatchGroups(List<Long> set, int noOfSmallGroups, int[] groupsAssign, int[] perGroupIndexes) {
        //Sort set such that each element belongs to its group
        Long[] oldSet = set.toArray(new Long[set.size()]);
        int[] groupSizes = new int[noOfSmallGroups];
        for (int i = 0; i < set.size(); i++) {
            int groupNo = groupsAssign[i];
            set.set(perGroupIndexes[groupNo] + groupSizes[groupNo], oldSet[i]);
            groupSizes[groupNo] += 1;
        }

    }

    private void computeStartingIndexOfGroups(int noOfSmallGroups, int[] perGroupIndexes) {
        int last = 0;
        for (int i = 1; i < noOfSmallGroups; i++) {
            int tmp = perGroupIndexes[i];
            perGroupIndexes[i] = perGroupIndexes[i - 1] + last;
            last = tmp;

        }
        perGroupIndexes[0] = 0;
    }

    private int numOfGroups(int numOfElems) {
        int i = ceilLog2(numOfElems / MW_SQRT);
        return 1 << i;
    }

    static int ceilLog2(int i) {
        return i < 2 ? 1 : 32 - Integer.numberOfLeadingZeros(i - 1);
    }

    private enum IterResult {NOT_FOUND, FOUND, EOF}


    private class SetGroupIterator {
        final int setNo;
        final int groupNo;
        final int endInd;
        int currInd;

        private SetGroupIterator(int setNo, int groupNo) {
            this.setNo = setNo;
            this.groupNo = groupNo;
            this.currInd = groupRanges[setNo][groupNo];
            this.endInd = endRange(setNo, groupNo);
        }

        IterResult skipTo(long target) {
            while (currInd < endInd) {
                long currElem = sets[setNo].get(currInd);
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

        long next() {
            if (hasNext()) {
                return sets[setNo].get(currInd++);
            } else {
                throw new IllegalStateException("Cannot fetch next elem");
            }
        }

        boolean hasNext() {
            return currInd < endInd;
        }
    }


//    public FastIterator.IterResult skipTo(long target) {
//        int insElem = PartionSetUtils.indexedBinarySearch(set, target, currInd, endInd-1);
//        if (insElem >= 0) {
//            currInd = insElem;
//            return FastIterator.IterResult.FOUND;
//        } else {
//            insElem = (-insElem) - 1;
//            currInd = insElem;
//            if (currInd < endInd) {
//                return FastIterator.IterResult.NOT_FOUND;
//            } else {
//                return FastIterator.IterResult.EOF;
//            }
//        }
//    }

}
