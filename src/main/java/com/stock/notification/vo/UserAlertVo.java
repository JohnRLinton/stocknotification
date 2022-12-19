package com.stock.notification.vo;

import com.sun.org.apache.bcel.internal.generic.BIPUSH;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UserAlertVo {
    private String stockCode;

    private int userId;

    private BigDecimal stockChange;

    private BigDecimal latestPrice;

    private BigDecimal userExpect;

    /**
     *股票变动差价
     */
    private BigDecimal difference;

    private Integer alertFrequency;
}
