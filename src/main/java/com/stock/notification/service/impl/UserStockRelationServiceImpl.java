package com.stock.notification.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.StockTradingDao;
import com.stock.notification.dao.UserStockRelationDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.StocktradingEntity;
import com.stock.notification.entity.UserEntity;
import com.stock.notification.entity.UserStockRelationEntity;
import com.stock.notification.service.StockService;
import com.stock.notification.service.StockTradingService;
import com.stock.notification.service.UserService;
import com.stock.notification.service.UserStockRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service("userStockRelationService")
public class UserStockRelationServiceImpl extends ServiceImpl<UserStockRelationDao, UserStockRelationEntity> implements UserStockRelationService {

    @Resource
    UserStockRelationDao userStockRelationDao;

    @Autowired
    StockService stockService;

    @Autowired
    UserService userService;

    /**
     * 获取用户所持股票信息
     * @param userId
     * @return
     */
    @Override
    public List<StockEntity> querySelectStock(int userId) {
        List<UserStockRelationEntity> userList = userStockRelationDao.selectList(new QueryWrapper<UserStockRelationEntity>().eq("userid", userId).eq("valid_stockcode",0));
        List<StockEntity> stockCollect = userList.stream().map((item) -> {
            String stockCode = item.getStockCode();
            StockEntity stockEntity = stockService.getByCode(stockCode);
            return stockEntity;
        }).collect(Collectors.toList());
        return stockCollect;
    }

    /**
     * 用户添加股票
     * @param stockCode
     * @param userId
     */
    @Override
    public void addShares(String stockCode, int userId) {
        UserStockRelationEntity stockRelationEntity = new UserStockRelationEntity();
        stockRelationEntity.setStockCode(stockCode);
        stockRelationEntity.setUserId(userId);
        stockRelationEntity.setValidStockCode(0);
        userStockRelationDao.insert(stockRelationEntity);
    }

    /**
     * 用户删除股票，逻辑删除
     * @param stockCode
     * @param userId
     */
    @Override
    public void removeShares(String stockCode, int userId) {
        UserStockRelationEntity relationEntity = new UserStockRelationEntity();
        relationEntity.setUserId(userId);
        relationEntity.setStockCode(stockCode);
        userStockRelationDao.update(relationEntity,new UpdateWrapper<UserStockRelationEntity>().eq("validStockCode",1));
    }

    /**
     * 用户收藏股票
     * @param stockCode
     * @param userId
     */
    @Override
    public void addCollect(String stockCode, int userId) {
        UserStockRelationEntity stockRelationEntity = new UserStockRelationEntity();
        stockRelationEntity.setStockCode(stockCode);
        stockRelationEntity.setUserId(userId);
        stockRelationEntity.setValidCollectCode(0);
        userStockRelationDao.insert(stockRelationEntity);
    }

    /**
     * 查看收藏该股票的用户
     * @param stockCode
     * @return
     */
    @Override
    public List<UserEntity> queryUserByStock(String stockCode) {
        List<UserStockRelationEntity> userList = userStockRelationDao.selectList(new QueryWrapper<UserStockRelationEntity>().eq("stockcode",stockCode).eq("valid_collectcode",0));
        List<UserEntity> userCollect = userList.stream().map((item) -> {
            int userId = item.getUserId();
            UserEntity UserEntity = userService.getById(userId);
            return UserEntity;
        }).collect(Collectors.toList());
        return userCollect;
    }


}
