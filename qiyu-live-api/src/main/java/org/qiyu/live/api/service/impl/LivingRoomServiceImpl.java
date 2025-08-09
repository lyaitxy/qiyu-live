package org.qiyu.live.api.service.impl;

import io.micrometer.common.util.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.service.ILivingRoomService;
import org.qiyu.live.api.vo.LivingRoomInitVO;
import org.qiyu.live.api.vo.req.LivingRoomReqVO;
import org.qiyu.live.api.vo.resp.LivingRoomPageRespVO;
import org.qiyu.live.api.vo.resp.LivingRoomRespVO;
import org.qiyu.live.common.interfaces.dto.PageWrapper;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.living.dto.LivingRoomReqDTO;
import org.qiyu.live.living.dto.LivingRoomRespDTO;
import org.qiyu.live.living.rpc.ILivingRoomRpc;
import org.qiyu.live.user.interfaces.IUserRpc;
import org.qiyu.live.user.interfaces.dto.UserDTO;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @DubboReference
    private IUserRpc userRpc;
    @DubboReference
    private ILivingRoomRpc livingRoomRpc;

    @Override
    public Integer startingLiving(Integer type) {
        Long userId = QiyuRequestContext.getUserId();
        UserDTO userDTO = userRpc.getByUserId(userId);
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setAnchorId(userId);
        livingRoomReqDTO.setRoomName("主播-" + userId + "的直播间");
        livingRoomReqDTO.setCovertImg(userDTO.getAvatar());
        livingRoomReqDTO.setType(type);
        return livingRoomRpc.startLivingRoom(livingRoomReqDTO);
    }

    @Override
    public boolean closeLiving(Integer roomId) {
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAnchorId(QiyuRequestContext.getUserId());
        return livingRoomRpc.closeLiving(livingRoomReqDTO);
    }

    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        // UserDTO userDTO = userRpc.getUserById(userId);
        Map<Long, UserDTO> userDTOMap = userRpc.batchQueryUserInfo(Arrays.asList(respDTO.getAnchorId(), userId).stream().distinct().collect(Collectors.toList()));
        UserDTO anchor = userDTOMap.get(respDTO.getAnchorId());
        UserDTO watcher = userDTOMap.get(userId);
        LivingRoomInitVO respVO = new LivingRoomInitVO();
        respVO.setAnchorNickName(anchor.getNickName());
        respVO.setWatcherNickName(watcher.getNickName());
        respVO.setNickName(watcher.getNickName());
        respVO.setUserId(userId);
        respVO.setAvatar(StringUtils.isEmpty(anchor.getAvatar()) ? "https://s1.ax1x.com/2022/12/18/zb6q6f.png" : anchor.getAvatar());
        respVO.setWatcherAvatar(StringUtils.isEmpty(watcher.getAvatar()) ? "https://s1.ax1x.com/2022/12/18/zb6q6f.png" : watcher.getAvatar());
        respVO.setWatcherAvatar(watcher.getAvatar());
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
            //直播间不存在，设置roomId为-1
            respVO.setRoomId(-1);
        }else {
            respVO.setRoomId(respDTO.getId());
            respVO.setAnchorId(respDTO.getAnchorId());
            respVO.setAnchor(respDTO.getAnchorId().equals(userId));
        }
        respVO.setDefaultBgImg("http://www.news.cn/photo/20250425/e2f1fb2690d74d099f6b5478be683fcc/ad07d300366b41b29df095f5f8946217.jpg");
        return respVO;
    }

    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> pageWrapper = livingRoomRpc.list(ConvertBeanUtils.convert(livingRoomReqVO, LivingRoomReqDTO.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(pageWrapper.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(pageWrapper.isHasNext());
        return livingRoomPageRespVO;
    }
}
