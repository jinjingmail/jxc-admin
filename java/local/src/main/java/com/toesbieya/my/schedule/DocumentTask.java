package com.toesbieya.my.schedule;

import com.toesbieya.my.constant.DocumentConstant;
import com.toesbieya.my.utils.DateUtil;
import com.toesbieya.my.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@DependsOn("redisUtil")
public class DocumentTask {
    @PostConstruct
    public void run() {
        template("初始化单据ID", 0);
    }

    //每天零点重置单据ID的值
    @Async("scheduledExecutor")
    @Scheduled(cron = "1 0 0 */1 * ?")
    public void autoRefreshDocumentID() {
        template("定时更新单据ID", 1);
    }

    //启动时初始化单据ID
    private void initDocumentsID() {
        String[] fields = new String[DocumentConstant.DOCUMENT_TYPE.length + 1];
        fields[0] = "date";
        System.arraycopy(DocumentConstant.DOCUMENT_TYPE, 0, fields, 1, fields.length - 1);

        List<Object> result = RedisUtil.hmget(DocumentConstant.DOCUMENT_TYPE_REDIS_KEY, fields);

        long now = DateUtil.getTimestampNow();

        //日期不是今天时，更新全部
        Object firstItem = result.get(0);
        if (null == firstItem || now != (long) firstItem) {
            updateAllDocument(now);
            log.info("更新【{}】种单据ID成功", DocumentConstant.DOCUMENT_TYPE.length);
            return;
        }

        //判断是否有新增的单据类型，有则更新
        int updateNum = 0;
        Map<String, Object> update = new HashMap<>();
        int resultSize = result.size();

        for (int i = 1; i < resultSize; i++) {
            if (result.get(i) == null) {
                updateNum++;
                update.put(DocumentConstant.DOCUMENT_TYPE[i - 1], 1);
            }
        }

        if (updateNum > 0) {
            RedisUtil.hmset(DocumentConstant.DOCUMENT_TYPE_REDIS_KEY, update);
            log.info("更新增加的单据【{}】种", updateNum);
        }
    }

    //每种单据的值置为1
    private void updateAllDocument(long now) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", now);
        for (String v : DocumentConstant.DOCUMENT_TYPE) {
            map.put(v, 1);
        }
        RedisUtil.hmset(DocumentConstant.DOCUMENT_TYPE_REDIS_KEY, map);
    }

    //模板
    private void template(String action, int type) {
        Instant start = Instant.now();

        String lockKey = DocumentConstant.UPDATE_DOCUMENTS_LOCK_KEY;

        try (RedisUtil.Locker locker = new RedisUtil.Locker(lockKey)) {
            if (!locker.lock()) {
                log.error("{}失败，获取锁【{}】失败", action, lockKey);
                return;
            }

            if (type == 0) {
                initDocumentsID();
            }
            else {
                updateAllDocument(DateUtil.getTimestampNow());
            }

            log.info("{}成功，耗时：{}毫秒", action, ChronoUnit.MILLIS.between(start, Instant.now()));
        }
        catch (Exception e) {
            log.error("{}失败", action, e);
        }
    }
}