package com.github.zouzhberk.study.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by zouzhberk on 12/4/16.
 */
public class RxTransformDemo {


    @Test
    public void testBuffer() {

        Flowable<String> f1 = Flowable.intervalRange(1, 10, 1, 1, TimeUnit.SECONDS).map(index -> "f1-" + index);

        Disposable a = f1.buffer(3).map(x -> "buffer-" + x).subscribe(System.out::println);

        //sf1.groupBy((x) -> x).map(x -> x.to);
        while (!a.isDisposed()) {
        }

    }
}
