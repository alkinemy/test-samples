package sample.kakao;

import org.junit.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainTest {

	@Test
	public void test() {
		DirectProcessor<String> hotSource = DirectProcessor.create();

		Flux<String> hotFlux = hotSource.map(String::toUpperCase);


//		hotSource.onNext("test");

		hotFlux.subscribe(d -> System.out.println("Subscriber 1 to Hot Source: "+d));

		hotSource.onNext("blue");
		hotSource.onNext("green");

		hotFlux.subscribe(d -> System.out.println("Subscriber 2 to Hot Source: "+d));

		hotSource.onNext("orange");
		hotSource.onNext("purple");
		hotSource.onComplete();
	}

	@Test
	public void test2() throws InterruptedException {
		Scheduler s = Schedulers.newParallel("parallel-scheduler", 4);

		Flux<String> flux = Flux
			.range(1, 10)
			.map(i -> 10 + i)
			.doOnNext(i -> System.out.println(i + " | INITIAL: " + Thread.currentThread().getName()))
//			.flatMap(i -> Mono.fromCallable(() -> i + " | ZERO: " + Thread.currentThread().getName()).subscribeOn(s))
			.map(i -> i + " | FIRST: " + Thread.currentThread().getName())
			.map(i -> i + " | SECOND: " + Thread.currentThread().getName())
//			.publishOn(s)
//			.subscribeOn(s)
			.map(i -> i + " | THIRD: " + Thread.currentThread().getName())
			.map(i -> i + " | FOURTH: " + Thread.currentThread().getName())
			;

		new Thread(() -> flux.subscribe(i -> System.out.println(i + " | FIFTH: " + Thread.currentThread().getName()))).start();

		new CountDownLatch(1).await(1, TimeUnit.SECONDS);
	}

}