import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;

/**
 * ForkJoinPool은 마치 분할 정복 알고리즘과 같이 task를 쪼개어 작업하고(분할) 그 결과를 다시 합쳐가며(정복) 효율적으로 작업
 */
public class ForkJoinPoolExampleTest {

    @Test
    @DisplayName("공용 스레드풀을 사용하는 ForkJoinPool을 만들거나 커스텀 ForkJoinPool을 만들 수 있다.")
    void testCommonPool() {
        // 커스텀 ForkJoinPool은 꼭 사용 후 리소스 반환을 해주어야 함
        ForkJoinPool forkJoinPool2 = new ForkJoinPool(10);
        forkJoinPool2.shutdown();

        // commonPool은 여러곳에서 사용하는 공용풀을 가져와서 사용하기 때문에 어쩌면 효율적, 잘못하면 의도치 않게 동작 가능
        // (한쪽에서 스레드를 많이 잡아먹으면 다른쪽에서는 가용가능한 스레드가 적기 때문에)
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    }

    @Test
    void testForkJoinPool() {
        int[] array = {1,2,5,6,7,4,2,6,7,2,3,8,1,100,500};
        long result = Sum.sumArray(array);

        System.out.println("Fork Join result ="+result);
    }
}
