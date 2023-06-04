package com.auction.usedauction.util.s3;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Slf4j
public class BackUpData {

    private Map<String, List<String>> backUp;
    private int level;

    public BackUpData() {
        level = 0;
        backUp = new HashMap<>();
        backUp.put(BackUpCommand.INSERT, new ArrayList<>());
        backUp.put(BackUpCommand.DELETE, new ArrayList<>());
    }

    public void insert(String backUpCommand, String path) {
        backUp.get(backUpCommand).add(path);
        log.info("insert backup data, command = {}, path = {}", backUpCommand, path);
    }

    public List<BackUpDTO> getBackUp(String backUpCommand) {
        return backUp.get(backUpCommand).stream()
                .map(path -> new BackUpDTO(backUpCommand, path))
                .collect(toList());
    }

    public int getInsertSize() {
        return backUp.get(BackUpCommand.INSERT).size();
    }

    public int getDeleteSize() {
        return backUp.get(BackUpCommand.DELETE).size();
    }

    public void prevLevel() {
        level -= 1;
    }

    public void nextLevel() {
        level += 1;
    }

    public boolean isFirstLevel() {
        return level == 0;
    }

    public void clean() {
        backUp.clear();
    }

    public boolean isEmpty() {
        return backUp.isEmpty();
    }
}
