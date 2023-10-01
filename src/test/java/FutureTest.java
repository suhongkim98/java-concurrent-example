import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Future를 이용하면 예약된 작업에 대한 결과를 알 수 있습니다.
 */
public class FutureTest {

    @Test
    @DisplayName("executor.submit()은 Future 객체를 리턴한다.")
    void testSubmitReturnFuture() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Assertions.assertInstanceOf(Future.class, executor.submit(() -> {}));
    }

    @Test
    @DisplayName("executore.submit(..) 파라미터로 Runnable 혹은 Callable 을 넣을 수 있다.")
    void testSubmitParams() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Runnable은 반환 값이 void 이기 때문에, 작업을 처리하는 것 뿐 작업 결과를 리턴할 수 없었다.
        Runnable runnable = () -> { // void형 리턴
            System.out.println("hi");
        };
        executor.submit(runnable);

        // Callable은 Runnable과 유사하지만, 작업의 결과를 받을 수 있다는 차이가 있다.
        Callable<String> callable = () -> {
            return "hello";
        }; // return String
        executor.submit(callable);
    }

    @Test
    @DisplayName("future.get()로 작업이 종료될 때 까지 기다리고 그 결과를 출력할 수 있다.")
    void testFutureGet() {
        final int maxCore = Runtime.getRuntime().availableProcessors();
        final ExecutorService executor = Executors.newFixedThreadPool(maxCore);
        final List<Future<String>> futures = new ArrayList<>();

        // 첫번째 executor submit 부터 차례대로 futures 리스트에 추가한다.
        // futures 리스트에는 첫번째 작업부터 순서대로 future 이 추가되지만, 작업 종료 순서는 멀티스레드 환경에서 순차적이지 않다.
        for (int i = 1; i < 5; i++) {
            final int index = i;
            futures.add(executor.submit(() -> {
                System.out.println("finished job" + index);
                return "job" + index + " " + Thread.currentThread().getName();
            }));
        }

        // 첫번째 submit 결과부터 차례대로 get() 메서드를 이용해 순차적으로 작업 종료를 기다리고 로그를 찍기 때문에
        // 작업 종료 순서와 상관없이 첫번째 실행결과부터 순서대로 출력된다.
        // 결과를 출력 시 전체 작업 종료 순서와 상관없이 첫번째 작업이 늦게 처리된다면 다른 작업에 대한 로그도 늦게 출력이 된다. -> 비효율적이다. -> BlockingQueue 로 해결 가능
        for (Future<String> future : futures) {
            String result = null;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            System.out.println(result);
        }

        executor.shutdownNow();
        System.out.println("end");
    }

    @Test
    @DisplayName("작업이 끝나면 바로 BlockingQueue 에 결과를 추가한다.")
    void testBlockingQueue() throws InterruptedException {
        final int maxCore = Runtime.getRuntime().availableProcessors();
        final ExecutorService executor = Executors.newFixedThreadPool(maxCore);
        final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10); // Blocking Queue // 내부적으로 add 시 스레드 세이프하다.

        // 작업을 summit 한다.
        // 작업이 완료되면 그 결과가 blocking queue 에 들어가도록 한다.
        for (int i = 0; i < 4; i++) {
            final int index = i;
            executor.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("finished " + index);
                String result = index + ", " + threadName;

                try {
                    queue.put(result); // 작업 결과를 블로킹 큐에 삽입 // 작업 종료 순서를 완전보장하지 않음
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        for (int i = 0 ; i < 4; i++) { // 작업 결과를 하나씩 출력한다.
            String result = queue.take();
            System.out.println(result);
        }

        executor.shutdownNow();
        System.out.println("end");
    }
}
