package com.github.zouzhberk.study.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Producer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by clouder on 11/29/16.
 */

public class RxJava2Demo
{

    @Test
    public void testMaybe()
    {
        Maybe.just(1)
                .map(v -> v + 1)
                .filter(v -> v == 1)
                .defaultIfEmpty(2)
                .test()
                .assertResult(21);

    }

    @Test
    public void testCompletable()
    {
        Completable.create(new CompletableOnSubscribe()
        {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception
            {
                Path filePath = Paths.get("build.gradle");
                Files.readAllLines(filePath);
                e.onComplete();
            }
        }).subscribe(() -> System.out.println("OK!"),
                Throwable::printStackTrace);
    }

    @Test
    public void testObservableVsStream()
    {
        Path filePath = Paths.get("build.gradle");

        Observable.fromCallable(() -> Files.readAllLines(filePath));


    }

    @Test
    public void testNullValue()
    {
        Maybe.fromCallable(() -> null)
                .subscribe(System.out::println, Throwable::printStackTrace);
        Observable.just(null);
        Single.just(null);
        Flowable.just(null);
        Maybe.just(null);
        Observable.fromCallable(() -> null)
                .subscribe(System.out::println, Throwable::printStackTrace);
        Observable.just(1).map(v -> null)
                .subscribe(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void rxjavaAndReactor()
    {

        Path filePath = Paths.get("build.gradle");


        Flowable<String> flowable = Flowable
                .fromCallable(() -> Files.readAllLines(filePath))
                .flatMap(x -> Flowable.fromIterable(x));

        flowable.count().subscribe(System.out::println);
        Flux.from(flowable).count().subscribe(System.out::println);

        try
        {
            Flux<String> flux = Flux.fromIterable(Files.readAllLines(filePath));
            flux.count()
                    .subscribe(System.out::println);
            Flowable.fromPublisher(flux).count()
                    .subscribe(System.out::println);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {

        Subscriber<String> s = new Subscriber<String>()
        {
            @Override
            public void onSubscribe(Subscription s)
            {
                System.out.println("xxxxxxx" + s);
                s.request(2);


                //    s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s)
            {
                System.out.println(s);
            }

            @Override
            public void onError(Throwable t)
            {
                t.printStackTrace();
            }

            @Override
            public void onComplete()
            {

                System.out.printf("completed!");
            }
        };
        Flowable.fromArray("xxx", "123", "213dsf", "cvdf")
                .subscribeOn(Schedulers.newThread()).subscribe(s);

        Maybe.fromCallable(() -> "OK!").subscribeOn(Schedulers.newThread())
                .subscribe();
//        SingleObserver<? super String> sub;
//        Single.fromCallable(() -> "OK!1").subscribe(sub);
        Completable.fromCallable(() -> "Completable OK")
                .subscribe(() -> System.out.println("s"));
        Observer<? super String> s1 = new Observer<String>()
        {
            @Override
            public void onSubscribe(Disposable d)
            {
                System.out.println(d.isDisposed());

                // d.dispose();
            }

            @Override
            public void onNext(String value)
            {
                System.out.println(value);
            }

            @Override
            public void onError(Throwable e)
            {

            }

            @Override
            public void onComplete()
            {

            }
        };
        Observable.fromArray("adssad").subscribe(s1);
    }
}
