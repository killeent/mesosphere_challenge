package main;

/**
 * Quick and dirty triple. Not meant to be good in any sort of way.
 *
 * @author Trevor Killeen (2015)
 */
public class Triple<T> {

    public T first;
    public T second;
    public T third;

    public Triple(T first, T second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
