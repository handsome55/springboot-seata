package com.bruce.seata.tcc.impl;

import com.bruce.seata.dao.StorageDao;
import com.bruce.seata.entity.Storage;
import com.bruce.seata.tcc.StorageTccAction;
import io.seata.rm.tcc.api.BusinessActionContext;
import com.bruce.seata.tcc.ResultHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
/**
 * @description:
 */
@Slf4j
@Component
public class StorageTccActionImpl implements StorageTccAction {
    private final StorageDao storageDao;
    public StorageTccActionImpl(StorageDao storageDao){
        this.storageDao = storageDao;
    }

    /**
     * try 尝试
     *
     * BusinessActionContext 上下文对象，用来在两个阶段之间传递数据
     * BusinessActionContextParameter 注解的参数数据会被存入 BusinessActionContext
     * TwoPhaseBusinessAction 注解中commitMethod、rollbackMethod 属性有默认值，可以不写
     *
     * @param businessActionContext
     * @param productId
     * @param count
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean prepareDecreaseStorage(BusinessActionContext businessActionContext, Long productId, Integer count) {
        log.info("减少商品库存，第一阶段，锁定减少的库存量，productId="+productId+"， count="+count);
        Storage storage = storageDao.findOneByProductId(productId);
        if (storage.getResidue()-count<0) {
            throw new RuntimeException("库存不足");
        }
        /*
        库存减掉count， 冻结库存增加count
         */
        storageDao.updateFrozen(productId, storage.getResidue()-count, storage.getFrozen()+count);
        //保存标识
        ResultHolder.setResult(getClass(), businessActionContext.getXid(), "p");
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
        long productId = Long.parseLong(businessActionContext.getActionContext("productId").toString());
        int count = Integer.parseInt(businessActionContext.getActionContext("count").toString());
        log.info("减少商品库存，第二阶段提交，productId="+productId+"， count="+count);
        //防止重复提交
        if (ResultHolder.getResult(getClass(), businessActionContext.getXid()) == null) {
            return true;
        }
        storageDao.updateFrozenToUsed(productId, count);
        //删除标识
        ResultHolder.removeResult(getClass(), businessActionContext.getXid());
        return true;
    }

    /**
     * cancel 撤销
     *
     * @param businessActionContext
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean rollback(BusinessActionContext businessActionContext) {
        long productId = Long.parseLong(businessActionContext.getActionContext("productId").toString());
        int count = Integer.parseInt(businessActionContext.getActionContext("count").toString());
        log.info("减少商品库存，第二阶段，回滚，productId="+productId+"， count="+count);
        //防止重复回滚
        if (ResultHolder.getResult(getClass(), businessActionContext.getXid()) == null) {
            return true;
        }
        storageDao.updateFrozenToResidue(productId, count);
        //删除标识
        ResultHolder.removeResult(getClass(), businessActionContext.getXid());
        return true;
    }
}
