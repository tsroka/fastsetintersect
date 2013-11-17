package org.bb.fsi;

import com.google.common.base.Stopwatch;
import org.bb.fsi.sets.BitImagePartitionedSet;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class Main {

    public static void main(String[] args) throws IOException {
//        System.in.read();
        Stopwatch w = new Stopwatch();
        Set<Long>[] sets = genNSets(1000000, 1000000, 100000);
        BitImagePartitionedSet[] biSets = genNBitSets(2, sets);
//        System.in.read();

    }

    private static Set<Long>[] genNSets(int... sizes) {
        Set[] sets = new Set[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            sets[i] = genRandSet(sizes[i]);
        }
        return sets;
    }

    private static BitImagePartitionedSet[] genNBitSets(int images, Set<Long>... sets) {
        Stopwatch w = new Stopwatch();

        long[][] conv = new long[sets.length][];
        for (int i = 0; i < sets.length; i++) {
            conv[i] = toArray(sets[i]);
        }
        w.start();
        BitImagePartitionedSet[] bisets = new BitImagePartitionedSet[sets.length];
        for (int i = 0; i < sets.length; i++) {
            bisets[i] = BitImagePartitionedSet.creatRandPartSet(conv[i], images);
        }
        w.stop();
        System.out.printf("NBisets: %s [ms]\n ", w.elapsedMillis());

        return bisets;
    }

    private static Set<Long> retainAll(Set<Long>... sets) {
        TreeSet<Long> s = new TreeSet<>(sets[0]);
        for (int i = 1; i < sets.length; i++) {
            s.retainAll(sets[i]);
        }
        return s;
    }

    private static Set<Long> genRandSet(int size) {
        Random rand = new Random();
        Set<Long> set = new TreeSet<>();
        long i=0;
        while (set.size() < size) {
            set.add(i++);
        }
        set.add(5l);
        return set;

    }

    public static long[] toArray(Set<Long> set) {
        long[] a = new long[set.size()];
        int ind = 0;
        for (Long l : set) {
            a[ind++] = l;
        }
        return a;
    }

}
