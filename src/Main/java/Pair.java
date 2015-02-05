package main.java;

/**
 * Quick and dirty pair. Not meant to be good in any sort of way.
 *
 * @author Trevor Killeen (2015).
 */
public class Pair<T> {

    public T first;
    public T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }
}
