package com.bruce.seata.tcc.impl;

import com.bruce.seata.entity.Order;
import io.seata.rm.tcc.api.BusinessActionContext;
import com.bruce.seata.dao.OrderDao;
import com.bruce.seata.tcc.OrderTccAction;
import com.bruce.seata.tcc.ResultHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @description:
 */
@Slf4j
@Component
public class OrderTccActionImpl implements OrderTccAction {
    private final OrderDao orderDao;

    public OrderTccActionImpl(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    /**
     * try 尝试
     * <p>
     * BusinessActionContext 上下文对象，用来在两个阶段之间传递数据
     * BusinessActionContextParameter 注解的参数数据会被存入 BusinessActionContext
     * TwoPhaseBusinessAction 注解中commitMethod、rollbackMethod 属性有默认值，可以不写
     *
     * @param businessActionContext
     * @param orderNo
     * @param userId
     * @param productId
     * @param count
     * @param money
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean prepareCreateOrder(BusinessActionContext businessActionContext,
                                      String orderNo,
                                      Long userId,
                                      Long productId,
                                      Integer count,
                                      BigDecimal money) {
        orderDao.save(new Order(orderNo, userId, productId, count, money, 0));
        ResultHolder.setResult(OrderTccAction.class, businessActionContext.getXid(), "p");
        return true;
    }

    /**
     * commit 提交
     *
     * @param businessActionContext
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean commit(BusinessActionContext businessActionContext) {
        //检查标记是否存在，如果标记不存在不重复提交
        String p = ResultHolder.getResult(OrderTccAction.class, businessActionContext.getXid());
        if (p == null) {
            return true;
        }

        /**
         * 上下文对象从第一阶段向第二阶段传递时，先转成了json数据，然后还原成上下文对象
         * 其中的整数比较小的会转成Integer类型，所以如果需要Long类型，需要先转换成字符串在用Long.valueOf()解析。
         */
        String orderNo = businessActionContext.getActionContext("orderNo").toString();
        orderDao.updateStatusByOrderNo(orderNo, 1);
        //提交完成后，删除标记
        ResultHolder.removeResult(OrderTccAction.class, businessActionContext.getXid());
        return true;
    }

    /**
     * cancel 撤销
     * <p>
     * 第一阶段没有完成的情况下，不必执行回滚。因为第一阶段有本地事务，事务失败时已经进行了回滚。
     * 如果这里第一阶段成功，而其他全局事务参与者失败，这里会执行回滚
     * 幂等性控制：如果重复执行回滚则直接返回
     *
     * @param businessActionContext
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean rollback(BusinessActionContext businessActionContext) {
        //检查标记是否存在，如果标记不存在不重复提交
        String p = ResultHolder.getResult(OrderTccAction.class, businessActionContext.getXid());
        if (p == null) {
            return true;
        }
        String orderNo = businessActionContext.getActionContext("orderNo").toString();
        orderDao.deleteByOrderNo(orderNo);
        //提交完成后，删除标记
        ResultHolder.removeResult(OrderTccAction.class, businessActionContext.getXid());
        return true;
    }

}
