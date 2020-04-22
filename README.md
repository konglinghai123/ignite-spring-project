
ignite-spring-boot-starter
==========================

## 项目初衷

这个框架是专门为中小型项目打造的微服务框架，底层基于apache ignite，特点是可以做到不依赖外部中间件，实现
`RPC服务`,`分布式缓存`，`分布式计算`，`分布式消息`等功能特性
框架也基于ignite的集群管理，实现了基于集群组的颗粒度的服务调用，即针对集群组的调用

* JDK和Spring boot版本
    <p>jdk版本为`1.8` </p>
    <p>Spring boot 版本要求`1.5.3`以上</p>

## 框架说明

服务中启动的 Spring boot 应用同时启动了ignite的server和client模式，注入到了Spring容器中
```
    @Autowired
    @Qualifier("igniteClient")
    private Ignite igniteClient;

    @Autowired
    @Qualifier("igniteServer")
    private Ignite igniteServer;
```

因此你可以无缝地使用框架没有封装的ignite功能，更多的ignite的功能，请参考中文官网
（https://www.ignite-service.cn/doc/java/）

## 目录
* [RPC服务](#RPC)
* [分布式消息](#Message)
* [分布式广播](#BroadCast)
* [分布式计算](#Computer)

## quick-start

### 构建
```
cd ignite-spring-boot-starter
mvn clean install
```

### 构建一个基于ignite的 spring boot 项目

* 添加依赖:

```xml
   <dependency>
        <groupId>com.github.kong.spring.boot</groupId>
        <artifactId>ignite-spring-boot-starter</artifactId>
        <version>1.0</version>
   </dependency>
```


* 在application-yml添加ignite的相关配置信息，样例配置如下:

* zookeeper 发现
```yml
#zookeeper发现
ignite-cluster:
  name: hello_client_1 #节点名称
  role: client
  des: 测试服务
  zookeeperUrl: 192.168.56.100:2181
  localAddress: 127.0.0.1
  localPort: 47600
```

* 动态ip发现
```yml
ignite-cluster:
  name: hello_server #节点名称
  role: server
  des: 测试服务端
  multicast-group: 224.0.1.111 #组播地址
  localAddress: 127.0.0.1
  localPort: 48600
```


* 为了开发方便，如果Spring boot Appliction 类的不在包名`com.github.kong`目录下，接下来在Spring Boot Application的上添加`@ComponentScan("com.github.kong.*")`，这样idea可以通过看到一些Bean是否已经注入了，当然也可以不添加，框架也有写扫描注入

```java
@SpringBootApplication
@ComponentScan("com.github.kong.*")
public class HelloWorldServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloWorldServerApplication.class);
    }
}
```


## <span id="RPC">RPC服务的创建与消费</span>

### 发布服务基于ignite的RPC服务
* 编写你的ignite服务，需要添加要发布的服务实现上添加`@IgniteRpcService`注解，继承`IgniteService`.
* `HelloWorld` 是定义的接口

```java
@Service
@IgniteRpcService(des = "这是一个例子")
public class HelloWorldService extends IgniteService implements HelloWorld {


}
```

* `@IgniteRpcService` 注解的定义
```java
/**
 * 服务提供者注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgniteRpcService {

    /**
     * @return
     */
    String version() default "1.0";

    //接口描述
    String des() default "";

    //单个节点部署的实例数
    int maxPerNodeCount() default 1;

    //整个集群部署的最大实例数，0：无限制
    int total() default 0;

}
```

* 启动你的Spring Boot应用，观察控制台，可以看到ignite启动相关信息.


### 调用已经发布的RPC服务

* Spring boot 应用配置同上，唯一不同的是，需要更改配置
```yml
#zookeeper发现
ignite-cluster:
  name: hello_client_1 #节点名称 (必须在集群中唯一)
```

* 通过`@IgniteRpcReference`注入需要使用的interface.

```java
@Controller
public class HelloWorldController {

    @IgniteRpcReference
    private HelloWorld helloWorldService;

    @RequestMapping("/helloworld")
    @ResponseBody
    public String test(){
        return helloWorldService.sayHello("kong");
    }

}
```

* 调用不同版本的RPC服务
```java
 @IgniteRpcReference(version = "1.1")
    private HelloWorld helloWorldService;
```

* `@IgniteRpcReference` 注解的定义
```java
/**
 * 网格服务注入注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgniteRpcReference {

    String version() default "1.0";

    //默认使用负载均衡
    boolean isLoadbalance() default true;

    //默认不设超时
    long timeout() default 0;
}

```


## <span id="Message">分布式消息</span>

分布式消息是基于内存的消息订阅系统，如果需要持久化请使用外部的消息系统

### 定义话题消费者
```java
@Service
@IgniteMessageListener(topic = "hello",isBroadcast = false)
public class HelloWorldMessage implements IgniteMessageRecevicer<String> {

    @Override
    public boolean apply(UUID uuid, MessageModel<String> messageModel) {
        System.out.println(messageModel);
        return true;
    }
}
```

* `@IgniteMessageListener` 注解的定义
```java
/**
 * 服务提供者注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface IgniteMessageListener {


    //消息主题
    String topic();

    //消息描述
    String des() default "";

    //是否针对集群内的所有节点（是否允许重复消费）
    boolean isBroadcast() default true;

}
```

* `MessageModel` 是一个消息封装，发送消息时必须用它来发送

### 发送话题消息

```java
@Controller
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private IgniteMessageSender sender;

    @RequestMapping("/sayHello")
    @ResponseBody
    public String test(){
        sender.toRemote("hello", new MessageModel<>("1212"));
        return "1212";
    }
}
```

* `IgniteMessageSender`是框架注入的Bean，可以直接引用

## <span id="BroadCast">分布式广播</span>

分布式广播是指：对集群组的所有节点发送消息，然后获取所有节点返回的结果，原来是基于ignite的分布式闭包利用反射机制调用spring容器内Bean的方法

### 发送一个分布式广播

```java
@Controller
@RequestMapping("/broadcast")
public class BroadcastController {

    @Autowired
    private BroadcastServiceExecutor broadcastServiceExecutor;

    @RequestMapping("/sayHello")
    @ResponseBody
    public List<String> test(){
        return (List<String>) broadcastServiceExecutor.broadcast("server", TestBroadService.class,"sayHello","12123");
    }
}
```

* `BroadcastServiceExecutor`是框架注入的Bean，可以直接引用

* `broadcast` 方法提供3个方法定义
```java
   /**
     * 向其他集群广播
     * @param targetRole 集群标识
     * @param targetClass api类
     * @param methodName 方法名称
     * @param args 参数
     * @return
     */
    public List broadcast(String targetRole,Class targetClass,String methodName,Object... args){...}

      /**
         * 向远端集群广播消息
         * @param targetClass
         * @param methodName
         * @param args
         * @return
         */
     public List broadcastRemote(Class targetClass,String methodName,Object... args){...}


        /**
          * 向集群内广播消息
          * @param targetClass
          * @param methodName
          * @param args
          * @return
          */
      public List broadcastLocal(Class targetClass,String methodName,Object... args){...}

```


## <span id="Computer">分布式计算</span>


分布式计算允许用户执行基于内存的Map-Reduce任务

* 创建 `Map-Reduce` 任务 ,需继承`ComputeTaskSplitAdapter`(import org.apache.ignite.compute.ComputeTaskSplitAdapter)，泛型<T,R>
* T:入参，R：返回类型

```java
//字数统计测试
@Service
public class MapExampleCharacterCountTask  extends ComputeTaskSplitAdapter<List<String>,Integer>  {


    @Nullable
    @Override
    public Integer reduce(List<ComputeJobResult> results) throws IgniteException {
        return results.stream().mapToInt(ComputeJobResult::<Integer>getData).sum();
    }

    @Override
    protected Collection<? extends ComputeJob> split(int gridSize, List<String> arg) throws IgniteException {
        LinkedList jobs = new LinkedList();

        List<List<String>> list = CollectionUtils.split(arg,10000);

        for(final List<String> words : list){
            jobs.add(new ComputeJobAdapter() {
                @Override
                public Object execute() throws IgniteException {
                    int i = 0;
                    for(String s : words){
                        i = i + s.length();
                    }
                    return i;
                }
            });
        }

        return jobs;
    }
}

```

* 执行 `Map-Reduce` 任务
```java
@Controller
@RequestMapping("/mr")
public class MRTestController {

    @Autowired
    private MapReduceTaskExecutor<List<String>,Integer> mapReduceTaskExecutor;

    @RequestMapping("/test")
    @ResponseBody
    public Object test(){
        try {

            List<String> records = new ArrayList<>();
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(new FileInputStream("D:\\data\\cs2.csv"), Charset.forName("GBK"));

            while (csvReader.readRecord()){
                // 读一整行
                records.add(csvReader.getRawRecord());
            }

            List<String> bigRecords = new ArrayList<>();
            for(int i = 0; i < 5; i++){
                bigRecords.addAll(records);
            }
            return mapReduceTaskExecutor.execute(MapExampleCharacterCountTask.class,bigRecords);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

}
```

* `MapReduceTaskExecutor`是框架注入的Bean，可以直接引用

## 集群管理api

框架注入了`IgniteManager`这个bean，可以实现以下功能

```java
public interface IgniteManager {

    /**
     * 获取节点列表
     *
     * @return
     */
    List<NodeInfo> list();

    /**
     * 获取节点的详细信息
     *
     * @param nodeId
     * @return
     */
    ClusterMetrics info(String nodeId);

    /**
     * 获取微服务的基本信息
     *
     * @return
     */
    List<ServiceInfo> servieInfos();

    /**
     * 集群消息信息
     * @return
     */
    List<MessageInfo> messagInfos();

}
```

* 使用`@Autowired` 注入即可
```java
@Controller
@RequestMapping("/admin")
public class AdminCotroller {

    @Autowired
    private IgniteManager igniteManager;

    @RequestMapping("/nodes")
    @ResponseBody
    public List<NodeInfo> nodes(){
        return igniteManager.list();
    }


    @RequestMapping("/nodeInfo/{id}")
    @ResponseBody
    public ClusterMetrics info(@PathVariable("id") String id){
        return igniteManager.info(id);
    }

    @RequestMapping("/services")
    @ResponseBody
    public List<ServiceInfo> services(){
        return igniteManager.servieInfos();
    }

}
```

