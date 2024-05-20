package com.toast.apocalypse.common.util;

import net.minecraft.util.RandomSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

// Uh, I couldn't come up with a better name for this class
public class DataStructureUtils {

    public static <T> T[] copyOfFill(T[] original, int newLength, T rest) {
        T[] copy = Arrays.copyOf(original, newLength);

        for (int i = original.length; i < newLength; i++) {
            copy[i] = rest;
        }
        return copy;
    }

    @Nullable
    public static <T> T getRandomListElement(@Nonnull Random random, @Nonnull List<T> list) {
        return list.isEmpty() ? null :
                (list.size() == 1) ? list.get(0) : list.get(random.nextInt(list.size()));
    }

    @Nullable
    public static <T> T getRandomListElement(@Nonnull RandomSource random, @Nonnull List<T> list) {
        return list.isEmpty() ? null :
                (list.size() == 1) ? list.get(0) : list.get(random.nextInt(list.size()));
    }


    @Nullable
    public static <T> T getRandomCollectionElementFiltered(Random random, Collection<T> collection, Predicate<T> predicate) {
        final List<T> candidateElements = new ArrayList<>();

        for (T element : collection) {
            if (predicate.test(element))
                candidateElements.add(element);
        }
        if (candidateElements.isEmpty())
            return null;

        return candidateElements.get(random.nextInt(candidateElements.size()));
    }


    @Nullable
    public static <T> T getRandomCollectionElementFiltered(RandomSource random, Collection<T> collection, Predicate<T> predicate) {
        final List<T> candidateElements = new ArrayList<>();

        for (T element : collection) {
            if (predicate.test(element))
                candidateElements.add(element);
        }
        if (candidateElements.isEmpty())
            return null;

        return candidateElements.get(random.nextInt(candidateElements.size()));
    }

    @Nullable
    public static <K, V> V getRandomMapElementFiltered(Random random, Map<K, V> map, BiPredicate<K, V> biPredicate) {
        final List<V> candidateElements = new ArrayList<>();

        for (K key : map.keySet()) {
            V value = map.get(key);

            if (biPredicate.test(key, value))
                candidateElements.add(value);
        }
        if (candidateElements.isEmpty())
            return null;

        return candidateElements.get(random.nextInt(candidateElements.size()));
    }

    @Nullable
    public static <K, V> V getRandomMapElementFiltered(RandomSource random, Map<K, V> map, BiPredicate<K, V> biPredicate) {
        final List<V> candidateElements = new ArrayList<>();

        for (K key : map.keySet()) {
            V value = map.get(key);

            if (biPredicate.test(key, value))
                candidateElements.add(value);
        }
        if (candidateElements.isEmpty())
            return null;

        return candidateElements.get(random.nextInt(candidateElements.size()));
    }

    @Nullable
    public static <K, V> K randomMapKeyFiltered(Random random, Map<K, V> map, BiPredicate<K, V> biPredicate) {
        final List<K> candidateKeys = new ArrayList<>();

        for (K key : map.keySet()) {
            V value = map.get(key);

            if (biPredicate.test(key, value))
                candidateKeys.add(key);
        }
        if (candidateKeys.isEmpty())
            return null;

        return candidateKeys.size() == 1 ? candidateKeys.get(0) : candidateKeys.get(random.nextInt(candidateKeys.size()));
    }


    @Nullable
    public static <K, V> K randomMapKeyFiltered(RandomSource random, Map<K, V> map, BiPredicate<K, V> biPredicate) {
        final List<K> candidateKeys = new ArrayList<>();

        for (K key : map.keySet()) {
            V value = map.get(key);

            if (biPredicate.test(key, value))
                candidateKeys.add(key);
        }
        if (candidateKeys.isEmpty())
            return null;

        return candidateKeys.size() == 1 ? candidateKeys.get(0) : candidateKeys.get(random.nextInt(candidateKeys.size()));
    }
}
