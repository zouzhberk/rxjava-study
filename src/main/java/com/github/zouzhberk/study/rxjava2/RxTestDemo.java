package com.github.zouzhberk.study.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zouzhberk on 12/11/16.
 */
public class RxTestDemo {

    @Test
    public void testRxTest() throws InterruptedException {
        List<String> list = Arrays.asList(
                "orange", "blue", "red", "green", "yellow", "cyan", "purple"
        );

//        Flowable.fromIterable(list).subscribeOn(Schedulers.newThread()).sorted().test().assertValues(list.stream().sorted().toArray(String[]::new));
//        Flowable.fromIterable(list).count().test().assertValue(Integer.valueOf(list.size()).longValue());
        List<String> out1 = Flowable.fromIterable(list).sorted().test().values();
        System.out.println(out1);

        TimeUnit.SECONDS.sleep(1);
    }


    @Test
    public void rxTestWithTime() {
        int count = Flowable.interval(1, TimeUnit.SECONDS).take(2).test().valueCount();
        System.out.println(count);

        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //System.out.println(a.isDisposed());
    }

}
