package dev.siample.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class WSMessageFromClientDTO {

    /**
     * Spring converts JSON → Java object
     * Angular sent body: JSON.stringify({ payload: 'kukorica' })
     */
    private String payload;

}
