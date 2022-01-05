# 多线程编程

## Thread 和Runnable

两种使用多线程的方式，第一种是继承Thread类，第二种是实现runnable接口（如果实现runnable接口的话，就要使用new Thread(new Runnable Class())来使用该多线程的方式）。

优缺点：

- Java是单继承，多实现，Runnable在使用上较Thread更加方便
- Runnable 跟家复合面向对象，将线程单独进行对象的封装
- Runnable 降低了线程对象和线程任务的耦合性质
- 如果不需要使用Thread的方法的话，Runnable是更加轻量的实现。

优先使用Runnable接口来自定义线程类。

## Callable，Future 和Future Task

我们希望任务结束后，会有返回值，但是run方法是没有返回值的。 使用Callable接口和Future接口解决这个问题（异步模型）。

### Callable 接口

Callable 和 Runnable类似，只有一个抽象方法的函数式接口，Callable提供的方法是有返回值的，支持泛型。

```java
@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

Callable要配合线程工具ExecutorService 使用（线程池）。ExecutorService可以使用submit方法来让一个Callable接口执行，放回一个Future，后续的程序可以通过这个Future的get方法获得结果。

```java
// 自定义Callable
class Task implements Callable<Integer>{
    @Override
    public Integer call() throws Exception {
        // 模拟计算需要一秒
        Thread.sleep(1000);
        return 2;
    }
    public static void main(String args[]) throws Exception {
        // 使用
        ExecutorService executor = Executors.newCachedThreadPool();
        Task task = new Task();
        Future<Integer> result = executor.submit(task);
        // 注意调用get方法会阻塞当前线程，直到得到结果。
        // 所以实际编码中建议使用可以设置超时时间的重载get方法。
        System.out.println(result.get()); 
    }
}
```

有个问题，为什么不主动退出程序？（没有设置超时时间么？但是已经结束了，为什么不主动退出程序）

[Red Spider](http://concurrent.redspider.group/article/01/2.html)