package com.stock.notification.entity;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


/**
 * 股票
 *
 * @author luoyang
 * @email 772767100@qq.com
 * @date 2022-12-6，13：30
 */

@Data
@TableName("stockinfo")
public class StockEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 股票名
     */
    private String stockName;

    /**
     * 股票代码
     */
    @TableId
    private String stockcCode;

    /**
     * 地区代码
     */
    private String areaCode;

    /**
     * 股票类型
     */
    private String stockType;

    /**
     * 贡献
     */
    private String contribution;

    /**
     * 总市值
     */
    private BigDecimal totalValue;

    /**
     * 总股本
     */
    private BigDecimal totalIssue;
}
