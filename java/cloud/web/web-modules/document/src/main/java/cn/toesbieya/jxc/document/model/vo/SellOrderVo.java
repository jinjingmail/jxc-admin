package cn.toesbieya.jxc.document.model.vo;

import cn.toesbieya.jxc.common.model.entity.BizSellOrder;
import cn.toesbieya.jxc.common.model.entity.BizSellOrderSub;
import cn.toesbieya.jxc.common.model.entity.RecAttachment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SellOrderVo extends BizSellOrder {
    private List<BizSellOrderSub> data;
    private List<RecAttachment> imageList;
    private List<RecAttachment> uploadImageList;
    private List<String> deleteImageList;

    public SellOrderVo(BizSellOrder parent) {
        super(parent);
    }
}
