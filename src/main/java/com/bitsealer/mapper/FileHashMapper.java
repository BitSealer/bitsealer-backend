package com.bitsealer.mapper;

import com.bitsealer.dto.FileHashDto;
import com.bitsealer.model.FileHash;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component          // Spring lo puede inyectar
public class FileHashMapper {

    public FileHashDto toDto(FileHash e) {
        return new FileHashDto(
                e.getId(),
                e.getFileName(),   // tu campo en la entidad
                e.getSha256(),
                e.getCreatedAt()
        );
    }

    public List<FileHashDto> toDto(List<FileHash> list) {
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}
