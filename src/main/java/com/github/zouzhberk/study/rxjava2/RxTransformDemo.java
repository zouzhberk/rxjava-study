package com.github.zouzhberk.study.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.functions.Functions;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zouzhberk on 12/4/16.
 */
public class RxTransformDemo
{


    @Test
    public void testScan()
    {
//        Flowable<Integer> f1 = Flowable.range(1, 10).cache();
//        f1.reduce((x, y) -> x + y).subscribe(System.out::println);
//        f1.takeLast(2).reduce((x, y) -> x + y).blockingGet();

//        Flowable.generate((x) -> x.onNext(1)).ofType(Integer.class).take(10).subscribe(System
//                .out::println);
        class Fib
        {
            long a;
            long b;

            public Fib(long a, long b)
            {
                this.a = a;
                this.b = b;
            }

            public long fib()
            {
                return a + b;
            }
        }

        //斐波那契数列
        Flowable.create(new FlowableOnSubscribe<Fib>()
        {
            @Override
            public void subscribe(FlowableEmitter<Fib> e) throws Exception
            {
                Fib start = new Fib(1L, 1L);

                while (!e.isCancelled()) {
                    e.onNext(start);
                    start = new Fib(start.b, start.fib());
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).map(x -> x.fib()).take(10).subscribe(System.out::println);

        Flowable.generate(() -> new Fib(1L, 1L), (x, y) -> {
            Fib fib = new Fib(x.b, x.fib());
            y.onNext(fib);
            return fib;
        }).ofType(Fib.class).map(x -> x.fib()).take(10).subscribe(System.out::println);

        Path filePath = Paths.get("build.gradle");

        Flowable.generate(() -> {
            return Files.newBufferedReader(filePath);
        }, (reader, e) -> {
            System.out.println("READ LINE");
            e.onNext(reader.readLine());
//            if (new Random().nextInt(5) > 2)
//                throw new IllegalStateException("READ ERROR");
        }, (reader) -> {
            reader.close();
            System.out.println("READ CLOSE");
        }).skip(12).take(3)
                .subscribe(System.out::println);

//        Flowable.generate(() -> new Integer(1), (x, y) -> {
//
//            if (x == 1) {
//                y.onNext(2);
//                return 2;
//            }
//            y.onNext(x + 1);
//            return x + 1;
//        }).ofType(Integer.class).scan((x, y) -> x + y).take(20).subscribe(System.out::println);
    }

    @Test
    public void testBuffer()
    {
        Flowable<String> f1 = Flowable.intervalRange(1, 10, 1, 1, TimeUnit.SECONDS).delay((t) ->
                Flowable.timer(t % 3 + new Random().nextLong() % 3, TimeUnit.SECONDS))
                .map(index -> index % 3 + "-f1-" + index).cache();
//        f1.buffer(5, TimeUnit.SECONDS).map(x -> "buffer-" + x).toList().subscribe(System
//                .out::println);

        f1.subscribe(System.out::println);
        f1.map(x -> "cache-" + x).subscribe(System.out::println);

        f1.window(5, TimeUnit.SECONDS).map(x -> x.toList())
                .subscribe(x -> x.subscribe(System.out::println));

        Disposable b = f1.groupBy((x) -> x.split("-", 2)[0])
                .subscribe(x -> x.toList().subscribe(System.out::println));

        Map<String, List<String>> map = f1.toList().blockingGet().stream()
                .collect(Collectors.groupingBy((x) -> x.split
                        ("-", 2)[0]));
        System.out.println(map);
        while (!b.isDisposed()) {
        }

        try {
            Flux.fromIterable(Files.newDirectoryStream(Paths.get(".")));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
