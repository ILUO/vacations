package com.iluo.controller;

import com.alibaba.fastjson.JSONObject;
import com.iluo.dao.GoodsDAO;
import com.iluo.po.Goods;
import com.iluo.po.GoodsExample;
import com.iluo.po.MiaoshaUser;
import com.iluo.redis.GoodsKey;
import com.iluo.redis.RedisService;
import com.iluo.service.MiaoshaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Yang Xing Luo on 2019/12/27.
 */

@RestController
@RequestMapping("/miaosha")
@Api(value = "秒杀")
public class MiaoShaController implements InitializingBean{
    @Autowired
    private GoodsDAO goodsDAO;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MiaoshaService miaoshaService;

    private HashMap<Long,Boolean> localHashMap = new HashMap<>();

    //@AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @PostMapping("/{path}/Pessimism")
    @ApiOperation(value = "悲观锁防止超发")
    public JSONObject miaoshaPessimism(MiaoshaUser miaoshaUser, @PathVariable("path") String path,
                                       @ApiParam(value = "商品ID") @RequestParam("goodsId") Long goodsId){
//        if(!miaoshaService.checkPath(miaoshaUser,goodsId,path)) return CommonUtil.errorJson("秒杀接口错误");
        return miaoshaService.miaoshaPessimism(miaoshaUser,goodsId);
    }


    @GetMapping("/getPath")
    @ApiOperation(value = "获得秒杀接口路径")
    public String getmiaoshaPath(MiaoshaUser miaoshaUser,@ApiParam(value = "商品ID") @RequestParam("goodsId") Long goodsId){
        return miaoshaService.getMiaoshaPath(miaoshaUser,goodsId);
    }

    //@AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @PostMapping("/{path}/Optimism")
    @ApiOperation(value = "乐观锁防止超发")
    public JSONObject miaoshaOptimism(MiaoshaUser miaoshaUser,@PathVariable("path") String path,
                                      @ApiParam(value = "商品id") @RequestParam("goodsId") Long goodsId){
        return miaoshaService.miaoshaOptimism(miaoshaUser,goodsId);
    }


    @PostMapping("/{path}/MessageQueue")
    @ApiOperation(value = "消息队列秒杀")
    public JSONObject miaoshaMessageQueue(MiaoshaUser miaoshaUser,@PathVariable("path") String path,
                                          @ApiParam(value = "商品id") @RequestParam("googsId") Long goodsId){
        return miaoshaService.miaoshaMessageQueue(miaoshaUser,goodsId,path,localHashMap);
    }

    @Override
    public void afterPropertiesSet(){
        GoodsExample goodsExample = new GoodsExample();
        List<Goods> goodsList = goodsDAO.selectByExample(goodsExample);
        if(goodsList == null) return;
        for(Goods goods:goodsList){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),goods.getGoodsStock());
            localHashMap.put(goods.getId(),false);
        }
    }
}
