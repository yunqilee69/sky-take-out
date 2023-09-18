package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取营业额统计
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate beginDate, LocalDate endDate) {
        // 存放beginDate到endDate的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(beginDate);
        while (!beginDate.equals(endDate)) {
            // 计算日期的后一天对应的日期
            beginDate = beginDate.plusDays(1);
            dateList.add(beginDate);
        }

        // 获取每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询当天营业额，状态已完成订单的金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> params = new HashMap<>();
            params.put("beginTime", beginTime);
            params.put("endTime", endTime);
            params.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(params);
            // 当天没有营业额
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 组装数据
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));
        return turnoverReportVO;
    }
}
