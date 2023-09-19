package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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
            params.put("begin", beginTime);
            params.put("end", endTime);
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

    /**
     * 获取用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 存放beginDate到endDate的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 新增用户数量
        List<Integer> newUserList = new ArrayList<>();
        // 总用户数量
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> params = new HashMap<>();
            params.put("end", endTime);

            // 总用户数量
            Integer totalUser = userMapper.countByMap(params);

            // 新增用户数量
            params.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(params);

            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }

        // 组装数据
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList, ","));
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));
        return userReportVO;
    }

    /**
     * 获取订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 存放beginDate到endDate的所有日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            // 计算日期的后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 存放每天的订单总数
        List<Integer> orderCountList = new ArrayList<>();
        // 存放每天的有效订单总数
        List<Integer> validOrderCountList = new ArrayList<>();
        // 遍历dateList集合，查询每天的有效订单和订单总数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询每天的订单总数
            Integer orderCount = getOrderCount(beginTime, endTime, null);

            // 查询每天的有效订单
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        // 订单总数量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 有效订单总数量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount.doubleValue();
        }

        // 组装数据
        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(StringUtils.join(dateList, ","));
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);
        orderReportVO.setOrderCompletionRate(orderCompletionRate);
        return orderReportVO;
    }

    /**
     * 统计指定时间区间内的销量前10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 获取销量前10
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        // 组装数据
        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        salesTop10ReportVO.setNameList(StringUtils.join(nameList, ","));
        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        salesTop10ReportVO.setNumberList(StringUtils.join(numberList, ","));

        return salesTop10ReportVO;
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询数据库获取运营数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);

        // 查询概览数据
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));

        // 通过POI写入excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);

            // 填充数据
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 填充时间
            sheet.getRow(1).getCell(0).setCellValue("时间：" + beginDate + "至" + endDate);

            // 填充第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(1).setCellValue(businessData.getTurnover());
            row.getCell(3).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(5).setCellValue(businessData.getNewUsers());

            // 填充第5行
            row = sheet.getRow(4);
            row.getCell(1).setCellValue(businessData.getValidOrderCount());
            row.getCell(3).setCellValue(businessData.getUnitPrice());

            // 填充明细数据
            // 获取开始天数到结束天数的总数，1号到10号共10天
            Long days = endDate.toEpochDay() - beginDate.toEpochDay() + 1;
            for (int i = 0; i < days; i++) {
                // 获取具体某一天的数据
                LocalDate date = beginDate.plusDays(i);
                BusinessDataVO tData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                row = sheet.getRow(7 + i);
                row.getCell(0).setCellValue(date.toString());
                row.getCell(1).setCellValue(tData.getTurnover());
                row.getCell(2).setCellValue(tData.getValidOrderCount());
                row.getCell(3).setCellValue(tData.getOrderCompletionRate());
                row.getCell(4).setCellValue(tData.getUnitPrice());
                row.getCell(5).setCellValue(tData.getNewUsers());
            }

            // 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            in.close();
            out.close();
            excel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("begin", begin);
        params.put("end", end);
        params.put("status", status);

        return orderMapper.countByMap(params);
    }
}
