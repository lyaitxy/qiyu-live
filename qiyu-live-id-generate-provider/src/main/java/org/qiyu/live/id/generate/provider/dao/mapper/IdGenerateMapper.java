package org.qiyu.live.id.generate.provider.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.qiyu.live.id.generate.provider.dao.po.IdGeneratePO;

import java.util.List;

@Mapper
public interface IdGenerateMapper extends BaseMapper<IdGeneratePO> {

    @Select("select * from t_id_generate_config")
    List<IdGeneratePO> selectAll();

    /**
     * 将当前记录的开始值和结束值都加上步长
     * @param id
     * @param version
     * @return
     */
    @Update("UPDATE t_id_generate_config SET next_threshold=next_threshold+step, \n" +
            "\tcurrent_start = current_start + step, \n" +
            "\t`version` = `version` + 1\n" +
            "\tWHERE id = #{id} AND `version` = #{version}")
    int updateNewIdAndVersion(@Param("id") int id, @Param("version") int version);
}
