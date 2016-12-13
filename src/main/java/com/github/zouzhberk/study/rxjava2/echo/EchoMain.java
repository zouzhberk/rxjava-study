package com.github.zouzhberk.study.rxjava2.echo;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Console;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by zouzhberk on 12/12/16.
 */
public class EchoMain {
    public static void printLine(double p, int width) {
        System.out.printf("\r");
        Flowable.range(1, width).map(x -> Math.abs(x - p) < 0.5 ? "*" : "-")
                .blockingSubscribe(x -> System.out.printf("%s", x));
    }

    public static Flowable<String> getRowData(int width, double... p) {
        Set<Integer> set = Arrays.stream(p).boxed().mapToInt(x -> x.intValue()).distinct()
                .boxed().collect(Collectors.toSet());
        return Flowable.concatArray(Flowable.just("\r"), Flowable.range(1, width).map(x -> set
                .contains(x) ? "*" : "-"));
    }

    public static void renderRowData(int width, double... p) {
        Set<Integer> set = Arrays.stream(p).boxed().mapToInt(x -> x.intValue()).distinct()
                .boxed().collect(Collectors.toSet());
        // System.out.printf("\r");
        Flowable.range(1, width).map(x -> set.contains(x) ? "*" : "-").blockingSubscribe(x -> {
            System.out.printf("%s", x);
        });
        System.out.printf("\n");
    }

    public static Flowable<String> getEmptyRowData(int width) {
        return Flowable.range(1, width).map(x -> "-").startWith("\r");
    }

    public static void printPoints(java.util.List<Point> points, int width, int height) {

        Map<Integer, List<Point>> map = points.stream()
                .collect(Collectors.groupingBy(x -> Double.valueOf(x.getY()).intValue()));

        Flowable.range(1, height).map(x -> map.getOrDefault(x, Collections.emptyList())).subscribe(
                x -> {
                    //System.out.printf("\r");
                    renderRowData(width, x.stream().mapToDouble(p -> p.getY()).toArray());
                    Runtime.getRuntime().exec("clear");
                });

    }


    @Test
    public void printRect() {

        Disposable d = Single.fromCallable(() -> {
            Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
            double height = screen.getHeight() / pixelPerInch;
            double width = screen.getWidth() / pixelPerInch;
            double x = Math.pow(height, 2);
            double y = Math.pow(width, 2);
            return new Point2D.Double(screen.getHeight(), screen.getWidth());
        }).subscribeOn(Schedulers.computation()).subscribe(System.out::println);

        while (!d.isDisposed()) {

        }
        Console console = System.console();

        console.printf("%s", "asdfasdfas");

    }

    public static void printLine(int p, int width) {
        System.out.printf("\r");
        Flowable.range(1, width).map(x -> x == p ? "*" : "-")
                .blockingSubscribe(x -> System.out.printf("%s", x));
    }


    @Test
    public void testprintline() throws InterruptedException {
        System.out.printf("\rdasdfasdfads1232\n");
        System.out.printf("\rdasdfasdfads");
        TimeUnit.MILLISECONDS.sleep(500);
        System.out.printf("\r123232dasdfasdfads");
        System.out.printf("\r123232dasdfasdfads1111");
//        printLine(20, 100);
    }

    public static void main(String[] args) {


        //System.out.close();
        ConnectableFlowable<Point> f1 = Flowable.create((FlowableEmitter<Point> e) -> {
            while (!e.isCancelled()) {
                Point point = MouseInfo.getPointerInfo().getLocation();
                e.onNext(point);

            }
        }, BackpressureStrategy.DROP).delay(10, TimeUnit.MILLISECONDS).map(x -> new Point(Double.valueOf(x.getX()).intValue() / 10, Double
                .valueOf(x.getY()).intValue() / 20))
                .distinctUntilChanged((x, y) -> {
                    return x.distance(y) < 2;
                }).subscribeOn(Schedulers.single())
                .publish();

        f1.connect();


        Disposable d = f1.buffer(1).subscribe(x -> printPoints(x, 160, 45));
        d.dispose();
        while (!d.isDisposed()) {
        }
    }

}
