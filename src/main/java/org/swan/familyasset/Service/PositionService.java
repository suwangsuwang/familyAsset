package org.swan.familyasset.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.swan.familyasset.Entity.Asset;
import org.swan.familyasset.Entity.Position;
import org.swan.familyasset.Mapper.AssetMapper;
import org.swan.familyasset.Mapper.PositionMapper;
import org.swan.familyasset.UserContext;
import org.swan.familyasset.VO.PositionVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<PositionVO> list() {

        Long userId = UserContext.getUserId();

        String key = "position:list:" + userId;

        List<PositionVO> cache =(List<PositionVO>) redisTemplate.opsForValue().get(key);

        if (cache != null) {
            return cache;
        }
        List<Position> positions = positionMapper.findByUserId(userId);

        List<PositionVO> result = new ArrayList<>();

        for (Position p: positions) {

            Asset asset = assetMapper.findById(p.getAssetId());

            // 模拟市场价格
            BigDecimal marketPrice = new BigDecimal("500");

            BigDecimal profit = marketPrice.subtract(p.getAvgCost()).multiply(p.getQuantity());

            PositionVO vo = new PositionVO();

            vo.setAssetName(asset.getName());

            vo.setQuantity(p.getQuantity());

            vo.setAvgCost(p.getAvgCost());

            vo.setMarketPrice(marketPrice);

            vo.setProfit(profit);

            result.add(vo);
        }

        redisTemplate.opsForValue().set(key, result, 10, TimeUnit.MINUTES);
        return result;
    }
}
