package com.github.zouzhberk.study.rxjava2;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableTransformer;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by clouder on 12/6/16.
 */
public class RxAsynDemo
{


    @Test
    public void testScheduler()
    {
        java.util.function.Consumer<Object> pc = x -> System.out
                .println("Thread[" + Thread.currentThread().getName() + " ," + Thread
                        .currentThread().getId() + "] :" + x);
        Executor executor = Executors.newFixedThreadPool(2);
        Schedulers.from(executor).scheduleDirect(() -> pc.accept("executor one"));
        Schedulers.from(executor).scheduleDirect(() -> pc.accept("executor two"));
        Schedulers.trampoline().scheduleDirect(() -> pc.accept("trampoline"), 1, TimeUnit.SECONDS);
        Schedulers.single().scheduleDirect(() -> pc.accept("single one DONE"));
        Schedulers.single().scheduleDirect(() -> pc.accept("single two DONE"));
        Schedulers.computation()
                .scheduleDirect(() -> pc.accept("computation one DONE"), 1, TimeUnit.SECONDS);
        Schedulers.computation()
                .scheduleDirect(() -> pc.accept("computation two DONE"), 1, TimeUnit.SECONDS);
        Schedulers.io().scheduleDirect(() -> pc.accept("io one DONE"));
        Schedulers.io().scheduleDirect(() -> pc.accept("io two DONE"), 1, TimeUnit.SECONDS);
        Schedulers.io().scheduleDirect(() -> pc.accept("io tree DONE"), 1, TimeUnit.SECONDS);
        Schedulers.newThread().scheduleDirect(() -> pc.accept("newThread tree DONE"));
        System.out.println("Finished!");
        try {
            TimeUnit.SECONDS.sleep(5);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSubscribeOn()
    {
        Consumer<Object> consumer = x -> System.out
                .println("Thread[" + Thread.currentThread().getName() + " ," + Thread
                        .currentThread().getId() + "] :" + x);


        Flowable<Path> f1 = Flowable.create((FlowableEmitter<Path> e) -> {
            Path dir = Paths.get("/home/clouder/berk/workspaces/cattle").toRealPath();
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
                Iterator<Path> iter = dirStream.iterator();
                while (iter.hasNext() && !e.isCancelled()) {
                    consumer.accept("f1");
                    e.onNext(iter.next());
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER);

        f1.subscribeOn(Schedulers.newThread()).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation()).take(5).doOnNext(consumer).observeOn(Schedulers
                .single()).subscribe(consumer);

        try {
            TimeUnit.SECONDS.sleep(5);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
//        f1.subscribeOn(Schedulers.io()).filter(Files::isHidden).flatMap(
//
//        );
    }


    @Test
    public void testAsynchronized() throws IOException
    {
        Consumer<Object> threadConsumer = x -> System.out
                .println("Thread[" + Thread.currentThread().getName() + " ," + Thread
                        .currentThread().getId() + "] :" + x);


        Path dir = Paths.get("/home/clouder/berk/workspaces/cattle").toRealPath();

        Flowable<Path> f1 = Flowable.create((FlowableEmitter<Path> e) -> {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
                Iterator<Path> iter = dirStream.iterator();
                while (iter.hasNext() && !e.isCancelled()) {
                    threadConsumer.accept("f1");

                    e.onNext(iter.next());
                }
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).filter(x -> Files.isRegularFile(x)).take(3);
        //f1.subscribe(threadConsumer);


        Executor executor = Executors.newFixedThreadPool(5);
        f1.doOnNext(threadConsumer).observeOn(Schedulers.from(executor))
                .flatMap(x -> Flowable.fromCallable(() -> Files.readAllBytes(x).length)
                )
                .doOnNext(threadConsumer).map(x -> "subscribe-" + x)
                .subscribeOn(Schedulers.from(executor))
                .subscribe(threadConsumer);
//        f1.map(x -> "observeOn" + x).observeOn(Schedulers.newThread()).subscribe(threadConsumer);
//
//        f1.subscribeOn(Schedulers.newThread()).subscribe(threadConsumer);
        try {
            TimeUnit.SECONDS.sleep(20);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
