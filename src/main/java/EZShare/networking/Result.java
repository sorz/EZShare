package EZShare.networking;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created on 2017/5/10.
 */
public class Result<T> {
    final private T ok;
    final private IOException err;

    public Result(T ok) {
        this.ok = ok;
        this.err = null;
    }

    public Result(IOException err) {
        this.ok = null;
        this.err = err;
    }

    public static <T> Result<T> of(T ok) {
        return new Result<>(ok);
    }

    public static Result<None> of () {
        return new Result<None>(new None());
    }

    public boolean isOk() {
        return ok != null;
    }

    public T ok() {
        return ok;
    }

    public IOException err() {
        return err;
    }

    public Result<T> ifErr(Consumer<IOException> consumer) {
        if (err != null)
            consumer.accept(err);
        return this;
    }

    public Result<T> ifOk(Consumer<T> consumer) {
        if (err == null)
            consumer.accept(ok);
        return this;
    }

    public static class None {};
}
