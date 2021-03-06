package com.toesbieya.my.schedule;

import com.toesbieya.my.mapper.StatisticMapper;
import com.toesbieya.my.model.entity.StatFinishOrder;
import com.toesbieya.my.model.entity.StatProfitGoods;
import com.toesbieya.my.model.entity.StatProfitTotal;
import com.toesbieya.my.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StatisticTask {
    @Resource
    private StatisticMapper statisticMapper;

    @PostConstruct
    public void run() {
        this.autoStat();
    }

    //每天零点统计信息
    @Async("scheduledExecutor")
    @Scheduled(cron = "1 0 0 */1 * ?")
    public void autoStat() {
        log.info("开始统计信息...");

        long lastDay = DateUtil.getTimestampBeforeNow(1);
        long today = DateUtil.getTimestampNow();

        //检查是否已统计过昨日已完成的订单数
        if (!statisticMapper.checkDailyFinishOrderExist(lastDay)) {
            Integer lastDayPurchaseOrderFinish = statisticMapper.getPurchaseOrderLastDayFinishOrderNum();
            Integer lastDaySellOrderFinish = statisticMapper.getSellOrderLastDayFinishOrderNum();

            StatFinishOrder param = new StatFinishOrder();
            param.setPurchase(lastDayPurchaseOrderFinish);
            param.setSell(lastDaySellOrderFinish);
            param.setTime(lastDay);

            statisticMapper.insertFinishOrder(param);
        }

        //检查是否已统计过昨日的采购额、销售额、毛利
        if (!statisticMapper.checkDailyProfitExist(lastDay)) {
            //昨日各个商品的采购额、销售额
            List<StatProfitGoods> purchaseProfitGoods = statisticMapper.getPurchaseOrderDailyProfitGoods(lastDay, today);
            List<StatProfitGoods> sellProfitGoods = statisticMapper.getSellOrderDailyProfitGoods(lastDay, today);

            //昨日的采购总额、销售总额
            BigDecimal totalPurchase = new BigDecimal(0);
            BigDecimal totalSell = new BigDecimal(0);

            //根据商品分类计算
            Map<Integer, StatProfitGoods> map = new HashMap<>(sellProfitGoods.size());

            for (StatProfitGoods p : sellProfitGoods) {
                map.put(p.getCid(), p);
            }

            for (StatProfitGoods p : purchaseProfitGoods) {
                p.setTime(lastDay);
                Integer cid = p.getCid();
                BigDecimal purchase = p.getPurchase();
                totalPurchase = totalPurchase.add(purchase);
                StatProfitGoods s = map.get(cid);

                if (s != null) {
                    BigDecimal sell = s.getSell();
                    totalSell = totalSell.add(sell);
                    p.setSell(sell);
                    p.setProfit(sell.subtract(purchase));
                    map.remove(cid);
                }
                else {
                    p.setSell(new BigDecimal(0));
                    p.setProfit(p.getSell().subtract(purchase));
                }
            }

            for (StatProfitGoods s : map.values()) {
                s.setTime(lastDay);
                s.setPurchase(new BigDecimal(0));
                BigDecimal sell = s.getSell();
                s.setProfit(sell);
                totalSell = totalSell.add(sell);
                purchaseProfitGoods.add(s);
            }

            BigDecimal totalProfit = totalSell.subtract(totalPurchase);

            StatProfitTotal statProfitTotal = new StatProfitTotal();
            statProfitTotal.setPurchase(totalPurchase);
            statProfitTotal.setSell(totalSell);
            statProfitTotal.setProfit(totalProfit);
            statProfitTotal.setTime(lastDay);

            statisticMapper.insertProfitTotal(statProfitTotal);

            if (purchaseProfitGoods.size() > 0) {
                statisticMapper.insertProfitGoodsBatch(purchaseProfitGoods);
            }
        }

        log.info("结束统计信息...");
    }
}
