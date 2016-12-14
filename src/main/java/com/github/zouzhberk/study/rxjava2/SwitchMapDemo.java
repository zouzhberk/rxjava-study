package com.github.zouzhberk.study.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by clouder on 12/14/16.
 */
public class SwitchMapDemo
{
    @Test
    public void testSwitchOnNext()
    {
        java.util.function.Function<String, Consumer<Object>> m = s -> v -> System.out
                .println("[" + System.currentTimeMillis() / 100 + "] " + s + "-" + v);


        Flowable<Long> f1 = Flowable.interval(0, 100, TimeUnit.MILLISECONDS)
                .delay(1200, TimeUnit.MILLISECONDS).doOnNext(m.apply("f1"));
        Flowable<Long> f2 = Flowable.interval(300, 200, TimeUnit.MILLISECONDS).doOnNext(m.apply
                ("f2"));
        Flowable<Long> f3 = Flowable.interval(200, 300, TimeUnit.MILLISECONDS).doOnNext(m.apply
                ("f3"));

        //f1.distinctUntilChanged()
        Flowable.defer(() ->f1);

        Flowable.range(1,3).delay(x -> Flowable.timer(x, TimeUnit.SECONDS)).map(x -> Flowable
                .fromArray(f1,f2,f3));
        Flowable<Long> f4 = Flowable.switchOnNext(Flowable.fromArray(f1, f3, f2));


        Disposable d = f4.subscribe(m.apply("out"));
        while (!d.isDisposed()) {
        }
    }
}
