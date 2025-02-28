import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * java.util.concurrent.Executors와 java.util.concurrent.ExecutorService를 이용하면 간단히 쓰레드풀을 생성하여 병렬처리를 할 수 있다.
 */
public class ExecutorsTest {

    @Test
    @DisplayName("Executors는 여러 유형의 ExecutorService 객체를 생성하기 위한 팩토리 메서드를 제공한다.")
    void testCreateExecutorServiceByFactoryMethod() {
        /**
         * newFixedThreadPool(int) : 인자 개수만큼 고정된 쓰레드풀을 만듭니다.
         * newCachedThreadPool(): 필요할 때, 필요한 만큼 쓰레드풀을 생성합니다. 이미 생성된 쓰레드를 재활용할 수 있기 때문에 성능상의 이점이 있을 수 있습니다.
         * newScheduledThreadPool(int): 일정 시간 뒤에 실행되는 작업이나, 주기적으로 수행되는 작업이 있다면 ScheduledThreadPool을 고려해볼 수 있습니다.
         * newSingleThreadExecutor(): 쓰레드 1개인 ExecutorService를 리턴합니다. 싱글 쓰레드에서 동작해야 하는 작업을 처리할 때 사용합니다.
         * newWorkStealingPool(): 경합을 줄이기 위해 여러 대기열을 사용할 수 있는 스레드풀을 만든다. (내부적으로 커스텀 ForkJoinPool 사용)
         * newVirtualThreadPerTaskExecutor(): 가상스레드를 생성합니다. Executors로 가상스레드를 다루고 싶을 때 사용
         */

        // 4개의 스레드를 가진 스레드풀 생성하기
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // SingleThreadExecutor는 Thread가 1개인 Executor입니다. 1개이기 때문에 작업을 예약한 순서대로 처리를 합니다.
        // 스레드가 1개 뿐이니 동시성(Concurrency)을 고려할 필요가 없습니다.
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

        ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Test
    @DisplayName("shutdown 메서드는 이미 제출(submit)한 작업이 실행 완료된 후 종료를 진행하며, 더 이상 새로운 작업을 추가하지 않는다.")
    void testShutdownExecutorService() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 모든 작업을 처리한 후 스레드풀 종료 (shutdown is non-blocking)
        executor.shutdown();
    }

    @Test
    @DisplayName("shutdownNow 메서드는 활발하게 실행 중인 모든 작업을 interrupt로 중지하려고 시도해 대기 중인 작업의 처리를 중지하고, 실행 대기 중인 작업 목록을 반환")
    void testShutdownNowExecutorService() {
        // 싱글 스레드풀
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("== catch InterruptedException ==");
                throw new RuntimeException(e);
            }
        });

        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("== catch InterruptedException ==");
                throw new RuntimeException(e);
            }
        });

        // 실행 중인 작업을 강제로 종료시키기 위해 노력은 하지만 반드시 성공한다고 보장할 수 없다.
        // 예를 들어 Thread#interrupt를 이용해 강제 종료를 구현한 경우 실행 중인 작업이 인터럽트에 대응하도록 설계하지 않았다면 종료되지 않을 수 있다
        List<Runnable> runnables = executor.shutdownNow();

        // 싱글 스레드풀이라서 1개만 제출되고 대기 큐에 있던 작업 1개만 반환
        Assertions.assertEquals(1, runnables.size());
    }

    @Test
    @DisplayName("close 메서드 활용한 graceful shutdown 예시, 최대 하루까지 모든 작업이 종료되기를 기다리고 종료")
    void testCloseExecutorService() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("== Job1 " + threadName + " start ==");

            for (int i = 0 ; i < 3 ; i++) {
                try {
                    System.out.println("task");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        executor.close(); // 모든 작업이 끝나면 스레드풀을 종료한다.
        System.out.println("shutdown waiting......");
    }

    @Test
    @DisplayName("java docs graceful shutdown 예시")
    void testGracefulShutdown() {
        // shutdown은 논블로킹임
        // shutdown 메서드를 우선 호출하고 주어진 타임아웃 내까지 블로킹을 건다.
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 하나의 잡이 3초가 걸린다고 가정
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("== Job1 " + threadName + " start ==");

            for (int i = 0 ; i < 3 ; i++) {
                try {
                    System.out.println("task");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("== catch InterruptedException ==");
                    throw new RuntimeException(e);
                }
            }
        });
        executor.shutdown();
        System.out.println("shutdown waiting......");

        try {
            // shutdown() 호출 전에 등록된 Task 중에 아직 완료되지 않은 Task가 있을 수 있습니다.
            // Timeout을 60초 설정하고 완료되기를 기다립니다.
            // 60초 전에 완료되면 true를 리턴하며, 60초가 지나도 완료되지 않으면 false를 리턴합니다.
            if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println(LocalTime.now() + " All jobs are terminated");
            } else {
                System.out.println(LocalTime.now() + " some jobs are not terminated, shutdown now");

                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // shutdown 을 호출한 스레드가 interrupt 상태이면 InterruptedException 를 던진다.
            System.out.println("== catch InterruptedException ==");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("end");
    }

    @Test
    @DisplayName("무한 루프 시 shutdown 예시, 무한 루프 while 문을 실행하는 작업(Runnable)은 인터럽트로 종료시킬 수 없다")
    void testLoop() {
        // shutdown은 논블로킹임, 때론 shutdown 호출 후 블로킹이 필요할 수 있다.
        // shutdown 메서드를 우선 호출하고 주어진 타임아웃 내까지 블로킹을 건다.
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 인터럽트 상태가 설정되어도 아무런 대응을 하지 않기 때문에 무한 루프 작업이 계속됨
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("== Job1 " + threadName + " start ==");

            int i = 0;
            while (true) {
                System.out.println("task " + i++);
            }
        });
        executor.shutdown(); // 모든 작업이 끝나면 스레드풀을 종료한다.
        System.out.println("shutdown waiting......");

        try {
            // shutdown() 호출 전에 등록된 Task 중에 아직 완료되지 않은 Task가 있을 수 있습니다.
            // Timeout을 10초 설정하고 완료되기를 기다립니다.
            // 10초 전에 완료되면 true를 리턴하며, 10초가 지나도 완료되지 않으면 false를 리턴합니다.
            if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.out.println(LocalTime.now() + " All jobs are terminated");
            } else {
                System.out.println(LocalTime.now() + " some jobs are not terminated, shutdown now");

                // 모든 Task를 강제 종료합니다.
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // shutdown 을 호출한 스레드가 interrupt 상태이면 InterruptedException 를 던진다.
            System.out.println("catch InterruptedException");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("end");
    }

    /**
     * execute() 메서드와 submit() 메서드의 차이
     *
     * execute() 메서드는 Executor 인터페이스에 정의 되어있습니다.
     * submit() 메서드는 ExecutorService 인터페이스에 정의되어있고 Executor 인터페이스를 상속받았습니다.
     *
     * execute() 메서드는 Runnable 인터페이스만 인자로 받을 수 있습니다.
     * submit() 메서드는 Runnable 인터페이스와 Callable 인터페이스 모두 인자로 받을 수 있습니다.
     *
     * execute() 메서드는 반환타입이 void입니다. 즉, 반환 값이 없습니다.
     * submit() 메서드는 Future 객체를 반환합니다.
     *
     * execute() 메서드는결과에 대해 신경 쓰지 않고 스레드 풀의 작업자 스레드(Worker Thread)에 의해 코드가 병렬로 실행되기를 원할 때 사용됩니다.
     * submit() 메서드는 작업의 결과에 대해 관심이 있을 때 사용할 수 있습니다.
     */
    @Test
    @DisplayName("submit 메서드로 ExecutorService에 작업을 추가할 수 있다.")
    void testSubmit() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // submit() 메서드로 생성한 스레드풀을 이용해 멀티 스레드로 처리할 작업을 예약한다.
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job1 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job2 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job3 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job4 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job5 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job6 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job7 " + threadName);
        });

        // 작업이 모두 완료되면 쓰레드풀을 종료시킵니다.
        executor.shutdown();
    }

    @Test
    @DisplayName("executor.submit()은 Future 객체를 리턴한다.")
    void testSubmitReturnFuture() {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Assertions.assertInstanceOf(Future.class, executor.submit(() -> {}));

        executor.shutdown();
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

        Future<String> res = executor.submit(callable);

        try {
            String result = res.get();
            System.out.println(result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();
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
    @DisplayName("작업이 끝나면 바로 BlockingQueue 에 결과를 추가할 수 있다.")
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

    @Test
    @DisplayName("FutureTask 는 Future 자체를 Object로 만들어준다.")
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

        executor.shutdown();
    }

    @Test
    @DisplayName("singleThreadExecutor는 싱글 스레드이기 때문에 주어진 작업을 차례대로 수행한다.")
    void testSingleThreadExecutor() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job1 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job2 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job3 " + threadName);
        });
        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Job4 " + threadName);
        });

        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS); // 블로킹 걸기 위함
        System.out.println("end");
    }

    @Test
    @DisplayName("ScheduledExecutorService는 ExecutorService를 상속 받은 인터페이스로 특정 시간 이후에 작업을 실행할 수 있다.")
    void testScheduledExecutorServiceSchedule() throws InterruptedException {
        System.out.println("start");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.schedule(() -> System.out.println("After 1 seconds..."), 1, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(() -> System.out.println("After 2 seconds..."), 2, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(() -> System.out.println("After 3 seconds..."), 3, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(() -> System.out.println("After 5 seconds..."), 5, TimeUnit.SECONDS);

        Thread.sleep(8000);
        scheduledExecutorService.shutdown();
    }

    @Test
    @DisplayName("ScheduledExecutorService는 ExecutorService를 상속 받은 인터페이스로 주기적으로 작업을 실행할 수 있다.")
    void testScheduledExecutorServiceFixtedRate() throws InterruptedException {
        System.out.println("start");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println("Period is 2 seconds... (scheduleAtFixedRate)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 3, 2, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            System.out.println("Period is 2 seconds... (scheduleWithFixedDelay)");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 3, 2, TimeUnit.SECONDS);

        Thread.sleep(10000);
        scheduledExecutorService.shutdown();
    }
}
