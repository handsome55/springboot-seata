作业二：分布式事务专项练习

场景分析：
假设一个电商系统中用户每成功下单一次就可以添加对应的积分记录。
现有架构中，系统存在一个订单（Order）服务和一个用户账户（Account）服务。
当用户执行下单操作时，需要通过订单服务生成一个订单记录，
同时通过用户账户服务新增一条账户积分变更记录，我们希望这两个操作的数据是一致的，
即订单创建操作和积分记录变更要不同时成功，要不同时失败。针对这一场景，
我们可以应用哪些技术体系来实现这一目标？具体的实现过程又是怎么样的？

项目解析：
1. nacos-1.4.0 
2. seata-1.4.2 
3. springboot-2.1.8.RELEASE 
4. springCloud-Greenwich.SR2
5. mysql
其中mysql中有seata数据库和业务数据库，nacos数据库,nacos、seata、mysql在本机部署启动


目录结构说明:
1. account-service：用户账户服务
2. storage-service：商品库存服务
3. order-service：订单服务
其余的sql，order.sql account.sql storage.sql为业务表，其余sql为seata使用。

启动顺序:
1. mysql：初始化数据库。
2. nacos：启动注册中心服务，standlone模式启动。
3. seata：启动seata服务。
4. account-service：运行用户账户服务。
5. storage-service：运行商品库存服务。
6. order-service：运行订单服务。
测试：通过postman等工具，调用 order-server 的下订单接口。
1. 首先调用order服务的下单服务，触发事件。
