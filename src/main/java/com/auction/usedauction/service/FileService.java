package com.auction.usedauction.service;

import com.auction.usedauction.domain.file.File;
import com.auction.usedauction.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {
    private final FileRepository fileRepository;

    @Transactional
    public Long registerFile(File file) {
        return  fileRepository.save(file).getId();
    }
}
