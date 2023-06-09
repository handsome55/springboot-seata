package com.bruce.seata.service;

import com.bruce.seata.entity.Order;
import com.bruce.seata.dao.OrderDao;
import com.bruce.seata.feign.AccountApi;
import com.bruce.seata.feign.StorageApi;
import com.bruce.seata.tcc.OrderTccAction;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author brucelee
 */
@Slf4j
@Service("orderServiceImpl")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private StorageApi storageApi;
    @Autowired
    private AccountApi accountApi;
    @Autowired
    OrderTccAction orderTccAction;

    /**
     * 创建订单
     *
     * @param order
     * @return 测试结果：
     * 1.添加本地事务：仅仅扣减库存
     * 2.不添加本地事务：创建订单，扣减库存
     */
    @Override
    @GlobalTransactional(name = "fsp-create-order", rollbackFor = Exception.class)
    public void create(Order order) {

        log.info("------->交易开始");
        //本地方法
        orderTccAction.prepareCreateOrder(null,
                this.generateOrderNo(),
                order.getUserId(),
                order.getProductId(),
                order.getCount(),
                order.getMoney());

        //远程方法 扣减库存
        storageApi.decrease(order.getProductId(), order.getCount());
        //远程方法 扣减账户余额
        log.info("------->扣减账户开始order中");
        accountApi.decrease(order.getUserId(), order.getMoney());
        log.info("------->扣减账户结束order中");

        log.info("------->交易结束");
    }

    /**
     * 修改订单状态
     */
    @Override
    public void update(Long userId, BigDecimal money, Integer status) {
        log.info("修改订单状态，入参为：userId={},money={},status={}", userId, money, status);
        orderDao.update(userId, money, status);
    }

    private String generateOrderNo() {
        return LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern("yyMMddHHmmssSSS")
                );
    }
}
