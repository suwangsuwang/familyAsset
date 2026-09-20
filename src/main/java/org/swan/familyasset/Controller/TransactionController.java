package org.swan.familyasset.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.swan.familyasset.Entity.Transaction;
import org.swan.familyasset.Service.PortfolioService;
import org.swan.familyasset.Service.TransactionService;

import java.util.Map;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PortfolioService portfolioService;

    @PostMapping("/buy")
    public String buy(@RequestBody Transaction transaction) {
        transactionService.buy(transaction);
        System.out.println(transactionService.getClass());
        return "买入成功";
    }

    @PostMapping("/sell")
    public String sell(@RequestBody Transaction transaction) {
        transactionService.sell(transaction);
        return "卖出成功";
    }

    @GetMapping("/portfolio/summary")
    public Map<String, Object> summary() {
        return portfolioService.summary();
    }

    @PostMapping("/test-self")
    public String testSelf(@RequestBody Transaction transaction) {
        transactionService.testSelfInvocation(transaction);
        return "测试完成";
    }

    @PostMapping("/test-propagation")
    public String testPropagation() {

        transactionService.outer();

        return "测试完成";
    }

    @PostMapping("/test-outbox")
    public String testOutbox() {

        transactionService.testOutbox();

        return "测试完成";
    }

    @PostMapping("/test-outboxRollBack")
    public String testOutboxRollBack() {

        transactionService.testOutboxRollback();

        return "测试完成";
    }
}
