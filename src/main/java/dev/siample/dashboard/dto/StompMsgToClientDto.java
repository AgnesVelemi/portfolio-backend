package dev.siample.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class StompMsgToClientDto {
    private String outMessage;
    private OffsetDateTime timestamp;
    private String clientType;
}
