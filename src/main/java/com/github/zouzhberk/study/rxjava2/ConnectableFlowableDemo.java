package com.github.zouzhberk.study.rxjava2;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by clouder on 12/9/16.
 */
public class ConnectableFlowableDemo {

    @Test
    public void testCache() throws InterruptedException {
        Consumer<Object> consumer = v -> System.out.println("[" + System.currentTimeMillis() / 1000 + "] " + v);
        Flowable<Long> f1 = Flowable.interval(1, TimeUnit.SECONDS).cache();

        f1.map(x -> "x1:" + x).subscribe(consumer);
        TimeUnit.SECONDS.sleep(6);
        f1.map(x -> "x2:" + x).subscribe(consumer);
        TimeUnit.SECONDS.sleep(20);

    }

    @Test
    public void testReplay1() throws Exception {
        Consumer<Object> consumer = v -> System.out.println("[" + System.currentTimeMillis() / 1000 + "] " + v);
        consumer.accept("start");
        ConnectableFlowable<Long> f1 = Flowable.intervalRange(1, 100, 0, 1, TimeUnit.SECONDS).doOnNext(consumer).onBackpressureLatest().replay(4, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(5);
        f1.connect();
        TimeUnit.SECONDS.sleep(5);
        f1.observeOn(Schedulers.single()).map(x -> "o1-" + x).subscribe(consumer);

        TimeUnit.SECONDS.sleep(5);
        f1.map(x -> "o2-" + x).subscribe(consumer);
        TimeUnit.SECONDS.sleep(20);

    }

    public static void main(String[] args) throws InterruptedException {

        Consumer<Object> consumer = v -> System.out.println("[" + System.currentTimeMillis() / 1000 + "] " + v);
        ConnectableFlowable<String> f1 = Flowable.generate(() -> new BufferedReader(new
                        InputStreamReader(System.in))
                , (reader, e) -> {
                    while (true) {
                        String line = reader.readLine();
                        if (line == null || line.equalsIgnoreCase("exit")) {
                            break;
                        }
                        e.onNext(line);

                    }
                    e.onComplete();
                }).ofType(String.class).subscribeOn(Schedulers.io()).doOnNext(consumer).publish();//.replay(3);

        //TimeUnit.SECONDS.sleep(5);
        f1.connect(consumer);


        //f1.replay()

        TimeUnit.SECONDS.sleep(5);
        f1.map(x -> "s0- " + x).subscribe(consumer);
        TimeUnit.SECONDS.sleep(5);
        f1.map(x -> "s1- " + x).subscribe(consumer);
        TimeUnit.SECONDS.sleep(50);

    }

    @Test
    public void testConnectableFlowable() throws InterruptedException {
        ConnectableFlowable<String> f1 = Flowable.generate(() -> new BufferedReader(new
                        InputStreamReader(System.in))
                , (reader, e) -> {
                    while (true) {
                        String line = reader.readLine();
                        if (line == null || line.equalsIgnoreCase("exit")) {
                            break;
                        }
                        e.onNext(line);
                    }
                    e.onComplete();
                }).ofType(String.class).subscribeOn(Schedulers.io()).publish();

        TimeUnit.SECONDS.sleep(5);
        f1.connect(System.out::println);


        //f1.replay()

        TimeUnit.SECONDS.sleep(5);
        f1.map(x -> "s0- " + x).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(5);
        f1.map(x -> "s1- " + x).subscribe(System.out::println);
        TimeUnit.SECONDS.sleep(50);


    }

    public static void main1(String[] args) throws InterruptedException {
        Consumer<Object> consumer = x -> System.out
                .println("Thread[" + Thread.currentThread().getName() + " ," + Thread
                        .currentThread().getId() + "] :" + x);

        ConnectableFlowable<String> f1 = from(System.in);
        TimeUnit.SECONDS.sleep(10);


        f1.connect(System.out::println);

        TimeUnit.SECONDS.sleep(10);
        f1.observeOn(Schedulers.newThread()).map(x -> "connenect- " + x).subscribe(consumer);

        TimeUnit.SECONDS.sleep(5);

        f1.map(x -> "connenect1- " + x).subscribe(consumer);
        TimeUnit.SECONDS.sleep(50);
    }

    public static ConnectableFlowable<String> from(InputStream inputStream) {
        return Flowable.generate(() -> {
            return new BufferedReader(new InputStreamReader(inputStream));
        }, (reader, e) -> {
            while (true) {
                String line = reader.readLine();

                if (line == null || line.equalsIgnoreCase("exit")) {
                    break;
                }
                System.out.println("reader: " + line);
                e.onNext(line);
            }
            e.onComplete();
        }).ofType(String.class).subscribeOn(Schedulers.io()).doOnNext(System.out::println)
                .publish();
    }

}
