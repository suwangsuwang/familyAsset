package org.swan.familyasset.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swan.familyasset.Entity.Position;
import org.swan.familyasset.Service.PositionService;
import org.swan.familyasset.VO.PositionVO;

import java.util.List;

@RestController
@RequestMapping("/position")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping("/list")
    public List<PositionVO> list() {
        return positionService.list();
    }
}
