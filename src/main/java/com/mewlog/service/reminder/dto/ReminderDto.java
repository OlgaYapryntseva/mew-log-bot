package com.mewlog.service.reminder.dto;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReminderDto {
	 Set<Long> ownersId;
	 LocalDateTime lastPoopDate;
}
