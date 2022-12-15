package com.worldcrossword;

import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import com.worldcrossword.puzzle.repository.DictionaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

@SpringBootApplication
@RequiredArgsConstructor
public class WorldcrosswordApplication implements CommandLineRunner {

    private final DictionaryRepository dictionaryRepository;

    public static void main(String[] args) {
        SpringApplication.run(WorldcrosswordApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
		
		if(dictionaryRepository.findAll().size() == 0) {
			        // csv 사전파일 가져오기.
				ClassPathResource resource = new ClassPathResource("dictionary.csv");
			File csv = resource.getFile();
			String line = "";

			try(BufferedReader br = new BufferedReader(new FileReader(csv))) {
				br.readLine();
				while ((line = br.readLine()) != null) { // readLine()은 파일에서 개행된 한 줄의 데이터를 읽어온다.
					String[] lineArr = line.split(","); // 파일의 한 줄을 ,로 나누어 배열에 저장 후 리스트로 변환한다.
					if(lineArr[2].equals("row")) continue;
					if(lineArr[3].startsWith("\"")) lineArr[3] = lineArr[3].substring(1);
					dictionaryRepository.save(DictionaryEntity.builder().english(lineArr[1]).part(lineArr[2]).mean(lineArr[3]).build());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		        
    }

}
