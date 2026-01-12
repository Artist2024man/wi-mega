package com.wuin.wi_mega.binance.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSocketDataEvent {

    private String event;

    private String clientOrderId;

    private Long orderId;

    private String orderType;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserSocketDataEvent event1 = (UserSocketDataEvent) o;
        return Objects.equals(event, event1.event) && Objects.equals(clientOrderId, event1.clientOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, clientOrderId);
    }

    @Override
    public String toString() {
        return "UserSocketDataEvent{" +
                "event='" + event + '\'' +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", orderId=" + orderId +
                ", orderType='" + orderType + '\'' +
                '}';
    }
}
