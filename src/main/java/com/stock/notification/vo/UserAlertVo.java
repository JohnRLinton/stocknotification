package com.stock.notification.vo;

import com.sun.org.apache.bcel.internal.generic.BIPUSH;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class UserAlertVo {
    private String stockCode;

    private Set<String> userSet;

    private BigDecimal stockChange;

    private BigDecimal latestPrice;

    private BigDecimal userExpect;

    private Integer alertFrequency;
}
