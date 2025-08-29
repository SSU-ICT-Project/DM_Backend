package com.dm.dmbackend.global.common.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Column(length = 50)
    private String placeName;

    @Column(length = 50)
    private String placeAddress;

    @Column(length = 50)
    private String latitude;

    @Column(length = 50)
    private String longitude;
}
