package com.toesbieya.my.controller;

import com.toesbieya.my.constant.MsgConstant;
import com.toesbieya.my.model.entity.Msg;
import com.toesbieya.my.model.vo.UserVo;
import com.toesbieya.my.model.vo.search.MsgPersonalSearch;
import com.toesbieya.my.model.vo.search.MsgSearch;
import com.toesbieya.my.service.MsgService;
import com.toesbieya.my.utils.Result;
import com.toesbieya.my.utils.SessionUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("message")
public class MsgController {
    @Resource
    private MsgService msgService;

    @PostMapping("manage/search")
    public Result search(@RequestBody MsgSearch vo) {
        return Result.success(msgService.search(vo));
    }

    @PostMapping("manage/add")
    public Result add(@RequestBody Msg msg) {
        String errMsg = validateAdd(msg);
        if (errMsg != null) return Result.fail(errMsg);

        UserVo user = SessionUtil.get();

        setAddInfo(user, msg);

        return msgService.add(msg);
    }

    @PostMapping("manage/update")
    public Result update(@RequestBody Msg msg) {
        String errMsg = validateAdd(msg);
        if (errMsg == null) errMsg = validateUpdate(msg);
        if (errMsg != null) return Result.fail(errMsg);

        return msgService.update(msg);
    }

    @PostMapping("manage/publish")
    public Result publish(@RequestBody Msg msg) {
        boolean isFirstCreate = msg.getId() == null;

        String errMsg = validateAdd(msg);
        if (errMsg == null && !isFirstCreate) errMsg = validateUpdate(msg);
        if (errMsg != null) return Result.fail(errMsg);

        UserVo user = SessionUtil.get();

        if (isFirstCreate) setAddInfo(user, msg);

        msg.setPid(user.getId());
        msg.setPname(user.getName());
        msg.setPtime(System.currentTimeMillis());
        msg.setStatus(MsgConstant.STATUS_PUBLISHED);

        return msgService.publish(msg);
    }

    @PostMapping("manage/withdraw")
    public Result withdraw(@RequestBody Msg msg) {
        if (msg.getId() == null) return Result.fail("参数错误");

        UserVo user = SessionUtil.get();

        msg.setWid(user.getId());
        msg.setWname(user.getName());
        msg.setWtime(System.currentTimeMillis());
        msg.setStatus(MsgConstant.STATUS_WITHDREW);

        return msgService.withdraw(msg);
    }

    @GetMapping("manage/del")
    public Result del(@RequestParam Integer id) {
        return msgService.del(id);
    }

    @PostMapping("user/search")
    public Result searchPersonal(@RequestBody MsgPersonalSearch vo) {
        UserVo user = SessionUtil.get();

        vo.setUid(user.getId());
        vo.setCtime(user.getCtime());

        return Result.success(msgService.searchPersonal(vo));
    }

    @GetMapping("user/read")
    public Result read(@RequestParam Integer id) {
        return msgService.read(SessionUtil.get(), id);
    }

    @GetMapping("user/readAll")
    public Result readAll() {
        return msgService.readAll(SessionUtil.get());
    }

    private void setAddInfo(UserVo user, Msg msg) {
        msg.setCid(user.getId());
        msg.setCname(user.getName());
        msg.setCtime(System.currentTimeMillis());
        msg.setStatus(MsgConstant.STATUS_DRAFT);
    }

    private String validateAdd(Msg msg) {
        if (StringUtils.isEmpty(msg.getTitle())
                || msg.getType() == null
                || msg.getBroadcast() == null
                || msg.getBroadcast().equals(MsgConstant.TO_RANGE) && StringUtils.isEmpty(msg.getRecipient())
        ) return "参数错误";
        return null;
    }

    private String validateUpdate(Msg msg) {
        if (StringUtils.isEmpty(msg.getId())
                || !msg.getStatus().equals(MsgConstant.STATUS_DRAFT)
        ) return "参数错误";
        return null;
    }
}
