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
}
