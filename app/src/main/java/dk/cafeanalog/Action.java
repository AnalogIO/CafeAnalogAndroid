package dk.cafeanalog;

@SuppressWarnings("ALL")
public interface Action<T> {
    void run(T param);
}
