package com.github.zouzhberk.study.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.internal.operators.flowable.FlowableFromPublisher;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by clouder on 12/2/16.
 */
public class RxJavaObservableDemo
{

    public void testFlowable()
    {
        Observable.fromPublisher(null);
        Flowable.fromPublisher(null);

        Maybe.fromSingle(null);
        Single.fromPublisher(null);
        Completable.fromPublisher(null);

    }

    @Test
    public void testFromFuture()
    {
        ExecutorService executor = Executors
                .newFixedThreadPool(2);
        System.out.println("MAIN: " + Thread.currentThread().getId());
        Callable<String> callable = () ->
        {
            Path filePath = Paths.get("build.gradle");

            System.out.println("callable: " + Thread.currentThread().getId());
            long count = Files.readAllLines(filePath).stream()
                    .flatMap(s -> Arrays.stream(s.split
                            (""))).count();
            return count + "";
        };

        Future<String> future = executor.submit(callable);

//        try
//        {
//            System.out.println(future.get());
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//        catch (ExecutionException e)
//        {
//            e.printStackTrace();
//        }


        Flowable<String> flowable = Flowable.fromFuture(future).publish();

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
        try
        {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void testProduct()
    {
        System.out.println(Thread.currentThread().getId());
        Disposable a = Flowable
                .just("asdfasd", "1221ad").subscribeOn(Schedulers
                        .newThread())
                .subscribe(v ->
                {
                    System.out.println(Thread.currentThread().getId());
                });
        a.dispose();
        while (!a.isDisposed())
        {

        }


//        Flowable.range()
        //Flowable.fromArray(null);
        //Flowable.fromIterable(null);
    }


    public static void main(String[] args)
    {

    }
}
