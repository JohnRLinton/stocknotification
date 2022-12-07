package com.stock.notification.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 股票交易详细信息
 *
 * @author luoyang
 * @email 772767100@qq.com
 * @date 2022-12-6，13：30
 */

@Data
@TableName("stocktrading")
public class StocktradiingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 股票代码
     */
    @TableId
    private String stockCode;

    /**
     * 最新价格
     */
    private BigDecimal latestPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 今开
     */
    private BigDecimal todayPrice;

    /**
     * 昨收
     */
    private BigDecimal pre;

    /**
     * 换手率
     */
    private BigDecimal turnoverRate;

    /**
     * 成交量
     */
    private BigDecimal amount;

    /**
     * 成交额
     */
    private BigDecimal turnover;
}
