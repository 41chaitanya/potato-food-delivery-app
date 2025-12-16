package com.microServiceTut.menu_service.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateMenuItemRequest {

    private String name;

    private String description;

    private Double price;

    private boolean available;

}
