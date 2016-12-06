package com.github.zouzhberk.study.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by clouder on 12/5/16.
 */
public class RxFilterDemo
{

    @Test
    public void testTake()
    {
        Flowable<String> f1 = Flowable
                .fromArray("blue", "red", "green", "yellow11", "orange", "cyan", "purple"
                );

        f1.elementAt(4, "hello").subscribe(System.out::println);
        //out: orange
        f1.takeUntil(x -> x.length() > 5).map(x -> "takeUntil-" + x).toList()
                .subscribe(System.out::println);
        //out: [takeUntil-blue, takeUntil-red, takeUntil-green, takeUntil-yellow11]
        f1.takeWhile(x -> x.length() <= 5).map(x -> "takeWhile-" + x).toList()
                .subscribe(System.out::println);
        //out: [takeWhile-blue, takeWhile-red, takeWhile-green]

        f1.skipWhile(x -> x.length() <= 5).map(x -> "skipWhile-" + x).toList()
                .subscribe(System.out::println);
        //[skipWhile-yellow11, skipWhile-orange, skipWhile-cyan, skipWhile-purple]

        Disposable d = f1.delay(v -> Flowable.timer(v.length(), TimeUnit.SECONDS))
                .skipUntil(Flowable.timer(5, TimeUnit.SECONDS)).map(x -> "skipUntil-" + x)
                .subscribe(System.out::println);
//        skipUntil-green
//        skipUntil-orange
//        skipUntil-purple
//        skipUntil-yellow11

        while (!d.isDisposed()) {
        }
    }

    @Test
    public void testDebounce()
    {
        final long[] start = {System.currentTimeMillis()};
        Flowable<Long> f1 = Flowable.interval(200, TimeUnit.MILLISECONDS, Schedulers.single())
                .delay((t) ->
                        Flowable.timer(new Random()
                                .nextLong() % 200, TimeUnit.MILLISECONDS, Schedulers.single()))
                .doOnNext(x -> {
                    long count = (System.currentTimeMillis() - start[0]) / 100;
                    start[0] = System.currentTimeMillis();
                    Flowable.rangeLong(0, count).map(y -> "-")
                            .subscribe(System.out::print);
                    System.out.printf("%02d", x);
                });

        Disposable d = null;
        d = f1.debounce(250, TimeUnit.MILLISECONDS, Schedulers.single())
                .map(x -> " debounce[" + Thread.currentThread() + "]-"
                        + x)
                .take(10)
                .subscribe(System.out::println);

        d = f1.throttleFirst(500, TimeUnit.MILLISECONDS, Schedulers.single())
                .map(x -> " throttleLast[" + Thread.currentThread() + "]-"
                        + x)
                .take(10)
                .subscribe(System.out::println);
//        f1.sample(500, TimeUnit.MILLISECONDS).take(10).map(x -> " sample-" + x).
//                subscribe(System.out::println);
        while (!d.isDisposed()) {

        }
    }


}
