package com.stock.notification.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author 罗洋
 * @email 772767100@qq.com
 */

@Data
public class StockVo {



    private String stockCode;

    /**
     * 最新价格
     */
    private BigDecimal latestPrice;



    /**
     * 涨跌幅
     */
    private BigDecimal stockChange;
}
