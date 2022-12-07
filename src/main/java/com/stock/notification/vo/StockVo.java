package com.stock.notification.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 罗洋
 * @email 772767100@qq.com
 */

@Data
public class StockVo {

    private String stockName;

    private String stockCode;

    /**
     * 最新价格
     */
    private BigDecimal latestPrice;

    private String areaCode;

    /**
     * 涨跌幅
     */
    private BigDecimal stockChange;
}
