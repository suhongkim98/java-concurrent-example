import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 가상 스레드는 일회용 휴지같은 느낌, 실제로 쓸 땐 쓰고나서 자원 회수할 생각하지 말고 버리기 GC에게 맡기자
public class VirtualThreadTest {

    @Test
    @DisplayName("가상스레드 간단 시작하기!")
    void testVirtualThread() throws InterruptedException {
        Thread thread = Thread.ofVirtual()
                .name("test-thread-1")
                .start(() -> System.out.println("Hello virtual thread"));

        thread.join();
        System.out.println(thread.getName() + " terminated");
    }

    @Test
    @DisplayName("Thread.ofVirtual() 메서드를 호출하여 가상 스레드 생성을 위한 Thread.Builder 인스턴스를 생성할 수 있다")
    void testCreatedBuilderInstance() {
        Assertions.assertInstanceOf(Thread.Builder.class, Thread.ofVirtual());
    }

    @Test
    @DisplayName("start 파라미터로 runnable 을 넣을 수 있다.")
    void test() throws InterruptedException {
        Runnable runnable = () -> { // void형 리턴
            System.out.println("hi vt");
        };

        Thread thread1 = Thread.ofVirtual()
                .name("test-thread-1")
                .start(runnable);

        Thread thread2 = Thread.ofVirtual()
                .name("test-thread-2")
                .start(runnable);

        thread1.join();
        System.out.println(thread1.getName() + " terminated");

        thread2.join();
        System.out.println(thread2.getName() + " terminated");
    }
}
