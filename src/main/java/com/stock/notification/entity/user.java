package com.stock.notification.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * 用户信息
 *
 * @author luoyang
 * @email 772767100@qq.com
 * @date 2022-12-6，13：30
 */


@Data
@TableName("userinfo")
public class user implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户id
     */
    @TableId
    private Integer userId;

    /**
     * 地区代码
     */
    private String areaCode;

    /**
     * 附属信息
     */
    private String attr;

}
