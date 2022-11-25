package com.worldcrossword;

import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

@SpringBootApplication
public class WorldcrosswordApplication implements CommandLineRunner {

    @Autowired
    DictionaryRepository dictionaryRepository;

    public static void main(String[] args) {
        SpringApplication.run(WorldcrosswordApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // csv 사전파일 가져오기.
        ClassPathResource resource = new ClassPathResource("dictionary.csv");
        File csv = resource.getFile();
        BufferedReader br = null;
        String line = "";

        try {
            br = new BufferedReader(new FileReader(csv));
            while ((line = br.readLine()) != null) { // readLine()은 파일에서 개행된 한 줄의 데이터를 읽어온다.
                String[] lineArr = line.split(","); // 파일의 한 줄을 ,로 나누어 배열에 저장 후 리스트로 변환한다.
                if(lineArr[2].equals("row")) continue;
                dictionaryRepository.save(DictionaryEntity.builder().english(lineArr[1]).part(lineArr[2]).mean(lineArr[3]).build());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close(); // 사용 후 BufferedReader를 닫아준다.
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

}
