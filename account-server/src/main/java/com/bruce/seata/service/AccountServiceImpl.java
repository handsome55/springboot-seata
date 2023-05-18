package com.bruce.seata.service;

import com.bruce.seata.tcc.AccountTccAction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author brucelee
 */
@Service("accountServiceImpl")
public class AccountServiceImpl implements AccountService {

    private final AccountTccAction accountTccAction;

    public AccountServiceImpl(AccountTccAction accountTccAction) {
        this.accountTccAction = accountTccAction;
    }

    /**
     * 扣减账户余额
     *
     * @param userId 用户id
     * @param money  金额
     */
    @Override
    public void decrease(Long userId, BigDecimal money) {
        accountTccAction.prepareDecreaseMoney(null, userId, money);
    }
}
