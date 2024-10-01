package com.readmate.ReadMate.book.dto.res;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class AladinBookResponse {
    private List<AladinBook> item;


}
