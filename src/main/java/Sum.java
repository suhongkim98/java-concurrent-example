import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * ForkJoinPool 예제
 */
public class Sum extends RecursiveTask<Long> {

    private static final int SEQUENTIAL_THRESHOLD = 5;
    private final int low;
    private final int high;
    private final int[] array; // 전체 배열의 reference

    public Sum(int[] arr, int lo, int hi) { // 현객체로 처리할 배열과 시작 인덱스, 끝인덱스 세팅)
        array = arr;
        low = lo;
        high = hi;
    }

    @Override
    protected Long compute() { // compute는 추상 메소드이기에 반드시 구현해줘야 한다. 리턴값은 RecursiveTask<Long> 의 Long과 일치시킨다.
        if (high - low <= SEQUENTIAL_THRESHOLD) { // 충분히 작은 배열 구간이면 값을 계산해 리턴한다.
            long sum = 0;
            for (int i = low; i < high; ++i)
                sum += array[i];
            return sum;
        } else {
            // 배열이 기준보다 크다면 divede and conquer방식으로 적당히 둘이상의 객체로 나누고,
            // 현재 쓰레드에서 처리할 객체는 compute를 호출해 값을 계산하고, fork할 객체는 join하여 값을 기다린후 얻는다.
            // 새로운 업무 단위로 나누고자 할때는 fork, fork된 업무에서 결과를 취합하고자 할때는 join을 사용
            int mid = low + (high - low) / 2;
            Sum left = new Sum(array, low, mid);
            Sum right = new Sum(array, mid, high);
            left.fork();
            long rightAns = right.compute();
            long leftAns = left.join();

            // 값을 모두 얻은후에는 다 합해서 리턴한다.
            return leftAns + rightAns;
        }
    }

    public static long sumArray(int[] array) {
        // ForkJoinPool의 시작전과 후의 thread pool size를 비교
        int beforeSize = ForkJoinPool.commonPool().getPoolSize();
        System.out.println("ForkJoin commonPool beforeSize=" + beforeSize);
        long result = ForkJoinPool.commonPool().invoke(new Sum(array, 0, array.length));
        int afterSize = ForkJoinPool.commonPool().getPoolSize();
        System.out.println("ForkJoin commonPool afterSize=" + afterSize);
        return result;
    }
}

