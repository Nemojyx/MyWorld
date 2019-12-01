package com.jyx.ArrayList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
/*
了解
    UUID.randomUUID().toString()是javaJDK提供的一个自动生成主键的方法。
    UUID(Universally Unique Identifier)全局唯一标识符,是指在一台机器上生成的数字，
    它保证对在同一时空中的所有机器都是唯一的，是由一个十六位的数字组成,表现出来的形式。

     */
/*
1.故障现象，线程少的时候没有报错，但是数据异常；
当将线程数量增加到30个时，就会出现异常java.util.ConcurrentModificationException并发修改异常
解释这个异常，当同时有两个人在签名，第一个刚写了一个字就被第二个人抢过去了，笔没离开纸，在纸上画了好长一道

2.导致原因
    多线程并发争抢同一个资源类，且没加锁
3.解决方法
3.1：用new Vector<>(),他的底层源码有加锁，但是一致性和并发性是矛盾的，一致性（安全）上来了，并发性就不好了，所以不用这种方法
3.2 ：Collections.synchronizedList(new ArrayList<>());//括号里放的类型是一个list集合，
3.3：极为推荐的解决方式new CopyOnWriteArrayList();写实复制类，他的底层实现了list接口，并解决了线程安全问题，发现其实还可以放set也就是说，你懂得
他的底层解决方式是：
public boolean add(E e) {
        final ReentrantLock lock = this.lock;同学1拿到笔笔（lock上锁）
        lock.lock();开始写名字
        try {
            Object[] elements = getArray();他先拿到原始版本的名单
            int len = elements.length;看一下原来多长
            Object[] newElements = Arrays.copyOf(elements, len + 1);在新的版本原来的长度中扩容1个长度
            newElements[len] = e;将同学1的名字写在新版本的扩容的那一个长度中
            setArray(newElements);然后将他名字set到新的表中
            return true;我写好了，给其他人一个反馈，将之前那个表断了，在我这个新的表中写就好了
        } finally {
            lock.unlock();最后将笔给后面的人（解锁）
        }
    }

4.优化建议
在高并发的情况下考虑使用新的JUC包中的CopyOnWriteArrayList()类优化
 */

public class ArrayListNotSafeDemo {
    public static void main(String[] args) {
        //主方法中不要写业务逻辑，可以用alt+shift+m将写好的逻辑抽取出来
        listNotSafe();
        mapNotSafe();
        setNotSafe();

    }


    private static void mapNotSafe() {
        Map<String,String> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 30; i++) {
            //一个用lambda表达式写的线程，String.valueOf(i)就是线程名
            new Thread(()->{
                //随机产生一个字符串，截取到第8位
                //前面的Thread.currentThread().getName()是获取线程名
                map.put(Thread.currentThread().getName(), UUID.randomUUID().toString().substring(0,8));
                System.out.println(map);
            },String.valueOf(i)).start();
        }
    }


    private static void setNotSafe() {
        Set<String> set = new CopyOnWriteArraySet<>();
        for (int i = 0; i < 30; i++) {
            //一个用lambda表达式写的线程，String.valueOf(i)就是线程名
            new Thread(()->{
                //随机产生一个字符串，截取到第8位
                set.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(set);
            },String.valueOf(i)).start();
        }
    }


    //list的线程安全的解决方式，而set也可以用这种方式解决
    private static void listNotSafe() {
//3.0        List<String> list = new ArrayList<>();线程不安全解决方式在下一行代码
//3.1        List<String> list = new Vector<>();
//3.2        List<System> list = Collections.synchronizedList(new ArrayList<>());括号里放的类型是一个list集合
        List<String> list = new CopyOnWriteArrayList();//写实复制类，他的底层实现了list接口，他的底层也是Object数组
        //三个线程
        for (int i = 0; i < 30; i++) {
            //一个用lambda表达式写的线程，String.valueOf(i)就是线程名
            new Thread(()->{
                //随机产生一个字符串，截取到第8位
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(list);
            },String.valueOf(i)).start();
        }
    }
}
