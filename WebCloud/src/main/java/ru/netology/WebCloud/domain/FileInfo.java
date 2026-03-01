package ru.netology.WebCloud.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    private String filename;
    private Long size;

    private String lastModified;





}
