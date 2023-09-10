import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * FutureTask 는 Future 자체를 Object로 만들어준다.
 */
public class FutureTaskTest {

    @Test
    void testFutureTaskTest() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newCachedThreadPool();
        FutureTask<String> f = new FutureTask<>(()->{
            Thread.sleep(2000); // 스레드 잠들기
            String threadName = Thread.currentThread().getName();
            System.out.println("Job1 " + threadName);
            return "Hello";
        });
        executor.execute(f);

        System.out.println(f.isDone());
        Assertions.assertFalse(f.isDone()); // 작업이 완료되지 않았으므로 false

        Thread.sleep(2100);
        System.out.println(f.isDone());
        Assertions.assertTrue(f.isDone()); // 작업이 완료되어있으므로 true

        System.out.println(f.get());
    }
}
