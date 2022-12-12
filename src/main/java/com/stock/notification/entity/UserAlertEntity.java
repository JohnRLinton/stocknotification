package com.stock.notification.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户预警
 */

@Data
@TableName("useralert")
public class UserAlertEntity implements Serializable {
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
     * 预警类型
     */
    private Integer alertType;

    /**
     * 预警类容
     */
    private String alertContent;

    /**
     * 预警频率
     */
    private Integer alertFrequency;
}
