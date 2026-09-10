package org.swan.familyasset.Service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swan.familyasset.Entity.Asset;
import org.swan.familyasset.Entity.Position;
import org.swan.familyasset.Entity.SellEvent;
import org.swan.familyasset.Entity.Transaction;
import org.swan.familyasset.Mapper.AssetMapper;
import org.swan.familyasset.Mapper.PositionMapper;
import org.swan.familyasset.Mapper.TransactionMapper;
import org.swan.familyasset.UserContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    public void buy(Transaction transaction) {

        Long userId = UserContext.getUserId();
        // 1. 校验资产
        Asset asset = assetMapper.findById(transaction.getAssetId());

        if (asset == null || !asset.getUserId().equals(userId)) {
            throw new RuntimeException("非法操作");
        }
        // 2. 保存 transaction
        transaction.setType("BUY");
        transaction.setUserId(userId);
        transactionMapper.insert(transaction);

        // 3. 查询 position
        Position position = positionMapper.findByUserIdAndAssetId(userId, transaction.getAssetId());

        // 第一次买入
        if (position == null) {
            position = new Position();

            position.setUserId(userId);
            position.setAssetId(transaction.getAssetId());

            position.setQuantity(transaction.getQuantity());

            position.setAvgCost(transaction.getPrice());

            positionMapper.insert(position);
            redisTemplate.delete("position:list:" + userId);
            return;
        }

        // 原有持仓
        BigDecimal oldQuantity = position.getQuantity();
        BigDecimal oldAvgCost = position.getAvgCost();

        // 新的买入
        BigDecimal buyQuantity = transaction.getQuantity();
        BigDecimal buyPrice = transaction.getPrice();

        // 新总数量
        BigDecimal newQuantity = oldQuantity.add(buyQuantity);

        // 原总成本
        BigDecimal oldTotal = oldQuantity.multiply(oldAvgCost);

        // 新买入成本
        BigDecimal buyTotal = buyQuantity.multiply(buyPrice);

        // 新平均成本
        BigDecimal newAvgCost = oldTotal.add(buyTotal).divide(newQuantity, 4, RoundingMode.HALF_UP);

        // 更新
        position.setQuantity(newQuantity);
        position.setAvgCost(newAvgCost);

        positionMapper.update(position);
        redisTemplate.delete("position:list:" + userId);

    }


    @Transactional
    public void sell(Transaction transaction) {

        Long userId = UserContext.getUserId();
        // 1. 校验持仓
        Asset asset = assetMapper.findById(transaction.getAssetId());
        // 2. 查询 position
        Position position = positionMapper.findByUserIdAndAssetId(userId, transaction.getAssetId());
        if (position == null || !position.getUserId().equals(userId)) {
            throw new RuntimeException("非法操作");
        }

        if (position.getQuantity().compareTo(transaction.getQuantity()) < 0) {
            throw new RuntimeException("持仓不足");
        }

        transaction.setUserId(userId);
        transaction.setType("SELL");

        transactionMapper.insert(transaction);


        // 原有持仓
        BigDecimal oldQuantity = position.getQuantity();
        BigDecimal oldAvgCost = position.getAvgCost();

        // 新的卖出
        BigDecimal sellQuantity = transaction.getQuantity();
        BigDecimal sellPrice = transaction.getPrice();

        //
        BigDecimal realizedProfit = sellPrice.subtract(position.getAvgCost()).multiply(transaction.getQuantity());

        BigDecimal newQuantity = position.getQuantity().subtract(sellQuantity);

        // 原总成本
        BigDecimal oldTotal = oldQuantity.multiply(oldAvgCost);
        BigDecimal sellTotal = sellQuantity.multiply(oldAvgCost);

        // 新平均成本
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            positionMapper.deleteById(position.getId());
        } else {
            // 更新
            position.setQuantity(newQuantity);


            positionMapper.update(position);
        }
        redisTemplate.delete("position:list:" + userId);

        SellEvent event = new SellEvent();
        event.setUserId(userId);
        event.setAssetId(transaction.getAssetId());
        event.setQuantity(transaction.getQuantity());
        event.setSellPrice(transaction.getPrice());
        event.setAvgCost(position.getAvgCost());

        rabbitTemplate.convertAndSend("sell.exchange", "sell.success", event);
    }
}
