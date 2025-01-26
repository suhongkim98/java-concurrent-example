import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

// Spring 에서 제공하는 ThreadPoolTaskExecutor
public class SpringThreadPoolTaskExecutorTest {

    private static ThreadPoolTaskExecutor executor;

    @BeforeAll
    static void beforeAll() {
        executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("example-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 최초 스레드풀에 setCorePoolSize 만큼의 스레드를 만들어 놓고 이게 모두 사용 중일 경우
        // setQueueCapacity 사이즈의 큐에 작업을 쌓는다
        // setQueueCapacity 사이즈의 큐 마저도 가득 찬 상태라면 최대 setMaxPoolSize 만큼 스레드를 추가로 생성해서 동시처리량을 늘린다.
        executor.setCorePoolSize(1);
        executor.setQueueCapacity(1);
        executor.setMaxPoolSize(3);

        executor.setWaitForTasksToCompleteOnShutdown(true); // shutdown될 때 큐에 적재된 모든 작업이 처리될 때 까지 기다린다.
        executor.setAwaitTerminationSeconds(5); // 무한정 기다릴 수 없으니 n초로 제한한다
        executor.initialize();
    }

    @Test
    @DisplayName("queue capacity 에 가득차면 활성 스레드 수가 늘어나는 예제")
    void testQueueCapacity() {
        executor.execute(() -> {
            try {
                System.out.println("task 1, active thread count = " + executor.getActiveCount());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("== catch InterruptedException");
                throw new RuntimeException(e);
            }
        });

        executor.execute(() -> {
            try {
                System.out.println("task 2, active thread count = " + executor.getActiveCount());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("== catch InterruptedException");
                throw new RuntimeException(e);
            }
        });


        executor.execute(() -> {
            try {
                System.out.println("task 3, active thread count = " + executor.getActiveCount());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("== catch InterruptedException");
                throw new RuntimeException(e);
            }
        });

        executor.shutdown(); // 얘는 블로킹이네
    }

    @Test
    @DisplayName("shutdown 명령 내렸는데 task 수행시간이 setAwaitTerminationSeconds 설정보다 크다면?")
    void testAwaitTerminationSeconds() {
        executor.execute(() -> {
            try {
                System.out.println("active thread count = " + executor.getActiveCount());
                Thread.sleep(1000000);
            } catch (InterruptedException e) {
                System.out.println("== catch InterruptedException");
                throw new RuntimeException(e);
            }
        });

        executor.shutdown(); // 얘는 블로킹이네
    }
}
