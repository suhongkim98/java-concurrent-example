import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Callback 을 이용하여 비동기 실행 결과를 처리할 수 있다.
 * 비동기를 다루는 가장 깔끔한 방법이다.
 */
public class CallbackTest {

    @Test
    void testCallback() throws InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool();

        CallbackFutureTask<String> f = new CallbackFutureTask<String>(() -> {
            Thread.sleep(2000);
            // if(1 == 1) throw new RuntimeException("Async ERROR!!!"); // 강제로 예외를 던져보자
            return "Hello";
        },
                System.out::println, // 성공 시 출력
                e -> System.out.println("Error: " + e.getMessage()) // 실패 시
        );

        executor.execute(f);
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS); // 블로킹 걸기 위함
    }
}
