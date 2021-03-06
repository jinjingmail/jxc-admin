package cn.toesbieya.jxc.document.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SellOrderSearch extends DocSearch {
    private Integer customer_id;
    private String customer_name;
    private String finish;
    private Long ftimeStart;
    private Long ftimeEnd;
}
