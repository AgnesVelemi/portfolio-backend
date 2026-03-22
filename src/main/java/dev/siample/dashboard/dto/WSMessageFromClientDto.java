package dev.siample.dashboard.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class WSMessageFromClientDto {

    /**
     * Angular has sent body: JSON.stringify({ payload: 'kukorica' }) --> Spring converts JSON → Java object
    */
    private String payload;

}