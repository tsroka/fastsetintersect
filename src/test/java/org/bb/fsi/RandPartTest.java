package org.bb.fsi;


import com.google.common.base.Stopwatch;
import org.bb.fsi.sets.BItImageIntersect;
import org.bb.fsi.sets.BitImagePartitionedSet;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class RandPartTest {

    public static final int TEST_COUNT = 1000;

    @Test
    public void testCeilLog2() {
        RandPart.ceilLog2(1);
    }

    @Test
    public void testCosTam() {
        Stopwatch w = new Stopwatch();
        w.start();
        Set<Long> set1 = genRandSet(1000);
        Set<Long> set2 = genRandSet(2000);
        Set<Long> set3 = genRandSet(10000);
        Set<Long> set4 = genRandSet(10000);
        w.stop();
        System.out.printf("WG: %s [ms]\n ", w.elapsedMillis());
        w.reset();
        List<Long> list = toList(set1);
        List<Long> list1 = toList(set2);
        List<Long> list2 = toList(set3);
        List<Long> list3 = toList(set4);
        w.start();
        RandPart randPart = new RandPart(list, list1, list2, list3);
        w.stop();
        System.out.printf("WS: %s [ms]\n ", w.elapsedMillis());

//        RandPart randPart = new RandPart(Arrays.asList(1,2,5,10,11,19,22,32,434,22), );
//        RandPart randPart = new RandPart(genRandSet(1000));
        w.reset();
        w.start();
        List<Long> intersect = randPart.intersect();
        w.stop();

        System.out.printf("W1: %s [ms] num: %d\n", w.elapsedMillis(), intersect.size());

        w.reset();
        w.start();
        set1.retainAll(set2);
        set1.retainAll(set3);
        set1.retainAll(set4);
        w.stop();
        System.out.printf("W2: %s [ms] num: %d\n", w.elapsedMillis(), set1.size());


    }

    @Test
    public void testCosTam2() throws IOException {
        Stopwatch w = new Stopwatch();
        Set<Long>[] sets = genNSets(900, 1000, 1100, 1200, 1300, 1400);
        BitImagePartitionedSet[] biSets = genNBitSets(2, sets);
        long[][] arraySets = genArraySets(sets);

        w.start();
        List<Long> intersect = null;
        for (int i = 0; i < TEST_COUNT; i++) {
            intersect = BItImageIntersect.intersect(biSets);
        }
        w.stop();
        System.out.printf("W1: %s [ms] num: %d\n", w.elapsedMillis(), intersect.size());

        w.reset();
        w.start();
        List<Long> linearInters = null;
        for (int i = 0; i < TEST_COUNT; i++) {
            linearInters = IntersectUtils.linearIntersect(genFastIter(arraySets));
        }
        w.stop();
        System.out.printf("WMergeScan: %s [ms] num: %d\n", w.elapsedMillis(), linearInters.size());
        w.reset();

        w.reset();
        w.start();
        Set<Long> s = null;
        for (int i = 0; i < TEST_COUNT; i++) {
            s = retainAll(sets);
        }
        w.stop();
        System.out.printf("W2: %s [ms] num: %d\n", w.elapsedMillis(), s.size());


    }

    private long[][] genArraySets(Set<Long>... sets) {
        long arr[][] = new long[sets.length][];
        for (int i = 0; i < sets.length; i++) {
            arr[i] = toArray(sets[i]);
            Arrays.sort(arr[i]);
        }
        return arr;
    }

    private FastIterator[] genFastIter(long[]... sets) {
        FastIterator[] iters = new FastIterator[sets.length];
        for (int i = 0; i < sets.length; i++) {
            iters[i] = new SortedArrayIterator(0, sets[i].length, sets[i]);
        }
        return iters;
    }

    private Set<Long>[] genNSets(int... sizes) {
        Set[] sets = new Set[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            sets[i] = genRandSet(sizes[i]);
        }
        return sets;
    }

    private BitImagePartitionedSet[] genNBitSets(int images, Set<Long>... sets) {
        Stopwatch w = new Stopwatch();

        long[][] conv = genArraySets(sets);

        w.start();
        BitImagePartitionedSet[] bisets = new BitImagePartitionedSet[sets.length];
        for (int i = 0; i < sets.length; i++) {
            bisets[i] = BitImagePartitionedSet.creatRandPartSet(conv[i], images);
        }
        w.stop();
        System.out.printf("NBisets: %s [ms]\n ", w.elapsedMillis());

        return bisets;
    }

    private Set<Long> retainAll(Set<Long>... sets) {
        TreeSet<Long> s = new TreeSet<>(sets[0]);
        for (int i = 1; i < sets.length; i++) {
            s.retainAll(sets[i]);
        }
        return s;
    }

    private Set<Long> genRandSet(int size) {
        Random rand = new Random();
        Set<Long> set = new TreeSet<>();
        while (set.size() < size) {
            set.add(rand.nextLong()%1400);
        }
        set.add(5l);
        return set;

    }

    public long[] toArray(Set<Long> set) {
        long[] a = new long[set.size()];
        int ind = 0;
        for (Long l : set) {
            a[ind++] = l;
        }
        return a;
    }

    public List<Long> toList(Set<Long> set) {
        Long[] a = set.toArray(new Long[set.size()]);
        Arrays.sort(a);
        return Arrays.asList(a);
    }
}
