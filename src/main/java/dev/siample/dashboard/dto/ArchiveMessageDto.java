package dev.siample.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveMessageDto {
    private int arrivalNumber;
    private String sentFrom;
    private String sentFromIP;
    private String outMessage;
    private String timestamp;
    private String sentFromTimezone;
}
