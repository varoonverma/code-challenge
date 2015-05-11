package com.sliding.window;

/**
 * Wrapper for the raw input received from the buffer.
 * It tracks the index position this input had on the buffer.
 *
 * Created by varunverma on 9/05/2015.
 */
class Item<T> {
    private final T value;
    private final int index;

    public Item(T value, int index) {
        this.value = value;
        this.index = index;
    }

    public T getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }
}