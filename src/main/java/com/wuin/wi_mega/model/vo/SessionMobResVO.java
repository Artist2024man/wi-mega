package com.wuin.wi_mega.model.vo;

import com.wuin.wi_mega.binance.bo.BinanceAlgoOrderDTO;
import com.wuin.wi_mega.binance.bo.BinancePosition;
import com.wuin.wi_mega.repository.domain.AppAccountSessionDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class SessionMobResVO extends AppAccountSessionDO {

    @Schema(description = "持仓列表")
    private List<BinancePosition> positions;

    @Schema(description = "所有的条件单列表")
    private List<BinanceAlgoOrderDTO> algoOrders;

}
