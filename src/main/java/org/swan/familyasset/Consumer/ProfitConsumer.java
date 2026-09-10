package org.swan.familyasset.Consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.swan.familyasset.Entity.ProfitRecord;
import org.swan.familyasset.Entity.SellEvent;
import org.swan.familyasset.Mapper.ProfitRecordMapper;

import java.math.BigDecimal;

@Component
public class ProfitConsumer {

    @Autowired
    private ProfitRecordMapper profitRecordMapper;

    @RabbitListener(queues = "profit.queue")
    public void handleSell(SellEvent event) {

        BigDecimal cost = event.getAvgCost()
                .multiply(event.getQuantity());

        BigDecimal revenue = event.getSellPrice()
                .multiply(event.getQuantity());


        BigDecimal profit = revenue.subtract(cost);

        ProfitRecord record = new ProfitRecord();

        record.setUserId(event.getUserId());
        record.setAssetId(event.getAssetId());
        record.setQuantity(event.getQuantity());
        record.setCost(cost);
        record.setRevenue(revenue);
        record.setProfit(profit);
        profitRecordMapper.insert(record);

        System.out.println("实现收益: " + profit);
    }
}
