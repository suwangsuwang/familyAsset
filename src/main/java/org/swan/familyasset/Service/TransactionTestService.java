package org.swan.familyasset.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.swan.familyasset.Entity.Transaction;
import org.swan.familyasset.Mapper.TransactionMapper;

import java.math.BigDecimal;

@Service
public class TransactionTestService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inner() {
        System.out.println("进入 inner()");

        Transaction transaction = new Transaction();
        transaction.setUserId(1L);
        transaction.setAssetId(1L);
        transaction.setType("INNER");
        transaction.setPrice(new BigDecimal("200.00"));
        transaction.setQuantity(new BigDecimal("1.00"));

        transactionMapper.insert(transaction);

        throw new RuntimeException("inner 异常");
    }


}
