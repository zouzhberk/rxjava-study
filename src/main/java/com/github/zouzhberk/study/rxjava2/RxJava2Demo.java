package com.github.zouzhberk.study.rxjava2;

import io.reactivex.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.flowable.FlowableInternalHelper;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by clouder on 11/29/16.
 */
public class RxJava2Demo {

    public static void main(String[] args) {

        Subscriber<String> s = new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                System.out.println("xxxxxxx" + s);
               s.request(2);


            //    s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {

                System.out.printf("completed!");
            }
        };
        Flowable.fromArray("xxx", "123", "213dsf", "cvdf").subscribeOn(Schedulers.newThread()).subscribe(s);

        Maybe.fromCallable(() -> "OK!").subscribeOn(Schedulers.newThread()).subscribe();
//        SingleObserver<? super String> sub;
//        Single.fromCallable(() -> "OK!1").subscribe(sub);
        Completable.fromCallable(() -> "Completable OK").subscribe(() -> System.out.println("s"));
        Observer<? super String> s1 = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println(d.isDisposed());

               // d.dispose();
            }

            @Override
            public void onNext(String value) {
                System.out.println(value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        Observable.fromArray("adssad").subscribe(s1);
    }
}
