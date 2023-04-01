package com.auction.usedauction.repository.file;

import com.auction.usedauction.domain.file.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

}
