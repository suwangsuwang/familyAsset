package org.swan.familyasset.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swan.familyasset.Entity.Asset;
import org.swan.familyasset.Mapper.AssetMapper;
import org.swan.familyasset.UserContext;

import java.util.List;

@Service
public class AssetService {

    @Autowired
    private AssetMapper assetMapper;

    public void addAsset(Asset asset) {
        Long userId = UserContext.getUserId();
        asset.setUserId(userId);

        assetMapper.insert(asset);
    }

    public List<Asset> list() {
        Long userId = UserContext.getUserId();
        return assetMapper.findByUserId(userId);
    }

}
