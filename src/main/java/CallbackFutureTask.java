import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallbackFutureTask<T> extends FutureTask<T> {
    private final SuccessCallback<T> successCallback;
    private final ExceptionalCallback exceptionalCallback;

    public CallbackFutureTask(Callable<T> callable, SuccessCallback<T> successCallback, ExceptionalCallback exceptionalCallback) {
        super(callable);
        this.successCallback = Objects.requireNonNull(successCallback);
        this.exceptionalCallback = Objects.requireNonNull(exceptionalCallback);
    }

    @Override
    protected void done() {
        try {
            successCallback.onSuccess(get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            exceptionalCallback.onError(e.getCause());
        }
    }
}
