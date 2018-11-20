package reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class Test1 {

	@Test
	public void test() {

		long start = System.currentTimeMillis();
		List<Integer> integers = Flux.just(1, 2, 3, 4, 5, 6, 7)
			.flatMap(i -> Mono.fromCallable(() -> getResult(i))
				.timeout(Duration.ofSeconds(5))
				.onErrorResume(e -> Mono.just(new Result(0)))
				.doOnNext(r -> System.out.println(Thread.currentThread().getName() + ": " + r.getI()))
				.subscribeOn(Schedulers.elastic()))
			.map(Result::square)
			.take(Duration.ofSeconds(5))
			.sort(Comparator.comparingInt(i -> i))
			.filter(s -> s % 2 == 1)
			.collectList()
			.block();

		long end = System.currentTimeMillis();

		System.out.println("time: " + (end - start) + ", result: " + integers);

	}

	private Result getResult(int i) {
		if (i == 1) {
			try {
				Thread.sleep(3000);
				return new Result(i);
			} catch (Exception e) {
				throw new IllegalStateException("1");
			}
		}
		if (i == 4) {
			try {
				Thread.sleep(10000);
				System.out.println("test: " + i);
				return new Result(i);
			} catch (Exception e) {
				throw new IllegalStateException("4");
			}
		}
		return new Result(i);
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
