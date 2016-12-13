package com.github.zouzhberk.study.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.FlowableOperator;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.Cancellation;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by clouder on 12/6/16.
 */
public class RxErrorHandlingDemo
{

    @Test
    public void testRetryWithTimeout()
    {
        Function<Object, Object> exceptionMap = x -> {
            if (new Random().nextInt(5) > 3) {
                throw new IOException(x + "");
            }
            if (new Random().nextInt(2) < 1) {
                throw new SQLException(x + "");
            }
            return x;
        };
        Flowable<Long> f1 = Flowable.interval(500, TimeUnit.MILLISECONDS);
        Disposable d = f1.map(x -> "retry- " + x).map(exceptionMap).doOnError
                (Throwable::printStackTrace).retry(10, e -> {
            TimeUnit.SECONDS.sleep(2);
            return e instanceof SQLException;
        })
                .retryWhen(attempts -> {
                    return attempts.zipWith(Flowable.range(1, 3),
                            (e, i) -> i)
                            .flatMap
                                    (i -> {
                                        System.out.println("delay retry by " + i + " second(s)");
                                        return Flowable.timer(i, TimeUnit.SECONDS);
                                    }).doOnNext(System.err::println);
                })
                .subscribe(System.out::println, (e) -> {
                    System.out.println("final error- " + e);
                }, () -> {
                    System.out.println("finish OK");
                });
        while (!d.isDisposed()) {
        }

    }


    @Test
    public void testRetry()
    {
        Function<Long, Long> exceptionMap = x -> {
            if (new Random().nextInt(5) > 3) {
                throw new IOException(x + "");
            }
//            if (new Random().nextInt(10) < 1) {
//                throw new SQLException(x + "");
//            }
            return x;
        };
        Flowable<Long> f1 = Flowable.interval(500, TimeUnit.MILLISECONDS);
        Disposable d = f1.map(exceptionMap).doOnError(Throwable::printStackTrace)
                .retry(3, e -> e instanceof IOException)
                .subscribe(System.out::println, Throwable::printStackTrace);
        while (!d.isDisposed()) {
        }

    }

    @Test
    public void testErrorReturn()
    {
        Function<Long, Long> exceptionMap = x -> {
            if (new Random().nextInt(5) > -1) {
                throw new IOException(x + "");
            }
            return x;
        };

        Flowable<Long> f1 = Flowable.interval(500, TimeUnit.MILLISECONDS).map(index -> {
            throw new IOException(index + "");
        }).map(index -> {
            throw new IllegalArgumentException(index + "");
        });
        f1.onErrorReturnItem(-1L).take(5)
                .subscribe(System.out::println, Throwable::printStackTrace);
        Disposable d = f1.onErrorResumeNext(e -> {
            if (e instanceof IOException) {
                return Flowable.error(new UncheckedIOException((IOException) e));
            }
            return Flowable.error(e);
        }).subscribe(System.out::println, Throwable::printStackTrace);


        while (!d.isDisposed()) {
        }
    }

    public static class ErrorResumeOperator<D, U> implements FlowableOperator<D, U>
    {
        private final Function<U, D> function;
        private final D defaultValue;

        public ErrorResumeOperator(Function<U, D> function, D defaultValue)
        {
            this.function = function;
            this.defaultValue = defaultValue;
        }

        @Override
        public Subscriber<? super U> apply(Subscriber<? super D> observer) throws Exception
        {
            Subscriber<U> subscriber = new Subscriber<U>()
            {
                @Override
                public void onSubscribe(Subscription s)
                {
                    observer.onSubscribe(s);
                }

                @Override
                public void onNext(U onNext)
                {
                    try {
                        observer.onNext(function.apply(onNext));
                    }
                    catch (Exception e) {
                        observer.onNext(defaultValue);
                    }
                }

                @Override
                public void onError(Throwable t)
                {
                    observer.onError(t);
                }

                @Override
                public void onComplete()
                {
                    observer.onComplete();
                }
            };
            return subscriber;
        }
    }

    @Test
    public void errorOnNext3()
    {
        //Flowable.error(new SQLException("asdfdf")).ex;
    }

    @Test
    public void errorOnNext2()
    {
        Function<Long, Long> exceptionMap = x -> {
            if (new Random().nextInt(5) > 2) {
                throw new IOException(x + "");
            }
            return x;
        };

        Flowable<Long> f1 = Flowable.interval(500, TimeUnit.MILLISECONDS).map(exceptionMap);


        Cancellation d = Flux.from(f1)
                .onErrorResumeWith(e -> Flowable.just(-1L))
                .doOnError(Throwable::printStackTrace).mapError(e -> e)
                .switchOnError(Flowable.just(-1L))
                .take(10)
                .subscribe(System.out::println, Throwable::printStackTrace);


        try {
            TimeUnit.SECONDS.sleep(10);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        //while (d.dispose();)

    }

    @Test
    public void testErrorLift()
    {
        Function<Long, Long> exceptionMap = x -> {
            if (new Random().nextInt(5) > 2) {
                throw new IOException(x + "");
            }
            return x;
        };
        Flowable<Long> f1 = Flowable.interval(500, TimeUnit.MILLISECONDS);
        Disposable d = f1.lift(new ErrorResumeOperator<>(exceptionMap, -1L)).take(5)
                .subscribe(System.out::println);

        while (!d.isDisposed()) {
        }
    }

    @Test
    public void errorOnNext()
    {

        Function<Long, Long> exceptionMap = x -> {
            if (new Random().nextInt(5) > 2) {
                throw new IOException(x + "");
            }
            return x;
        };

        Flowable<Long> f1 = Flowable.interval(500, TimeUnit.MILLISECONDS).flatMap(index ->
                Flowable.just(index).map(exceptionMap).onErrorReturnItem(-1L));
        f1.take(5).subscribe(System.out::println);

        FlowableOperator<? extends Long, ? super Long> lifter = new FlowableOperator<Long, Long>()
        {
            @Override
            public Subscriber<? super Long> apply(Subscriber<? super Long> observer) throws Exception
            {
                Subscriber<Long> subscriber = new Subscriber<Long>()
                {

                    @Override
                    public void onSubscribe(Subscription s)
                    {

                    }

                    @Override
                    public void onNext(Long aLong)
                    {

                    }

                    @Override
                    public void onError(Throwable t)
                    {

                    }

                    @Override
                    public void onComplete()
                    {

                    }
                };
                return subscriber;
            }
        };
        Flowable<Long> f2 = Flowable.interval(500, TimeUnit.MILLISECONDS).lift(lifter);

        Disposable d = null;
//        Disposable d = f1.onErrorReturnItem(-1L).filter((x) -> x >= 0).take(5).subscribe(System
//                .out::println);


        d = f1.onErrorResumeNext(e -> {
            Exceptions.propagate(e);
            return Flowable.just(-1L);
        }).filter((x) -> x >= 0L).map(x -> "onErrorResumeNext-" + x).take(10)
                .forEach(System.out::println);


        while (!d.isDisposed()) {

        }

    }
}
