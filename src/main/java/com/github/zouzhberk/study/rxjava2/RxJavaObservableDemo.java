package com.github.zouzhberk.study.rxjava2;

import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by clouder on 12/2/16.
 */
public class RxJavaObservableDemo {

    public static void main(String[] args) {

    }

    @Test
    public void testFromAndJust() {
        List<String> list = Arrays.asList(
                "blue", "red", "green", "yellow", "orange", "cyan", "purple"
        );

        Flowable.fromIterable(list).skip(2).subscribe(System.out::println);
        System.out.println("------fromArray-----");
        Flowable.fromArray(list.toArray()).subscribe(System.out::println);
        System.out.println("------fromArray-----");


        Subscriber<? super String> s = new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("onSubscribe," + s);
                s.request(1);
                System.out.println("onSubscribe," + s);
            }

            @Override
            public void onNext(String s) {
                System.out.println("onNext," + s);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("onError," + t);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete!");
            }
        };
        //Flowable.just("blue").subscribe(s);
        Flowable.just("blue").subscribe(System.out::println);
        System.out.println("------fromArray END-----");
    }

    public void testFlowable() {
        Observable.fromPublisher(null);
        Flowable.fromPublisher(null);

        Maybe.fromSingle(null);
        Single.fromPublisher(null);
        Completable.fromPublisher(null);

    }

    @Test
    public void testZip() {
        Flowable<String> f1 = Flowable.intervalRange(1, 10, 1, 1, TimeUnit.SECONDS).map(index -> "f1-" + index);
        Flowable<String> f2 = Flowable.intervalRange(1, 3, 2, 2, TimeUnit.SECONDS).map(index -> "f2-" + index);

        Flowable.zip(f1, f2, (x, y) -> x + "-" + y).map(x -> "zip-" + x).subscribe(System.out::println);

        Flowable.combineLatest(f1, f2, (x, y) -> x + "-" + y).map(x -> "combineLatest-" + x).subscribe(System.out::println);


        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    //@org.testng.annotations.Test
    public void testFromPublisher() {
        List<String> list = Arrays.asList(
                "blue", "red", "green", "yellow", "orange", "cyan", "purple"
        );

        Flowable<String> f1 = Flowable.intervalRange(1, 5, 1, 1, TimeUnit.SECONDS).map(index -> "f1-" + index).doOnNext(System.out::println);
        Flowable<String> f2 = Flowable.intervalRange(1, 3, 2, 2, TimeUnit.SECONDS).map(index -> "f2-" + index).doOnNext(System.out::println);
//
//        Flowable.ambArray(f1, f2).map(x -> "amb: " + x).subscribe(System.out::println);
//        System.out.println("----------concat-----------");
//        Flowable.concat(f1, f2).map(x -> "concat: " + x).subscribe(System.out::println);
//
//        System.out.println("----------merge-----------");
//        Flowable.merge(f1, f2).map(x -> "merge: " + x).subscribe(System.out::println);
//
//        Flowable<String>[] flowables = new Flowable[]{f1, f2};
//        Flowable.switchOnNext(Flowable.intervalRange(0, 2, 0, 3, TimeUnit.SECONDS).map(i -> flowables[i.intValue()])).map(x -> "switchOnNext-" + x).subscribe(System.out::println);
//        Flowable.intervalRange(0, 2, 0, 3, TimeUnit.SECONDS).map(i -> flowables[i.intValue()]).switchMap((io.reactivex.functions.Function) Functions.identity()).map(x -> "switchMap-" + x).subscribe(System.out::println);

        ConnectableFlowable<String> cf1 = f1.startWith(Arrays.asList("hello", "world")).publish();
//        try {
//            TimeUnit.SECONDS.sleep(3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        System.out.printf("connect");
        cf1.connect(System.out::println);
        cf1.map(x -> "connectable-" + x).subscribe(System.out::println);
//
//        cf1.connect(System.out::println);
//        cf1.map(x -> "connectable1-" + x).subscribe(System.out::println);
////        f1.publish().connect();

        //f1.startWith(Arrays.asList("hello","world")).subscribe(System.out::println);;

        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testFromFuture() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        System.out.println("MAIN: " + Thread.currentThread().getId());
        Callable<String> callable = () ->
        {
            Path filePath = Paths.get("build.gradle");

            System.out.println("callable: " + Thread.currentThread().getId());
            Map<Character, Long> count = Files
                    .readAllLines(filePath).stream()
                    .flatMap(s -> Arrays.stream(s.split
                            (""))).flatMapToInt(x -> x.chars())
                    .mapToObj(x -> Character.valueOf((char) x))

                    .collect(Collectors.groupingBy(Function.identity(),
                            TreeMap::new,
                            Collectors.counting()));

            String str = count.entrySet().stream()
                    .sorted(Comparator.comparingLong(x
                            -> x
                            .getValue())
                    )
                    .map(x -> x
                            .getKey())
                    .map(x -> x.toString())
                    .collect
                            (Collectors
                                    .joining());
            return str;
        };

        Future<String> future = executor.submit(callable);

        Flowable<String> flowable = Flowable.fromFuture(future);

        System.out.println(flowable.subscribeOn(Schedulers.from(executor))
                .blockingFirst());
        flowable.subscribeOn(Schedulers.from(executor)).subscribe
                (value
                        ->
                {
                    System.out.println(value);
                    System.out.println("consumer: " + Thread.currentThread()
                            .getId());
                });

        System.out.println("END");
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFromFuture1() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        System.out.println("MAIN: " + Thread.currentThread().getId());
        Callable<String> callable = () -> {
            System.out.println("callable [" + Thread.currentThread().getId() + "]: ");
            Path filePath = Paths.get("build.gradle");
            return Files.readAllLines(filePath).stream().flatMap(s -> Arrays.stream(s.split
                    (""))).count() + "";
        };

        Future<String> future = executor.submit(callable);

        Consumer<String> onNext = v -> System.out
                .println("consumer[" + Thread.currentThread().getId() + "]:" + v);

        Flowable<String> flowable = Flowable.fromCallable(callable);
        flowable.observeOn(Schedulers.from(executor)).map(x ->
                "FromCallable1:" + x)
                .subscribe
                        (onNext);
        flowable.observeOn(Schedulers.from(executor)).map(x ->
                "FromCallable2:" + x)
                .subscribe
                        (onNext);
        //Flowable.fromFuture(future).subscribe(onNext);

        System.out.println("END");
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testProduct() {
        System.out.println(Thread.currentThread().getId());
        Disposable a = Flowable
                .just("asdfasd", "1221ad").subscribeOn(Schedulers
                        .newThread())
                .subscribe(v ->
                {
                    System.out.println(Thread.currentThread().getId());
                });
        a.dispose();
        while (!a.isDisposed()) {

        }


//        Flowable.range()
        //Flowable.fromArray(null);
        //Flowable.fromIterable(null);
    }
}
