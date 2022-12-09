package com.stock.notification.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stock.notification.dao.UserDao;
import com.stock.notification.dao.UserStockRelationDao;
import com.stock.notification.entity.StockEntity;
import com.stock.notification.entity.UserEntity;
import com.stock.notification.entity.UserStockRelationEntity;
import com.stock.notification.service.UserService;
import com.stock.notification.service.UserStockRelationService;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    @Override
    public UserEntity getById(int userId) {
        return  baseMapper.selectById(new QueryWrapper<UserEntity>().eq("userid",userId));
    }
}
