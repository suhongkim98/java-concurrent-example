import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentExampleTest {

    @Test
    @DisplayName("100개 뿐인 바나나는 사람 당 1개씩 할당받아 100명만이 먹을 수 있다.")
    void testConcurrentBanana() throws InterruptedException {
        // given
        AtomicInteger banana = new AtomicInteger(100); // 바나나 개수

        int people = 3000; // 바나나를 먹고자하는 경쟁자들
        CountDownLatch doneSignal = new CountDownLatch(people); // 사람 수만큼 작업 완료 시그널을 기다리기 위함
        ExecutorService executorService = Executors.newFixedThreadPool(people); // 사람 수 만큼 스레드 생성

        // 성공, 실패 카운트
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // when
        for (int i = 0 ; i < people ; i++) {
            executorService.execute(() -> { // 사람들이 바나나를 집어가기 시작했습니다.
                try {
                    Thread.sleep((int)(Math.random() * 300)); // 사람마다 바나나를 집는 속도는 다르다.
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (banana.decrementAndGet() >= 0) { // 바나나 빼기 1 했을 때 0보다 같거나 크다면 성공
                    success.getAndIncrement(); // 성공++
                } else {
                    fail.getAndIncrement(); // 실패--
                }

                doneSignal.countDown(); // 작업 완료 시 카운트--
            });
        }
        // 작업 완료를 기다리는 중
        doneSignal.await();
        executorService.shutdown();

        // then
        Assertions.assertEquals(100, success.get()); // 100명 만이 바나나 먹기 성공
        Assertions.assertEquals(2900, fail.get()); // 나머지는 다 실패
    }
}
