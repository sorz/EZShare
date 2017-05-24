package EZShare.server;

/**
 * Helper class in stream.
 * Count the number of object went through map().
 *
 * Created on 2017/5/24.
 */
class Counter<T> {
    private int num;

    synchronized T count(T obj) {
        num++;
        return obj;
    }

    public int getCount() {
        return num;
    }
}
