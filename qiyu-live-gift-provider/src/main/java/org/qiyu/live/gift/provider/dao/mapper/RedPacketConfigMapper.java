package org.qiyu.live.gift.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.qiyu.live.gift.provider.dao.po.RedPacketConfigPO;

@Mapper
public interface RedPacketConfigMapper extends BaseMapper<RedPacketConfigPO> {

    @Update("UPDATE t_red_packet_config \n" +
            "SET total_get_price = total_get_price + #{price} \n" +
            "WHERE code = #{code}")
    void incrTotalGetPrice(@Param("code") String code, @Param("price") Integer price);
    @Update("UPDATE t_red_packet_config \n" +
            "SET total_get_count = total_get_count + 1 \n" +
            "WHERE code = #{code}")
    void incrTotalGetCount(String code);
}
