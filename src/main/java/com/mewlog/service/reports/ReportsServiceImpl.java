package com.mewlog.service.reports;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mewlog.repository.AnimalRepository;
import com.mewlog.repository.model.Animal;
import com.mewlog.service.reports.dto.ReportLogDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService {

	@Autowired
	AnimalRepository animalRepository;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public String fingMessageByPeriod(long chatId, String callbackData, String animalId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime dateStart = getDateStart(now, callbackData);
		LocalDateTime dateEnd = now.with(LocalTime.MAX);
		Animal animal = animalRepository.findById(new ObjectId(animalId)).get();
		List<ReportLogDto> reportLogs = animal.getLogs().stream()
				.filter(log -> !log.getDateCreate().isBefore(dateStart) && !log.getDateCreate().isAfter(dateEnd))
				.map(log -> new ReportLogDto(log.getDateCreate(), log.getMessage())).collect(Collectors.toList());
		return reportLogs.stream().map(r -> "\n" + r.getDateCreate() + " - " + r.getMessage())
				.collect(Collectors.joining());
	}

	@Override
	public String fingMessageByKeyAndPeriod(long chatId, String callbackData, String keyword, String animalId) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime dateStart = getDateStart(now, callbackData);
		LocalDateTime dateEnd = now.with(LocalTime.MAX);
		Animal animal = animalRepository.findById(new ObjectId(animalId)).get();
		System.out.println("animal = " + animal);
		List<ReportLogDto> reportLogs = animal.getLogs().stream()
				.filter(log -> !log.getDateCreate().isBefore(dateStart) && !log.getDateCreate().isAfter(dateEnd)
						&& log.getMessage().toLowerCase().contains(keyword.toLowerCase()))
				.map(log -> new ReportLogDto(log.getDateCreate(), log.getMessage())).collect(Collectors.toList());
		return reportLogs.stream().map(r -> "\n" + r.getDateCreate() + " - " + r.getMessage())
				.collect(Collectors.joining());
	}

	private LocalDateTime getDateStart(LocalDateTime now, String callbackData) {
		if (callbackData.equals("месяц")) {
			return now.minusMonths(1).with(LocalTime.MIN);
		}
		if (callbackData.equals("год")) {
			return now.minusYears(1).with(LocalTime.MIN);
		}
		return now.with(LocalTime.MIN);
	}

}
