import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Future를 사용해서도 비동기 프로그래밍이 가능했지만, Future만으로 힘든 작업들도 많았다.
 *
 * Future는 취소하거나 get()에 타임아웃을 설정할 수는 있지만, 외부에서 완료시킬 수는 없다.
 * 블로킹 코드(get())를 사용하지 않고는 작업이 끝났을 때 콜백을 실행할 수 없다.
 * 여러 Future를 조합할 수 없다. (ex. 유튜브 영상 정보를 가져오고 해당 영상의 댓글 목록 가져오기)
 * 예외 처리용 API를 제공하지 않는다.
 * CompletableFuture는 Future만으로는 힘들었던 비동기 작업을 가능하게 하는 인터페이스이다. CompletableFuture를 사용하면 ExecutorService 객체와 Future 객체를 따로 만들지 않아도 된다.
 */
public class CompleteFutureTest {

    /**
     * Runnable처럼 리턴값 없이 사용할 때는 runAsync()를 사용한다
     * 리턴값이 있는 경우에는 supplyAsync()를 사용한다.
     */
    @Test
    void testCompleteFuture() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Return: " + Thread.currentThread().getName());
            return "ParamReturn";
        }).thenApply((s) -> { // Asynchronous하게 콜백을 실행
            System.out.println("CallBack: " + Thread.currentThread().getName());
            return s.toUpperCase();
        });

        System.out.println("get: " + future.get());
    }

    @Test
    @DisplayName("두 번째 인자로 ExecutorService를 주면 해당 스레드 풀 내에서 작업을 할당해서 처리한다.")
    void testCompleteFutureWithExecutorService() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Hello: " + Thread.currentThread().getName());
            return "HelloReturn";
        }, executorService).thenAccept((s) -> {
            System.out.println("HelloCallBack: " + Thread.currentThread().getName());
            System.out.println(s.toUpperCase());
        });

        future.get();
        executorService.shutdown();
    }

    @Test
    @DisplayName("콜백 작업을 executor 스레드 풀에서 실행하도록 할 수도 있다.")
    void testCompleteFutureWithExecutorServiceCallback() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("Bye: " + Thread.currentThread().getName());
            return "ByeReturn";
        }, executorService).thenAcceptAsync((s) -> {
            System.out.println("ByeCallBack: " + Thread.currentThread().getName());
            System.out.println(s.toUpperCase());
        }, executorService);

        future.get();
        executorService.shutdown();
    }
}
