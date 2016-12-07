package com.github.zouzhberk.study.rxjava2;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by clouder on 12/6/16.
 */
public class RxAsynDemo
{

    @Test
    public void testAsynchronized() throws IOException
    {


//        Path filePath = Paths.get("build.gradle");
//        try {
//            Files.list(filePath);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
        Path dir = Paths.get(".").toRealPath();

        Subscriber<? super Path> subscriber = new Subscriber<Path>()
        {
            @Override
            public void onSubscribe(Subscription s)
            {
                s.request(100);
            }

            @Override
            public void onNext(Path path)
            {
                System.out.println(path);
            }

            @Override
            public void onError(Throwable t)
            {
                t.printStackTrace();
            }

            @Override
            public void onComplete()
            {
                System.out.println("-----------Completed!-------------");
            }
        };
        Flowable.create(new FlowableOnSubscribe<Path>()
        {
            @Override
            public void subscribe(FlowableEmitter<Path> e) throws Exception
            {
                try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {

                    Iterator<Path> iter = dirStream.iterator();

                    while (iter.hasNext() && !e.isCancelled()) {
                        e.onNext(iter.next());
                    }
                    e.onComplete();
                    dirStream.close();
                }
//                finally {
//                    System.out.println("------begin close------");
//                    dirStream.close();
//                }
            }
        }, BackpressureStrategy.BUFFER).observeOn(Schedulers.newThread()).subscribe(subscriber);

        try {
            TimeUnit.SECONDS.sleep(20);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
