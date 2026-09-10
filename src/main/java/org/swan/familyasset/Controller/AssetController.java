package org.swan.familyasset.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.swan.familyasset.Entity.Asset;
import org.swan.familyasset.Service.AssetService;

import java.util.List;

@RestController
@RequestMapping("/asset")
public class AssetController {

    @Autowired
    private AssetService assetService;

    @PostMapping("/add")
    public String add(@RequestBody Asset asset) {
        assetService.addAsset(asset);
        return "添加成功";
    }

    @GetMapping("/list")
    public List<Asset> list() {
        return assetService.list();
    }
}
