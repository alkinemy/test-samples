package reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class Test1 {

	@Test
	public void test() throws InterruptedException {

		Scheduler scheduler = Schedulers.newElastic("test");

		long start = System.currentTimeMillis();
		List<Integer> integers = Flux.just(1, 2, 3, 4, 5, 6, 7)
			.flatMap(i -> Mono.fromCallable(() -> getResult(i))
				.doOnError(e -> System.out.println("Fail1: " + e.getMessage() + ", " + e.getClass()))
				.onErrorResume(e -> Mono.empty())
				.timeout(Duration.ofSeconds(5), Mono.empty())
				.subscribeOn(scheduler)
			)
			.doOnNext(r -> System.out.println(Thread.currentThread().getName() + ": " + r.getI()))
			.map(Result::square)
			.filter(s -> s % 2 == 1)
			.sort(Comparator.comparingInt(i -> i))
			.collectList()
			.block();

		long end = System.currentTimeMillis();

		System.out.println("time: " + (end - start) + ", result: " + integers);
//		Thread.sleep(Integer.MAX_VALUE);

	}

	private Result getResult(int i) {
		if (i == 1) {
			try {
				Thread.sleep(3000);
				return new Result(i);
			} catch (Exception e) {
				throw new IllegalStateException("1", e);
			}
		}
		if (i == 3) {
			throw new TestException("3");
		}
		if (i == 4) {
			try {
				Thread.sleep(10000);
				System.out.println("test: " + i);
				return new Result(i);
			} catch (Exception e) {
				throw new IllegalStateException("4", e);
			}
		}
		return new Result(i);
	}

}

class TestException extends RuntimeException {

	public TestException(String message) {
		super(message);
	}

	public TestException(String message, Throwable cause) {
		super(message, cause);
	}
}

class Result {
	private int i;

	public Result(int i) {
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public int square() {
		return i * i;
	}

}
