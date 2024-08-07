package com.mewlog.service.reports;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.repository.model.Animal;
import com.mewlog.service.reports.dto.ReportLogDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService{
	
	@Autowired
	AnimalRepository animalRepository;
	
	@Autowired
	ModelMapper modelMapper;
	
	
	@Override
	public List<ReportLogDto> fingMessageByPeriod(long chatId, LocalDateTime dateStart, LocalDateTime dateEnd) {
		Animal animal = animalRepository.findByOwnersId(chatId);
		return animal.getLogs().stream()
	            .filter(log -> !log.getDateCreate().isBefore(dateStart) && !log.getDateCreate().isAfter(dateEnd))
	            .map(log -> new ReportLogDto(log.getDateCreate(), log.getMessage()))
	            .collect(Collectors.toList());
	
	}

	@Override
	public List<ReportLogDto> fingMessageByTextAndPeriod(long chatId, String text, LocalDateTime dateStart, LocalDateTime dateEnd) {
		Animal animal = animalRepository.findByOwnersId(chatId);
		System.out.println("animal = " + animal);	
		return animal.getLogs().stream()
	            .filter(log -> !log.getDateCreate().isBefore(dateStart) && !log.getDateCreate().isAfter(dateEnd) && log.getMessage().contains(text))
	            .map(log -> new ReportLogDto(log.getDateCreate(), log.getMessage()))
	            .collect(Collectors.toList());
	}

}
