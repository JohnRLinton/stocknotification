package com.stock.notification.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户所持股票信息
 */

@Data
@TableName()
public class UserStockRelationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId
    private Integer userId;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 是否所持股票
     */
    private Integer validStockCode;

    /**
     * 是否收藏股票
     */
    private Integer validCollectCode;
}
