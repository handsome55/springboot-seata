package com.bruce.seata.service;

import java.math.BigDecimal;

/**
 * @author brucelee
 */
public interface AccountService {

    /**
     * 扣减账户余额
     * @param userId 用户id
     * @param money 金额
     */
    void decrease(Long userId, BigDecimal money);
}
