package org.swan.familyasset.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swan.familyasset.Entity.Asset;
import org.swan.familyasset.Entity.Transaction;
import org.swan.familyasset.Mapper.AssetMapper;
import org.swan.familyasset.Mapper.TransactionMapper;
import org.swan.familyasset.UserContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    public Map<String, Object> summary() {
        Long userId = UserContext.getUserId();

        List<Asset> assets = assetMapper.findByUserId(userId);

        BigDecimal totalInvest = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (Asset asset : assets) {

            List<Transaction> list = transactionMapper.findByAssetId(asset.getId());

            BigDecimal quantity = BigDecimal.ZERO;
            BigDecimal invest = BigDecimal.ZERO;

            for (Transaction t : list) {
                if ("BUY".equals(t.getType())) {
                    quantity = quantity.add(t.getQuantity());
                    invest = invest.add(t.getPrice().multiply(t.getQuantity()));
                }

                if ("SELL".equals(t.getType())) {

                    quantity = quantity.subtract(t.getQuantity());
                }
            }

            // 模拟当前价格 (后面升级)
            BigDecimal currentPrice = new BigDecimal("500");

            BigDecimal value = currentPrice.multiply(quantity);

            totalInvest = totalInvest.add(invest);
            totalValue = totalValue.add(value);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalInvest", totalInvest);
        result.put("totalValue", totalValue);
        result.put("profit", totalValue.subtract(totalInvest));

        return result;
    }
}
