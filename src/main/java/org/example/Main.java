package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        List<String> hooligans = HooliganGenerator.generateHooligans(250000).stream().map(h ->
        {
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(h);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).toList();

        try {
            Files.write(Path.of("hooligans.txt"), hooligans);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }


}